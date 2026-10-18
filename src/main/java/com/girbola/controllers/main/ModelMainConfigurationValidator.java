package com.girbola.controllers.main;

import com.girbola.configuration.Configuration;

final class ModelMainConfigurationValidator {

    private ModelMainConfigurationValidator() {
    }

    static ValidationResult validate(Configuration configuration) {
        if (configuration == null) {
            return ValidationResult.MISSING_CONFIGURATION;
        }
        String workDirectory = configuration.getWorkDir();
        if (workDirectory == null || workDirectory.isBlank()) {
            return ValidationResult.MISSING_WORK_DIRECTORY;
        }
        return ValidationResult.VALID;
    }

    enum ValidationResult {
        VALID,
        MISSING_CONFIGURATION,
        MISSING_WORK_DIRECTORY
    }
}
