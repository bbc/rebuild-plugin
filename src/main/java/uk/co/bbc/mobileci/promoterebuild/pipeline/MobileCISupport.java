package uk.co.bbc.mobileci.promoterebuild.pipeline;

import com.cloudbees.groovy.cps.NonCPS;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import java.io.IOException;
import java.util.Collection;


public final class MobileCISupport {

    private BuildChangeSet buildChangeSet;
    private PromotedJob promotedJob;
    private KVStoreProxy kvStoreProxy;
    private Versioning versioning;

    public MobileCISupport(BuildChangeSet buildChangeSet, PromotedJob promotedJob, KVStoreProxy kvStoreProxy, Versioning versioning) {
        this.buildChangeSet = buildChangeSet;
        this.promotedJob = promotedJob;
        this.kvStoreProxy = kvStoreProxy;
        this.versioning = versioning;
    }

    @NonCPS
    @Whitelisted
    public boolean getPromotion() {
        return promotedJob.isPromotion();
    }

    @NonCPS
    @Whitelisted
    public boolean isPromotion() {
        return promotedJob.isPromotion();
    }

    @NonCPS
    @Whitelisted
    public String getFromHash() {
        return promotedJob.getHash();
    }

    @NonCPS
    @Whitelisted
    public String getFromBuildNumber() {
        return promotedJob.getFromBuildNumber();
    }

    @NonCPS
    @Whitelisted
    public String getBuildTriggerHash() {
        return buildChangeSet.getBuildTriggerHash();
    }

    public String toString() {
        return "PromotedJob: from: " + getFromBuildNumber() + " for:" + getFromHash();
    }

    @NonCPS
    @Whitelisted
    public void store(String key, String value) throws IOException {
        kvStoreProxy.store(key, value);
    }

    @NonCPS
    @Whitelisted
    public String retrieve(String key) {
        return kvStoreProxy.retrieve(key);
    }

    @NonCPS
    @Whitelisted
    public String getChangeSet() {
        return buildChangeSet.getChangeSet();
    }

    @NonCPS
    @Whitelisted
    public String getBranchName() {
        return buildChangeSet.getBranchName();
    }

    @NonCPS
    @Whitelisted
    public Collection<String> getBranchNames() {
        return buildChangeSet.getBranchNames();
    }

    @NonCPS
    @Whitelisted
    public boolean isVersionSet() {
        return versioning.isVersionSet();
    }

    @NonCPS
    @Whitelisted
    public void setVersion(String majorVersion, String minorVersion) throws IOException {
        versioning.setVersion(majorVersion, minorVersion);
    }

    @NonCPS
    @Whitelisted
    public String getTargetVersion() {
        return versioning.getTargetVersion();
    }

    @NonCPS
    @Whitelisted
    public void storeTargetVersion() throws IOException {
        versioning.storeTargetVersion();
    }

    @NonCPS
    @Whitelisted
    public String getFinalVersion() {
        return versioning.getFinalVersion();
    }
}
