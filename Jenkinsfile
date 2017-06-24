pipeline {
  agent any
  stages {
    stage('Compile') {
      steps {
        echo 'Hello'
      }
    }
    stage('') {
      steps {
        withMaven(jdk: 'Java8', maven: 'Maven3') {
          sh 'mvn verify'
        }
        
      }
    }
  }
}