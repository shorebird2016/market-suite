package org.marketsuite.framework.model.type;

public enum GapType {
    TYPE_1("Run Away"),
    TYPE_2("Partial Fill Gap"),
    TYPE_3("Gap Broken"),
    TYPE_4("Not Earning Gap"),
    TYPE_5("No Gap/No Earning Date"),
    ;

    GapType(String display_string) { displayString = display_string; }
    private String displayString;
    //convert all into human readable string
    public String toString() { return displayString; }
}
