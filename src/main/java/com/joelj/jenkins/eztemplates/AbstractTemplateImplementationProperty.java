package com.joelj.jenkins.eztemplates;

import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.utils.JobUtils;
import hudson.model.Job;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.OptionalJobProperty;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractTemplateImplementationProperty<J extends Job<?, ?>> extends OptionalJobProperty<J> {

    protected static final Logger LOG = Logger.getLogger("ez-templates");

    private String templateJobName;
    protected List<String> exclusions; // Non-final until we drop support for upgrade from 1.1.x

    @SuppressWarnings("unchecked")
    public AbstractTemplateImplementationProperty(String templateJobName, List<String> exclusions) {
        this.templateJobName = templateJobName;
        this.exclusions = exclusions;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    @Exported
    public String getTemplateJobName() {
        return templateJobName;
    }

    public void setTemplateJobName(String templateJobName) {
        this.templateJobName = templateJobName;
    }

    public Job findTemplate() {
        return JobUtils.findProject(getTemplateJobName());
    }

    public abstract Exclusions exclusionDefinitions();

    @SuppressWarnings("UnusedDeclaration")
    public abstract static class AbstractTemplateImplmentationPropertyDescriptor extends OptionalJobPropertyDescriptor {

        protected final Class<? extends Job> jobType;

        protected AbstractTemplateImplmentationPropertyDescriptor(Class<? extends Job> jobType) {
            this.jobType = jobType;
        }


        /**
         * Jenkins-convention to populate the drop-down box with discovered templates
         *
         * @return populated data to fill the drop-down box with discovered templates
         */
        public ListBoxModel doFillTemplateJobNameItems() {
            ListBoxModel items = new ListBoxModel();
            // Add null as first option - dangerous to force an existing project onto a template in case
            // a noob destroys their config
            items.add(Messages.TemplateImplementationProperty_noTemplateSelected(), null);
            // Add all discovered templates

            for (Job project : JobUtils.findProjectsWithProperty(TemplateProperty.class)) {
                // FIXME filter by correct type
                // fullName includes any folder structure
                items.add(project.getFullDisplayName(), project.getFullName());
            }
            return items;
        }

        @Override
        public String getDisplayName() {
            return Messages.TemplateImplementationProperty_displayName();
        }

        public FormValidation doCheckTemplateJobName(@QueryParameter final String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error(Messages.TemplateImplementationProperty_noTemplateSelected());
            }
            return FormValidation.ok();
        }

        public abstract Collection<Exclusion> getExclusionDefinitions();

        public abstract List<String> getDefaultExclusions();
    }
}
