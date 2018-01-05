package com.joelj.jenkins.eztemplates.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.XmlFile;
import hudson.model.*;
import hudson.util.AtomicFileWriter;
import jenkins.model.Jenkins;
import jenkins.security.NotReallyRoleSensitiveCallable;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.StaplerRequest;

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

public class ProjectUtils {

    @SuppressFBWarnings
    public static Collection<AbstractProject> findProjectsWithProperty(final Class<? extends JobProperty<?>> property) {
        List<AbstractProject> projects = Jenkins.getInstance().getAllItems(AbstractProject.class);
        return Collections2.filter(projects, new Predicate<AbstractProject>() {
            @Override
            public boolean apply(@Nonnull AbstractProject abstractProject) {
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
            if (project.getFullName().equals(fullName)) {
                return project;
            }
        }
        return null;
    }

    public static void updateProjectWithXmlSource(AbstractItem project, Path source) throws IOException {
        try (InputStream is = Files.newInputStream(source)) {
            ProjectUtils.updateByXml(project, new StreamSource(is));
        }
    }

    /**
     * Copied from 1.580.3 {@link AbstractItem#updateByXml(javax.xml.transform.Source)}, removing the save event and
     * returning the project after the update. Note - newer version uses rebuildDependencyGraphAsync which may be a problem.
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

}
