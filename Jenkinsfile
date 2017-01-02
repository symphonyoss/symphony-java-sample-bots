node('maven') {
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
   sh "echo JAVA_HOME is '$JAVA_HOME'"
   sh "${mvnCmd} clean package -DskipTests=true"

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
