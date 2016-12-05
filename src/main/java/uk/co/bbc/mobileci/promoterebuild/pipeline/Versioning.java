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

    Versioning(PromotedJob job, KVStoreProxy store, int buildNumber) {
        this.job = job;
        this.store = store;
        this.buildNumber = buildNumber;
    }

    boolean isVersionSet() {
        boolean set = false;
        String majorVersion = store.retrieve(majorVersionKey);

        if (majorVersion.length() > 0) {
           set = true;
        }

        return set;
    }

    void setVersion(String majorVersion, String minorVersion) throws IOException {
        store.store(majorVersionKey, majorVersion);
        store.store(minorVersionKey, minorVersion);
    }

    String getTargetVersion() {
        String majorVersion = store.retrieve(majorVersionKey);
        String minorVersion = store.retrieve(minorVersionKey);

        if (job.isPromotion()) {
            if (job.isMajorRelease()) {
                int majorInt = Integer.valueOf(majorVersion) + 1;
                return "" + majorInt + ".0.0";
            } else {
                int minorInt = Integer.valueOf(minorVersion) +1;
                return majorVersion + "." + minorInt + ".0";
            }
        }

        return "" + majorVersion + "." + minorVersion + ".0-dev." + buildNumber;
    }
}
