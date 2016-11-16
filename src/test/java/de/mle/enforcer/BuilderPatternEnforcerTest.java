package de.mle.enforcer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.testng.annotations.Test;

public class BuilderPatternEnforcerTest {
    private static final String FILE_PROTOCOL = "file:";
    private BuilderPatternEnforcer enforcer = new BuilderPatternEnforcer();
    private static final String TARGET_DIR = BuilderPatternEnforcerTest.class.getResource("/").toString();

    @Test
    public void execute() throws EnforcerRuleException, ExpressionEvaluationException, IOException {
        // given
        EnforcerRuleHelper helper = mock(EnforcerRuleHelper.class);
        MavenProject mavenProject = mock(MavenProject.class);
        File rootProjectDir = new File(TARGET_DIR).getParentFile().getParentFile();
        String sourceDir = rootProjectDir.getPath().replaceFirst(FILE_PROTOCOL, "") + "/src/main/java";

        when(mavenProject.getCompileSourceRoots()).thenReturn(Arrays.asList(new String[] { sourceDir }));

        when(helper.evaluate("${project}")).thenReturn(mavenProject);

        // when
        enforcer.execute(helper);

        // then
        Map<Pattern, List<String>> rules = enforcer.rules;
        assertThat(rules.entrySet(), hasSize(3));
        assertThat(rules.get(Pattern.BUILDER), hasSize(3));
        assertThat(rules.get(Pattern.SETTER), hasSize(2));
        assertThat(rules.get(Pattern.DATA), hasSize(2));
    }

}
