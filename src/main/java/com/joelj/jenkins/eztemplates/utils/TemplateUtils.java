package com.joelj.jenkins.eztemplates.utils;

import com.joelj.jenkins.eztemplates.AbstractTemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.ExclusionUtil;
import com.joelj.jenkins.eztemplates.exclusion.EzContext;
import com.joelj.jenkins.eztemplates.listener.EzBulkChange;
import com.joelj.jenkins.eztemplates.listener.EzTemplateChange;
import com.joelj.jenkins.eztemplates.pipeline.PipelineTemplateImplementationProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.JobProperty;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

public class TemplateUtils {
    private static final Logger LOG = Logger.getLogger("ez-templates");

    private static boolean isProject(Job job) {
        return job instanceof AbstractProject;
    }

    public static void handleTemplateSaved(Job templateProject, TemplateProperty property) throws IOException {
        Collection<Job> implementations = property.getImplementations();
        String detail = implementations.isEmpty() ? "No implementations to sync." : " Syncing implementations.";
        LOG.info(String.format("Template [%s] was saved.%s", templateProject.getFullDisplayName(), detail));
        for (Job impl : implementations) {
            handleTemplateImplementationSaved(impl, getTemplateImplementationProperty(impl)); // ? continue on exception
        }
    }

    public static void handleTemplateDeleted(Job templateProject, TemplateProperty property) throws IOException {
        LOG.info(String.format("Template [%s] was deleted.", templateProject.getFullDisplayName()));
        for (Job impl : property.getImplementations()) {
            EzTemplateChange change = new EzTemplateChange(impl, TemplateProperty.class);
            try {
                LOG.info(String.format("Removing template from [%s].", impl.getFullDisplayName()));
                impl.removeProperty(AbstractTemplateImplementationProperty.class);
                impl.save();
            } finally {
                change.commit();
            }
        }
    }

    public static void handleTemplateRename(Job templateProject, TemplateProperty property, String oldFullName, String newFullName) throws IOException {
        Collection<Job> implementations = property.getImplementations(oldFullName);
        String detail = implementations.isEmpty() ? "No implementations to sync." : "Syncing implementations.";
        LOG.info(String.format("Template [%s] was renamed. %s", templateProject.getFullDisplayName(), detail));

        for (Job impl : implementations) {
            EzTemplateChange change = new EzTemplateChange(impl, TemplateProperty.class);
            try {
                LOG.info(String.format("Updating template in [%s].", impl.getFullDisplayName()));
                AbstractTemplateImplementationProperty implProperty = getTemplateImplementationProperty(impl);
                implProperty.setTemplateJobName(newFullName);
                impl.save();
            } finally {
                change.commit();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void handleTemplateCopied(Job copy, Job original) throws IOException {
        LOG.info(String.format("Template [%s] was copied to [%s]. Forcing new project to be an implementation of the original.", original.getFullDisplayName(), copy.getFullDisplayName()));
        copy.removeProperty(TemplateProperty.class);
        JobProperty<?> childProp;
        // TODO abstract pipeline knowledge
        if( isProject(copy) ) {
            childProp = TemplateImplementationProperty.newImplementation(original.getFullName());
        } else {
            childProp = PipelineTemplateImplementationProperty.newImplementation(original.getFullName());
        }
        copy.removeProperty(AbstractTemplateImplementationProperty.class);
        copy.addProperty(childProp);
    }

    public static void handleTemplateImplementationSaved(Job implementationProject, AbstractTemplateImplementationProperty<?> property) throws IOException {
        EzTemplateChange change = new EzTemplateChange(implementationProject, property.getClass());
        try {
            if (property.getTemplateJobName() == null) {
                LOG.warning(String.format("Implementation [%s] has no template selected.", implementationProject.getFullDisplayName()));
                return;
            }

            LOG.info(String.format("Implementation [%s] syncing with [%s].", implementationProject.getFullDisplayName(), property.getTemplateJobName()));

            Job templateProject = property.findTemplate();
            if (templateProject == null) {
                // If the template can't be found, then it's probably a bug
                throw new IllegalStateException(String.format("Cannot find template [%s] used by implementation [%s]", property.getTemplateJobName(), implementationProject.getFullDisplayName()));
            }

            EzContext context = new EzContext(property.getExclusions());
            Collection<Exclusion> enabledExclusions = ExclusionUtil.enabledExclusions(property.exclusionDefinitions().getAll());
            applyTemplate(implementationProject, templateProject, context, enabledExclusions);
        } catch (RuntimeException e) {
            change.abort();
            throw e;
        } finally {
            change.commit();
        }
    }

    private static void applyTemplate(Job implementationProject, Job templateProject, EzContext context, Collection<Exclusion> exclusions) throws IOException {
        // 1. Capture values we want to keep
        for (Exclusion exclusion : exclusions) {
            context.setCurrentExclusionId(exclusion.getId());
            exclusion.preClone(context, implementationProject);
        }

        // 2. Load template's XML directly over the impl (in-memory, without affecting impl XML)
        implementationProject = cloneTemplate(implementationProject, templateProject);

        // 3. Restore values we kept
        // BulkChange makes sure save events are not generated for each postClone change made
        EzBulkChange bulkChange = new EzBulkChange(implementationProject);
        try {
            for (Exclusion exclusion : exclusions) {
                context.setCurrentExclusionId(exclusion.getId());
                exclusion.postClone(context, implementationProject);
            }
        } catch (RuntimeException e) {
            // Reset impl in-memory state from its XML
            LOG.severe(String.format("Rolling back [%s].", implementationProject.getFullDisplayName()));
            bulkChange.revert();
            throw e;
        } finally {
            // 4. Save impl in-memory state to XML
            // This may cascade (depth first) if the impl is also a template.
            bulkChange.commit();
        }
    }

    @SuppressFBWarnings
    private static Job cloneTemplate(Job implementationProject, Job templateProject) throws IOException {
        JobUtils.updateProjectWithXmlSource( implementationProject, templateProject.getConfigFile().getFile().toPath());
        return JobUtils.findJob(implementationProject.getFullName()); // Must search again as the old instance is defunct
    }

    /**
     * @param item A job of some kind
     * @return null if this is not a template implementation project
     */
    public static AbstractTemplateImplementationProperty getTemplateImplementationProperty(Item item) {
        return JobUtils.getProperty(item, AbstractTemplateImplementationProperty.class);
    }

}
