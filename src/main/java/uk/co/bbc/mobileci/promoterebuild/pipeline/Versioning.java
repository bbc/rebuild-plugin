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

    String getTargetVersion() throws NumberFormatException {
        String majorVersion = store.retrieve(majorVersionKey);
        String minorVersion = store.retrieve(minorVersionKey);

        if (job.isPromotion()) {
            if (job.isMajorRelease()) {
                int majorInt = Integer.parseInt(majorVersion) + 1;
                return "" + majorInt + ".0.0";
            } else {
                int minorInt = Integer.parseInt(minorVersion) +1;
                return majorVersion + "." + minorInt + ".0";
            }
        }

        return "" + majorVersion + "." + minorVersion + ".0-dev." + buildNumber;
    }

    void storeTargetVersion() throws IOException, NumberFormatException {
        if (job.isPromotion()) {
            if (job.isMajorRelease()) {
                String majorVersion = store.retrieve(majorVersionKey);
                int majorInt = Integer.parseInt(majorVersion) + 1;
                store.store(majorVersionKey, String.valueOf(majorInt));
            } else {
                String minorVersion = store.retrieve(minorVersionKey);
                int minorInt = Integer.parseInt(minorVersion) +1;
                store.store(minorVersionKey, String.valueOf(minorInt));
            }
        }
    }

    String getFinalVersion() {
        String majorVersion = store.retrieve(majorVersionKey);
        String minorVersion = store.retrieve(minorVersionKey);

        if (job.isPromotion()) {
            if (job.isMajorRelease()) {
                return "" + majorVersion + ".0.0";
            } else {
                return majorVersion + "." + minorVersion + ".0";
            }
        }

        return "" + majorVersion + "." + minorVersion + ".0-dev." + buildNumber;
    }
}
