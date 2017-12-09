package com.joelj.jenkins.eztemplates;

import com.joelj.jenkins.eztemplates.exclusion.Exclusions;
import com.joelj.jenkins.eztemplates.listener.VersionEvaluator;
import hudson.BulkChange;
import hudson.model.*;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.SaveableListener;
import hudson.triggers.TimerTrigger;
import hudson.util.ListBoxModel;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static com.joelj.jenkins.eztemplates.EzMatchers.*;
import static com.joelj.jenkins.eztemplates.FieldMatcher.hasField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

/**
 * Tests of the job and property behaviours.
 */
public class BehaviourTest {

    @Rule
    public final JenkinsRule jenkins = new JenkinsRule();

    private FreeStyleProject project(String name) throws Exception {
        return jenkins.createFreeStyleProject(name);
    }

    private FreeStyleProject template(String name) throws Exception {
        FreeStyleProject template = project(name);
        template.addProperty(new TemplateProperty());
        return template;
    }

    private FreeStyleProject template(String name, String parentTemplate) throws Exception {
        FreeStyleProject template = template(name);
        assignTemplate(parentTemplate, template);
        return template;
    }

    private FreeStyleProject impl(String name, AbstractProject template) throws Exception {
        return impl(name, template.getFullName());
    }

    private FreeStyleProject impl(String name, String template) throws Exception {
        FreeStyleProject impl = project(name);
        return assignTemplate(template, impl);
    }

    private static FreeStyleProject assignTemplate(String template, FreeStyleProject impl) throws java.io.IOException {
        BulkChange change = new BulkChange(impl);
        impl.addProperty(TemplateImplementationProperty.newImplementation(template));
        change.abort(); // Leaves XML unchanged, doesn't save()
        return impl;
    }

    private static void addTriggerWithoutTemplating(AbstractProject project) throws Exception {
        // This makes a change to a child project that will be overwritten by template merge (with default exclusions)
        BulkChange change = new BulkChange(project);
        project.addTrigger(new TimerTrigger("* H * * *"));
        change.abort(); // Leaves XML unchanged, doesn't save()
        assertThat(project.getTriggers().isEmpty(), is(false));
    }

    private static void save(Item project) {
        if (VersionEvaluator.jobSaveUsesBulkchange()) {
            SaveableListener.fireOnChange(project, Items.getConfigFile(project));
        } else {
            ItemListener.fireOnUpdated(project);
        }
    }

    // Identity

    @Test
    public void impl_finds_known_templates() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        template.setDisplayName("Alpha Template");
        FreeStyleProject template2 = template("beta-template");
        template2.setDisplayName("Beta Template");
        // When:
        ListBoxModel knownTemplates = new TemplateImplementationProperty.DescriptorImpl().doFillTemplateJobNameItems();
        // Then:
        assertThat(knownTemplates, contains(
                both(hasField("name", "No template selected")).and(hasField("value", null)),
                both(hasField("name", "Alpha Template")).and(hasField("value", "alpha-template")),
                both(hasField("name", "Beta Template")).and(hasField("value", "beta-template"))
        ));
    }

    @Test
    public void impl_has_default_exclusions() throws Exception {
        // Given:
        FreeStyleProject impl = impl("my-impl", (String) null);
        // When:
        List<String> exclusions = impl.getProperty(TemplateImplementationProperty.class).getExclusions();
        // Then:
        assertThat(exclusions, is(equalTo(Exclusions.DEFAULT)));
    }

    @Test
    public void impl_knows_its_template() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        // When:
        FreeStyleProject impl = impl("alpha-1", template);
        // Then:
        assertThat(impl, hasTemplate("alpha-template"));
    }

    @Test
    public void template_knows_its_children() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        // When:
        FreeStyleProject impl = impl("alpha-1", template);
        FreeStyleProject impl2 = impl("alpha-2", template);
        // Then:
        assertThat(template, hasImplementations(impl, impl2));
    }

    // Listeners

    @Test
    public void saving_impl_initiates_a_merges() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        // When:
        addTriggerWithoutTemplating(impl);
        save(impl);
        // Then:
        assertThat(impl.getTriggers().isEmpty(), is(true));
    }

    @Test
    public void saving_template_initiates_a_merge() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        // When:
        addTriggerWithoutTemplating(impl);
        save(template);
        // Then:
        assertThat(impl.getTriggers().isEmpty(), is(true));
    }

    @Test
    public void saving_something_else_works() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        FreeStyleProject other = project("beta");
        // When:
        addTriggerWithoutTemplating(impl);
        save(other);
        // Then:
        assertThat(impl.getTriggers().isEmpty(), is(false));
    }

    @Test
    public void saving_impl_with_no_template_works() throws Exception {
        // Given:
        FreeStyleProject impl = impl("alpha-1", "null"); // FIXME this really should be tested via web submission
        // When:
        addTriggerWithoutTemplating(impl);
        save(impl);
        // Then:
        assertThat(impl.getTriggers().isEmpty(), is(false));
    }

    @Test
    public void deleting_impl_removes_from_template() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        // When:
        impl.delete();
        // Then:
        assertThat(template, hasNoImplementations());
    }

    @Test
    public void deleting_template_frees_an_impl() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        // When:
        template.delete();
        // Then:
        assertThat(impl, hasNoTemplate());
    }

    @Test
    public void renaming_template_updates_impl() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        FreeStyleProject template2 = template("beta-template");
        FreeStyleProject impl2 = impl("beta-1", template2);
        // When:
        template.renameTo("gamma-template");
        // Then:
        assertThat(impl, hasTemplate("gamma-template"));
        assertThat(impl2, hasTemplate("beta-template"));
    }

    @Test
    public void moving_template_updates_impl() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        FreeStyleProject impl = impl("alpha-1", template);
        // When:
        template.renameTo("subfolder/alpha-template");
        // Then:
        assertThat(impl, hasTemplate("subfolder/alpha-template"));
    }

    @Test
    public void copying_template_creates_impl() throws Exception {
        // Given:
        FreeStyleProject template = template("alpha-template");
        // When:
        FreeStyleProject impl = (FreeStyleProject) jenkins.jenkins.copy((TopLevelItem) template, "alpha-1");
        // Then:
        assertThat(impl, hasTemplate("alpha-template"));
    }

    @Test
    public void saving_propagates_across_nested_templates() throws Exception {
        assumeThat("Only works on >=2.32.2", VersionEvaluator.jobSaveUsesBulkchange(), is(true));
        // Given:
        FreeStyleProject grandparent = template("food");
        FreeStyleProject parent = template("fruit", "food");
        FreeStyleProject child = impl("apple", "fruit");
        // When:
        addTriggerWithoutTemplating(child);
        save(grandparent);
        Thread.sleep(10000); // FIXME
        // Then:
        assertThat(child.getTriggers().isEmpty(), is(true));
    }


}