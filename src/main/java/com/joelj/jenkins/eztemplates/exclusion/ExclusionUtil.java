package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ExclusionUtil {

    /**
     * {@link Exclusion}s currently enabled in Jenkins.
     *
     * @return Never null
     */
    public static Collection<Exclusion> enabledExclusions(Map<String, Exclusion> all) {
        // TODO Stop ez-templates being so special!
        return Collections2.filter(all.values(), input -> EzTemplatesExclusion.ID.equals(input.getId()) || input.getDisabledText() == null);
    }

    public static <K, V> Map<K, V> index(Function<V,K> keyFunction, V... values) {
        return Maps.uniqueIndex(Arrays.asList(values), keyFunction);
    }

    public static Map<String, Exclusion> index(Exclusion... values) {
        return index(Exclusion::getId, values);
    }

    @SuppressFBWarnings
    public static String checkPlugin(String id) {
        return Jenkins.getInstance().getPlugin(id) == null ? String.format("Plugin %s is not installed", id) : null;
    }
}
