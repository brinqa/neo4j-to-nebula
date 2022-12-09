pipeline {
    agent any

    parameters {
        booleanParam(
            name: "RELEASE",
            description: "Build a release from current commit.",
            defaultValue: false)
    }

    stages {
        stage('Init') {
            steps {
                bitbucketStatusNotify 'INPROGRESS'
                slackSend color: "good", message: "Build started - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            }
        }
        stage('Clone') {
            steps {
                git branch: '1.x',
                    credentialsId: 'c9d506bf-4bd8-416e-9b85-cf8e4700d6f6',
                    url: 'git@bitbucket.org:brinqa/neo4j-to-nebula.git'

            }
        }
        stage ('Build') {
            when {
                not {
                    expression {
                        params.RELEASE
                    }
                }
            }
            steps {
                withMaven (
                    jdk: 'JDK 11',
                    mavenOpts: '-Xmx4G -Xms4G',
                    mavenSettingsConfig: 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1390757734630'
                ) {
                    sh 'mvn clean deploy'
                }

            }
        }
        stage ('Release') {
            when {
                expression {
                    params.RELEASE
                }
            }
            steps {
                withMaven (
                    jdk: 'JDK 11',
                    mavenOpts: '-Xmx4G -Xms4G',
                    mavenSettingsConfig: 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1390757734630'
                ) {
                    sh "mvn -B -Dresume=false release:clean release:prepare release:perform"
                }
                script {
                    currentBuild.keepLog = true
                }

            }
        }
    }

    post {
        // Finally let bitbucket know
        success {
            bitbucketStatusNotify 'SUCCESSFUL'
        }
        failure {
            bitbucketStatusNotify 'FAILED'
        }
        always {
            script {
                if (currentBuild.currentResult == "SUCCESS" ) {
                    slackSend color: "good", message: "Build is successful - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
                else if (currentBuild.currentResult == "FAILURE" ) {
                    slackSend color: "danger", message: "Build failed - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
                else if (currentBuild.currentResult == "UNSTABLE" ) {
                    slackSend color: "warning", message: "Build is unstable - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
                else {
                    slackSend color: "danger", message: "Build status is unknown - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
            }
            cleanWs()
        }
    }
}
