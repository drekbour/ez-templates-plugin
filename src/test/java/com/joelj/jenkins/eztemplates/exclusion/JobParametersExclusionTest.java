package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import com.joelj.jenkins.eztemplates.Equaliser;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(JUnitParamsRunner.class)
public class JobParametersExclusionTest {

    private List<ParameterDefinition> template;
    private List<ParameterDefinition> child;

    private static StringParameterDefinition string(String name, String defaultValue, String desc) {
        return Equaliser.proxy(StringParameterDefinition.class, name, defaultValue, desc);
    }

    private static ChoiceParameterDefinition choice(String name, String choices, String desc) {
        return Equaliser.proxy(ChoiceParameterDefinition.class, name, Joiner.on("\n").join(Chars.asList(choices.toCharArray())), desc);
    }

    private static List<ParameterDefinition> params(ParameterDefinition... defs) {
        return Lists.newArrayList(defs);
    }

    @Test
    public void merge_identical_params_retains_description_from_template() {
        // Given:
        template = params(string("alpha", "alpha-default", "YYYYYYYY"));
        child = params(string("alpha", "XXXXXXXX", "alpha-description"));
        // When:
        ParametersDefinitionProperty merged = JobParametersExclusion.merge(child, template);
        // Then:
        assertThat(merged.getParameterDefinitions(), is(equalTo(params(string("alpha", "XXXXXXXX", "YYYYYYYY")))));
    }

    @Test
    public void can_remove_one_param() {
        // Given:
        template = params(string("alpha", "alpha-default", "alpha-description"));
        child = params(string("alpha", "alpha-default", "alpha-description"),
                string("beta", "beta-default", "beta-description"));
        // When:
        ParametersDefinitionProperty merged = JobParametersExclusion.merge(child, template);
        // Then:
        assertThat(merged.getParameterDefinitions(), is(equalTo(params(string("alpha", "alpha-default", "alpha-description")))));
    }

    @Test
    public void can_remove_all_params() {
        // Given:
        template = params();
        child = params(string("alpha", "alpha-default", "alpha-description"),
                string("beta", "beta-default", "alpha-description"));
        // When:
        ParametersDefinitionProperty merged = JobParametersExclusion.merge(child, template);
        // Then:
        assertThat(merged, is(nullValue()));
    }

    @Test
    public void can_add_one_param() {
        // Given:
        template = params(string("alpha", "alpha-default", "alpha-description"),
                string("beta", "beta-default", "beta-description"));
        child = params(string("alpha", "alpha-default", "alpha-description"));
        // When:
        ParametersDefinitionProperty merged = JobParametersExclusion.merge(child, template);
        // Then:
        assertThat(merged.getParameterDefinitions(), is(equalTo(template)));
    }

    @Test
    public void can_change_type_of_param() {
        // Given:
        template = params(string("alpha", "alpha-default", "YYYYYYYY"));
        child = params(choice("alpha", "a,b,c", "alpha-description"));
        // When:
        ParametersDefinitionProperty merged = JobParametersExclusion.merge(child, template);
        // Then:
        assertThat(merged.getParameterDefinitions(), is(equalTo(params(string("alpha", "alpha-default", "YYYYYYYY")))));
    }

    @Test
    @TestCaseName("[{index}] {method}_{3}: ({0},{1}) -> {2}")
    // JENKINS-38755
    @Parameters({
            "ABC, ABC, ABC, no_change",
            "ABC, CAB, CAB, reordering_is_retained",
            "ABCD, ABC, ABCD, template_additions_are_added",
            "ABCDE, BCE, ABCDE, template_additions_are_added_with_order_if_child_matches",
            "ABCDE, DBC, ADBCE, template_additions_are_added_without_order_if_child_reordered",
            "ABC, ABCD, ABCD, child_additions_are_retained",
            "ABC, DABC, DABC, child_additions_are_retained_with_order",
            "ABC, AB, ABC, child_removals_are_ignored_WRONGLY",
            "AB, ABC, ABC, template_removals_are_ignored_WRONGLY"
    })
    public void choice_param(String templateC, String childC, String resultC, String comment) {
        // Given:
        template = params(choice("alpha", templateC, "alpha-description"));
        child = params(choice("alpha", childC, "alpha-description"));
        // When:
        ParametersDefinitionProperty merged = JobParametersExclusion.merge(child, template);
        // Then:
        List<ParameterDefinition> result = params(choice("alpha", resultC, "alpha-description"));
        assertThat(merged.getParameterDefinitions(), is(equalTo(result)));
    }

}