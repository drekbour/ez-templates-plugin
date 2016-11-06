package com.joelj.jenkins.eztemplates.exclusion;

import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.AbstractProject;

public class DisabledExclusion extends HardCodedExclusion<AbstractProject> {

    public static final String ID = "disabled";
    private boolean disabled;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Retain local disabled setting";
    }

    @Override
    public String getDisabledText() {
        return null;
    }

    @Override
    public void preClone(AbstractProject implementationProject) {
        disabled = implementationProject.isDisabled();
    }

    @Override
    public void postClone(AbstractProject implementationProject) {
        EzReflectionUtils.setFieldValue(AbstractProject.class, implementationProject, "disabled", disabled);
    }

}
