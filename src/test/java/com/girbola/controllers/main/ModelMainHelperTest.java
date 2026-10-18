package com.girbola.controllers.main;

import com.girbola.configuration.Configuration;
import java.util.Optional;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelMainHelperTest {

    @Test
    void validatesMissingConfiguration() {
        assertEquals(ModelMainConfigurationValidator.ValidationResult.MISSING_CONFIGURATION,
                ModelMainConfigurationValidator.validate(null));
    }

    @Test
    void validatesMissingWorkDirectory() {
        Configuration configuration = new Configuration();

        assertEquals(ModelMainConfigurationValidator.ValidationResult.MISSING_WORK_DIRECTORY,
                ModelMainConfigurationValidator.validate(configuration));

        configuration.setWorkDir("  ");
        assertEquals(ModelMainConfigurationValidator.ValidationResult.MISSING_WORK_DIRECTORY,
                ModelMainConfigurationValidator.validate(configuration));
    }

    @Test
    void validatesConfiguredWorkDirectory() {
        Configuration configuration = new Configuration();
        configuration.setWorkDir("C:\\Media");

        assertEquals(ModelMainConfigurationValidator.ValidationResult.VALID,
                ModelMainConfigurationValidator.validate(configuration));
    }

    @Test
    void resolvesExitDialogActions() {
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        assertEquals(ModelMainExitHandler.ExitDecision.SAVE,
                ModelMainExitHandler.resolveDecision(Optional.of(save)));
        assertEquals(ModelMainExitHandler.ExitDecision.EXIT_WITHOUT_SAVE,
                ModelMainExitHandler.resolveDecision(Optional.of(no)));
        assertEquals(ModelMainExitHandler.ExitDecision.CANCEL,
                ModelMainExitHandler.resolveDecision(Optional.of(cancel)));
        assertEquals(ModelMainExitHandler.ExitDecision.CANCEL,
                ModelMainExitHandler.resolveDecision(Optional.empty()));
    }
}
