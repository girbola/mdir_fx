/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.ui.UI_Tools;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class HideButtons {

	final private String ERROR = HideButtons.class.getSimpleName();

	private int test = 100;

	private int visible;
	private HBox sortit_buttons_hbox;
	private HBox sorted_buttons_hbox;
	private HBox asitis_buttons_hbox;

	private boolean asitis_show = true;
	private boolean sortit_show = true;
	private boolean sorted_show = true;

	private Model_main model;

	private Image show_im;
	private Image hide_im;

	public HideButtons(Model_main model) {
		this.model = model;
		visible = 3;
		show_im = GUI_Methods.loadImage("showTable.png", GUIPrefs.BUTTON_WIDTH);
		hide_im = GUI_Methods.loadImage("hideTable.png", GUIPrefs.BUTTON_WIDTH);

		sprintf("HideButtons instantiated...");
	}

	public HideButtons(Model_main model, boolean sortit_show, boolean sorted_show, boolean asitis_show) {
		this.model = model;
		visible = 3;
		if (sortit_show && sorted_show && asitis_show) {
			sortit_show = true;
			sorted_show = false;
			asitis_show = false;
			visible = 1;
		}
	}

	public void setSortItButtons_hbox(HBox hbox) {
		this.sortit_buttons_hbox = hbox;
	}

	public void setAsItIsButtons_hbox(HBox hbox) {
		this.asitis_buttons_hbox = hbox;
	}

	public void setSortedButtons_hbox(HBox hbox) {
		this.sorted_buttons_hbox = hbox;
	}

	public HBox getSortedButtons_hbox() {
		return this.sorted_buttons_hbox;
	}

	public HBox getSortItButtons_hbox() {
		return this.sortit_buttons_hbox;
	}

	public HBox getAsItIsButtons_hbox() {
		return this.asitis_buttons_hbox;
	}

	private void setShowTableButton(Button button) {
		ImageView iv = (ImageView) button.getGraphic();
		iv.setImage(show_im);
		button.setGraphic(iv);
		setButtonsState(button, true);
	}

	private void setHideTabeleButton(Button button) {
		ImageView iv = (ImageView) button.getGraphic();
		iv.setImage(hide_im);
		button.setGraphic(iv);

		setButtonsState(button, false);
	}

	private void setButtonsState(Button button, boolean value) {
		Bounds bound = UI_Tools.getNodeLayoutBounds(button);

		Node node = button.getParent().getParent(); // VBox - Buttons -
		// TextField - Tables
		if (node instanceof VBox) {
			VBox vbox = (VBox) node;
			for (Node v : vbox.getChildren()) {
				if (v instanceof HBox) {
					// HBox
					HBox hbox = (HBox) v;
					setRegionSize(hbox, bound.getWidth(), bound.getHeight());
					for (Node buttons : ((HBox) v).getChildren()) {
						// sprintf("HBOX: " + buttons.getId());
						// Button
						if (buttons instanceof Button) {
							// sprintf("v is instanceof button: " +
							// buttons.getId());
							if (buttons.getId().equals(button.getId())) {
								// sprintf("sortit_shot button found");
								if (value) {
									setRegionSize(vbox, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

								} else {
									setRegionSize(vbox, bound.getWidth(), bound.getHeight());

								}
								// sprintf("sortit show button size is: " +
								// bound.getWidth() + " height: " +
								// bound.getHeight());
							} else {
								buttons.setVisible(value);
							}
						}
					}
				}
				if (v instanceof TextField) {
					sprintf("textfield: ");
					v.setVisible(value);
				}
				if (v instanceof Separator) {
					v.setVisible(value);
				}
			}
		}
	}

	private void setRegionSize(Region node, double width, double height) {
		sprintf("visible node hbox = " + visible);
		node.setPrefWidth(width);
		node.setMinWidth(width);
		// node.setMaxWidth(width);

	}

	public void setAccelerator(Button button, TableType tableType, int number) {
		// button.setMnemonicParsing(true);
		KeyCode numberKey = null;
		if (number == 1) {
			numberKey = KeyCode.DIGIT1;
		} else if (number == 2) {
			numberKey = KeyCode.DIGIT2;
		} else if (number == 3) {
			numberKey = KeyCode.DIGIT3;
		}
		button.getScene().getAccelerators().put(new KeyCodeCombination(numberKey, KeyCombination.CONTROL_DOWN),
				(Runnable) () -> {
					if (tableType.equals(TableType.ASITIS)) {
						button.fire();
						hide_show_table(button, TableType.ASITIS.getType());

					} else if (tableType.equals(TableType.SORTED.getType())) {
						button.fire();
						hide_show_table(button, TableType.SORTED.getType());
					} else if (tableType.equals(TableType.SORTIT.getType())) {
						button.fire();
						hide_show_table(button, TableType.SORTIT.getType());
					}
				});
	}

	void hide_show_table(Button button, String tableType) {

		if (tableType.equals(TableType.ASITIS.getType())) {

			if (asitis_show) {
				updateTableVisible();
				if (visible == 1) {
					return;
				}
				setHideTabeleButton(button);
				model.tables().getAsItIs_table().setVisible(false);
				asitis_show = false;
			} else {
				updateTableVisible();
				setShowTableButton(button);
				model.tables().getAsItIs_table().setVisible(true);
				asitis_show = true;
			}
		} else if (tableType.equals(TableType.SORTED.getType())) {
			if (sorted_show) {
				updateTableVisible();
				if (visible == 1) {
					return;
				}
				setHideTabeleButton(button);
				model.tables().getSorted_table().setVisible(false);
				sorted_show = false;
			} else {
				updateTableVisible();
				setShowTableButton(button);
				model.tables().getSorted_table().setVisible(true);
				sorted_show = true;
			}
		} else if (tableType.equals(TableType.SORTIT.getType())) {
			if (sortit_show) {
				updateTableVisible();
				if (visible == 1) {
					return;
				}
				sprintf("sortit_show true");
				setHideTabeleButton(button);
				model.tables().getSortIt_table().setVisible(false);
				sortit_show = false;
			} else {
				updateTableVisible();

				setShowTableButton(button);
				model.tables().getSortIt_table().setVisible(true);
				sortit_show = true;
			}
		} else {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}

	}

	public int getTest() {
		return this.test;
	}

	private void updateTableVisible() {
		visible = 0;
		sprintf("Visible is: " + visible);
		if (model.tables().getSortIt_table().isVisible()) {
			visible++;
		}
		if (model.tables().getSorted_table().isVisible()) {
			visible++;
		}
		if (model.tables().getAsItIs_table().isVisible()) {
			visible++;
		}
		sprintf("Visible is: " + visible);

	}
}
