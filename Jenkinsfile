node {
   // Define Maven coordinates
   def pomVersion = version()
   def artifactId = "symphony-java-sample-bots"

   // define Origin commands
   def ocCmd = "oc --token=`cat /var/run/secrets/kubernetes.io/serviceaccount/token` --server=https://openshift.default.svc.cluster.local --certificate-authority=/run/secrets/kubernetes.io/serviceaccount/ca.crt"
   def mvnCmd = "mvn"

   // Define Openshift coordinates
   def projectName = "cicd"
   def imageStream = "openjdk:8-jdk"

   stage 'Build'
   git branch: 'dev', url: 'https://github.com/symphonyoss/symphony-java-sample-bots.git'

   withMavenEnv() {
       sh "echo JAVA_HOME is '$JAVA_HOME'"
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

// This method sets up the Maven and JDK tools, puts them in the environment along
// with whatever other arbitrary environment variables we passed in, and runs the
// body we passed in within that environment.
void withMavenEnv(List envVars = [], def body) {
    // The names here are currently hardcoded for my test environment. This needs
    // to be made more flexible.
    // Using the "tool" Workflow call automatically installs those tools on the
    // node.
    //String mvntool = tool name: "M3", type: 'hudson.tasks.Maven$MavenInstallation'
    String jdktool = tool name: "oracle-jdk8", type: 'hudson.model.JDK'

    env.JAVA_HOME = "${jdktool}"

    // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're
    // using.
    //List mvnEnv = ["PATH+MVN=${mvntool}/bin", "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}", "MAVEN_HOME=${mvntool}"]

    // Add any additional environment variables.
    //mvnEnv.addAll(envVars)

    // Invoke the body closure we're passed within the environment we've created.
    //withEnv(mvnEnv) {
        body.call()
    //}
}
