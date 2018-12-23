package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.model.AbstractItem;
import hudson.model.Job;
import hudson.model.JobProperty;

import java.io.IOException;

public class EzTemplatesExclusion extends AbstractExclusion<Job> {

    public static final String ID = "ez-templates";
    private static final String DESCRIPTION = "Retain EZ Templates mandatory fields";

    public EzTemplatesExclusion() {
        super(ID, DESCRIPTION);
    }

    private static class Data {
        String displayName;
        JobProperty templateProperty;
        JobProperty templateImplementationProperty;
    }

    @Override
    public String getDisabledText() {
        return "Cannot unselect this one!";
    }

    @Override
    public void preClone(EzContext context, Job implementationProject) {
        if (!context.isSelected()) return;
        Data data = new Data();
        data.displayName = implementationProject.getDisplayNameOrNull();
        data.templateProperty = implementationProject.getProperty(TemplateProperty.class);
        data.templateImplementationProperty = TemplateUtils.getTemplateImplementationProperty(implementationProject);
        context.record(data);
    }

    @Override
    public void postClone(EzContext context, Job implementationProject) {
        if (!context.isSelected()) return;
        try {
            fixProperties((Data) context.remember(), implementationProject);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void fixProperties(Data data, Job implementationProject) throws IOException {
        EzReflectionUtils.setFieldValue(AbstractItem.class, implementationProject, "displayName", data.displayName);

        implementationProject.removeProperty(data.templateImplementationProperty.getClass()); // If parent template is also an imple of a grand-parent
        implementationProject.addProperty(data.templateImplementationProperty);

        // Remove the cloned TemplateProperty belonging to the template
        implementationProject.removeProperty(TemplateProperty.class);
        if (data.templateProperty != null) {
            // !null means the Impl is _also_ a template for grand-children.
            implementationProject.addProperty(data.templateProperty);
        }
    }

}
