package de.mle.enforcer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BuilderPatternEnforcerTest {
	private static final String TARGET_DIR = BuilderPatternEnforcerTest.class.getResource("/").toString();
	private static final File ROOT_PROJECT_DIR = new File(TARGET_DIR).getParentFile().getParentFile();
	private static final String PROTOCOL = "file:";

	@Test
	@SuppressWarnings("unchecked")
	public void checkRuleSet() throws ExpressionEvaluationException, EnforcerRuleException {
		// given
		BuilderPatternEnforcer enforcer = new BuilderPatternEnforcer();
		EnforcerRuleHelper helper = initMavenProjectAndAssembleSourcePath("/src/main/java/de/mle/patterns/correct");
		mockLog(helper);

		// when
		enforcer.execute(helper);

		// then
		Map<Pattern, List<String>> rules = (Map<Pattern, List<String>>) ReflectionTestUtils.getField(enforcer, "rules");

		assertThat(rules.entrySet(), hasSize(3));
		assertThat(rules.get(Pattern.BUILDER), hasSize(3));
		assertThat(rules.get(Pattern.SETTER), hasSize(2));
		assertThat(rules.get(Pattern.DATA), hasSize(2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void checkPatternMatching() throws ExpressionEvaluationException {
		// given
		BuilderPatternEnforcer enforcer = new BuilderPatternEnforcer();
		EnforcerRuleHelper helper = initMavenProjectAndAssembleSourcePath("/src/main/java/de/mle");
		Log log = mockLog(helper);

		// when
		try {
			enforcer.execute(helper);
			Assert.fail();
		} catch (EnforcerRuleException e) {
			// then
			assertThat(e.getMessage(), is("Found builder pattern violations or adjacent errors!"));
		}

		// then
		List<String> errors = (List<String>) ReflectionTestUtils.getField(enforcer, "errors");

		assertThat(errors, hasSize(2));

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(log, times(3)).warn(argument.capture());
		List<String> allLogMessages = argument.getAllValues();

		// @formatter:off
		assertThat(allLogMessages, containsInAnyOrder(
				endsWith("Found builder pattern violations or adjacent errors:"),
				endsWith("/src/main/java/de/mle/patterns/incorrect/SampleBuilderWithData.java"),
				endsWith("/src/main/java/de/mle/patterns/incorrect/SampleBuilderWithSetter.java")));
		// @formatter:on
	}

	private Log mockLog(EnforcerRuleHelper helper) {
		Log log = mock(Log.class);
		when(helper.getLog()).thenReturn(log);
		return log;
	}

	private EnforcerRuleHelper initMavenProjectAndAssembleSourcePath(String sourcePathSuffix) throws ExpressionEvaluationException {
		EnforcerRuleHelper helper = mock(EnforcerRuleHelper.class);
		MavenProject mavenProject = mock(MavenProject.class);

		String sourceDir = ROOT_PROJECT_DIR.getPath().replaceFirst(PROTOCOL, "") + sourcePathSuffix;

		when(mavenProject.getCompileSourceRoots()).thenReturn(Arrays.asList(new String[] { sourceDir }));
		when(helper.evaluate("${project}")).thenReturn(mavenProject);

		return helper;
	}
}
