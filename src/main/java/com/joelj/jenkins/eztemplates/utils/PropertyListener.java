package com.joelj.jenkins.eztemplates.utils;

import com.google.common.base.Throwables;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.listeners.ItemListener;


/**
 * Listens to changes only on {@link Job}s with a given {@link hudson.model.JobProperty}.
 */
public abstract class PropertyListener<J extends JobProperty> extends ItemListener {

    private final Class<J> propertyType;

    @SuppressWarnings("unchecked")
    public PropertyListener(Class<J> propertyType) {
        this.propertyType = propertyType;        // TODO Prefer TypeToken not available in guava-11
    }

    @Override
    public final void onCreated(Item item) {
        J property = getProperty(item, propertyType);
        if (property != null) {
            try {
                onCreatedProperty((Job) item, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see ItemListener#onCreated(Item)
     */
    public void onCreatedProperty(Job item, J property) throws Exception {
    }

    @Override
    public final void onCopied(Item src, Item item) {
        J property = getProperty(item, propertyType);
        if (property != null) {
            try {
                onCopiedProperty((Job) src, (Job) item, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see ItemListener#onCopied(Item, Item)
     */
    public void onCopiedProperty(Job src, Job item, J property) throws Exception {
    }

    @Override
    public final void onDeleted(Item item) {
        J property = getProperty(item, propertyType);
        if (property != null) {
            try {
                onDeletedProperty((Job) item, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see ItemListener#onDeleted(Item)
     */
    public void onDeletedProperty(Job item, J property) throws Exception {
    }

    @Override
    public final void onRenamed(Item item, String oldName, String newName) {
        J property = getProperty(item, propertyType);
        if (property != null) {
            try {
                onRenamedProperty((Job) item, oldName, newName, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see ItemListener#onRenamed(Item, String, String)
     */
    public void onRenamedProperty(Job item, String oldName, String newName, J property) throws Exception {

    }

    @Override
    public final void onLocationChanged(Item item, String oldFullName, String newFullName) {
        J property = getProperty(item, propertyType);
        if (property != null) {
            try {
                onLocationChangedProperty((Job) item, oldFullName, newFullName, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see ItemListener#onLocationChanged(Item, String, String)
     */
    public void onLocationChangedProperty(Job item, String oldFullName, String newFullName, J property) throws Exception {
    }

    @Override
    public final void onUpdated(Item item) {
        J property = getProperty(item, propertyType);
        if (property != null) {
            try {
                onUpdatedProperty((Job) item, property);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * @see ItemListener#onUpdated(Item)
     */
    public void onUpdatedProperty(Job item, J property) throws Exception {
    }

    /**
     * @param item         A job of some kind
     * @param propertyType The property to look for
     * @return null if this property isn't found
     */
    @SuppressWarnings("unchecked")
    public static <J extends JobProperty> J getProperty(Item item, Class<J> propertyType) {
        // TODO Does this method already exist somewhere in Jenkins?
        // TODO bad home for this method
        if (item instanceof Job) {
            return (J) ((Job) item).getProperty(propertyType); // Why do we need to cast to J?
        }
        return null;
    }

}
