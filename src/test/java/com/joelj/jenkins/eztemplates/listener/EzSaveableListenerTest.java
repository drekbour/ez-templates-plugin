package com.joelj.jenkins.eztemplates.listener;

import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.ParametersDefinitionProperty;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class EzSaveableListenerTest {

    @ClassRule
    public static JenkinsVersionRule jenkinsVersion = new JenkinsVersionRule();

    @Mock
    private AbstractProject job;
    private XmlFile xmlFile = new XmlFile(new File("filename"));
    private ParametersDefinitionProperty property = new ParametersDefinitionProperty();
    private EzSaveableListener<ParametersDefinitionProperty> listener;

    @Before
    public void setUp() {
        given(job.getProperty(ParametersDefinitionProperty.class)).willReturn(property);
    }

    @Test
    public void notifiesChange() throws Exception {
        // Given:
        jenkinsVersion.set("2.32.2");
        listener = newListener();
        // When:
        listener.onChange(job, xmlFile);
        // Then:
        verify(listener).onChangedProperty(job, xmlFile, property);
    }

    @Test
    public void filtersChangeIfCurrentlyBeingTemplated() {
        // Given:
        jenkinsVersion.set("2.32.2");
        listener = newListener();
        // When:
        EzTemplateChange change = new EzTemplateChange(job, ParametersDefinitionProperty.class);
        try {
            listener.onChange(job, xmlFile);
        } finally {
            change.commit();
        }
        // Then:
        verifyZeroInteractions(listener);
    }

    @Test
    public void filtersChangeOnOlderJenkins() {
        // Given:
        jenkinsVersion.set("2.32.1");
        listener = newListener();
        // When:
        listener.onChange(job, xmlFile);
        // Then:
        verifyZeroInteractions(listener);
    }

    @Test
    public void filtersChangeIfPropertyIsMissing() {
        // Given:
        jenkinsVersion.set("2.32.2");
        listener = newListener();
        given(job.getProperty(ParametersDefinitionProperty.class)).willReturn(null);
        // When:
        listener.onChange(job, xmlFile);
        // Then:
        verifyZeroInteractions(listener);
    }

    @Test
    public void rethrowsExceptions() {
        // Given:
        try {
            jenkinsVersion.set("2.32.2");
            listener = brokenListener();
            // When:
            listener.onChange(job, xmlFile);
            fail();
        } catch (Exception e) {
            // Then:
            assertThat(e, hasToString(containsString("EZ Templates failed")));
        }
    }

    private EzSaveableListener<ParametersDefinitionProperty> newListener() {
        return spy(new EzSaveableListener<ParametersDefinitionProperty>(ParametersDefinitionProperty.class) {
        });
    }

    private EzSaveableListener<ParametersDefinitionProperty> brokenListener() {
        return spy(new EzSaveableListener<ParametersDefinitionProperty>(ParametersDefinitionProperty.class) {
            @Override
            public void onChangedProperty(Job job, XmlFile file, ParametersDefinitionProperty property) throws Exception {
                throw new Exception();
            }
        });
    }

}