package com.joelj.jenkins.eztemplates.exclusion;

import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.AbstractItem;
import hudson.model.Job;

public class DescriptionExclusion extends HardCodedExclusion<Job> {

    public static final String ID = "description";
    private String description;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Retain local description";
    }

    @Override
    public String getDisabledText() {
        return null;
    }

    @Override
    public void preClone(Job implementationProject) {
        description = implementationProject.getDescription();
    }

    @Override
    public void postClone(Job implementationProject) {
        EzReflectionUtils.setFieldValue(AbstractItem.class, implementationProject, "description", description);
    }

}
