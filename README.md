testInProgress
==============

This jenkins plugin allows you to see how the junit tests progress during a build.

See ![Documentation](https://wiki.jenkins-ci.org/display/JENKINS/Test+In+Progress) on the Jenkins site.

[![Build Status](https://buildhive.cloudbees.com/job/jenkinsci/job/testInProgress-plugin/badge/icon)](https://buildhive.cloudbees.com/job/jenkinsci/job/testInProgress-plugin/)

# Usage

## Jenkinsfile of Pipeline
```
pipeline {
    agent any

    tools {
        maven "3.3.9"
    }

    stages {
        stage("Build") {
            steps {
                wrap([$class: 'TestInProgressBuildWrapper']) {
                    checkout scm

                    sh '''
                        echo "PATH = ${PATH}"
                        echo "M2_HOME = ${M2_HOME}"
                        mvn test -Pcary
                    '''
                }
            }
        }
    }
}

```