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
  post {
    always {
      archiveArtifacts '**/build/distributions/*.zip'
      emailext to: 'w@vecsight.com,t@vecsight.com',
        subject: "Pipeline '${env.JOB_NAME}' ${env.BUILD_DISPLAY_NAME} resulted ${currentBuild.currentResult}",
        body: "Build URL: ${env.BUILD_URL}",
        attachmentsPattern: '**/build/distributions/*.zip',
        attachLog: true
    }
  }
}
