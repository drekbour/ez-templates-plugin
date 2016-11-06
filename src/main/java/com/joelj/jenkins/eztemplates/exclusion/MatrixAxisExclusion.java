package com.joelj.jenkins.eztemplates.exclusion;

import com.google.common.base.Throwables;
import com.joelj.jenkins.eztemplates.utils.EzReflectionUtils;
import hudson.model.Job;

import java.lang.reflect.Method;

public class MatrixAxisExclusion extends HardCodedExclusion<Job> {

    public static final String ID = "matrix-axis";
    private static final String MATRIX_PROJECT = "hudson.matrix.MatrixProject";
    private Object axes; // AxesList

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Retain local matrix axes";
    }

    @Override
    public String getDisabledText() {
        return ExclusionUtil.checkPlugin("matrix-project");
    }

    @Override
    public void preClone(Job implementationProject) {
        if (isMatrixProject(implementationProject)) {
            axes = EzReflectionUtils.getFieldValue(implementationProject.getClass(), implementationProject, "axes");
        }
    }

    @Override
    public void postClone(Job implementationProject) {
        if (isMatrixProject(implementationProject)) {
            fixAxisList(implementationProject, axes);
        }
    }

    /**
     * Inlined from MatrixProject#setAxes(hudson.matrix.AxisList) except it doesn't call save.
     *
     * @param matrixProject The project to set the Axis on.
     * @param axisList      The Axis list to set.
     */
    private static void fixAxisList(Job matrixProject, Object axisList) {
        if (axisList == null) {
            return; //The "axes" field can never be null. So just to be extra careful.
        }
        EzReflectionUtils.setFieldValue(matrixProject.getClass(), matrixProject, "axes", axisList);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(MATRIX_PROJECT); // TODO add EzReflectionUtils.invoke ?
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals("rebuildConfigurations")) {
                    hudson.util.ReflectionUtils.makeAccessible(m);
                    hudson.util.ReflectionUtils.invokeMethod(m, matrixProject, new Object[]{null});
                }
            }
        } catch (ClassNotFoundException e) {
            Throwables.propagate(e);
        }
    }

    private static boolean isMatrixProject(Job project) {
        return MATRIX_PROJECT.equals(project.getClass().getName());
    }

}
