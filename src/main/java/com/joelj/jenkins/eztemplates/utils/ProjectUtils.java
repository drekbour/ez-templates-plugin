package com.joelj.jenkins.eztemplates.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.joelj.jenkins.eztemplates.jobtypes.JobsFacade;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.XmlFile;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.triggers.Trigger;
import hudson.util.AtomicFileWriter;
import jenkins.model.Jenkins;
import jenkins.security.NotReallyRoleSensitiveCallable;

public class ProjectUtils {

    @SuppressFBWarnings
    public static Collection<Job> findProjectsWithProperty(final Class<? extends JobProperty<?>> property, final Class< ? extends Job > jobType) {
        List<Job> projects = JobsFacade.getAllJobs( jobType );
        return Collections2.filter(projects, new Predicate<Job>() {
            @Override
            public boolean apply(@Nonnull Job abstractProject) {
                return abstractProject.getProperty(property) != null;
            }
        });
    }

    public static AbstractProject findProject(StaplerRequest request) {
        Ancestor ancestor = request.getAncestors().get(request.getAncestors().size() - 1);
        while (ancestor != null && !(ancestor.getObject() instanceof AbstractProject)) {
            ancestor = ancestor.getPrev();
        }
        if (ancestor == null) {
            return null;
        }
        return (AbstractProject) ancestor.getObject();
    }

    /**
     * Get a project by its fullName (including any folder structure if present).
     */
    @SuppressFBWarnings
    public static AbstractProject findProject(String fullName) {
        List<AbstractProject> projects = Jenkins.getInstance().getAllItems(AbstractProject.class);
        for (AbstractProject project : projects) {
            if (fullName.equals(project.getFullName())) {
                return project;
            }
        }
        return null;
    }

    /**
     * Silently saves the project without triggering any save events.
     * Use this method to save a project from within an Update event handler.
     */
    public static void silentSave(Job project) throws IOException {
        project.getConfigFile().write(project);
    }

    /**
     * Copied from 1.580.3 {@link AbstractItem#updateByXml(javax.xml.transform.Source)}, removing the save event and
     * returning the project after the update.
     */
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings
    public static Job updateProjectWithXmlSource(final Job project, Source source) throws IOException {

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
            if (o!=project) {
                // ensure that we've got the same job type. extending this code to support updating
                // to different job type requires destroying & creating a new job type
                throw new IOException("Expecting "+project.getClass()+" but got "+o.getClass()+" instead");
            }
            Items.whileUpdatingByXml(new NotReallyRoleSensitiveCallable<Void,IOException>() {
                @Override public Void call() throws IOException {
                    project.onLoad(project.getParent(), project.getRootDir().getName());
                    return null;
                }
            });
            Jenkins.getInstance().rebuildDependencyGraph();

            // if everything went well, commit this new version
            out.commit();
            return findProject(project.getFullName());
        } finally {
            out.abort(); // don't leave anything behind
        }
    }

    public static List<Trigger<?>> getTriggers(Job implementationProject) {
        return JobsFacade.getTriggersToReplace( implementationProject );
    }
}
