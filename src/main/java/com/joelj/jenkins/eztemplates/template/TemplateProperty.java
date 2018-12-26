package com.joelj.jenkins.eztemplates.template;

import com.google.common.collect.Collections2;
import com.joelj.jenkins.eztemplates.ChildProperty;
import com.joelj.jenkins.eztemplates.Messages;
import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import com.joelj.jenkins.eztemplates.utils.JobUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Extension;
import hudson.model.Job;
import jenkins.model.OptionalJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

import static com.joelj.jenkins.eztemplates.utils.TemplateUtils.getChildProperty;

/**
 * Owning {@link Job} can be used as a template.
 */
@XStreamAlias("ezTemplate")
public class TemplateProperty extends OptionalJobProperty<Job<?, ?>> {

    public Collection<Job> getImplementations(final String templateFullName) {
        Collection<Job> projects = JobUtils.findJobsWithProperty(ChildProperty.class);
        return Collections2.filter(projects, job -> templateFullName.equals(getChildProperty(job).getTemplateJobName()));
    }

    @DataBoundConstructor
    public TemplateProperty() {
    }

    public Collection<Job> getImplementations() {
        return getImplementations(owner.getFullName());
    }

    @Extension
    public static class DescriptorImpl extends OptionalJobPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.TemplateProperty_displayName();
        }


        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            // Would be nice if this had a relationship with isApplicable of its counterparts.
            return EzReflectionUtils.isAssignable("hudson.model.AbstractProject", jobType)
                    || EzReflectionUtils.isAssignable("org.jenkinsci.plugins.workflow.job.WorkflowJob", jobType);
        }
    }
}
