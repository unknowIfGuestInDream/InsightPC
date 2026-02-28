/*
 * Copyright (c) 2024 unknowIfGuestInDream.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of unknowIfGuestInDream, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UNKNOWIFGUESTINDREAM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

        stage('Prepare JRE') {
            steps {
                sh 'rm -f *linux*21*.tar.gz *mac*21*.tar.gz *windows*21*.zip || true'
                copyArtifacts filter: '*linux*21*,*mac*21*,*windows*21*', fingerprintArtifacts: true, projectName: 'env/JRE', selector: lastSuccessful()
                sh 'java -version'
                sh "$M2_HOME/bin/mvn -version"
            }
            post {
                failure {
                    echo 'Prepare JRE failed'
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
                    sh "rm -rf jretemp && mkdir -v jretemp && unzip -q *windows*21*.zip -d jretemp && mv jretemp/* jretemp/jre"
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
                    sh "rm -rf jretemp && mkdir -v jretemp && tar -xzf *mac*21*.tar.gz -C jretemp && mv jretemp/* jretemp/jre"
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
                    sh "rm -rf jretemp && mkdir -v jretemp && tar -xzf *linux*21*.tar.gz -C jretemp && mv jretemp/* jretemp/jre"
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
                    sh "rm -rf jretemp"
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
    sh "cd staging && zip -qr ../insightpc-${os}_${version}_b${BUILD_NUMBER}_\$(date +%Y%m%d).zip . && cd .."
    sh "cp -r jretemp/jre staging/"
    sh "cd staging && zip -qr ../insightpc-${os}_${version}_withJRE_b${BUILD_NUMBER}_\$(date +%Y%m%d).zip . && cd .."
    sh "rm -rf staging"
}
