package uk.co.bbc.mobileci.promoterebuild.pipeline;

import jenkins.branch.BranchProperty;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.fail;

/**
 * Copyright © 2017 Media Applications Technologies. All rights reserved.
 */
public class MultiBranchPipelineTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule story = new JenkinsRule();
    @Rule
    public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();

    @Test
    public void canGetBranchNameInMultibranchJob() throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; node {checkout scm; echo 'MOBILE CI BRANCH NAME: ' + mobileCiSupport.getBranchName()}");
        sampleRepo.write("file", "some content");
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--all", "--message=flow");
        WorkflowMultiBranchProject mp = story.jenkins.createProject(WorkflowMultiBranchProject.class, "p");
        mp.getSourcesList().add(new BranchSource(new GitSCMSource(null, sampleRepo.toString(), "", "*", "", false), new DefaultBranchPropertyStrategy(new BranchProperty[0])));
        WorkflowJob p = scheduleAndFindBranchProject(mp, "master");
        story.waitUntilNoActivity();
        WorkflowRun b1 = p.getLastBuild();
        story.assertLogContains("MOBILE CI BRANCH NAME: master", b1);
        sampleRepo.git("checkout", "-b", "feature");
        sampleRepo.write("Jenkinsfile", "echo \"branch=${env.BRANCH_NAME}\"; node {checkout scm; echo 'MOBILE CI BRANCH NAME: ' + mobileCiSupport.getBranchName()}");
        sampleRepo.write("file", "subsequent content");
        sampleRepo.git("commit", "--all", "--message=tweaked");
        p = scheduleAndFindBranchProject(mp, "feature");
        story.waitUntilNoActivity();
        b1 = p.getLastBuild();
        story.assertLogContains("MOBILE CI BRANCH NAME: feature", b1);
    }

    public static WorkflowJob scheduleAndFindBranchProject(WorkflowMultiBranchProject mp, String name) throws Exception {
        mp.scheduleBuild2(0).getFuture().get();
        return findBranchProject(mp, name);
    }

    public static WorkflowJob findBranchProject(WorkflowMultiBranchProject mp, String name) throws Exception {
        WorkflowJob p = mp.getItem(name);
        if (p == null) {
            fail(name + " project not found");
        }
        return p;
    }
}
