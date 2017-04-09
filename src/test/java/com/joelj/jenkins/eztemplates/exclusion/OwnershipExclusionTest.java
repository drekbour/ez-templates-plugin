package com.joelj.jenkins.eztemplates.exclusion;

import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.joelj.jenkins.eztemplates.exclusion.OwnershipExclusion.PROPERTY_CLASSNAME;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OwnershipExclusionTest {

    @Mock
    private AbstractProject implementationProject;
    @Mock
    private JobProperty property;
    private OwnershipExclusion exclusion = new OwnershipExclusion();

    @Test
    public void missingChildPropertyMeansKeepTemplateProperty() throws Exception {
        // Given:
        given(implementationProject.getProperty(PROPERTY_CLASSNAME)).willReturn(null);
        // When:
        exclusion.preClone(implementationProject);
        exclusion.postClone(implementationProject);
        // Then:
        verify(implementationProject, never()).addProperty(any(JobProperty.class));
    }

    @Test
    public void missingTemplatePropertyWillBeReplacedByChild() throws Exception {
        // Given:
        given(implementationProject.getProperty(PROPERTY_CLASSNAME)).willReturn(property);
        given(implementationProject.removeProperty(any(Class.class))).willReturn(null);
        // When:
        exclusion.preClone(implementationProject);
        exclusion.postClone(implementationProject);
        // Then:
        verify(implementationProject).addProperty(property);
    }

    @Test
    public void childTemplatePropertyWillBeRetained() throws Exception {
        // Given:
        given(implementationProject.getProperty(PROPERTY_CLASSNAME)).willReturn(property);
        given(implementationProject.removeProperty(any(Class.class))).willReturn(mock(JobProperty.class));
        // When:
        exclusion.preClone(implementationProject);
        exclusion.postClone(implementationProject);
        // Then:
        verify(implementationProject).addProperty(property);
    }
}