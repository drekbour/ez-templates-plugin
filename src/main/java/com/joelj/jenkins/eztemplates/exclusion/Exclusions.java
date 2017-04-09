package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Exclusions {

    public static final Map<String, Exclusion> ALL;
    public static final List<String> DEFAULT;

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
        builder.add(new OwnershipExclusion());
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

    /**
     * Exclusions relevant to the given implementation
     */
    public static Collection<Exclusion> configuredExclusions(TemplateImplementationProperty property) {
        return Lists.newArrayList(Collections2.transform(
                Maps.filterKeys(ALL, Predicates.in(property.getExclusions())).values(),
                CLONER
        ));
    }

    @SuppressFBWarnings
    public static String checkPlugin(String id) {
        return Jenkins.getInstance().getPlugin(id) == null ? String.format("Plugin %s is not installed", id) : null;
    }

    private static final Function<Exclusion, Exclusion> CLONER = new Function<Exclusion, Exclusion>() {
        @Override
        public Exclusion apply(@Nonnull Exclusion exclusion) {
            try {
                return exclusion.clone();
            } catch (CloneNotSupportedException e) {
                throw Throwables.propagate(e);
            }
        }
    };

}
