package com.girbola.dialogs;

import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.CopyAnswerType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class YesNoCancelDialogController {

	private FileInfo fileInfo;
	private String headerText;
	private String contentText;
	private String buttonYesText;
	private String buttonNoText;
	private String cancelText;
	private Stage stage;
	private SimpleIntegerProperty answer;
	private SimpleStringProperty rememberAnswer;

	public void init(Stage stage, SimpleIntegerProperty answer, FileInfo fileInfo, SimpleStringProperty rememberAnswer,
			String headerText, String contentText, String yesText, String noText, String cancelText) {
		this.stage = stage;
		this.answer = answer;
		this.rememberAnswer = rememberAnswer;
		header_ta.setText(headerText);
		content_ta.setText(contentText);
		yes_btn.setText(yesText);
		no_btn.setText(noText);
		abort_btn.setText(cancelText);
	}

   @FXML private TextArea header_ta;
   @FXML private TextArea content_ta;

   @FXML private Button yes_btn;

   @FXML private Button no_btn;

   @FXML private Button abort_btn;
   @FXML private CheckBox checkBox;

   @FXML void abort_btn_action(ActionEvent event) {
		answer.set(2);
		stage.close();
	}

   @FXML void no_btn_action(ActionEvent event) {
		answer.set(1);
		if (checkBox.isSelected()) {
			rememberAnswer.set(CopyAnswerType.DONTCOPY);
		}
		stage.close();
	}

   @FXML void yes_btn_action(ActionEvent event) {
		answer.set(0);
		if (checkBox.isSelected()) {
			rememberAnswer.set(CopyAnswerType.COPY);
		}
		stage.close();
	}

}
