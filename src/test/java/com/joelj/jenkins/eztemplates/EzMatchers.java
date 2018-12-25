package com.joelj.jenkins.eztemplates;

import com.joelj.jenkins.eztemplates.project.ProjectChildProperty;
import com.joelj.jenkins.eztemplates.template.TemplateProperty;
import hudson.model.Job;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Collection;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

abstract class EzMatchers {

    static class HasTemplate extends FeatureMatcher<Job<?, ?>, String> {

        HasTemplate(String templateName) {
            super(equalTo(templateName), "an impl with template", "template name");
        }

        @Override
        protected String featureValueOf(Job<?, ?> actual) {
            ProjectChildProperty prop = actual.getProperty(ProjectChildProperty.class);
            return prop == null ? null : prop.findTemplate().getFullName();
        }
    }

    public static HasTemplate hasTemplate(String templateName) {
        return new HasTemplate(templateName);
    }

    public static HasTemplate hasNoTemplate() {
        return new HasTemplate(null);
    }

    static class HasImplementations extends FeatureMatcher<Job<?, ?>, Collection<Job>> {

        HasImplementations(Matcher impls) {
            super(impls, "a template with impls", "implementations");
        }

        @Override
        protected Collection<Job> featureValueOf(Job<?, ?> actual) {
            return actual.getProperty(TemplateProperty.class).getImplementations();
        }
    }

    public static HasImplementations hasImplementations(Job... impls) {
        return new HasImplementations(contains(impls));
    }

    public static HasImplementations hasNoImplementations() {
        return new HasImplementations(Matchers.<Job>empty());
    }

}
