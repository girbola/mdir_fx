package common.utils.ui;

import com.girbola.messages.Messages;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenUtils {

	public static Rectangle2D screenBouds() {
		Messages.sprintf("Getting screenBounds:");
		return Screen.getPrimary().getVisualBounds();
	}
}

