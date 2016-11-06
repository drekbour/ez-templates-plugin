package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import hudson.model.Job;
import hudson.model.JobProperty;

import java.io.IOException;

/**
 * Generic {@link Exclusion} which retains a given {@link JobProperty} through cloning
 */
public class JobPropertyExclusion<J extends Job> extends HardCodedExclusion<J> {
    private final String id;
    private final String description;
    private final String className;
    protected JobProperty cached; // Should be JobProperty<J>

    public JobPropertyExclusion(String id, String description, String className) {
        this.id = id;
        this.description = description;
        this.className = className;
    }

    @Override
    public void preClone(J implementationProject) {
        cached = implementationProject.getProperty(className);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postClone(J implementationProject) {
        try {
            if (cached != null) {
                // Removed from template = removed from all impls
                if (implementationProject.removeProperty(cached.getClass()) != null) {
                    implementationProject.addProperty(cached);
                }
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDisabledText() {
        // Assumes id is _also_ the plugin
        return ExclusionUtil.checkPlugin(id);
    }
}
