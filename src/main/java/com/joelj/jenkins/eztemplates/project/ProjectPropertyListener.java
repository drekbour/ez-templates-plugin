package com.joelj.jenkins.eztemplates.project;

import com.joelj.jenkins.eztemplates.listener.EzTemplateChange;
import com.joelj.jenkins.eztemplates.listener.PropertyListener;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.model.Job;

import java.io.IOException;

import static com.joelj.jenkins.eztemplates.listener.VersionEvaluator.preferSaveableListener;

/**
 * React to changes being made on template implementation projects
 */
@Extension
@Deprecated
public class ProjectPropertyListener extends PropertyListener<ProjectChildProperty> {

    public ProjectPropertyListener() {
        super(ProjectChildProperty.class);
    }

    @Override
    public void onUpdatedProperty(Job item, ProjectChildProperty property) throws IOException {
        if (preferSaveableListener() || EzTemplateChange.contains(item, property.getClass())) {
            return; // Ignore item listener updates if we trust the more general-purpose SaveableListener
        }
        TemplateUtils.handleTemplateImplementationSaved(item, property);
    }

}
