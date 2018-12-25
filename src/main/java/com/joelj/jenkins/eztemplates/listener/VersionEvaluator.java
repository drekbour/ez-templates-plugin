package com.joelj.jenkins.eztemplates.listener;

import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

/**
 * Does this runtime include JENKINS-40435? If so we will use SaveableListener.onChange. If not, we
 * will use ItemListener.onUpdated.
 */
public class VersionEvaluator {
    private static final VersionNumber JENKINS40435_LTS = new VersionNumber("2.32.2");
    private static final VersionNumber JENKINS40435_LTS_MAX = new VersionNumber("2.33");
    private static final VersionNumber JENKINS40435 = new VersionNumber("2.37.rc");

    public static boolean preferSaveableListener() {
        VersionNumber v = Jenkins.getVersion();
        if (v == null) return false;
        if (v.isOlderThan(JENKINS40435_LTS)) return false;
        if (v.isNewerThan(JENKINS40435)) return true;
        return v.isOlderThan(JENKINS40435_LTS_MAX);
    }
}
