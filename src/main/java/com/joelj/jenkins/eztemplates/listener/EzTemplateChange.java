package com.joelj.jenkins.eztemplates.listener;

import hudson.BulkChange;
import hudson.model.Saveable;

import java.io.IOException;

/**
 * Track implementations being updated so we don't re-trigger templating.
 * Cut'n'paste from BulkChange 1.642.3 for simplicity.
 */
public class EzTemplateChange {
    private final Saveable saveable;
    public final Exception allocator;
    private final EzTemplateChange parent;

    private boolean completed;

    public EzTemplateChange(Saveable saveable) {
        this.parent = current();
        this.saveable = saveable;
        // rememeber who allocated this object in case
        // someone forgot to call save() at the end.
        allocator = new Exception();

        // in effect at construction
        INSCOPE.set(this);
    }

    /**
     * Saves the accumulated changes.
     */
    public void commit() throws IOException {
        if (completed) return;
        completed = true;

        // move this object out of the scope first before save, or otherwise the save() method will do nothing.
        pop();
        //saveable.save();
    }

    /**
     * Exits the scope of {@link BulkChange} without saving the changes.
     * <p>
     * <p>
     * This can be used when a bulk change fails in the middle.
     * Note that unlike a real transaction, this will not roll back the state of the object.
     * <p>
     * <p>
     * The abort method can be called after the commit method, in which case this method does nothing.
     * This is so that {@link BulkChange} can be used naturally in the try/finally block.
     */
    public void abort() {
        if (completed) return;
        completed = true;
        pop();
    }

    private void pop() {
        if (current() != this)
            throw new AssertionError("Trying to save BulkChange that's not in scope");
        INSCOPE.set(parent);
    }

    /**
     * {@link BulkChange}s that are effective currently.
     */
    private static final ThreadLocal<EzTemplateChange> INSCOPE = new ThreadLocal<EzTemplateChange>();

    /**
     * Gets the {@link BulkChange} instance currently in scope for the current thread.
     */
    public static EzTemplateChange current() {
        return INSCOPE.get();
    }

    /**
     * Checks if the given {@link Saveable} is currently in the bulk change.
     * <p>
     * <p>
     * The expected usage is from the {@link Saveable#save()} implementation to check
     * if the actual persistence should happen now or not.
     */
    public static boolean contains(Saveable s) {
        for (EzTemplateChange b = current(); b != null; b = b.parent)
            if (b.saveable == s || b.saveable == ALL)
                return true;
        return false;
    }

    /**
     * Magic {@link Saveable} instance that can make {@link BulkChange} veto
     * all the save operations by making the {@link #contains(Saveable)} method return
     * true for everything.
     */
    public static final Saveable ALL = new Saveable() {
        public void save() {
        }
    };

}
