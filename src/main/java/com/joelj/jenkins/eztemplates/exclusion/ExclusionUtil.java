package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.in;

public class ExclusionUtil {

    /**
     * {@link Exclusion}s currently enabled in Jenkins.
     *
     * @return Never null
     */
    public static Collection<Exclusion> enabledExclusions(Map<String, Exclusion> all) {
        // TODO Stop ez-templates being so special!
        return Collections2.filter(all.values(), new Predicate<Exclusion>() {
            @Override
            public boolean apply(@Nonnull Exclusion input) {
                return EzTemplatesExclusion.ID.equals(input.getId()) || input.getDisabledText() == null;
            }
        });
    }

    @SuppressFBWarnings
    public static String checkPlugin(String id) {
        return Jenkins.getInstance().getPlugin(id) == null ? String.format("Plugin %s is not installed", id) : null;
    }
}
