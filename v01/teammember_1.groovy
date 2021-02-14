import groovy.json.JsonSlurper
import hudson.model.*
jenkins = Hudson.instance

jsonSlurper = new JsonSlurper()
gitToken = System.getenv().get('GIT_TOKEN')

def getTeamId(teamName) {
  /*
   Function to find teams ID
  */
  def organizationName = 'fuchicorp'
  def teamsUrl = "https://api.github.com/orgs/${organizationName}/teams"
  def teamId = null

  def get = new URL(teamsUrl).openConnection();
      get.setRequestMethod("GET")
      get.setRequestProperty("Authorization", "token ${gitToken}")
      get.setRequestProperty("Content-Type", "application/json")

  def data = jsonSlurper.parseText(get.getInputStream().getText())

  data.each() {
    if (it.name.toLowerCase() == teamName.toLowerCase()) {
      teamId = it.id
    }
  }
  return teamId
}

def getTeamMembers(teamName) {
  /*
  Function to find team members from github
  */
  def getTeamId = getTeamId(teamName)
  //println(getTeamId)
  def totalUsers = []
  def memberUrl = ""
  def pageCount = 1

  while (true) {
    // While loop to go each pages and get all members from team 
    memberUrl = "https://api.github.com/teams/${getTeamId}/members?page=${pageCount}"
    def get = new URL(memberUrl).openConnection();
      get.setRequestMethod("GET")
      get.setRequestProperty("Authorization", "token ${gitToken}")
      get.setRequestProperty("Content-Type", "application/json")
     def object = jsonSlurper.parseText(get.getInputStream().getText())

    //  Braking the while loop when no one found in the page
     if (! object.login) {
       break;
     }

    // Adding list of found people to totalUsers
    object.login.each{ totalUsers.add(it) }
    pageCount = pageCount + 1
  }
  return totalUsers
}

def googleDomainName        = jenkins.getRootUrl()
def fuchicorpJenkinsAdmin   = getTeamMembers('fuchicorp-jenkins-admin')
def commonJenkinsAdmin      = getTeamMembers('common-jenkins-admin')
def jenkinsAdmin            = []

if (googleDomainName == 'https://jenkins.fuchicorp.com/') {
    fuchicorpJenkinsAdmin.each() {
        jenkinsAdmin.add(it)
    }
} else {
    commonJenkinsAdmin.each() {
        jenkinsAdmin.add(it)
    }
}

println googleDomainName

println("list of jenkins admins $jenkinsAdmin")

//println("fuchicorp-jenkins-admin Members: ${getTeamMembers('fuchicorp-jenkins-admin')}")
//println("common-jenkins-admin: ${getTeamMembers('common-jenkins-admin')}")