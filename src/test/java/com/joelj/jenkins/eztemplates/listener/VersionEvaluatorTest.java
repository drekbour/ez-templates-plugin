package com.joelj.jenkins.eztemplates.listener;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnitParamsRunner.class)
public class VersionEvaluatorTest {

    @ClassRule
    public static JenkinsVersionRule jenkinsVersion = new JenkinsVersionRule();

    @Test
    @Parameters({
            "2.31, false",
            "2.32.1, false",
            "2.32.2, true",
            "2.32.3, true",
            "2.33, false",
            "2.36, false",
            "2.37, true",
            "2.38, true"
    })
    @TestCaseName("Does Jenkins v{0} use BulkChange ({1})")
    public void doesJenkinsUseBulkSave(String version, boolean expected) throws Exception {
        // Given:
        jenkinsVersion.set(version);
        // When:
        boolean usesBulkSave = VersionEvaluator.jobSaveUsesBulkchange();
        // Then:
        assertThat(usesBulkSave, is(expected));
    }

}