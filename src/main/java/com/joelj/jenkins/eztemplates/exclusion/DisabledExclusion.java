package com.joelj.jenkins.eztemplates.exclusion;

import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.AbstractProject;

public class DisabledExclusion extends AbstractExclusion {

    public static final String ID = "disabled";
    private static final String DESCRIPTION = "Retain local disabled setting";

    public DisabledExclusion() {
        super(ID, DESCRIPTION);
    }

    @Override
    public String getDisabledText() {
        return null;
    }

    @Override
    public void preClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        context.record(implementationProject.isDisabled());
    }

    @Override
    public void postClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        Boolean disabled = context.remember();
        EzReflectionUtils.setFieldValue(AbstractProject.class, implementationProject, "disabled", disabled);
    }

}
