package com.joelj.jenkins.eztemplates;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.joelj.jenkins.eztemplates.utils.JobUtils;
import hudson.Extension;
import hudson.model.Job;
import jenkins.model.OptionalJobProperty;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collection;

public class TemplateProperty extends OptionalJobProperty<Job<?, ?>> {

    public Collection<Job> getImplementations(final String templateFullName) {
        Class<? extends Job> jobClass = owner.getClass();
        Collection<Job> projects = JobUtils.findJobsWithProperty(AbstractTemplateImplementationProperty.class);
        return Collections2.filter(projects, new Predicate<Job>() {
            public boolean apply(@Nonnull Job job) {
                TemplateImplementationProperty prop = (TemplateImplementationProperty) job.getProperty(TemplateImplementationProperty.class);
                return templateFullName.equals(prop.getTemplateJobName());
            }
        });
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
            return JobUtils.canBeTemplated(jobType);
        }
    }
}
