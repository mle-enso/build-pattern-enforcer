package de.mle.enforcer;

import java.util.List;

import lombok.Value;

@Value
public class Rule {
    private Pattern pattern;
    private List<String> content;
}
