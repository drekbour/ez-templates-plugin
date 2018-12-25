package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import com.joelj.jenkins.eztemplates.template.TemplateProperty;
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
    public void preClone(EzContext context, Job child) {
        if (!context.isSelected()) return;
        Data data = new Data();
        data.displayName = child.getDisplayNameOrNull();
        data.templateProperty = child.getProperty(TemplateProperty.class);
        data.templateImplementationProperty = TemplateUtils.getChildProperty(child);
        context.record(data);
    }

    @Override
    public void postClone(EzContext context, Job child) {
        if (!context.isSelected()) return;
        try {
            fixProperties((Data) context.remember(), child);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void fixProperties(Data preClone, Job child) throws IOException {
        EzReflectionUtils.setFieldValue(AbstractItem.class, child, "displayName", preClone.displayName);

        child.removeProperty(preClone.templateImplementationProperty.getClass()); // If parent template is also an imple of a grand-parent
        child.addProperty(preClone.templateImplementationProperty);

        // Remove the cloned TemplateProperty belonging to the template
        child.removeProperty(TemplateProperty.class);
        if (preClone.templateProperty != null) {
            // !null means the Impl is _also_ a template for grand-children.
            child.addProperty(preClone.templateProperty);
        }
    }

}
