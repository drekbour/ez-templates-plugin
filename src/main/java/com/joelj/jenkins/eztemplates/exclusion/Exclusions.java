package com.joelj.jenkins.eztemplates.exclusion;

import java.util.List;
import java.util.Map;

public interface Exclusions {

    Map<String, Exclusion> getAll();

    List<String> getDefaults();

}
