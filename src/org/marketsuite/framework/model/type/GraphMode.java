package org.marketsuite.framework.model.type;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkConstants;

/**
 * Specify the mode for relative strength graphs:
 * (1) Origin mode - each symbol's quote compared against its first quote
 * (2) Baseline mode - each symbol's quote compared against a selected symbol
 */
public enum GraphMode {
    ORIGIN_MODE,
    BASELINE_MODE,
    ;

    public String toString() {
        switch (ordinal()) {
            case 0:
                return FrameworkConstants.FRAMEWORK_BUNDLE.getString("mode_origin_relative");

            case 1:
                return FrameworkConstants.FRAMEWORK_BUNDLE.getString("mode_baseline_relative");
        }
        return "";
    }
}
