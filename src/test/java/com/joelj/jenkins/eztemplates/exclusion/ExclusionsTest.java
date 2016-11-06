package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.collect.ImmutableList;
import com.joelj.jenkins.eztemplates.TemplateImplementationProperty;
import com.joelj.jenkins.eztemplates.project.ProjectExclusions;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExclusionsTest {

    private ProjectExclusions projectExclusions = new ProjectExclusions();

    @Test
    public void provides_correct_exclusions() {
        // Given:
        TemplateImplementationProperty property = mock(TemplateImplementationProperty.class);
        when(property.getExclusions()).thenReturn(ImmutableList.of("ownership", "scm", "ez-templates"));
        // When:
        Collection<Exclusion> exclusions = ExclusionUtil.configuredExclusions(projectExclusions.getAll(), property.getExclusions());
        // Then:
        assertThat(exclusions, containsInAnyOrder(
                hasProperty("id", equalTo("ownership")),
                hasProperty("id", equalTo("scm")),
                hasProperty("id", equalTo("ez-templates"))
        ));
    }

    @Test
    public void provides_unique_exclusions() {
        // Given:
        TemplateImplementationProperty property = mock(TemplateImplementationProperty.class);
        when(property.getExclusions()).thenReturn(ImmutableList.of("ownership"));
        Collection<Exclusion> exclusions = ExclusionUtil.configuredExclusions(projectExclusions.getAll(), property.getExclusions());
        // When:
        Collection<Exclusion> exclusions2 = ExclusionUtil.configuredExclusions(projectExclusions.getAll(), property.getExclusions());
        // Then:
        assertThat(exclusions, is(not(equalTo(exclusions2)))); // Assumes Exclusions have not implemented an equals() method!
    }

}