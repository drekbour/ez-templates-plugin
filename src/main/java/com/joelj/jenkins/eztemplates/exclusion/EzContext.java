package com.joelj.jenkins.eztemplates.exclusion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unique execution context for one templating invocation.
 */
public class EzContext {
    private final List<String> exclusionsSelected;
    private final Map<String, Object> store = new HashMap<String, Object>();
    private String currentExclusionId;

    public EzContext(List<String> exclusionsSelected) {
        this.exclusionsSelected = exclusionsSelected;
    }

    @Deprecated
    public void setCurrentExclusionId(String currentExclusionId) {
        this.currentExclusionId = currentExclusionId;
    }

    /**
     * Check if user chose the current Exclusion or not. It is expected that most {@link Exclusion}s will exit
     * immediately if this is false.
     *
     * @return true if the UI checkbox associated with the given {@link Exclusion} was true.
     */
    public boolean isSelected() {
        return exclusionsSelected.contains(currentExclusionId);
    }

    /**
     * Helper utility to record something prior to cloning. {@link #record} only supports a single object!
     *
     * @param item Thing to record.
     */
    public final void record(Object item) {
        store.put(currentExclusionId, item);
    }

    /**
     * Get back a previously {@link #record}ed item.
     *
     * @param <T> item type to save casting
     * @return Previously recorded item
     */
    @SuppressWarnings("unchecked")
    public final <T> T remember() {
        return (T) store.get(currentExclusionId);
    }

}
