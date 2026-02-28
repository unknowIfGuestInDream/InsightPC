pipeline {
    agent any
    options {
        timeout(time: 1, unit: "HOURS")
    }
    environment {
        USER_NAME = 'Jenkins'
    }
    tools {
        jdk "jdk21"
    }

    stages {
        stage('Check change') {
            when {
                expression { currentBuild.previousSuccessfulBuild != null }
                expression { currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause) == null }
            }
            steps {
                echo "Current commit: ${GIT_COMMIT}"
                echo "Current URL: ${env.GIT_URL}"
                script {
                    def prevBuild = currentBuild.previousSuccessfulBuild
                    def prevCommitId = ""
                    def actions = prevBuild.rawBuild.getActions(hudson.plugins.git.util.BuildData.class)
                    for (action in actions) {
                        if (action.getRemoteUrls().toString().contains(env.GIT_URL)) {
                            prevCommitId = action.getLastBuiltRevision().getSha1String()
                            break
                        }
                    }
                    if (prevCommitId == "") {
                        echo "prevCommitId does not exist."
                    } else {
                        echo "Previous successful commit: ${prevCommitId}"
                        if (prevCommitId == GIT_COMMIT) {
                            echo "no change, skip build"
                            currentBuild.getRawBuild().getExecutor().interrupt(Result.NOT_BUILT)
                            sleep(1)
                            cleanWs()
                        }
                    }
                }
            }
        }

        stage('Prepare JDK') {
            steps {
                sh 'rm -f *linux*21*.tar.gz *mac*21*.tar.gz *windows*21*.zip || true'
                copyArtifacts filter: '*linux*21*,*mac*21*,*windows*21*', fingerprintArtifacts: true, projectName: 'env/JDK', selector: lastSuccessful()
                sh 'java -version'
                sh "$M2_HOME/bin/mvn -version"
            }
            post {
                failure {
                    echo 'Prepare JDK failed'
                    cleanWs()
                }
                aborted {
                    echo 'Build aborted'
                    cleanWs()
                }
            }
        }

        stage('Prepare Windows Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Djavafx.platform=win -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} clean package"
                    sh "rm -rf jdktemp jretemp && mkdir -v jdktemp && unzip -q *windows*21*.zip -d jdktemp"
                    sh """
                        JDK_DIR=\$(ls -d jdktemp/jdk-*)
                        mkdir -v jretemp
                        jlink --module-path \${JDK_DIR}/jmods \
                          --add-modules java.se,jdk.unsupported,jdk.zipfs,jdk.management,jdk.crypto.ec,jdk.localedata,jdk.charsets \
                          --strip-debug --no-man-pages --no-header-files \
                          --compress zip-6 \
                          --output jretemp/jre
                        rm -rf jdktemp
                    """
                }
            }
        }

        stage('Build insightpc-windows') {
            steps {
                script {
                    packageApp('win')
                }
            }

            post {
                success {
                    archiveArtifacts 'insightpc*win*.zip'
                }
                failure {
                    echo 'Build insightpc-windows failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Prepare Mac Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Djavafx.platform=mac -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} clean package"
                    sh "rm -rf jdktemp jretemp && mkdir -v jdktemp && tar -xzf *mac*21*.tar.gz -C jdktemp"
                    sh """
                        JDK_DIR=\$(ls -d jdktemp/jdk-*/Contents/Home)
                        mkdir -v jretemp
                        jlink --module-path \${JDK_DIR}/jmods \
                          --add-modules java.se,jdk.unsupported,jdk.zipfs,jdk.management,jdk.crypto.ec,jdk.localedata,jdk.charsets \
                          --strip-debug --no-man-pages --no-header-files \
                          --compress zip-6 \
                          --output jretemp/jre
                        rm -rf jdktemp
                    """
                }
            }
        }

        stage('Build insightpc-mac') {
            steps {
                script {
                    packageApp('mac')
                }
            }

            post {
                success {
                    archiveArtifacts 'insightpc*mac*.zip'
                }
                failure {
                    echo 'Build insightpc-mac failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Prepare Linux Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh "$M2_HOME/bin/mvn -B --no-transfer-progress -s $M2_HOME/conf/settings.xml -Djavafx.platform=linux -Dmaven.test.skip=true -Dmaven.compile.fork=true -Duser.name=${USER_NAME} clean package"
                    sh "rm -rf jdktemp jretemp && mkdir -v jdktemp && tar -xzf *linux*21*.tar.gz -C jdktemp"
                    sh """
                        JDK_DIR=\$(ls -d jdktemp/jdk-*)
                        mkdir -v jretemp
                        jlink --module-path \${JDK_DIR}/jmods \
                          --add-modules java.se,jdk.unsupported,jdk.zipfs,jdk.management,jdk.crypto.ec,jdk.localedata,jdk.charsets \
                          --strip-debug --no-man-pages --no-header-files \
                          --compress zip-6 \
                          --output jretemp/jre
                        rm -rf jdktemp
                    """
                }
            }
        }

        stage('Build insightpc-linux') {
            steps {
                script {
                    packageApp('linux')
                }
            }

            post {
                success {
                    archiveArtifacts 'insightpc*linux*.zip'
                }
                failure {
                    echo 'Build insightpc-linux failed'
                }
                aborted {
                    echo 'Build aborted'
                }
            }
        }

        stage('Clean Workspace') {
            steps {
                script {
                    sh "rm -f insightpc*.zip"
                    sh "rm -f *linux*21*.tar.gz"
                    sh "rm -f *mac*21*.tar.gz"
                    sh "rm -f *windows*21*.zip"
                    sh "rm -rf jdktemp jretemp"
                }
            }
        }

    }
}

def packageApp(os) {
    def version = sh(
        script: "${M2_HOME}/bin/mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
        returnStdout: true
    ).trim()
    def scriptDir = (os == 'win') ? 'scripts/win' : (os == 'mac') ? 'scripts/mac' : 'scripts/linux'
    sh "rm -rf staging && mkdir -p staging"
    sh "cp target/insightpc.jar staging/"
    sh "cp -r target/lib staging/"
    sh "cp README.md LICENSE staging/"
    sh "cp ${scriptDir}/* staging/"
    sh "cp -r jretemp/jre staging/"
    sh "cd staging && zip -qr ../insightpc-${os}_${version}_b${BUILD_NUMBER}_\$(date +%Y%m%d).zip . && cd .."
    sh "rm -rf staging"
}
