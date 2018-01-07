package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.AbstractTemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractProject;

import java.util.logging.Logger;

@Extension
public class TemplateImplementationSaveableListener extends EzSaveableListener<AbstractTemplateImplementationProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public TemplateImplementationSaveableListener() {
        super(AbstractTemplateImplementationProperty.class);
    }

    @Override
    public void onChangedProperty(AbstractProject job, XmlFile file, AbstractTemplateImplementationProperty property) throws Exception {
        LOG.fine(String.format("Implementation [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateImplementationSaved((AbstractProject) job, property);
    }
}
