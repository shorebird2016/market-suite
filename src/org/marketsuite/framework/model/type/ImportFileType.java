package org.marketsuite.framework.model.type;

public enum ImportFileType {
    IBD50_XLS           ("IBD50 (.xls)"),
    IBD50_CSV           ("IBD50 (.csv)"),
    IBD_PORTFOLIO_CSV   ("IBD Portfolio (.csv)"),
    FINVIZ_CSV          ("Finviz (.csv)"),
    BARCHAT_CSV         ("Barchart (.csv)"),
    DVG_CSV             ("Divergence Export")
    ;

    private String displayText;
    ImportFileType(String display_text) {
        displayText = display_text;
    }
    //NOTE: must use final keyword such that it can be accessed
    public final String getDisplayText() { return displayText; }
}
