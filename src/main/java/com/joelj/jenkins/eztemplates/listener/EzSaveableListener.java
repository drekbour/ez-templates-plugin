package com.joelj.jenkins.eztemplates.listener;

import com.google.common.base.Throwables;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

import static com.joelj.jenkins.eztemplates.utils.ProjectUtils.getProperty;

public abstract class EzSaveableListener<J extends JobProperty> extends SaveableListener {

    private final Class<J> propertyType;

    @SuppressWarnings("unchecked")
    public EzSaveableListener(Class<J> propertyType) {
        this.propertyType = propertyType;        // TODO Prefer TypeToken not available in guava-11
    }

    @Override
    public void onChange(Saveable o, XmlFile file) {
        J property = getProperty(o, propertyType);
        if (property != null) {
            try {
                onChangedProperty((AbstractProject) o, file, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see SaveableListener#onChange(Saveable, XmlFile)
     */
    public void onChangedProperty(AbstractProject job, XmlFile file, J property) throws Exception {
    }

}
