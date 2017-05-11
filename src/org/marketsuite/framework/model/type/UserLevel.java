package org.marketsuite.framework.model.type;

public enum UserLevel {
    Basic("-bas"),
    Medium("-mid"),
    Expert("-adv"),
    ;

    UserLevel(String arg) { cmdArg = arg; }

    public String getCmdArg() { return cmdArg; }
    private String cmdArg;
}
