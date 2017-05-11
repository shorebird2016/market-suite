package org.marketsuite.framework.model.type;

//current state of any given IBD50 stock
public enum Ibd50State {
    Onlist,     //entering IBD50 point
    Offlist,    //exiting IBD50 point
    Active,     //remain on list
    Inactive;    //remain off list

    public static Ibd50State findState(String state) {
        if (state.equals(Onlist.toString())) return Ibd50State.Onlist;
        else if (state.equals(Offlist.toString())) return Ibd50State.Offlist;
        else if (state.equals(Active.toString())) return Ibd50State.Active;
        else return Ibd50State.Inactive;
    }
}
