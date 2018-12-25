package com.joelj.jenkins.eztemplates.project;

import com.joelj.jenkins.eztemplates.listener.EzSaveableListener;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Job;

import java.util.logging.Logger;

@Extension
public class ProjectSaveableListener extends EzSaveableListener<ProjectChildProperty> {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    public ProjectSaveableListener() {
        super(ProjectChildProperty.class);
    }

    @Override
    public void onChangedProperty(Job job, XmlFile file, ProjectChildProperty property) throws Exception {
        LOG.fine(String.format("Implementation [%s] saved", job.getFullDisplayName()));
        TemplateUtils.handleTemplateImplementationSaved(job, property);
    }
}
