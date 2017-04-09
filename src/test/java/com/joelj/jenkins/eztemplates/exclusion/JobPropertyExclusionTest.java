package com.joelj.jenkins.eztemplates.exclusion;

import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobPropertyExclusionTest {

    @Mock
    private AbstractProject implementationProject;
    @Mock
    private JobProperty property;
    private JobPropertyExclusion exclusion = new JobPropertyExclusion("id", "desc", "classname");

    @Test
    public void missingChildPropertyMeansKeepTemplateProperty() throws Exception {
        // Given:
        given(implementationProject.getProperty("classname")).willReturn(null);
        // When:
        exclusion.preClone(implementationProject);
        exclusion.postClone(implementationProject);
        // Then:
        verify(implementationProject, never()).addProperty(any(JobProperty.class));
    }

    @Test
    public void missingTemplatePropertyWillRemoveFromChild() throws Exception {
        // Given:
        given(implementationProject.getProperty("classname")).willReturn(property);
        given(implementationProject.removeProperty(any(Class.class))).willReturn(null);
        // When:
        exclusion.preClone(implementationProject);
        exclusion.postClone(implementationProject);
        // Then:
        verify(implementationProject, never()).addProperty(any(JobProperty.class));
    }

    @Test
    public void childTemplatePropertyWillBeRetained() throws Exception {
        // Given:
        given(implementationProject.getProperty("classname")).willReturn(property);
        given(implementationProject.removeProperty(any(Class.class))).willReturn(mock(JobProperty.class));
        // When:
        exclusion.preClone(implementationProject);
        exclusion.postClone(implementationProject);
        // Then:
        verify(implementationProject).addProperty(property);
    }
}