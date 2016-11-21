package com.joelj.jenkins.eztemplates.pipeline;

import com.joelj.jenkins.eztemplates.project.ProjectExclusionsTest;

public class PipelineExclusionsTest extends ProjectExclusionsTest {

    @Override
    public void defineExclusions() {
        exclusions = new PipelineExclusions();
    }
}