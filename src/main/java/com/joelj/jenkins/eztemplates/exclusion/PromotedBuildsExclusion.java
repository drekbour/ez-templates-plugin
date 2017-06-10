package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;

import java.io.FileInputStream;
import java.io.IOException;

import static com.joelj.jenkins.eztemplates.utils.PropertyListener.getProperty;


/**
 * Quirky {@link Exclusion} which only needs to take action if the user chooses NOT to retain local
 * promotions.
 */
public class PromotedBuildsExclusion extends JobPropertyExclusion {

    public static final String ID = "promoted-builds";
    private static final String DESCRIPTION = "Retain local build promotions";
    private static final String PROPERTY_CLASSNAME = "hudson.plugins.promoted_builds.JobPropertyImpl";

    public PromotedBuildsExclusion() {
        super(ID, DESCRIPTION, PROPERTY_CLASSNAME);
    }

    @Override
    public void preClone(EzContext context, AbstractProject implementationProject) {
        if (context.isSelected()) {
            // Record the childs promotions (they will need re-registering)
            super.preClone(context, implementationProject);
        } else {
            // Record the templates promotions (they will need copying)
            // PERF re-scanning to find the template!
            AbstractProject template = TemplateUtils.getTemplateImplementationProperty(implementationProject).findTemplate();
            context.record(getProperty(template, JobPropertyImpl.class));
        }
    }

    /**
     * Adds all the promotions from the template project into the implementation one. All existing promotions from the
     * implementation project are lost.
     *
     * @param implementationProject
     */
    @Override
    public void postClone(EzContext context, AbstractProject implementationProject) {
        if (context.isSelected()) {
            super.postClone(context, implementationProject);
        } else {
            JobPropertyImpl templatePromotions = context.remember();
            if (templatePromotions != null) {
                try {
                    LOG.fine(String.format("Copying [%s] to %s", templatePromotions.getFullDisplayName(), implementationProject.getFullDisplayName()));
                    replacePromotions(getProperty(implementationProject, JobPropertyImpl.class), templatePromotions);
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }

    private static void replacePromotions(JobPropertyImpl clonedPromotions, JobPropertyImpl templatePromotions) throws IOException {
        // Hard delete content from the clone
        Util.deleteRecursive(clonedPromotions.getRootDir());
        clonedPromotions.getItems().clear();
        // create PromotionProcess on the clone from the XML stored in the template
        for (PromotionProcess process : templatePromotions.getItems()) {
            clonedPromotions.createProcessFromXml(process.getName(), new FileInputStream(process.getConfigFile().getFile()));
        }
    }

}
