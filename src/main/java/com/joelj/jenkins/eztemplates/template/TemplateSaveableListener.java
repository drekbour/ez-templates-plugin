package com.joelj.jenkins.eztemplates.template;

import com.joelj.jenkins.eztemplates.listener.EzSaveableListener;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Job;

import java.util.logging.Logger;

@Extension
public class TemplateSaveableListener extends EzSaveableListener<TemplateProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public TemplateSaveableListener() {
        super(TemplateProperty.class);
    }

    @Override
    public void onChangedProperty(Job job, XmlFile file, TemplateProperty property) throws Exception {
        LOG.fine(String.format("Template [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateSaved(job, property);
    }
}
