package com.joelj.jenkins.eztemplates.exclusion;

import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;

import java.util.*;

public class JobParametersExclusion extends JobPropertyExclusion<Job> {

    public static final String ID = "job-params";
    private static final String DESCRIPTION = "Retain local job parameter values";

    public JobParametersExclusion() {
        super(ID, DESCRIPTION, ParametersDefinitionProperty.class.getName());
    }

    @Override
    public void postClone(EzContext context, Job implementationProject) {
        if (!context.isSelected()) return;
        ParametersDefinitionProperty cached = context.remember();
        context.record(merge(
                parameters(cached),
                parameters(implementationProject)
        ));
        super.postClone(context, implementationProject);
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
        for (ParameterDefinition newParameter : newParameters) { // 'new' parameters are the same as the template.
            boolean found = false;
            Iterator<ParameterDefinition> iterator = work.iterator();
            while (iterator.hasNext()) {
                ParameterDefinition oldParameter = iterator.next();
                if (key(newParameter).equals(key(oldParameter))) {
                    found = true;
                    iterator.remove(); // Make the next iteration a little faster.
                    updateChoiceParametersAsIntelligentlyAsWeCan(newParameter, oldParameter);
                    // #17 Description on parameters should always be overridden by template
                    EzReflectionUtils.setFieldValue(ParameterDefinition.class, oldParameter, "description", newParameter.getDescription()); // TODO can't we just CoW?
                    result.add(oldParameter);
                }
            }
            if (!found) {
                // Add new parameters not accounted for.
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

    private static void updateChoiceParametersAsIntelligentlyAsWeCan(ParameterDefinition template, ParameterDefinition child) {
        // JENKINS-38755 apply choice merging rules
        if (template instanceof ChoiceParameterDefinition) {
            LinkedList<String> templateChoices = new LinkedList<String>(((ChoiceParameterDefinition) template).getChoices());
            LinkedList<String> childChoices = new LinkedList<String>(((ChoiceParameterDefinition) child).getChoices());

            List<String> result = merge(templateChoices, childChoices);

            EzReflectionUtils.setFieldValue(ChoiceParameterDefinition.class, child, "choices", result); // TODO can't we just CoW?
        }
    }

    /**
     * Merge two overlapping queues into one.
     *
     * @return list retaining the order of rr elements then ll
     */
    private static <T> List<T> merge(Queue<T> ll, Queue<T> rr) {
        List<T> result = new ArrayList<T>(Math.max(ll.size(), rr.size()));

        T l = ll.poll();
        T r = rr.poll();

        while (r != null || l != null) {
            if (r == null) {
                // r=null -> consume l
                result.add(l);
                l = ll.poll();
            } else if (l == null) {
                // l=null ->  consume r
                result.add(r);
                r = rr.poll();
            } else if (r.equals(l)) {
                // l=r -> consume both
                result.add(r);
                r = rr.poll();
                l = ll.poll();
            } else if (rr.contains(l)) {
                // r contains l in different order -> consume r
                ll.remove(r); // may not remove anything
                result.add(r);
                r = rr.poll();
            } else {
                result.add(l);
                l = ll.poll();
            }

        }
        return result;
    }

    private static String key(ParameterDefinition parameterDefinition) {
        // JENKINS-38308 Support changing the type of parameter
        return parameterDefinition.getName() + parameterDefinition.getType();
    }

}
