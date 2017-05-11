package org.marketsuite.market;

/**
 * A data object stores information about a tree node. Stored as each node's UserObject.
 */
public class NodeInfo {
    public NodeInfo(String name, int nodeType) {
        this.name = name;
        this.nodeType = nodeType;
    }

    public String toString() { return name; }

    //----- accessor -----
    public String getName() { return name; }
    public int getNodeType() { return nodeType; }

    //----- variables -----
    private String name;
    private int nodeType;//MarketTree.NODETYPE_xxx
}
