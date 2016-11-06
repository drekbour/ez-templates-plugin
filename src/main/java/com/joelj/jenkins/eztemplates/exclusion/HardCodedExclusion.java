package com.joelj.jenkins.eztemplates.exclusion;

import hudson.model.Job;

public abstract class HardCodedExclusion<J extends Job> implements Exclusion {

    public abstract void preClone(J implementationProject);

    // TODO postClone to catch Exception on behalf of all subclasses
    public abstract void postClone(J implementationProject);

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getId());
    }

    @Override
    public Exclusion clone() throws CloneNotSupportedException {
        return (Exclusion) super.clone();
    }

}
