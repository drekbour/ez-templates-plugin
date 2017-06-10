package com.joelj.jenkins.eztemplates.exclusion;

import hudson.model.AbstractProject;

public interface Exclusion {
    String getId();

    String getDescription();

    String getDisabledText();

    /**
     * Capture content we want to keep.
     *
     * @param context               Templating execution, {@link Exclusion}s must check if they are selected in this execution!
     * @param implementationProject The child project before and any processing.
     */
    void preClone(EzContext context, AbstractProject implementationProject);

    /**
     * Restore content we kept - usually via reflection to prevent infinite save recursion.
     *
     * @param context               Templating execution, {@link Exclusion}s must check if they are selected in this execution!
     * @param implementationProject The child project immediately after it has been XML-cloned from its template.
     *                              Note it will be amended by successive {@link Exclusion}s so the overall state is
     *                              indeterminate although each individual {@link Exclusion} can presume its personal
     *                              view of the Job currently looks exactly like the template.
     * @see com.joelj.jenkins.eztemplates.utils.EzReflectionUtils
     */
    void postClone(EzContext context, AbstractProject implementationProject);

}
