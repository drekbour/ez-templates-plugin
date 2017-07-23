package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.collect.ImmutableList;
import com.joelj.jenkins.eztemplates.project.ProjectExclusions;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ExclusionUtilTest {

//    private ProjectExclusions projectExclusions = new ProjectExclusions();
//
//    @Test
//    public void provides_correct_exclusions() {
//        // Given:
//        // When:
//        Collection<Exclusion> exclusions = ExclusionUtil.enabledExclusions(projectExclusions.getAll(), ImmutableList.of("ownership", "scm", "ez-templates"));
//        // Then:
//        assertThat(exclusions, containsInAnyOrder(
//                hasProperty("id", equalTo("ownership")),
//                hasProperty("id", equalTo("scm")),
//                hasProperty("id", equalTo("ez-templates"))
//        ));
//    }
//    @Test
//    public void provides_unique_exclusions() {
//        // Given:
//        Collection<Exclusion> exclusions = ExclusionUtil.enabledExclusions(projectExclusions.getAll(), ImmutableList.of("ownership"));
//        // When:
//        Collection<Exclusion> exclusions2 = ExclusionUtil.enabledExclusions(projectExclusions.getAll(), ImmutableList.of("ownership"));
//        // Then:
//        assertThat(exclusions, is(not(equalTo(exclusions2)))); // Assumes Exclusions have not implemented an equals() method!
//    }

}