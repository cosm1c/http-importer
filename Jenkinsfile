#!groovy
pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Setup') {
            steps {
                echo "BUILD_TAG=${env.BUILD_TAG} BUILD_URL=${env.BUILD_URL} WORKSPACE=${env.WORKSPACE}"
                checkout scm
            }
        }
        stage('Build') {
            steps {
                ansiColor('xterm') {
                    timeout(10) {
                        sh 'sbt clean test'
                    }
                }
                junit 'target/test-reports/*.xml'
            }
        }
        stage('Deploy') {
            when {
                expression {
                    currentBuild.result == null || currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                echo 'TODO: deploy successful build'
            }
        }
    }
    post {
        success {
            echo 'Build successful.'
        }
        failure {
            echo 'Build failed.'
        }
        unstable {
            echo 'Build unstable.'
        }
    }
}
