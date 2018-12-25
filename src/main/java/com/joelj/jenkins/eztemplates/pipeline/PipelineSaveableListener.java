package com.joelj.jenkins.eztemplates.pipeline;

import com.joelj.jenkins.eztemplates.listener.EzSaveableListener;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Job;

import java.util.logging.Logger;

@Extension
public class PipelineSaveableListener extends EzSaveableListener<PipelineChildProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public PipelineSaveableListener() {
        super(PipelineChildProperty.class);
    }

    @Override
    public void onChangedProperty(Job job, XmlFile file, PipelineChildProperty property) throws Exception {
        LOG.fine(String.format("Implementation [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateImplementationSaved(job, property);
    }
}
