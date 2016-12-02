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

    private String majorVersionValue;
    private String minorVersionValue;
    private boolean majorRelease;

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

    boolean isMajorRelease() {
        return majorRelease;
    }
}
