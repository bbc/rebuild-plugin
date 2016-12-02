package uk.co.bbc.mobileci.promoterebuild.pipeline;

import java.io.IOException;

/**
 * Copyright © 2016 Media Applications Technologies. All rights reserved.
 */
class Versioning {

    private final PromotedJob job;
    private final KVStoreProxy store;
    private final int buildNumber;

    private String majorVersionKey = "majorVersion";
    private String minorVersionKey = "minorVersion";
    private String majorReleaseKey = "majorRelease";

    private String majorVersionValue;
    private String minorVersionValue;

    Versioning(PromotedJob job, KVStoreProxy store, int buildNumber) {
        this.job = job;
        this.store = store;
        this.buildNumber = buildNumber;
    }

    boolean isVersionSet() {
        boolean set = false;
        majorVersionValue = store.retrieve(majorVersionKey);
        minorVersionValue = store.retrieve(minorVersionKey);

        if (majorVersionValue.length() > 0) {
           set = true;
        }

        return set;
    }

    void setVersion(String majorVersion, String minorVersion) throws IOException {
        store.store(majorVersionKey, majorVersion);
        store.store(minorVersionKey, minorVersion);
    }

    String getVersion() {
        majorVersionValue = store.retrieve(majorVersionKey);
        minorVersionValue = store.retrieve(minorVersionKey);

        return majorVersionValue + '.' + minorVersionValue + ".0-dev." + buildNumber;
    }

    boolean setMajorRelease(boolean isMajorRelease) {
        try {
            if (job.isPromotion()) {
                store.store(majorReleaseKey, String.valueOf(isMajorRelease));
            } else {
                store.store(majorReleaseKey, String.valueOf(false));
            }
        } catch (IOException e) {
            // this should not happen
        }

        if (!job.isPromotion() && isMajorRelease) {
            return false;
        }

        return true;
    }

    boolean isMajorRelease() {
        return Boolean.valueOf(store.retrieve(majorReleaseKey));
    }
}
