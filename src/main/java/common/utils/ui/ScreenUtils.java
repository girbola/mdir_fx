package common.utils.ui;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenUtils {

	public static Rectangle2D screenBouds() {
		return Screen.getPrimary().getVisualBounds();
	}
}

