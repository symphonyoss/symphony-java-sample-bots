node {
   // define Origin commands
   def ocCmd = "oc --token=`cat /var/run/secrets/kubernetes.io/serviceaccount/token` --server=https://openshift.default.svc.cluster.local --certificate-authority=/run/secrets/kubernetes.io/serviceaccount/ca.crt"
   def mvnCmd = "mvn"

   // Define Openshift coordinates
   def projectName = "cicd"
   def imageStream = "openjdk:8-jdk"

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
   sh "${ocCmd} delete bc,dc,svc,route -l app=${artifactId} -n ${projectName}"
   // create build. override the exit code since it complains about exising imagestream
   sh "${ocCmd} new-build --name=${artifactId} --image-stream=${imageStream} --binary=true --labels=app=${artifactId} -n ${projectName} || true"
   // build image
   sh "${ocCmd} start-build ${artifactId} --from-dir=target/${artifactId}-${pomVersion} --wait=true -n ${projectName}"
   // deploy image
   sh "${ocCmd} new-app ${artifactId}:latest -n ${projectName}"
   sh "${ocCmd} expose svc/${artifactId} -n ${projectName}"
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
