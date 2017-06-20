package com.joelj.jenkins.eztemplates.listener;

import com.google.common.base.Throwables;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

import static com.joelj.jenkins.eztemplates.utils.ProjectUtils.getProperty;

public abstract class EzSaveableListener<J extends JobProperty> extends SaveableListener {

    private static final VersionNumber JENKINS40435 = new VersionNumber("2.32.2");
    static final boolean JOB_SAVE_USES_BULKCHANGE = Jenkins.getVersion().equals(JENKINS40435) || Jenkins.getVersion().isNewerThan(JENKINS40435); // JENKINS-40435
    private final Class<J> propertyType;

    @SuppressWarnings("unchecked")
    public EzSaveableListener(Class<J> propertyType) {
        this.propertyType = propertyType;        // TODO Prefer TypeToken not available in guava-11

    }

    @Override
    public final void onChange(Saveable o, XmlFile file) {
        if (!JOB_SAVE_USES_BULKCHANGE || EzTemplateChange.contains(o)) {
            return;
        }
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
