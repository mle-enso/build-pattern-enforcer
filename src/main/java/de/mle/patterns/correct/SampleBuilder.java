package de.mle.patterns.correct;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SampleBuilder {
    private String stringField;
    private int intField;
}
