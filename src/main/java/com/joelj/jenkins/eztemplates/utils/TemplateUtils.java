package com.joelj.jenkins.eztemplates.utils;

import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.exclusion.EzContext;
import com.joelj.jenkins.eztemplates.listener.EzTemplateChange;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.model.AbstractProject;
import hudson.model.Item;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TemplateUtils {
    private static final Logger LOG = Logger.getLogger("ez-templates");

    public static void handleTemplateSaved(AbstractProject templateProject, TemplateProperty property) throws IOException {
        Collection<AbstractProject> implementations = property.getImplementations();
        String detail = implementations.isEmpty() ? "No implementations to sync." : " Syncing implementations.";
        LOG.info(String.format("Template [%s] was saved.%s", templateProject.getFullDisplayName(), detail));
        for (AbstractProject impl : implementations) {
            handleTemplateImplementationSaved(impl, getTemplateImplementationProperty(impl)); // ? continue on exception
        }
    }

    public static void handleTemplateDeleted(AbstractProject templateProject, TemplateProperty property) throws IOException {
        LOG.info(String.format("Template [%s] was deleted.", templateProject.getFullDisplayName()));
        for (AbstractProject impl : property.getImplementations()) {
            EzTemplateChange change = new EzTemplateChange(impl);
            try {
                LOG.info(String.format("Removing template from [%s].", impl.getFullDisplayName()));
                impl.removeProperty(TemplateImplementationProperty.class);
                impl.save();
            } finally {
                change.commit();
            }
        }
    }

    public static void handleTemplateRename(AbstractProject templateProject, TemplateProperty property, String oldFullName, String newFullName) throws IOException {
        Collection<AbstractProject> implementations = TemplateProperty.getImplementations(oldFullName);
        String detail = implementations.isEmpty() ? "No implementations to sync." : " Syncing implementations.";
        LOG.info(String.format("Template [%s] was renamed.%s", templateProject.getFullDisplayName(), detail));
        for (AbstractProject impl : implementations) {
            EzTemplateChange change = new EzTemplateChange(impl);
            try {
                LOG.info(String.format("Updating template in [%s].", impl.getFullDisplayName()));
                TemplateImplementationProperty implProperty = getTemplateImplementationProperty(impl);
                if (oldFullName.equals(implProperty.getTemplateJobName())) {
                    implProperty.setTemplateJobName(newFullName);
                    impl.save();
                }
            } finally {
                change.commit();
            }
        }
    }

    public static void handleTemplateCopied(AbstractProject copy, AbstractProject original) throws IOException {
        LOG.info(String.format("Template [%s] was copied to [%s]. Forcing new project to be an implementation of the original.", original.getFullDisplayName(), copy.getFullDisplayName()));
        copy.removeProperty(TemplateProperty.class);
        copy.removeProperty(TemplateImplementationProperty.class);
        TemplateImplementationProperty implProperty = TemplateImplementationProperty.newImplementation(original.getFullName());
        copy.addProperty(implProperty);
    }

    public static void handleTemplateImplementationSaved(AbstractProject implementationProject, TemplateImplementationProperty property) throws IOException {
        EzTemplateChange change = new EzTemplateChange(implementationProject);
        try {
            if (property.getTemplateJobName() == null) {
                LOG.warning(String.format("Implementation [%s] has no template selected.", implementationProject.getFullDisplayName()));
                return;
            }

            LOG.info(String.format("Implementation [%s] syncing with [%s].", implementationProject.getFullDisplayName(), property.getTemplateJobName()));

            AbstractProject templateProject = property.findTemplate();
            if (templateProject == null) {
                // If the template can't be found, then it's probably a bug
                throw new IllegalStateException(String.format("Cannot find template [%s] used by implementation [%s]", property.getTemplateJobName(), implementationProject.getFullDisplayName()));
            }

            EzContext context = new EzContext(property.getExclusions());
            applyTemplate(implementationProject, templateProject, context, Exclusions.enabledExceptions());
        } finally {
            change.commit();
        }
    }

    private static void applyTemplate(AbstractProject implementationProject, AbstractProject templateProject, EzContext context, Collection<Exclusion> exclusions) throws IOException {
        // Capture values we want to keep
        for (Exclusion exclusion : exclusions) {
            context.setCurrentExclusionId(exclusion.getId());
            try {
                exclusion.preClone(context, implementationProject);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, String.format("Templating failed analyse %s", exclusion), e);
                throw e; // Fail immediately on any pre-clone issue
            }
        }

        implementationProject = cloneTemplate(implementationProject, templateProject);

        // BulkChange target must be the post-cloned instance of the impl
        BulkChange bulkChange = new BulkChange(implementationProject);
        try {
            // Restore values we kept
            boolean failure = false;
            for (Exclusion exclusion : exclusions) {
                context.setCurrentExclusionId(exclusion.getId());
                try {
                    exclusion.postClone(context, implementationProject);
                } catch (RuntimeException e) {
                    LOG.log(Level.WARNING, String.format("Templating failed apply %s", exclusion), e);
                    // since we've already cloned the template to the filesystem, attempt to apply all exclusions
                    failure = true;
                }
            }
            if (failure) {
                throw new RuntimeException("Templating failed, see logs");
            }
        } finally {
            bulkChange.commit(); // These changes have been made in memory so should be reflected on disk.
        }
    }

    @SuppressFBWarnings
    private static AbstractProject cloneTemplate(AbstractProject implementationProject, AbstractProject templateProject) throws IOException {
        File templateConfigFile = templateProject.getConfigFile().getFile();
        BufferedReader reader = new BufferedReader(new FileReader(templateConfigFile));
        try {
            Source source = new StreamSource(reader);
            implementationProject = ProjectUtils.updateProjectWithXmlSource(implementationProject, source);
        } finally {
            reader.close();
        }
        return implementationProject;
    }

    /**
     * @param item A job of some kind
     * @return null if this is not a template implementation project
     */
    public static TemplateImplementationProperty getTemplateImplementationProperty(Item item) {
        return ProjectUtils.getProperty(item, TemplateImplementationProperty.class);
    }

}
