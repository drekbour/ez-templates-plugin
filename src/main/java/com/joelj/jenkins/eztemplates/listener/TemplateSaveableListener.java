package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractProject;

import java.util.logging.Logger;

@Extension
public class TemplateSaveableListener extends EzSaveableListener<TemplateProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public TemplateSaveableListener() {
        super(TemplateProperty.class);
    }

    @Override
    public void onChangedProperty(AbstractProject job, XmlFile file, TemplateProperty property) throws Exception {
        LOG.fine(String.format("Template [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateSaved((AbstractProject) job, property);
    }
}
