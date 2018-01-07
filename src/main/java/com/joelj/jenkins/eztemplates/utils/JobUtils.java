package com.joelj.jenkins.eztemplates.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.XmlFile;
import hudson.model.AbstractItem;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.util.AtomicFileWriter;
import jenkins.model.Jenkins;
import jenkins.security.NotReallyRoleSensitiveCallable;

import javax.annotation.Nonnull;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class JobUtils {

    @SuppressFBWarnings
    public static Collection<Job> findJobsWithProperty(final Class<? extends JobProperty> property) {
        List<Job> projects = Jenkins.getInstance().getAllItems(Job.class);
        return Collections2.filter(projects, new Predicate<Job>() {
            @Override
            public boolean apply(@Nonnull Job job) {
                return job.getProperty(property) != null;
            }
        });
    }

    /**
     * Get a job by its fullName (including any folder structure if present).
     *
     * @param fullName full name of the job
     * @return job identified by the full name or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings
    public static <J extends Job> J findJob(String fullName) {
        List<Job> projects = Jenkins.getInstance().getAllItems(Job.class);
        for (Job project : projects) {
            if (fullName.equals(project.getFullName())) {
                return (J)project;
            }
        }
        return null;
    }

    public static void updateProjectWithXmlSource(AbstractItem project, Path source) throws IOException {
        try (InputStream is = Files.newInputStream(source)) {
            updateByXml(project, new StreamSource(is));
        }
    }

    /**
     * Copied from 1.580.3 {@link AbstractItem#updateByXml(javax.xml.transform.Source)}, removing the save event and
     * returning the job after the update. Note - newer version uses rebuildDependencyGraphAsync which may be a problem.
     * FIXME update
     *
     * @param project    job to persist
     * @param source configuration to be persisted
     * @return project as returned by {@link #findJob(String)}
     * @throws IOException if unable to persist
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings
    private static void updateByXml(final AbstractItem project, Source source) throws IOException {

        XmlFile configXmlFile = project.getConfigFile();
        AtomicFileWriter out = new AtomicFileWriter(configXmlFile.getFile());
        try {
            try {
                // this allows us to use UTF-8 for storing data,
                // plus it checks any well-formedness issue in the submitted
                // data
                Transformer t = TransformerFactory.newInstance()
                        .newTransformer();
                t.transform(source,
                        new StreamResult(out));
                out.close();
            } catch (TransformerException e) {
                throw new IOException("Failed to persist config.xml", e);
            }

            // try to reflect the changes by reloading
            Object o = new XmlFile(Items.XSTREAM, out.getTemporaryFile()).unmarshal(project);
            if (o != project) {
                // ensure that we've got the same job type. extending this code to support updating
                // to different job type requires destroying & creating a new job type
                throw new IOException("Expecting " + project.getClass() + " but got " + o.getClass() + " instead");
            }
            Items.whileUpdatingByXml(new NotReallyRoleSensitiveCallable<Void, IOException>() {
                @Override
                public Void call() throws IOException {
                    project.onLoad(project.getParent(), project.getRootDir().getName());
                    return null;
                }
            });
            Jenkins.getInstance().rebuildDependencyGraph();

            // if everything went well, commit this new version
            out.commit();
            //SaveableListener.fireOnChange(this, getConfigFile());
        } finally {
            out.abort(); // don't leave anything behind
        }
    }

    /**
     * @param item         A job of some kind
     * @param propertyType The property to look for
     * @return null if this property isn't found
     */
    @SuppressWarnings("unchecked")
    public static <J extends JobProperty> J getProperty(Object item, Class<J> propertyType) {
        // TODO Does this method already exist somewhere in Jenkins?
        // TODO bad home for this method
        if (item instanceof Job) {
            return (J) ((Job) item).getProperty(propertyType); // Why do we need to cast to J?
        }
        return null;
    }

    private static final String ABSTRACT_PROJECT_CLASS = "hudson.model.AbstractProject";
    private static final String WORKFLOW_JOB_CLASS = "org.jenkinsci.plugins.workflow.job.WorkflowJob";

    /**
     * Verifies if this template plugin applies to the Jenkins job type.
     *
     * @param jobType Jenkins job type.
     * @return {@code true} if it is a supported type.
     */
    public static boolean canBeTemplated(Class<? extends Job> jobType) {
        return EzReflectionUtils.isAssignable(ABSTRACT_PROJECT_CLASS, jobType)
                || EzReflectionUtils.isAssignable(WORKFLOW_JOB_CLASS, jobType);
    }

}
