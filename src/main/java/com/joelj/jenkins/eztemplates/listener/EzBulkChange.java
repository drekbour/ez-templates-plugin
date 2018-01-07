package com.joelj.jenkins.eztemplates.listener;

import com.joelj.jenkins.eztemplates.utils.JobUtils;
import hudson.BulkChange;
import hudson.model.AbstractItem;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Implements {@link #revert} ability that can restore a Jenkins item from the state stored on disk.
 */
public class EzBulkChange extends BulkChange {

    private static final Logger LOG = Logger.getLogger("ez-templates");

    private final AbstractItem item;

    public EzBulkChange(AbstractItem item) {
        super(item);
        this.item = item;
    }

    public void revert() {
        super.abort();
        try {
            JobUtils.updateProjectWithXmlSource(item, item.getConfigFile().getFile().toPath());
        } catch (IOException e) {
            // Cannot rethrow as this will mask whatever caused us to be aborting!
            LOG.severe(String.format("Could not revert %s : %s", item.getFullName(), e.getMessage()));
        }
    }
}
