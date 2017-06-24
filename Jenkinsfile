pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        echo 'Hello'
      }
    }
    stage('Maven') {
      steps {
        withMaven(jdk: 'Java8', maven: 'Maven3') {
          sh 'mvn verify'
        }
        
      }
    }
  }
}