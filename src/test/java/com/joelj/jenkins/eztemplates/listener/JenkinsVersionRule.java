package com.joelj.jenkins.eztemplates.listener;

import jenkins.model.Jenkins;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * Clumsy {@link TestRule} to restore the recorded version.
 */
public class JenkinsVersionRule extends ExternalResource {

    private String originalVersion;
    private String forcedVersion;

    @Override
    protected void before() throws Throwable {
        if (forcedVersion == null) {
            originalVersion = Jenkins.VERSION;
        }
    }

    @Override
    protected void after() {
        if (forcedVersion == null) {
            Jenkins.VERSION = originalVersion;
        }
        forcedVersion = null;
    }

    public void set(String version) {
        Jenkins.VERSION = version;
    }
}
