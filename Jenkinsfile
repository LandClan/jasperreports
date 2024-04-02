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
          bat 'mvn clean install -B -e -s %MAVEN_SETTINGS%'
          bat 'ant clean jar'
        }
      }
    }
  }

}
