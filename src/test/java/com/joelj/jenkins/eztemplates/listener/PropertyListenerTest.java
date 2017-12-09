package com.joelj.jenkins.eztemplates.listener;

import hudson.model.AbstractProject;
import hudson.model.ParametersDefinitionProperty;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PropertyListenerTest {

    @ClassRule
    public static JenkinsVersionRule jenkinsVersion = new JenkinsVersionRule();

    @Mock
    private AbstractProject job;
    private ParametersDefinitionProperty property = new ParametersDefinitionProperty();
    private PropertyListener<ParametersDefinitionProperty> listener;

    @Before
    public void setUp() {
        given(job.getProperty(ParametersDefinitionProperty.class)).willReturn(property);
    }

    @Test
    public void notifiesUpdated() throws Exception {
        // Given:
        jenkinsVersion.set("2.32.1");
        listener = newListener();
        // When:
        listener.onUpdated(job);
        // Then:
        verify(listener).onUpdatedProperty(job, property);
    }

    @Test
    public void filtersUpdatedIfCurrentlyBeingTemplated() throws Exception {
        // Given:
        jenkinsVersion.set("2.32.1");
        listener = newListener();
        // When:
        EzTemplateChange change = new EzTemplateChange(job, ParametersDefinitionProperty.class);
        try {
            listener.onUpdated(job);
        } finally {
            change.commit();
        }
        // Then:
        verifyZeroInteractions(listener);
    }

    @Test
    public void filtersUpdatedOnNewerJenkins() {
        // Given:
        jenkinsVersion.set("2.32.2");
        listener = newListener();
        // When:
        listener.onUpdated(job);
        // Then:
        verifyZeroInteractions(listener);
    }

    @Test
    public void filtersUpdatedIfPropertyIsMissing() {
        // Given:
        jenkinsVersion.set("2.32.1");
        listener = newListener();
        given(job.getProperty(ParametersDefinitionProperty.class)).willReturn(null);
        // When:
        listener.onUpdated(job);
        // Then:
        verifyZeroInteractions(listener);
    }

    private PropertyListener<ParametersDefinitionProperty> newListener() {
        return spy(new PropertyListener<ParametersDefinitionProperty>(ParametersDefinitionProperty.class) {
        });
    }
}