package de.mle;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SampleBuilder {
    private String stringField;
    private int intField;
}
