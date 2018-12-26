package com.joelj.jenkins.eztemplates.listener;

import hudson.XmlFile;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

import static com.joelj.jenkins.eztemplates.utils.JobUtils.getProperty;

public abstract class EzSaveableListener<J extends JobProperty> extends SaveableListener {

    private final Class<J> propertyType;

    @SuppressWarnings("unchecked")
    public EzSaveableListener(Class<J> propertyType) {
        this.propertyType = propertyType;        // TODO Prefer TypeToken not available in guava-11
    }

    @Override
    public final void onChange(Saveable o, XmlFile file) {
        if (EzTemplateChange.contains(o, propertyType)) {
            return;
        }
        J property = getProperty(o, propertyType);
        if (property != null) {
            try {
                onChangedProperty((Job) o, file, property);
            } catch (Exception e) {
                throw new RuntimeException("EZ Templates failed", e);
            }
        }
    }

    /**
     * @see SaveableListener#onChange(Saveable, XmlFile)
     */
    public void onChangedProperty(Job job, XmlFile file, J property) throws Exception {
    }

}
