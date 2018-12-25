package com.joelj.jenkins.eztemplates.pipeline;

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
public class PipelinePropertyListener extends PropertyListener<PipelineChildProperty> {

    public PipelinePropertyListener() {
        super(PipelineChildProperty.class);
    }

    @Override
    public void onUpdatedProperty(Job item, PipelineChildProperty property) throws IOException {
        if (preferSaveableListener() || EzTemplateChange.contains(item, property.getClass())) {
            return; // Ignore item listener updates if we trust the more general-purpose SaveableListener
        }
        TemplateUtils.handleTemplateImplementationSaved(item, property);
    }

}
