package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import com.joelj.jenkins.eztemplates.utils.TemplateUtils;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;

import java.io.FileInputStream;
import java.io.IOException;

import static com.joelj.jenkins.eztemplates.utils.JobUtils.getProperty;


/**
 * Quirky {@link Exclusion} which only needs to take action if the user chooses NOT to retain local
 * promotions.
 */
public class PromotedBuildsExclusion extends JobPropertyExclusion<AbstractProject> {

    public static final String ID = "promoted-builds";
    private static final String DESCRIPTION = "Retain local build promotions";
    private static final String PROPERTY_CLASSNAME = "hudson.plugins.promoted_builds.JobPropertyImpl";

    public PromotedBuildsExclusion() {
        super(ID, DESCRIPTION, PROPERTY_CLASSNAME);
    }

    @Override
    public void preClone(EzContext context, AbstractProject child) {
        if (context.isSelected()) {
            // Record the childs promotions (they will need re-registering)
            super.preClone(context, child);
        } else {
            // Record the templates promotions (they will need copying)
            // PERF re-scanning to find the template!
            Job template = TemplateUtils.getChildProperty(child).findTemplate();
            context.record(getProperty(template, JobPropertyImpl.class));
        }
    }

    /**
     * Adds all the promotions from the template project into the implementation one. All existing promotions from the
     * implementation project are lost.
     *
     * @param child
     */
    @Override
    public void postClone(EzContext context, AbstractProject child) {
        if (context.isSelected()) {
            super.postClone(context, child);
        } else {
            JobPropertyImpl templatePromotions = context.remember();
            if (templatePromotions != null) {
                try {
                    LOG.fine(String.format("Copying [%s] to %s", templatePromotions.getFullDisplayName(), child.getFullDisplayName()));
                    replacePromotions(getProperty(child, JobPropertyImpl.class), templatePromotions);
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
