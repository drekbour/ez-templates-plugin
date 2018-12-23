package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
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
public class TemplateImplementationProjectListener extends PropertyListener<TemplateImplementationProperty> {

    public TemplateImplementationProjectListener() {
        super(TemplateImplementationProperty.class);
    }

    @Override
    public void onUpdatedProperty(Job item, TemplateImplementationProperty property) throws IOException {
        if (preferSaveableListener || EzTemplateChange.contains(item, property.getClass())) {
            return; // Ignore item listener updates if we trust the more general-purpose SaveableListener
        }
        TemplateUtils.handleTemplateImplementationSaved(item, property);
    }

}
