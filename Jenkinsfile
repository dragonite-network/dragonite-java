pipeline {
  agent {
    docker {
      image 'gradle'
    }
    
  }
  stages {
    stage('build') {
      steps {
        sh '''
gradle clean
gradle distZip
'''
        archiveArtifacts '**/build/distributions/*.zip'
      }
    }
    stage('test') {
      steps {
        echo 'should run tests'
      }
    }
    stage('deploy') {
      steps {
        echo 'should deploy'
      }
    }
  }
}