package com.joelj.jenkins.eztemplates.exclusion;

import java.util.logging.Logger;

public abstract class AbstractExclusion implements Exclusion {

    protected static final Logger LOG = Logger.getLogger("ez-templates");

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getId());
    }

}
