package de.mle.patterns.incorrect;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SampleBuilderWithData {
    private String stringField;
    private int intField;
}
