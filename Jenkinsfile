node {
  echo 'Hello World'
  stage "Checkout from Github"
  // git([url: 'https://github.com/symphonyoss/symphony-java-sample-bots.git', branch: 'dev'])
  git branch: 'dev', url: 'https://github.com/symphonyoss/symphony-java-sample-bots.git'

  stage "Build package with Maven"
  def mvnHome = tool 'M3'
  sh "${mvnHome}/bin/mvn clean package"

  stage "Deploy to OpenShift"
  def builder = new com.openshift.jenkins.plugins.pipeline.OpenShiftBuilder("", "symphony-java-sample-bots", "myconfig", "", "", "", "", "true", "", "")
  step builder
}