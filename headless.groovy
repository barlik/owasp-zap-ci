#!groovy

podTemplate(
    cloud: 'openshift',
    containers: [
        containerTemplate(
            alwaysPullImage: false,
            args: '${computer.jnlpmac} ${computer.name}',
            command: '',
            envVars: [],
            image: 'openshift/jenkins-slave-maven-centos7',
            // livenessProbe: containerLivenessProbe(execArgs: '', failureThreshold: 0, initialDelaySeconds: 0, periodSeconds: 0, successThreshold: 0, timeoutSeconds: 0),
            name: 'jnlp',
            // ports: [],
            // privileged: false,
            ttyEnabled: true,
            workingDir: '/home/jenkins'
        ),
        containerTemplate(
            alwaysPullImage: false,
            // args: '-la',
            // command: 'ls',
            // envVars: [],
            image: '172.30.1.1:5000/tuesday3001/owasp-zap-openshift',
            // image: 'owasp-zap-openshift',
            // args: '${computer.jnlpmac} ${computer.name}',
            // command: '',
            // image: 'openshift/jenkins-slave-maven-centos7',
            livenessProbe: containerLivenessProbe(execArgs: '', failureThreshold: 0, initialDelaySeconds: 10, periodSeconds: 0, successThreshold: 0, timeoutSeconds: 0),
            name: 'zap',
            // ports: [],
            // privileged: false,
            ttyEnabled: true,
            workingDir: '/home/jenkins'
            // workingDir: '/tmp/wrk'
        )
    ],
    // inheritFrom: '',
    // instanceCap: 0,
    label: 'zap',
    // name: 'zap',
    // namespace: '',
    // nodeSelector: '',
    // serviceAccount: '',
    volumes: [
        emptyDirVolume(memory: true, mountPath: '/shared')
    ],
    // workspaceVolume: emptyDirWorkspaceVolume(false)
) {
    node('zap') {
        stage('Scan Web Application') {
            container('zap') {
                // Wait a bit
                echo 'Wait for 10secs for ZAP API to come up...'
                sleep 15
                // Start a Scan
                sh 'curl -v http://localhost:9090/JSON/spider/action/scan/?url=http://hipster:8080 2>&1'
                sleep 10
                sh 'curl -v http://localhost:9090/JSON/spider/view/status?scanId=0'
                echo 'Wait 30secs for Scan to Complete...'
                sleep 30
                sh 'curl -o /shared/report.html http://localhost:9090/HTML/core/view/alerts?baseurl=http://hipster:8080'
                sleep 10
            }
        }
        stage('Publish reports') {
            container('jnlp') {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: '/shared',
                    reportFiles: 'report.html',
                    reportName: 'ZAP API Scan',
                    reportTitles: 'ZAP API Scan'
                ])
            }
        }
    }
}
