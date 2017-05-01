package com.joelj.jenkins.eztemplates.utils;

import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.TemplateProperty;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.exclusion.HardCodedExclusion;
import com.joelj.jenkins.eztemplates.promotedbuilds.PromotedBuildsTemplateUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractProject;
import hudson.model.Item;
import jenkins.model.Jenkins;

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
        LOG.info(String.format("Template [%s] was saved. Syncing implementations.", templateProject.getFullDisplayName()));
        for (AbstractProject impl : property.getImplementations()) {
            handleTemplateImplementationSaved(impl, getTemplateImplementationProperty(impl)); // ? continue on exception
        }
    }

    public static void handleTemplateDeleted(AbstractProject templateProject, TemplateProperty property) throws IOException {
        LOG.info(String.format("Template [%s] was deleted.", templateProject.getFullDisplayName()));
        for (AbstractProject impl : property.getImplementations()) {
            LOG.info(String.format("Removing template from [%s].", impl.getFullDisplayName()));
            impl.removeProperty(TemplateImplementationProperty.class);
            ProjectUtils.silentSave(impl);
        }
    }

    public static void handleTemplateRename(AbstractProject templateProject, TemplateProperty property, String oldFullName, String newFullName) throws IOException {
        LOG.info(String.format("Template [%s] was renamed. Updating implementations.", templateProject.getFullDisplayName()));
        for (AbstractProject impl : TemplateProperty.getImplementations(oldFullName)) {
            LOG.info(String.format("Updating template in [%s].", impl.getFullDisplayName()));
            TemplateImplementationProperty implProperty = getTemplateImplementationProperty(impl);
            if (oldFullName.equals(implProperty.getTemplateJobName())) {
                implProperty.setTemplateJobName(newFullName);
                ProjectUtils.silentSave(impl);
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

        applyTemplate(implementationProject, templateProject, Exclusions.configuredExclusions(property));
    }

    private static void applyTemplate(AbstractProject implementationProject, AbstractProject templateProject, Collection<Exclusion> exclusions) throws IOException {
        // Capture values we want to keep
        for (Exclusion exclusion : exclusions) {
            try {
                ((HardCodedExclusion) exclusion).preClone(implementationProject);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, String.format("Templating failed analyse %s", exclusion), e);
                throw e; // Fail immediately on any pre-clone issue
            }
        }

        implementationProject = cloneTemplate(implementationProject, templateProject);

        // Restore values we want to keep - via reflection to prevent infinite save recursion
        boolean failure = false;
        for (Exclusion exclusion : exclusions) {
            try {
                ((HardCodedExclusion) exclusion).postClone(implementationProject);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, String.format("Templating failed apply %s", exclusion), e);
                // since we've already cloned the template to the filesystem, attempt to apply all exclusions
                failure = true;
            }
        }
        if (failure) {
            throw new RuntimeException("Templating failed, see logs");
        }

        ProjectUtils.silentSave(implementationProject);
    }

    @SuppressFBWarnings
    private static AbstractProject cloneTemplate(AbstractProject implementationProject, AbstractProject templateProject) throws IOException {
        AbstractProject cloned = synchronizeConfigFiles(implementationProject, templateProject);
        if (Jenkins.getInstance().getPlugin("promoted-builds") != null) {
            PromotedBuildsTemplateUtils.addPromotions(cloned, templateProject);
        }
        return cloned;
    }

    @SuppressFBWarnings
    private static AbstractProject synchronizeConfigFiles(AbstractProject implementationProject, AbstractProject templateProject) throws IOException {
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
    private static TemplateImplementationProperty getTemplateImplementationProperty(Item item) {
        return ProjectUtils.getProperty(item, TemplateImplementationProperty.class);
    }

}
