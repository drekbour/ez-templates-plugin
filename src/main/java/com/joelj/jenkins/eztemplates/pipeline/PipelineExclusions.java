package com.joelj.jenkins.eztemplates.pipeline;

import com.google.common.collect.ImmutableList;
import com.joelj.jenkins.eztemplates.exclusion.DescriptionExclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.exclusion.EzTemplatesExclusion;
import com.joelj.jenkins.eztemplates.exclusion.JobParametersExclusion;
import com.joelj.jenkins.eztemplates.exclusion.JobPropertyExclusion;
import com.joelj.jenkins.eztemplates.exclusion.MatrixAxisExclusion;
import com.joelj.jenkins.eztemplates.exclusion.OwnershipExclusion;
import com.joelj.jenkins.eztemplates.exclusion.TriggersExclusion;

import java.util.List;
import java.util.Map;

import static com.joelj.jenkins.eztemplates.exclusion.ExclusionUtil.index;
import static com.joelj.jenkins.eztemplates.project.ProjectExclusions.GITHUB_ID;
import static com.joelj.jenkins.eztemplates.project.ProjectExclusions.MATRIX_SECURITY_ID;

public class PipelineExclusions implements Exclusions {

    private static final Map<String, Exclusion> ALL;
    private static final List<String> DEFAULT;

    static {
        ALL = index(
                new EzTemplatesExclusion(),
                new JobParametersExclusion(),
                // UGLY: Add pipeline-specific triggers exclusion using same name and description
                new JobPropertyExclusion(TriggersExclusion.ID, TriggersExclusion.DESCRIPTION, "org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty"),
                new DescriptionExclusion(),
                new OwnershipExclusion(),
                new JobPropertyExclusion(MATRIX_SECURITY_ID, "Retain local matrix-build security", "hudson.security.AuthorizationMatrixProperty"),
                new JobPropertyExclusion(GITHUB_ID, "Retain local Github details", "com.coravy.hudson.plugins.github.GithubProjectProperty"),
                new MatrixAxisExclusion());

        DEFAULT = ImmutableList.of(
                EzTemplatesExclusion.ID,
                JobParametersExclusion.ID,
                DescriptionExclusion.ID
        );
    }

    @Override
    public Map<String, Exclusion> getAll() {
        return ALL;
    }

    @Override
    public List<String> getDefaults() {
        return DEFAULT;
    }
}
