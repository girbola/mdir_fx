package com.girbola.controllers.main.options;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

public class OptionsComponent {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(OptionsComponent.class);


    public static void openOptions() {
        sprintf("menuItem_tools_options_action starting Theme path is: " + conf.getThemePath());
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/main/options/Options.fxml"), bundle);
        Parent parent = null;
        try {
            parent = loader.load();
        } catch (IOException ex) {
            Messages.errorSmth(logger.getName(), bundle.getString("cannotLoadFXML"), ex, Misc.getLineNumber(), true);
        }
        OptionsController optionsController = (OptionsController) loader.getController();
        optionsController.init();

        Stage stage_opt = new Stage();
        stage_opt.setAlwaysOnTop(true);

        Scene scene_opt = new Scene(parent);
        scene_opt.getStylesheets()
                .add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.OPTIONPANE.getType()).toExternalForm());

        stage_opt.setScene(scene_opt);
        stage_opt.setOnCloseRequest(closeEvent -> {
            ConfigurationSQLHandler.updateConfiguration();
        });
        stage_opt.show();
    }

}
