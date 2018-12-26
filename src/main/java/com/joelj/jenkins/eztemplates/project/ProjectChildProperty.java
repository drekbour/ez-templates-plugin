package com.joelj.jenkins.eztemplates.project;

import com.google.common.collect.ImmutableList;
import com.joelj.jenkins.eztemplates.ChildProperty;
import com.joelj.jenkins.eztemplates.exclusion.AssignedLabelExclusion;
import com.joelj.jenkins.eztemplates.exclusion.DescriptionExclusion;
import com.joelj.jenkins.eztemplates.exclusion.DisabledExclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusion;
import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.exclusion.EzTemplatesExclusion;
import com.joelj.jenkins.eztemplates.exclusion.JobParametersExclusion;
import com.joelj.jenkins.eztemplates.exclusion.MatrixAxisExclusion;
import com.joelj.jenkins.eztemplates.exclusion.ScmExclusion;
import com.joelj.jenkins.eztemplates.exclusion.TriggersExclusion;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Extension;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;
import java.util.List;

/**
 * Owning classic {@link AbstractProject} is templated.
 */
@XStreamAlias("ezTemplateProjectChild")
public class ProjectChildProperty extends ChildProperty<AbstractProject<?, ?>> {

    private static final Exclusions EXCLUSION_DEFINITIONS = new ProjectExclusions();

    private final boolean syncMatrixAxis;
    private final boolean syncDescription;
    private final boolean syncBuildTriggers;
    private final boolean syncDisabled;
    private final boolean syncSecurity;
    private final boolean syncScm;
    private final boolean syncOwnership;
    private final boolean syncAssignedLabel;

    public static ProjectChildProperty newImplementation(String templateJobName) {
        return new ProjectChildProperty(
                templateJobName,
                EXCLUSION_DEFINITIONS.getDefaults(),
                true, true, false, false, false, false, false, false);
    }

    @Deprecated
    @DataBoundConstructor
    public ProjectChildProperty(String templateJobName, List<String> exclusions, boolean syncDescription, boolean syncDisabled, boolean syncMatrixAxis, boolean syncBuildTriggers, boolean syncSecurity, boolean syncScm, boolean syncOwnership, boolean syncAssignedLabel) {
        super(templateJobName, exclusions);
        // Support for rollback to <1.2.0
        this.syncDescription = !exclusions.contains(DescriptionExclusion.ID);
        this.syncDisabled = !exclusions.contains(DisabledExclusion.ID);
        this.syncMatrixAxis = !exclusions.contains(MatrixAxisExclusion.ID);
        this.syncBuildTriggers = !exclusions.contains(TriggersExclusion.ID);
        this.syncSecurity = !exclusions.contains(ProjectExclusions.MATRIX_SECURITY_ID);
        this.syncScm = !exclusions.contains(ScmExclusion.ID);
        this.syncOwnership = !exclusions.contains(ProjectExclusions.OWNERSHIP_ID);
        this.syncAssignedLabel = !exclusions.contains(AssignedLabelExclusion.ID);
    }

    public List<String> getExclusions() {
        if (exclusions == null) {
            LOG.info("Upgrading from earlier EZ Templates installation");
            ImmutableList.Builder<String> list = ImmutableList.builder();
            list.add(EzTemplatesExclusion.ID);
            list.add(JobParametersExclusion.ID);
            if (!syncDescription) list.add(DescriptionExclusion.ID);
            if (!syncDisabled) list.add(DisabledExclusion.ID);
            if (!syncMatrixAxis) list.add(MatrixAxisExclusion.ID);
            if (!syncBuildTriggers) list.add(TriggersExclusion.ID);
            if (!syncSecurity) list.add(ProjectExclusions.MATRIX_SECURITY_ID);
            if (!syncScm) list.add(ScmExclusion.ID);
            if (!syncOwnership) list.add(ProjectExclusions.OWNERSHIP_ID);
            if (!syncAssignedLabel) list.add(AssignedLabelExclusion.ID);
            exclusions = list.build();
        }
        return exclusions;
    }

    @Deprecated
    public boolean isSyncMatrixAxis() {
        return syncMatrixAxis;
    }

    @Deprecated
    public boolean isSyncDescription() {
        return syncDescription;
    }

    @Deprecated
    public boolean isSyncBuildTriggers() {
        return syncBuildTriggers;
    }

    @Deprecated
    public boolean isSyncDisabled() {
        return syncDisabled;
    }

    @Deprecated
    public boolean isSyncSecurity() {
        return syncSecurity;
    }

    @Deprecated
    public boolean isSyncScm() {
        return syncScm;
    }

    @Deprecated
    public boolean isSyncOwnership() {
        return syncOwnership;
    }

    @Deprecated
    public boolean isSyncAssignedLabel() {
        return syncAssignedLabel;
    }

    @Override
    public Exclusions exclusionDefinitions() {
        return EXCLUSION_DEFINITIONS;
    }

    @SuppressWarnings("UnusedDeclaration")
    @Extension
    public static class ProjectChildPropertyDescriptor extends ChildPropertyDescriptor {

        public ProjectChildPropertyDescriptor() {
            super(AbstractProject.class);
        }

        public Collection<Exclusion> getExclusionDefinitions() {
            return EXCLUSION_DEFINITIONS.getAll().values();
        }

        public List<String> getDefaultExclusions() {
            return EXCLUSION_DEFINITIONS.getDefaults();
        }
    }
}

