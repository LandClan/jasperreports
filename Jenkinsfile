pipeline {
  agent any
  tools {
    maven 'Maven-3.8.6'
    jdk 'JDK-11'
    ant 'ANT'
  }
  parameters {
    booleanParam(name: "RELEASE",
            description: "Build a release from current commit.",
            defaultValue: false)
  }
  stages {
    stage("Build & Deploy SNAPSHOT") {
      when {
        not {
          // Don't build the master branch since it represents the upstream that we forked.
          branch 'master'
        }
      }
      steps {
        configFileProvider([configFile(fileId: 'nexus-maven-settings-xml', variable: 'MAVEN_SETTINGS')]) {
          dir('jasperreports') {
            bat 'mvn clean install -B -e -s %MAVEN_SETTINGS% -DskipTests'
            bat 'ant clean jar'
          }
        }
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: '**/dist/*-landclan.jar', fingerprint: true
      deleteDir()
    }
  }
}
