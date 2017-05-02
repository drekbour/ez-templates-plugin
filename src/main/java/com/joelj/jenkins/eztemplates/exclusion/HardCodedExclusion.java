package com.joelj.jenkins.eztemplates.exclusion;

import hudson.model.AbstractProject;

public abstract class HardCodedExclusion implements Exclusion {

    /**
     * Capture content we want to keep. There will be a unique instance of an Exclusion per invocation so
     * instance fields are appropriate.
     *
     * @param implementationProject The child project before and any processing.
     */
    public abstract void preClone(AbstractProject implementationProject);

    /**
     * Restore content we kept - usually via reflection to prevent infinite save recursion
     *
     * @param implementationProject The child project immediately after it has been XML-cloned from its template.
     *                              Note it will be amended by successive {@link Exclusion}s so the overall state is
     *                              indeterminate although each individual {@link Exclusion} can presume its personal
     *                              view of the Job currently looks exactly like the template.
     * @see com.joelj.jenkins.eztemplates.utils.EzReflectionUtils
     */
    public abstract void postClone(AbstractProject implementationProject);

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getId());
    }

    /**
     * Called as part of a basic Prototype Pattern.
     *
     * @return unique instance of this {@link Exclusion}
     * @throws CloneNotSupportedException N/A
     */
    @Override
    public Exclusion clone() throws CloneNotSupportedException {
        return (Exclusion) super.clone();
    }

}
