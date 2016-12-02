package uk.co.bbc.mobileci.promoterebuild.pipeline;

/**
 * Copyright © 2016 Media Applications Technologies. All rights reserved.
 */
public class Versioning {

    private final PromotedJob job;
    private final KVStoreProxy store;

    private String majorVersionKey = "majorVersion";
    private String minorVersionKey = "minorVersion";

    private String majorVersionValue;
    private String minorVersionValue;

    public Versioning(PromotedJob job, KVStoreProxy store) {
        this.job = job;
        this.store = store;
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
}
