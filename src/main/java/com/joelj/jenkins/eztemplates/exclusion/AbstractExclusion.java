package com.joelj.jenkins.eztemplates.exclusion;

import java.util.logging.Logger;

/**
 * It is expected that all {@link Exclusion}s extend this. Implementations should use the inherited logger for any
 * messages!
 */
public abstract class AbstractExclusion implements Exclusion {

    protected static final Logger LOG = Logger.getLogger("ez-templates");

    private final String id;
    private final String description;

    protected AbstractExclusion(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Default implementation assumes the Exclusion's {@link #getId} is the plugin id so just checks to see
     * if it is currently installed.
     *
     * @return non-null if plugin is unavailable
     */
    @Override
    public String getDisabledText() {
        return Exclusions.checkPlugin(getId());
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getId());
    }

}
