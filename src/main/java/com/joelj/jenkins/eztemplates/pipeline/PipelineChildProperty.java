package com.joelj.jenkins.eztemplates.pipeline;

import com.joelj.jenkins.eztemplates.ChildProperty;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;
import java.util.List;

/**
 * Owning pipeline {@link WorkflowJob} is templated.
 */
@XStreamAlias("ezTemplatePipelineChild")
public class PipelineChildProperty extends ChildProperty<WorkflowJob> {

    private static final Exclusions EXCLUSION_DEFINITIONS = new PipelineExclusions();

    public static PipelineChildProperty newImplementation(String templateJobName) {
        return new PipelineChildProperty(
                templateJobName,
                EXCLUSION_DEFINITIONS.getDefaults());
    }

    @DataBoundConstructor
    public PipelineChildProperty(String templateJobName, List<String> exclusions) {
        super(templateJobName, exclusions);
    }

    @Override
    public Exclusions exclusionDefinitions() {
        return EXCLUSION_DEFINITIONS;
    }

    @SuppressWarnings("UnusedDeclaration")
    @Extension
    public static class PipelineChildPropertyDescriptor extends ChildPropertyDescriptor {
        public PipelineChildPropertyDescriptor() {
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
