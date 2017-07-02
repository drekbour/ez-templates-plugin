package com.joelj.jenkins.eztemplates.exclusion;

import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.AbstractItem;
import hudson.model.Job;

public class DescriptionExclusion extends AbstractExclusion<Job> {

    public static final String ID = "description";
    private static final String DESCRIPTION = "Retain local description";

    public DescriptionExclusion() {
        super(ID, DESCRIPTION);
    }

    @Override
    public String getDisabledText() {
        return null;
    }

    @Override
    public void preClone(EzContext context, Job implementationProject) {
        if (!context.isSelected()) return;
        context.record(implementationProject.getDescription());
    }

    @Override
    public void postClone(EzContext context, Job implementationProject) {
        if (!context.isSelected()) return;
        String description = context.remember();
        EzReflectionUtils.setFieldValue(AbstractItem.class, implementationProject, "description", description);
    }

}
