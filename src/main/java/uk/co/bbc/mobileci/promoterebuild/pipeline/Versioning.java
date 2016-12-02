package uk.co.bbc.mobileci.promoterebuild.pipeline;

/**
 * Copyright © 2016 Media Applications Technologies. All rights reserved.
 */
public class Versioning {

    private final PromotedJob job;
    private final KVStoreProxy store;
    private final int buildNumber;

    private String majorVersionKey = "majorVersion";
    private String minorVersionKey = "minorVersion";

    private String majorVersionValue;
    private String minorVersionValue;

    public Versioning(PromotedJob job, KVStoreProxy store, int buildNumber) {
        this.job = job;
        this.store = store;
        this.buildNumber = buildNumber;
    }

    public boolean isVersionSet() {
        boolean set = false;
        majorVersionValue = store.retrieve(majorVersionKey);
        minorVersionValue = store.retrieve(minorVersionKey);

        if (majorVersionValue.length() > 0) {
           set = true;
        }

        return set;
    }

    public String getVersion() {
        majorVersionValue = store.retrieve(majorVersionKey);
        minorVersionValue = store.retrieve(minorVersionKey);

        return majorVersionValue + '.' + minorVersionValue + ".0-dev." + buildNumber;
    }
}
