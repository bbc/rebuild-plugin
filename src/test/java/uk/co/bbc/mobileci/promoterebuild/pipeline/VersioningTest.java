package uk.co.bbc.mobileci.promoterebuild.pipeline;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import java.io.IOException;

/**
 * Copyright © 2016 Media Applications Technologies. All rights reserved.
 */
public class VersioningTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Test
    public void versionSet() throws IOException {

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                "  echo 'version set = ' + mobileCiSupport.isVersionSet()\n" +
                                "}", true));
                WorkflowRun workflowRun = doAnotherBuild(p);
                story.j.assertLogContains("version set = false", workflowRun);
            }
        });

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                " mobileCiSupport.setVersion('7', '1')\n" +
                                " echo 'version set = ' + mobileCiSupport.isVersionSet()\n" +
                                "}", true));
                WorkflowRun workflowRun = doAnotherBuild(p);
                story.j.assertLogContains("version set = true", workflowRun);

            }
        });

        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob p = story.j.jenkins.getItemByFullName("p", WorkflowJob.class);
                p.setDefinition(new CpsFlowDefinition(
                        "node {\n" +
                                " mobileCiSupport.getVersion()\n" +
                                " echo 'version = ' + mobileCiSupport.getVersion()\n" +
                                "}", true));
                WorkflowRun workflowRun = doAnotherBuild(p);
                story.j.assertLogContains("version = 7.1.0-dev." + workOutBuildNumber(p), workflowRun);
            }
        });

    }

    private WorkflowRun doAnotherBuild(WorkflowJob p) throws Exception {
        WorkflowRun workflowRun = p.scheduleBuild2(0).get();
        story.j.waitForCompletion(workflowRun);
        story.j.assertBuildStatusSuccess(workflowRun);
        return workflowRun;
    }

    private int workOutBuildNumber(WorkflowJob p) {
        return p.getLastCompletedBuild().getNumber();
    }
}
