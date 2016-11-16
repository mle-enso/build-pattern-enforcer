package de.mle;

import lombok.Builder;
import lombok.Setter;

@Builder
@Setter
@SuppressWarnings("unused")
public class SampleBuilderWithSetter {
    private String stringField;
    private int intField;
}
