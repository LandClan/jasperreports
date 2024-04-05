pipeline {
  agent any
  tools {
    maven 'Maven-3.8.6'
    jdk 'JDK-11'
    ant 'ANT'
  }
  parameters {
    booleanParam(name: "RELEASE",
            description: "Deploy a release from current commit.",
            defaultValue: false)
  }
  stages {
    stage("Build Ant JAR") {
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
    stage("Release") {
      when {
        branch 'landclan/master'
        expression { params.RELEASE }
      }
      steps {
        configFileProvider([configFile(fileId: 'nexus-maven-settings-xml', variable: 'MAVEN_SETTINGS')]) {
          withCredentials([usernamePassword(credentialsId: 'github-landclan', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
            dir('jasperreports') {
              // Locally install the Ant artifact to have it available in mvn release (which re-clones the repo, wiping
              // out artifacts).
              bat 'mvn install:install-file -Dfile="dist/jasperreports-landclan.jar" -DgroupId="net.sf.jasperreports" -DartifactId="jasperreports-landclan-intermediate" -Dversion="1.0.0" -Dpackaging=jar -DgeneratePom=true'
            }
            dir('jasperreports/landclan') {
              bat 'mvn release:prepare -X -B -e -s %MAVEN_SETTINGS%'
              bat 'mvn release:perform -B -e -s %MAVEN_SETTINGS%'
            }
          }
        }
      }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: '**/*-landclan*.jar', fingerprint: true
      deleteDir()
    }
  }
}
