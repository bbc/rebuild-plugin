package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.plugins.git.util.BuildData;
import org.apache.commons.lang.RandomStringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.scm.GitSampleRepoRule;
import org.jenkinsci.plugins.workflow.steps.scm.GitStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by beazlr02 on 23/04/16.
 */
public class PromotedJobWithGITGlobalTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule public JenkinsRule story = new JenkinsRule();

    @Rule public GitSampleRepoRule sampleRepo = new GitSampleRepoRule();



    @Test
    public void twoCommitsOneHash() throws Exception {

        String scriptThatLogsAssertableStrings =
                "node " +
                        "{\n" +
                        "  mobileCiSupport.setVersion('1', '5')\n" +
                        "  if( mobileCiSupport.isPromotion() ) {" +
                        "    echo 'PROMOTED:' + mobileCiSupport.getFromHash()\n" +
                        "    echo 'BUILDNUMBER:' + mobileCiSupport.getFromBuildNumber()\n" +
                        "    echo 'MAJOR_RELEASE:' + mobileCiSupport.isMajorRelease()\n" +
                        "    echo 'VERSION:' + mobileCiSupport.getTargetVersion()\n" +
                        "    \n" +
                        "  } else {\n" +
                        "    echo 'not a promotion'\n" +
                        "    \n" +
                        "  }\n" +
                        "}";

        commitJenkinsPipelineScript(scriptThatLogsAssertableStrings, sampleRepo);
        WorkflowJob p = setupPipelineInJenkins(story, sampleRepo);
        story.waitForCompletion(p.scheduleBuild2(0).get());

        commitSomething();
        commitSomething();

        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());

        String masterHeadHash = workOutCommitHashOfBuild(b);

        // do a couple more builds
        story.waitForCompletion(p.scheduleBuild2(0).get());
        story.waitForCompletion(p.scheduleBuild2(0).get());


        b = promoteMinorReleaseBuildNumber(p, 2);
        story.assertBuildStatusSuccess(b);

        story.assertLogContains("PROMOTED:"+masterHeadHash, b);
        story.assertLogContains("BUILDNUMBER:2", b);
        story.assertLogContains("MAJOR_RELEASE:false", b);
        story.assertLogContains("VERSION:1.6.0", b);
    }



    @Test
    public void promotingCommitNotFromHead() throws Exception {

        String script =
                "node " +
                        "{\n" +
                        "  mobileCiSupport.setVersion('5', '11')\n" +
                        "  if( mobileCiSupport.promotion ) {" +
                        "    echo 'PROMOTED:' + mobileCiSupport.fromHash\n" +
                        "    echo 'BUILDNUMBER:' + mobileCiSupport.fromBuildNumber\n" +
                        "    echo 'MAJOR_RELEASE:' + mobileCiSupport.isMajorRelease()\n" +
                        "    echo 'VERSION:' + mobileCiSupport.getTargetVersion()\n" +
                        "    \n" +
                        "  } else {\n" +
                        "    echo 'not a promotion'\n" +
                        "    \n" +
                        "  }\n" +
                        "}";

        commitJenkinsPipelineScript(script, sampleRepo);
        WorkflowJob p = setupPipelineInJenkins(story, sampleRepo);
        story.waitForCompletion(p.scheduleBuild2(0).get());

        commitSomething();
        commitSomething();

        WorkflowRun b = story.waitForCompletion(p.scheduleBuild2(0).get());
        String masterHeadHash = workOutCommitHashOfBuild(b);

        // do a couple more builds with some commits
        story.waitForCompletion(p.scheduleBuild2(0).get());
        commitSomething();
        commitSomething();
        story.waitForCompletion(p.scheduleBuild2(0).get());

        //PROMOTE build 2
        b = promoteMajorReleaseBuildNumber(p, 2);
        story.assertBuildStatusSuccess(b);

        story.assertLogContains("PROMOTED:"+masterHeadHash, b);
        story.assertLogContains("BUILDNUMBER:2", b);
        story.assertLogContains("MAJOR_RELEASE:true", b);
        story.assertLogContains("VERSION:6.0.0", b);
    }


    private WorkflowJob setupPipelineInJenkins(JenkinsRule story, GitSampleRepoRule sampleRepo) throws java.io.IOException {
        WorkflowJob p = story.jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsScmFlowDefinition(new GitStep(sampleRepo.toString()).createSCM(), "Jenkinsfile"));
        return p;
    }

    private String workOutCommitHashOfBuild(WorkflowRun b) {
        return b.getAction(BuildData.class).getLastBuiltRevision().getSha1().getName();
    }

    private void commitSomething() throws Exception {
        String randomFileName = RandomStringUtils.randomAlphabetic(10);
        sampleRepo.write(randomFileName, RandomStringUtils.randomAlphabetic(10));
        sampleRepo.git("add", randomFileName);
        sampleRepo.git("commit", "--message=commitForTest"+randomFileName);
    }

    private void commitJenkinsPipelineScript(String scriptThatLogsAssertableStrings, GitSampleRepoRule sampleRepo) throws Exception {
        sampleRepo.init();
        sampleRepo.write("Jenkinsfile", scriptThatLogsAssertableStrings);
        sampleRepo.git("add", "Jenkinsfile");
        sampleRepo.git("commit", "--message=files");
    }

    private WorkflowRun promoteMajorReleaseBuildNumber(WorkflowJob p, int buildNumber) throws IOException, SAXException, InterruptedException {
        return promoteBuildNumber(p, buildNumber, "?major");
    }

    private WorkflowRun promoteMinorReleaseBuildNumber(WorkflowJob p, int buildNumber) throws IOException, SAXException, InterruptedException {
        return promoteBuildNumber(p, buildNumber, "?minor");
    }

    private WorkflowRun promoteBuildNumber(WorkflowJob p, int buildNumber, String query) throws IOException, SAXException, InterruptedException {
        WorkflowRun b;
        b = p.getBuildByNumber(buildNumber);
        story.createWebClient().getPage(b, "promoterebuild" + query);
        b = p.getLastBuild();
        story.waitForCompletion(b);
        return b;
    }
}
