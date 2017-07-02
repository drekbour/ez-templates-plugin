package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Extension;
import hudson.model.Job;

import java.io.IOException;

/**
 * React to changes being made on template implementation projects
 */
@Extension
public class TemplateImplementationProjectListener extends PropertyListener<AbstractTemplateImplementationProperty> {

    public TemplateImplementationProjectListener() {
        super(AbstractTemplateImplementationProperty.class);
    }

    @Override
    public void onUpdatedProperty(Job item, AbstractTemplateImplementationProperty property) throws IOException {
        TemplateUtils.handleTemplateImplementationSaved(item, property);
    }

}
