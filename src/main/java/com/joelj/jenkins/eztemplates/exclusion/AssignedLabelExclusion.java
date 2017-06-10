package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import hudson.model.AbstractProject;
import hudson.model.Label;

import java.io.IOException;

public class AssignedLabelExclusion extends AbstractExclusion {

    public static final String ID = "assigned-label";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Retain local assigned label";
    }

    @Override
    public String getDisabledText() {
        return null;
    }

    @Override
    public void preClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        context.record(implementationProject.getAssignedLabel());
    }

    @Override
    public void postClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        try {
            Label label = context.remember();
            implementationProject.setAssignedLabel(label);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

}
