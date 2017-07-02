package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractProject;

import java.util.logging.Logger;

@Extension
public class TemplateImplementationSaveableListener extends EzSaveableListener<TemplateImplementationProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public TemplateImplementationSaveableListener() {
        super(TemplateImplementationProperty.class);
    }

    @Override
    public void onChangedProperty(AbstractProject job, XmlFile file, TemplateImplementationProperty property) throws Exception {
        LOG.warning(String.format("Implementation [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateImplementationSaved((AbstractProject) job, property);
    }
}
