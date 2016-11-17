package com.joelj.jenkins.eztemplates.pipeline;

import com.joelj.jenkins.eztemplates.AbstractTemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;
import java.util.List;

public class PipelineTemplateImplementationProperty extends AbstractTemplateImplementationProperty<WorkflowJob> {

    private static final Exclusions EXCLUSION_DEFINITIONS = new PipelineExclusions();

    public static PipelineTemplateImplementationProperty newImplementation(String templateJobName) {
        return new PipelineTemplateImplementationProperty(
                templateJobName,
                EXCLUSION_DEFINITIONS.getDefaults());
    }

    @DataBoundConstructor
    public PipelineTemplateImplementationProperty(String templateJobName, List<String> exclusions) {
        super(templateJobName, exclusions);
    }

    @Override
    public Exclusions exclusionDefinitions() {
        return EXCLUSION_DEFINITIONS;
    }

    @SuppressWarnings("UnusedDeclaration")
    @Extension
    public static class PipelineTemplateImplementationPropertyDescriptor extends AbstractTemplateImplementationPropertyDescriptor {
        public PipelineTemplateImplementationPropertyDescriptor() {
            super(WorkflowJob.class);
        }

        public Collection<Exclusion> getExclusionDefinitions() {
            return EXCLUSION_DEFINITIONS.getAll().values();
        }

        public List<String> getDefaultExclusions() {
            return EXCLUSION_DEFINITIONS.getDefaults();
        }
    }
}
