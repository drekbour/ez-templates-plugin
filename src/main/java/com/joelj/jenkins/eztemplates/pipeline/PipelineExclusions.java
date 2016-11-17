package com.joelj.jenkins.eztemplates.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.joelj.jenkins.eztemplates.exclusion.*;
import com.joelj.jenkins.eztemplates.project.ProjectExclusions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;

public class PipelineExclusions implements Exclusions {

    private static final Map<String, Exclusion> ALL;
    private static final List<String> DEFAULT;

    static {
        // Construct as delta from (default) ProjectExclusions
        ImmutableMap.Builder<String, Exclusion> builder = ImmutableMap.builder();
        builder.putAll(
                Maps.filterKeys(ProjectExclusions.ALL, not(in(Arrays.asList(
                        // the label where the job is going to be run on is defined on the pipeline itself, we do nothing.
                        AssignedLabelExclusion.ID,
                        // SCM to poll is defined on the pipeline itself (in fact, you can have several SCMs to poll), so we do nothing.
                        // See http://stackoverflow.com/a/31148178
                        ScmExclusion.ID,
                        // JENKINS-27299: pipeline can't currently be disabled, return false
                        DisabledExclusion.ID,
                        // Pipeline has a different property for holding triggers.
                        TriggersExclusion.ID
                )))));
        // Add pipeline-specific triggers exclusion using same name and description - this is an ugly line of code but is done once ever.
        builder.put(TriggersExclusion.ID, new JobPropertyExclusion(TriggersExclusion.ID, ProjectExclusions.ALL.get(TriggersExclusion.ID).getDescription(), "org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty"));
        ALL = builder.build();
    }

    static {
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
