package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.tabletype.TableType;
import javafx.beans.property.SimpleBooleanProperty;

public class ShowAndHideTables {

	private ModelMain model_Main;

	private SimpleBooleanProperty sortit_show_property = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty sorted_show_property = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty asitis_show_property = new SimpleBooleanProperty(true);

	public SimpleBooleanProperty getAsitis_show_property() {
		return asitis_show_property;
	}

	public void setAsitis_show_property(boolean asitis_show_property) {
		this.asitis_show_property.set(asitis_show_property);
	}

	public SimpleBooleanProperty getSortit_show_property() {
		return sortit_show_property;
	}

	public void setSortit_show_property(boolean sortit_show_property) {
		this.sortit_show_property.set(sortit_show_property);
	}

	public SimpleBooleanProperty getSorted_show_property() {
		return sorted_show_property;
	}

	public void setSorted_show_property(boolean sorted_show_property) {
		this.sorted_show_property.set(sorted_show_property);
	}

	public ShowAndHideTables(ModelMain model_Main) {
		this.model_Main = model_Main;
	}

	public void showTable(String tableType, boolean show) {
		if (tableType.equals(TableType.SORTIT.getType())) {
			sortit_show_property.set(show);
		} else if (tableType.equals(TableType.SORTED.getType())) {
			sorted_show_property.set(show);
		} else if (tableType.equals(TableType.ASITIS.getType())) {
			asitis_show_property.set(show);
		}
	}

	public int getVisibles() {
		int visibles = 0;

		if (model_Main.tables().getSortIt_table().isVisible()) {
			visibles++;
		}
		if (model_Main.tables().getSorted_table().isVisible()) {
			visibles++;
		}
		if (model_Main.tables().getAsItIs_table().isVisible()) {
			visibles++;
		}
		return visibles;
	}

}
