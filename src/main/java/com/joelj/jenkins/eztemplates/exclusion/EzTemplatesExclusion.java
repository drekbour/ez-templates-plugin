package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.AbstractItem;
import hudson.model.Job;
import hudson.model.JobProperty;

import java.io.IOException;

public class EzTemplatesExclusion extends HardCodedExclusion<Job> {

    public static final String ID = "ez-templates";
    private String displayName;
    private JobProperty templateProperty;
    private JobProperty templateImplementationProperty;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Retain EZ Templates mandatory fields";
    }

    @Override
    public String getDisabledText() {
        return "Cannot unselect this one!";
    }

    @Override
    public void preClone(Job implementationProject) {
        displayName = implementationProject.getDisplayNameOrNull();
        templateProperty = implementationProject.getProperty(TemplateProperty.class);
        templateImplementationProperty = implementationProject.getProperty(TemplateImplementationProperty.class);
    }

    @Override
    public void postClone(Job implementationProject) {
        try {
            fixProperties(implementationProject);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    private void fixProperties(Job implementationProject) throws IOException {

        EzReflectionUtils.setFieldValue(AbstractItem.class, implementationProject, "displayName", displayName);

        implementationProject.removeProperty(templateImplementationProperty.getClass()); // If parent template is also an imple of a grand-parent
        implementationProject.addProperty(templateImplementationProperty);

        // Remove the cloned TemplateProperty belonging to the template
        implementationProject.removeProperty(TemplateProperty.class);
        if (templateProperty != null) {
            // !null means the Impl is _also_ a template for grand-children.
            implementationProject.addProperty(templateProperty);
        }
    }

}
