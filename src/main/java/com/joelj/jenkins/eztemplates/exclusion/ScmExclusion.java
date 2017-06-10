package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import hudson.model.AbstractProject;
import hudson.scm.SCM;

import java.io.IOException;

public class ScmExclusion extends AbstractExclusion {

    public static final String ID = "scm";
    private static final String DESCRIPTION = "Retain local Source Code Management";

    public ScmExclusion() {
        super(ID, DESCRIPTION);
    }

    @Override
    public String getDisabledText() {
        return null;
    }

    @Override
    public void preClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        context.record(implementationProject.getScm());
    }

    @Override
    public void postClone(EzContext context, AbstractProject implementationProject) {
        if (!context.isSelected()) return;
        SCM scm = context.remember();
        try {
            implementationProject.setScm(scm);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

}
