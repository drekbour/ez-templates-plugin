package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import hudson.model.Job;
import hudson.model.JobProperty;

import java.io.IOException;

/**
 * Generic {@link Exclusion} which retains a given {@link JobProperty} through cloning
 */
public class JobPropertyExclusion<J extends Job> extends AbstractExclusion<J> {

    private final String className;

    public JobPropertyExclusion(String id, String description, String className) {
        super(id, description);
        this.className = className;
    }

    @Override
    public void preClone(EzContext context, J implementationProject) {
        if (!context.isSelected()) return;
        context.record(implementationProject.getProperty(className));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postClone(EzContext context, J implementationProject) {
        if (!context.isSelected()) return;
        JobProperty cached = context.remember();
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
}
