

import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
//import hudson.triggers.SCMTrigger
//import hudson.util.Secret
//import javaposse.jobdsl.plugin.*
import jenkins.model.Jenkins
import org.apache.commons.io.FilenameUtils

//import jenkins.model.JenkinsLocationConfiguration
//import com.cloudbees.plugins.credentials.CredentialsScope
//import com.cloudbees.plugins.credentials.domains.Domain
//import com.cloudbees.plugins.credentials.SystemCredentialsProvider
//import jenkins.model.JenkinsLocationConfiguration
//import org.jenkinsci.plugins.ghprb.GhprbGitHubAuth
//import org.jenkinsci.plugins.ghprb.GhprbTrigger.DescriptorImpl
//import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
//import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl.DescriptorImpl
//import org.jenkinsci.plugins.scriptsecurity.sandbox.Whitelist
//import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.BlanketWhitelist
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition

class InstallPipelines {
    def scmUrl = 'https://github.com/demirole/jenkins.git'
    final pipelineScripts = [
        [ 'name': 'workflow', 'scriptPath': 'pipelines/workflow-job.jenkins' ]
    ]

    def invoke() {
        def jenkins = Jenkins.get()

        def scm = new GitSCM(scmUrl)
        scm.branches = [new BranchSpec("*/master")]
        pipelineScripts.each { pipelineScript ->
            def workflowJob = new WorkflowJob(jenkins, pipelineScript.name)
            workflowJob.definition = new CpsScmFlowDefinition(scm, pipelineScript.scriptPath)
            addDescriptionToWorkflowJob(pipelineScript.name, workflowJob)
            try {
                jenkins.add(workflowJob, workflowJob.name)
            } catch(IllegalArgumentException exception) {
                println("Caught error while adding workflowjob ${pipelineScript.name}: A workflow with that name already exists!")
            }
        }

        jenkins.reload()
    }

    private void addDescriptionToWorkflowJob(String pipelineScriptName, WorkflowJob workflowJob) {
        def descriptionFile = new File(FilenameUtils.removeExtension(pipelineScriptName))
        if (descriptionFile.exists()) {
            workflowJob.description = descriptionFile.text
        }
    }
}

installPipelines = new InstallPipelines()
installPipelines.invoke()
