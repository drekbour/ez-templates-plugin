package com.joelj.jenkins.eztemplates.project;

import com.google.common.collect.ImmutableList;
import com.joelj.jenkins.eztemplates.exclusion.AssignedLabelExclusion;
import com.joelj.jenkins.eztemplates.exclusion.DescriptionExclusion;
import com.joelj.jenkins.eztemplates.exclusion.DisabledExclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.exclusion.EzTemplatesExclusion;
import com.joelj.jenkins.eztemplates.exclusion.JobParametersExclusion;
import com.joelj.jenkins.eztemplates.exclusion.JobPropertyExclusion;
import com.joelj.jenkins.eztemplates.exclusion.MatrixAxisExclusion;
import com.joelj.jenkins.eztemplates.exclusion.OwnershipExclusion;
import com.joelj.jenkins.eztemplates.exclusion.PromotedBuildsExclusion;
import com.joelj.jenkins.eztemplates.exclusion.ScmExclusion;
import com.joelj.jenkins.eztemplates.exclusion.TriggersExclusion;

import java.util.List;
import java.util.Map;

import static com.joelj.jenkins.eztemplates.exclusion.ExclusionUtil.index;

public class ProjectExclusions implements Exclusions {

    private static final Map<String, Exclusion> ALL;
    private static final List<String> DEFAULT;

    public static final String MATRIX_SECURITY_ID = "matrix-auth";
    public static final String OWNERSHIP_ID = "ownership";
    public static final String GITHUB_ID = "github";

    static {
        ALL = index(
                new EzTemplatesExclusion(),
                new JobParametersExclusion(),
                new TriggersExclusion(),
                new DisabledExclusion(),
                new DescriptionExclusion(),
                new PromotedBuildsExclusion(),
                new OwnershipExclusion(),
                new JobPropertyExclusion(MATRIX_SECURITY_ID, "Retain local matrix-build security", "hudson.security.AuthorizationMatrixProperty"),
                new JobPropertyExclusion(GITHUB_ID, "Retain local Github details", "com.coravy.hudson.plugins.github.GithubProjectProperty"),
                new ScmExclusion(),
                new AssignedLabelExclusion(),
                new MatrixAxisExclusion());

        DEFAULT = ImmutableList.of(
                EzTemplatesExclusion.ID,
                JobParametersExclusion.ID,
                DisabledExclusion.ID,
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
