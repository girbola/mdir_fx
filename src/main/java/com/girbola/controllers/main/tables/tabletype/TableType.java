package com.girbola.controllers.main.tables.tabletype;

public enum TableType {
    SORTIT("SortIt"),
    SORTED("Sorted"),
    ASITIS("AsItIs");

    private String type;

    TableType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
