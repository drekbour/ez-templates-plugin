package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import hudson.model.AbstractProject;
import hudson.model.JobProperty;

import java.io.IOException;
import java.util.logging.Logger;

public class OwnershipExclusion extends JobPropertyExclusion {

    private static Logger LOG = Logger.getLogger("ez-templates");

    public static final String ID = "ownership";
    protected static final String PROPERTY_CLASSNAME = "com.synopsys.arc.jenkins.plugins.ownership.jobs.JobOwnerJobProperty";

    public OwnershipExclusion() {
        super(ID, "Retain local ownership property", PROPERTY_CLASSNAME);
    }

    @Override
    public void postClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        // JENKINS-43293 It's permissable for template to never have had ownership instantiated. This looks the same
        // to the user as setting it explicitly to "unknown" but is not the same to ez.
        JobProperty cached = context.remember();
        try {
            if (cached != null) {
                if (implementationProject.removeProperty(cached.getClass()) == null) {
                    LOG.warning("JENKINS-43293 Template job ownership has not been initialised. You should set it explicitly to someone, then to unknown.");
                }
                implementationProject.addProperty(cached);
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }
}
