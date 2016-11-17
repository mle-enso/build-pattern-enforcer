package de.mle.enforcer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class BuilderPatternEnforcer implements EnforcerRule {
	private static final String RULE_SET_FILE = "/de/mle/enforcer/searchPatterns.txt";
	private List<String> errors = new ArrayList<>();
	private Map<Pattern, List<String>> rules;
	private Log log;

	@Override
	@SuppressWarnings("unchecked")
	public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
		log = helper.getLog();
		MavenProject project;
		try {
			project = (MavenProject) helper.evaluate("${project}");
		} catch (ExpressionEvaluationException e) {
			throw new EnforcerRuleException("Unable to aquire current Maven Project!", e);
		}
		loadRules();

		checkBuilderPatternAbuses(project.getCompileSourceRoots());
		evaluateErrors();
	}

	private void evaluateErrors() throws EnforcerRuleException {
		if (!errors.isEmpty()) {
			log.warn("Found builder pattern violations or adjacent errors:");
			errors.stream().forEach(error -> log.warn(error));
			throw new EnforcerRuleException("Found builder pattern violations or adjacent errors!");
		}
	}

	private void loadRules() throws EnforcerRuleException {
		try {
			String rulesYaml = IOUtils.toString(BuilderPatternEnforcer.class.getResourceAsStream(RULE_SET_FILE));
			rules = new ObjectMapper(new YAMLFactory()).readValue(rulesYaml, new TypeReference<Map<Pattern, List<String>>>() {
			});
		} catch (IOException e) {
			throw new EnforcerRuleException("Unable to load rules from file!", e);
		}
	}

	private void checkBuilderPatternAbuses(List<String> compileSourceRoots) {
		compileSourceRoots.stream().forEach(sourceFolder -> checkAllFiles(sourceFolder));
	}

	@SuppressWarnings("unchecked")
	private void checkAllFiles(String sourceFolder) {
		Iterator<File> sourceFiles = FileUtils.iterateFiles(new File(sourceFolder), new String[] { "java" }, true);
		StreamSupport.stream(Spliterators.spliteratorUnknownSize(sourceFiles, Spliterator.DISTINCT), true).forEach(file -> checkFile(file));
	}

	private void checkFile(File file) {
		String content;
		try {
			content = FileUtils.readFileToString(file);
		} catch (IOException e) {
			log.warn("Error reading source file " + file.getAbsolutePath());
			throw new RuntimeException(e);
		}

		boolean hasBuilderAnnotation = checkContentByPattern(Pattern.BUILDER, content);
		if (!hasBuilderAnnotation)
			return;

		boolean hasSetterAnnotation = checkContentByPattern(Pattern.SETTER, content);
		boolean hasDataAnnotation = checkContentByPattern(Pattern.DATA, content);

		if (hasDataAnnotation || hasSetterAnnotation)
			errors.add(file.getAbsolutePath());
	}

	private boolean checkContentByPattern(Pattern pattern, String content) {
		return rules.get(pattern).stream().filter(rule -> content.contains(rule)).findAny().isPresent();
	}

	@Override
	public String getCacheId() {
		return "";
	}

	@Override
	public boolean isCacheable() {
		return false;
	}

	@Override
	public boolean isResultValid(EnforcerRule arg0) {
		return false;
	}
}
