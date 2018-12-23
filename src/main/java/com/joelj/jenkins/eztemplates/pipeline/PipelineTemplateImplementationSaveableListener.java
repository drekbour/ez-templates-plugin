package com.joelj.jenkins.eztemplates.pipeline;

import com.joelj.jenkins.eztemplates.listener.EzSaveableListener;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Job;

import java.util.logging.Logger;

@Extension
public class PipelineTemplateImplementationSaveableListener extends EzSaveableListener<PipelineTemplateImplementationProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public PipelineTemplateImplementationSaveableListener() {
        super(PipelineTemplateImplementationProperty.class);
    }

    @Override
    public void onChangedProperty(Job job, XmlFile file, PipelineTemplateImplementationProperty property) throws Exception {
        LOG.fine(String.format("Implementation [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateImplementationSaved(job, property);
    }
}
