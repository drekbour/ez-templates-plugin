package com.joelj.jenkins.eztemplates;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.joelj.jenkins.eztemplates.utils.JobUtils;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Collection;

public class TemplateProperty extends JobProperty<Job<?, ?>> {

    public Collection<Job> getImplementations(final String templateFullName) {
        Class<? extends Job> jobClass = owner.getClass();
        Collection<Job> projects = JobUtils.findProjectsWithProperty(AbstractTemplateImplementationProperty.class);
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
    public static class DescriptorImpl extends JobPropertyDescriptor {
        @Override
        public JobProperty<?> newInstance(StaplerRequest request, JSONObject formData) throws FormException {
            // TODO Replace with OptionalJobProperty 1.637
            return formData.size() > 0 ? new TemplateProperty() : null;
        }

        @Override
        public String getDisplayName() {
            return Messages.TemplateImplementationProperty_displayName();
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return JobUtils.isPluginApplicableTo(jobType);
        }
    }
}
