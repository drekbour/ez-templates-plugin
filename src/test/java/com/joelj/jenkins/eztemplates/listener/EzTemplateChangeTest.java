package com.joelj.jenkins.eztemplates.listener;

import hudson.model.Saveable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EzTemplateChangeTest {

    @Mock
    private Saveable subject, subject2;

    @Test
    public void recordsAnItemInASpecificContext() {
        // When:
        EzTemplateChange change = new EzTemplateChange(subject, Boolean.TRUE);
        try {
            // Then:
            assertThat(EzTemplateChange.contains(subject, Boolean.TRUE), is(true));
            assertThat(EzTemplateChange.contains(subject, Boolean.FALSE), is(false));
        } finally {
            change.commit();
        }
    }

    @Test
    public void recordsMultipleItems() {
        // When:
        EzTemplateChange change = new EzTemplateChange(subject, Boolean.TRUE);
        EzTemplateChange change2 = new EzTemplateChange(subject2, Boolean.TRUE);
        try {
            // Then:
            assertThat(EzTemplateChange.contains(subject, Boolean.TRUE), is(true));
            assertThat(EzTemplateChange.contains(subject2, Boolean.TRUE), is(true));
        } finally {
            change2.commit(); // Must be reverse order
            change.commit();
        }
    }

    @Test
    public void forgetsACommittedItem() {
        // Given:
        EzTemplateChange change = new EzTemplateChange(subject, Boolean.TRUE);
        // When:
        change.commit();
        // Then:
        assertThat(EzTemplateChange.contains(subject, Boolean.TRUE), is(false));
    }

    @Test
    public void forgetsAnAbortedItem() {
        // Given:
        EzTemplateChange change = new EzTemplateChange(subject, Boolean.TRUE);
        // When:
        change.abort();
        // Then:
        assertThat(EzTemplateChange.contains(subject, Boolean.TRUE), is(false));
    }
}