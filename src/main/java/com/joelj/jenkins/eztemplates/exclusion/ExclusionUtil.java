package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Function;
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

    private static final Function<Exclusion, Exclusion> CLONER = new Function<Exclusion, Exclusion>() {
        @Override
        public Exclusion apply(@Nonnull Exclusion exclusion) {
            try {
                return exclusion.clone();
            } catch (CloneNotSupportedException e) {
                throw Throwables.propagate(e);
            }
        }
    };

    /**
     * Filter exclusions
     *
     * @param allExclusions  available exclusions
     * @param exclusionNames selected exclusions
     * @return Exclusions relevant to the given implementation
     */
    public static Collection<Exclusion> configuredExclusions(Map<String, Exclusion> allExclusions, List<String> exclusionNames) {
        return Lists.newArrayList(Collections2.transform(
                Maps.filterKeys(allExclusions, in(exclusionNames)).values(),
                CLONER
        ));
    }

    @SuppressFBWarnings
    public static String checkPlugin(String id) {
        return Jenkins.getInstance().getPlugin(id) == null ? String.format("Plugin %s is not installed", id) : null;
    }
}
