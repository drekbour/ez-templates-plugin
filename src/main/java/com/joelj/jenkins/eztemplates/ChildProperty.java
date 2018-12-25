package com.joelj.jenkins.eztemplates;

import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.template.TemplateProperty;
import com.joelj.jenkins.eztemplates.utils.JobUtils;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.OptionalJobProperty;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Owning {@link Job} is templated.
 *
 * @param <J> Type of {@link Job} this property applies to. It is critical that {@link J} is unique across all
 *           implementations, see {@link JobPropertyDescriptor#isApplicable(java.lang.Class)}
 */
public abstract class ChildProperty<J extends Job<?, ?>> extends OptionalJobProperty<J> {

    protected static final Logger LOG = Logger.getLogger("ez-templates");

    private String templateJobName;
    protected List<String> exclusions; // Non-final until we drop support for upgrade from 1.1.x

    @SuppressWarnings("unchecked")
    public ChildProperty(String templateJobName, List<String> exclusions) {
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

    public J findTemplate() {
        return JobUtils.findJob(getTemplateJobName());
    }

    public abstract Exclusions exclusionDefinitions();

    /**
     * Similarly abstract {@link hudson.model.Descriptor}
     */
    @SuppressWarnings("UnusedDeclaration")
    public abstract static class ChildPropertyDescriptor extends OptionalJobPropertyDescriptor {

        protected final Class<? extends Job> jobType;

        protected ChildPropertyDescriptor(Class<? extends Job> jobType) {
            this.jobType = jobType;
        }

        @Override
        public String getDisplayName() {
            return Messages.ChildProperty_displayName();
        }

        /**
         * Jenkins-convention to populate the drop-down box with discovered templates
         */
        public ListBoxModel doFillTemplateJobNameItems() {
            ListBoxModel items = new ListBoxModel();
            // Add null as first option - dangerous to force an existing project onto a template in case
            // a noob destroys their config
            items.add(Messages.ChildProperty_noTemplateSelected(), null);
            // Add all discovered templates

            for (Job job : JobUtils.findJobsWithProperty(TemplateProperty.class)) {
                if (isApplicable(job.getClass())) {
                    // fullName includes any folder structure
                    items.add(job.getFullDisplayName(), job.getFullName());
                }
            }
            return items;
        }

        /**
         * Jenkins-convention to validate the drop-down box with discovered templates
         */
        public FormValidation doCheckTemplateJobName(@QueryParameter final String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error(Messages.ChildProperty_noTemplateSelected());
            }
            return FormValidation.ok();
        }

        /**
         * All {@link Exclusion}s possibly supported by this {@link ChildProperty}, even if currently disabled.
         * @return {@link Exclusion}s to render for UI check boxes.
         */
        public abstract Collection<Exclusion> getExclusionDefinitions();

        /**
         * The set of {@link Exclusion}s ticked by default in the UI.
         * @return A collection of {@link Exclusion#getId()}
         */
        public abstract List<String> getDefaultExclusions();
    }
}
