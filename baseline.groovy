#!groovy
def reportDir = "/zap/wrk"
def reportFile = "baseline.html"

// def MINUTES = "1"
// def TARGET = "http://juice-shop"

stage('Initial setup') {
    properties([
        parameters([
            string(
                name: 'TARGET',
                description: 'Target url',
                defaultValue: 'http://juice-shop:80'
            ),
            string(
                name: 'MINUTES',
                description: 'The number of minutes to spider for',
                defaultValue: '1'
            ),
        ])
    ])
}

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
                // def tempDir = sh(returnStdout: true, script: "mktemp -d").trim()
                // dir(tempDir) {
                    def retVal = sh(returnStatus: true, script: "/zap/zap-baseline.py -m $MINUTES -r $reportFile -t $TARGET")
                    echo "Return value is: ${retVal}"

                    // Share the report
                    sh "cp $reportDir/$reportFile /shared"
                // }
            }
        }
        stage('Publish reports') {
            container('jnlp') {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: '/shared',
                    reportFiles: 'baseline.html',
                    reportName: 'ZAP Baseline Scan',
                    reportTitles: 'ZAP Baseline Scan'
                ])
            }
        }
    }
}
