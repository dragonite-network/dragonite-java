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
        echo 'deploying to yoshino'
        sshagent(['ssh_yoshino']) {
          sh 'scp -o StrictHostKeyChecking=no dragonite-forwarder/build/distributions/dragonite-forwarder*.zip tobyxdd@yoshino.vecsight.com:/home/tobyxdd/jenkins/dragonite-forwarder.zip'
          sh 'ssh -o StrictHostKeyChecking=no tobyxdd@yoshino.vecsight.com bash -c "cd /home/tobyxdd/jenkins/; ./dragonited.sh"'
        }
        echo 'deploying to batman'
        sshagent(['ssh_batman']) {
          sh 'scp -o StrictHostKeyChecking=no dragonite-forwarder/build/distributions/dragonite-forwarder*.zip tobyxdd@batman.vecsight.com:/home/tobyxdd/jenkins/dragonite-forwarder.zip'
          sh 'ssh -o StrictHostKeyChecking=no tobyxdd@batman.vecsight.com bash -c "cd /home/tobyxdd/jenkins/; ./dragonited.sh"'
        }
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
