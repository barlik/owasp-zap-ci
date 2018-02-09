#!groovy
node('master') {
    openshiftDeploy(
        apiURL: '',
        authToken: '',
        depCfg: 'selenium-test',
        namespace: '',
        verbose: 'false',
        waitTime: '',
        waitUnit: 'sec'
    )

    openshiftScale(
        apiURL: '',
        authToken: '',
        depCfg: 'owasp-zap-openshift',
        namespace: '',
        replicaCount: '1',
        verbose: 'false',
        verifyReplicaCount: 'true',
        waitTime: ''
    )
}

node('maven') {
    sh 'ls -la'
    sleep 5
    sh 'curl -o /tmp/report.html http://owasp-zap-openshift:8080/HTML/core/view/alerts'
    publishHTML([
        allowMissing: false,
        alwaysLinkToLastBuild: false,
        keepAll: true,
        reportDir: '/tmp',
        reportFiles: 'report.html',
        reportName: 'ZAP Scan',
        reportTitles: 'ZAP Scan'
    ])
}


node('master') {
    openshiftScale(
        apiURL: '',
        authToken: '',
        depCfg: 'owasp-zap-openshift',
        namespace: '',
        replicaCount: '0',
        verbose: 'false',
        verifyReplicaCount: 'true',
        waitTime: ''
    )
}


// node('zap') {
//     stage('Scan Web Application') {
//         container('zap') {
//             // sh(returnStatus: true, script: "rm -rf /tmp/workdir")
//             // sh 'mkdir /tmp/workdir'
//             // dir('/tmp/workdir') {
//                 // Wait a bit
//                 echo 'Wait for 10secs for ZAP API to come up...'
//                 sleep 15
//                 // Start a Scan
//                 sh 'curl -v http://localhost:9090/JSON/spider/action/scan/?url=http://hipster:8080 2>&1'
//                 sleep 10
//                 sh 'curl -v http://localhost:9090/JSON/spider/view/status?scanId=0'
//                 echo 'Wait 30secs for Scan to Complete...'
//                 sleep 30
//                 sh 'curl -o /shared/report.html http://localhost:9090/HTML/core/view/alerts?baseurl=http://hipster:8080'
//                 sleep 10
//             // }
//         }
//     }
//     stage('Publish reports') {
//         container('jnlp') {
//             publishHTML([
//                 allowMissing: false,
//                 alwaysLinkToLastBuild: false,
//                 keepAll: true,
//                 reportDir: '/shared',
//                 reportFiles: 'report.html',
//                 reportName: 'ZAP API Scan',
//                 reportTitles: 'ZAP API Scan'
//             ])
//         }
//     }
// }
