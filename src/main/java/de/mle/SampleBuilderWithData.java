package de.mle;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SampleBuilderWithData {
    private String stringField;
    private int intField;
}
