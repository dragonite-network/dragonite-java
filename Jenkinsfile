pipeline {
  agent {
    docker {
      image 'gradle'
    }
    
  }
  stages {
    stage('build') {
      steps {
        sh '''gradle clean
gradle distZip'''
        archiveArtifacts '**/build/distributions/*.zip'
      }
    }
  }
}