node {
   // define Origin commands
   def ocCmd = "oc --token=`cat /var/run/secrets/kubernetes.io/serviceaccount/token` --server=https://openshift.default.svc.cluster.local --certificate-authority=/run/secrets/kubernetes.io/serviceaccount/ca.crt"
   def mvnCmd = "mvn"

   // Define Openshift coordinates
   def buildTemplateName = "maven-bot-build"
   def botName = "echobot"

   stage 'Checkout'
   git branch: 'experimental', url: 'https://github.com/symphonyoss/symphony-java-sample-bots.git'
   echo "Checkout done"

   // Define Maven coordinates
   def pomVersion = version()
   def artifactId = "symphony-java-sample-bots"

   stage 'Build'

    withMavenEnv(["JAVA_OPTS=-Xmx1536m","MAVEN_OPTS=-Xmx1536m"]) {
       sh "${mvnCmd} clean package -DskipTests=true"
    }

   stage 'Deploy'
   sh "${ocCmd} process ${buildTemplateName} -v BOT_NAME=${botName} | oc create -f -"

   sh "${ocCmd} start-build ${botName} --from-dir=target/${artifactId}-${pomVersion} --wait=true -n ${projectName}"
}

def version() {
  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
  matcher ? matcher[0][1] : null
}

void withMavenEnv(List envVars, def body) {
    String mvntool = tool name: "maven-default", type: 'hudson.tasks.Maven$MavenInstallation'
    String jdktool = tool name: "oracle-jdk8", type: 'hudson.model.JDK'
    env.JAVA_HOME = "${jdktool}"

    envVars.add("PATH+MVN=${mvntool}/bin")
    envVars.add("PATH+JDK=${jdktool}/bin")
    envVars.add("JAVA_HOME=${jdktool}")
    envVars.add("MAVEN_HOME=${mvntool}")
    echo "envVars: ${envVars.toString()}"

    // Invoke the body closure we're passed within the environment we've created.
    withEnv(envVars) {
        body.call()
    }
}
