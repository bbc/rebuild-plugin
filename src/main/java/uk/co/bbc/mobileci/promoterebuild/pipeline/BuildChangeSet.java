package uk.co.bbc.mobileci.promoterebuild.pipeline;

import hudson.model.Run;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;
import hudson.scm.ChangeLogSet;
import org.eclipse.jgit.lib.ObjectId;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.*;

/**
 * Created by beazlr02 on 04/05/16.
 */
public final class BuildChangeSet {

    private WorkflowRun workflowRun;
    private boolean isMultibranchPipeline = false;
    private boolean isPipeline = false;

    public BuildChangeSet(WorkflowRun result) {
        this.workflowRun = result;
        isMultibranchPipeline = isMultibranchPipeline(result);
        isPipeline = !isMultibranchPipeline;
    }

    private boolean isMultibranchPipeline(WorkflowRun run) {
        boolean result = false;

        if(run != null) {
            result = run.getParent().getParent() instanceof WorkflowMultiBranchProject;
        }

        return result;
    }

    @Whitelisted
    public String getChangeSet() {
        StringBuilder changeSet = new StringBuilder();

        if (workflowRun != null) {
            for (ChangeLogSet<? extends ChangeLogSet.Entry> entries : workflowRun.getChangeSets()) {

                for (ChangeLogSet.Entry entry : entries) {

                    changeSet.append(entry.getAuthor())
                            .append(": ")
                            .append(entry.getMsg())
                            .append(" (")
                            .append(entry.getCommitId())
                            .append(")\n");
                }
            }
        }

        return changeSet.toString();
    }

    @Whitelisted
    public String getBranchName() {
        String result = "";

        if (getBranchNames().size() > 0) {
            result =  ((List<String>) getBranchNames()).get(0);
        }

        return result;
    }

    @Whitelisted
    public Collection<String> getBranchNames() {
        Map<String, List<String>> remotesToBranches = new HashMap<>();
        GitSCM jobBaseSCM = null;

        if(getScm(workflowRun) != null) {
            jobBaseSCM = getScm(workflowRun);
            List<BuildData> actions = workflowRun.getActions(BuildData.class);
            for (BuildData action : actions) {
                if (action.getRemoteUrls().iterator().hasNext()) {
                    String remote = action.getRemoteUrls().iterator().next();
                    Revision lastBuiltRevision = action.getLastBuiltRevision();
                    if (lastBuiltRevision != null) {
                        Collection<Branch> branches = lastBuiltRevision.getBranches();
                        for (Branch branch : branches) {
                            String name = branch.getName().replaceAll("refs/remotes/origin/", "").replaceAll("origin/", "");
                            List<String> names = remotesToBranches.get(remote);
                            if(names != null) {
                                names.add(name);
                            } else {
                                names = new ArrayList<>();
                                names.add(name);
                            }
                            remotesToBranches.put(remote, names);
                        }
                    }
                }
            }
        }

        List<String> branchNames = remotesToBranches.get(getBaseRemote(jobBaseSCM));
        if(branchNames == null) {
            branchNames = new ArrayList<>();
        }
        return branchNames;
    }

    private GitSCM getScm(Run<?, ?> up) {
        GitSCM scm = null;
        try {
            if(isPipeline) {
                scm = (GitSCM) ((CpsScmFlowDefinition) ((WorkflowJob) up.getParent()).getDefinition()).getScm();
            } else if (isMultibranchPipeline){
                scm = (GitSCM) up.getParent().getProperty(BranchJobProperty.class).getBranch().getScm();
            }
        } catch (ClassCastException ignored) {
        }
        return scm;
    }

    private String getBaseRemote(GitSCM jobBaseSCM) {
        if(jobBaseSCM != null) {
            return jobBaseSCM.getUserRemoteConfigs().get(0).getUrl();
        } else {
            return null;
        }
    }

    @Whitelisted
    public String getBuildTriggerHash() {
        Map<String, String> commitHashes = new HashMap<>();
        GitSCM jobBaseSCM = null;

        if(getScm(workflowRun) != null) {
            jobBaseSCM = getScm(workflowRun);
            List<BuildData> actions = workflowRun.getActions(BuildData.class);
            for (BuildData action : actions) {
                if (action.getRemoteUrls().iterator().hasNext()) {
                    String remote = action.getRemoteUrls().iterator().next();
                    Revision lastBuiltRevision = action.getLastBuiltRevision();
                    if (lastBuiltRevision != null) {
                        ObjectId sha1 = lastBuiltRevision.getSha1();
                        if (sha1 != null) {
                            String hash = sha1.getName();
                            commitHashes.put(remote, hash);
                        }
                    }
                }
            }
        }

        String hash = commitHashes.get(getBaseRemote(jobBaseSCM));
        if(hash == null) {
            hash = "";
        }
        return hash;
    }
}
