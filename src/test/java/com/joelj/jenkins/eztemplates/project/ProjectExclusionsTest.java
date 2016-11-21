package com.joelj.jenkins.eztemplates.project;

import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.exclusion.EzTemplatesExclusion;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class ProjectExclusionsTest {

    protected Exclusions exclusions;

    @Before
    public void defineExclusions() {
        exclusions = new ProjectExclusions();
    }

    @Test
    public void defaults_are_a_subset_of_all() {
        assertThat(exclusions.getAll().keySet().containsAll(exclusions.getDefaults()), is(true));
    }

    @Test
    public void defaults_contain_ez_templates() {
        assertThat(exclusions.getDefaults(), hasItem(EzTemplatesExclusion.ID));
    }

}