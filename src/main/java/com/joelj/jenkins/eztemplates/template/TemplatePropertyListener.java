package com.joelj.jenkins.eztemplates.template;

import com.joelj.jenkins.eztemplates.listener.PropertyListener;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.model.Job;

import java.io.IOException;


/**
 * React to changes being made on template projects
 */
@Extension
public class TemplatePropertyListener extends PropertyListener<TemplateProperty> {

    public TemplatePropertyListener() {
        super(TemplateProperty.class);
    }

    @Override
    public void onUpdatedProperty(Job item, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateSaved(item, property);
    }

    @Override
    public void onDeletedProperty(Job item, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateDeleted(item, property);
    }

    @Override
    public void onLocationChangedProperty(Job item, String oldFullName, String newFullName, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateRename(item, property, oldFullName, newFullName);
    }

    @Override
    public void onCopiedProperty(Job src, Job item, TemplateProperty property) throws IOException {
        TemplateUtils.handleTemplateCopied(item, src);
    }

}