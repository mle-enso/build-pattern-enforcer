package de.mle.enforcer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private List<String> errors = new ArrayList<>();
    Map<Pattern, List<String>> rules;

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();

        try {
            MavenProject project = (MavenProject) helper.evaluate("${project}");
            loadRules();

            checkBuilderPatternAbuses(project.getCompileSourceRoots(), log);
            // TODO: extract method
            if (!errors.isEmpty()) {
                log.warn("Found builder pattern violations or adjacent errors:");
                errors.stream().forEach(error -> log.warn(error));
                throw new EnforcerRuleException("Found builder pattern violations or adjacent errors!");
            }
        // TODO: keep smaller
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
        }
    }

    private void loadRules() throws EnforcerRuleException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // TODO: inline

        try {
            String rulesYaml = IOUtils.toString(BuilderPatternEnforcer.class.getResourceAsStream("/de/mle/enforcer/searchPatterns.txt"));
            rules = mapper.readValue(rulesYaml, new TypeReference<Map<Pattern, List<String>>>() {
            });
        } catch (IOException e) {
            throw new EnforcerRuleException("Unable to load rules from file!" + e.getLocalizedMessage(), e);
        }
    }

    private void checkBuilderPatternAbuses(List<String> compileSourceRoots, Log log) {
        compileSourceRoots.stream().forEach(sourceFolder -> checkAllFiles(sourceFolder, log));
    }

    private void checkAllFiles(String sourceFolder, Log log) {
        try {
            @SuppressWarnings("unchecked")
            Iterator<File> sourceFiles = FileUtils.iterateFiles(new File(sourceFolder), new String[] { "java" }, true);
            // TODO: stream iterator
            while (sourceFiles.hasNext()) {
                checkFile(sourceFiles.next());
            }
        } catch (IOException e) {
            log.warn("Error reaading source files from " + sourceFolder);
            throw new RuntimeException(e);
        }
    }

    private void checkFile(File file) throws IOException {
        String fileName = file.getAbsolutePath();
        String content = FileUtils.readFileToString(file);
        boolean hasBuilderAnnotation = checkContentByPattern(Pattern.BUILDER, content, fileName);
        boolean hasSetterAnnotation = checkContentByPattern(Pattern.SETTER, content, fileName);
        boolean hasDataAnnotation = checkContentByPattern(Pattern.DATA, content, fileName);

        // TODO: check earlier
        if (!hasBuilderAnnotation)
            return;
        if (hasDataAnnotation || hasSetterAnnotation)
            errors.add(file.getAbsolutePath()); // TODO: fileName
    }

    private boolean checkContentByPattern(Pattern pattern, String content, String fileName) {
        return rules.get(pattern).stream()
                .filter(rule -> matchesSingleRule(rule, content, fileName))
                .findAny().isPresent();
    }
    // TODO: inline
    private boolean matchesSingleRule(String rule, String content, String fileName) {
        return content.contains(rule);
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
