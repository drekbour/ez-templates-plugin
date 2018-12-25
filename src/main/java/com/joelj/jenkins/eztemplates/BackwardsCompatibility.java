package com.joelj.jenkins.eztemplates;

import com.joelj.jenkins.eztemplates.project.ProjectChildProperty;
import com.joelj.jenkins.eztemplates.template.TemplateProperty;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;

@SuppressWarnings("unused")
public class BackwardsCompatibility {

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void addAliases() {
        // data persisted from <= 1.3.1
        Items.XSTREAM2.addCompatibilityAlias("com.joelj.jenkins.eztemplates.TemplateImplementationProperty", ProjectChildProperty.class);
        Items.XSTREAM2.addCompatibilityAlias("com.joelj.jenkins.eztemplates.TemplateProperty", TemplateProperty.class);
    }
}
