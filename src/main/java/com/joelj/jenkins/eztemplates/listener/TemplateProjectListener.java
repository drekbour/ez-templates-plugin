package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;

import java.io.IOException;

/**
 * React to changes being made on template projects
 */
@Extension
public class TemplateProjectListener extends PropertyListener<TemplateProperty> {

    public TemplateProjectListener() {
        super(TemplateProperty.class);
    }

    @Override
    public void onUpdatedProperty(Job item, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateSaved((AbstractProject) item, property);
    }

    @Override
    public void onDeletedProperty(Job item, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateDeleted((AbstractProject) item, property);
    }

    @Override
    public void onLocationChangedProperty(Job item, String oldFullName, String newFullName, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateRename((AbstractProject) item, property, oldFullName, newFullName);
    }

    @Override
    public void onCopiedProperty(Job src, Job item, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateCopied((AbstractProject) item, (AbstractProject) src);
    }

}
