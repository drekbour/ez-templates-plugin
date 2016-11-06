package com.joelj.jenkins.eztemplates.project;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.joelj.jenkins.eztemplates.exclusion.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class ProjectExclusions implements Exclusions {

    private static final Map<String, Exclusion> ALL;
    private static final List<String> DEFAULT;

    public static final String MATRIX_SECURITY_ID = "matrix-auth";
    public static final String OWNERSHIP_ID = "ownership";
    public static final String GITHUB_ID = "github";

    static {
        ImmutableList.Builder<Exclusion> builder = ImmutableList.builder();
        builder.add(new EzTemplatesExclusion());
        builder.add(new JobParametersExclusion());
        builder.add(new TriggersExclusion());
        builder.add(new DisabledExclusion());
        builder.add(new DescriptionExclusion());
        builder.add(new JobPropertyExclusion(OWNERSHIP_ID, "Retain local ownership property", "com.synopsys.arc.jenkins.plugins.ownership.jobs.JobOwnerJobProperty"));
        builder.add(new JobPropertyExclusion(MATRIX_SECURITY_ID, "Retain local matrix-build security", "hudson.security.AuthorizationMatrixProperty"));
        builder.add(new JobPropertyExclusion(GITHUB_ID, "Retain local Github details", "com.coravy.hudson.plugins.github.GithubProjectProperty"));
        builder.add(new ScmExclusion());
        builder.add(new AssignedLabelExclusion());
        builder.add(new MatrixAxisExclusion());
        List<Exclusion> l = builder.build();
        ALL = Maps.uniqueIndex(l, new Function<Exclusion, String>() {
            @Override
            public String apply(@Nonnull Exclusion exclusion) {
                return exclusion.getId();
            }
        });
    }

    static {
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
