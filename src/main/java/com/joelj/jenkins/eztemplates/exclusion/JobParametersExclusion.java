package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.collect.Lists;
import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;

import java.util.*;
import java.util.logging.Logger;

public class JobParametersExclusion extends JobPropertyExclusion<Job> {

    private static final Logger LOG = Logger.getLogger("ez-templates");
    public static final String ID = "job-params";

    public JobParametersExclusion() {
        super(ID, "Retain local job parameter values", ParametersDefinitionProperty.class.getName());
    }

    @Override
    public void postClone(Job implementationProject) {
        super.cached = merge(
                parameters((ParametersDefinitionProperty) cached),
                parameters(implementationProject)
        );
        super.postClone(implementationProject);
    }

    @Override
    public String getDisabledText() {
        return null; // Always available
    }

    private static List<ParameterDefinition> parameters(Job implementationProject) {
        @SuppressWarnings("unchecked")
        ParametersDefinitionProperty parametersDefinitionProperty = (ParametersDefinitionProperty) implementationProject.getProperty(ParametersDefinitionProperty.class);
        return parameters(parametersDefinitionProperty);
    }

    private static List<ParameterDefinition> parameters(ParametersDefinitionProperty parametersDefinitionProperty) {
        return (parametersDefinitionProperty == null) ? Collections.<ParameterDefinition>emptyList() : parametersDefinitionProperty.getParameterDefinitions();
    }

    static ParametersDefinitionProperty merge(List<ParameterDefinition> oldParameters, List<ParameterDefinition> newParameters) {
        List<ParameterDefinition> result = new LinkedList<ParameterDefinition>();
        List<ParameterDefinition> work = new ArrayList<ParameterDefinition>(oldParameters);
        for (ParameterDefinition newParameter : newParameters) { //'new' parameters are the same as the template.
            boolean found = false;
            Iterator<ParameterDefinition> iterator = work.iterator();
            while (iterator.hasNext()) {
                ParameterDefinition oldParameter = iterator.next();
                if (key(newParameter).equals(key(oldParameter))) {
                    found = true;
                    iterator.remove(); //Make the next iteration a little faster.
                    // JENKINS-37399 Choice parameter options sync down from template
                    if (oldParameter instanceof ChoiceParameterDefinition) {
                        EzReflectionUtils.setFieldValue(ChoiceParameterDefinition.class, oldParameter, "choices", Lists.newArrayList(((ChoiceParameterDefinition) newParameter).getChoices()));
                    }
                    // #17 Description on parameters should always be overridden by template
                    EzReflectionUtils.setFieldValue(ParameterDefinition.class, oldParameter, "description", newParameter.getDescription());
                    result.add(oldParameter);
                }
            }
            if (!found) {
                //Add new parameters not accounted for.
                result.add(newParameter);
                LOG.info(String.format("\t+++ new parameter [%s]", newParameter.getName()));
            }
        }

        // Anything left in work was not matched and will be removed
        for (ParameterDefinition unused : work) {
            LOG.info(String.format("\t--- old parameter [%s]", unused.getName()));
        }

        return result.isEmpty() ? null : new ParametersDefinitionProperty(result);
    }

    private static String key(ParameterDefinition parameterDefinition) {
        // JENKINS-38308 Support changing the type of parameter
        return parameterDefinition.getName() + parameterDefinition.getType();
    }

}
