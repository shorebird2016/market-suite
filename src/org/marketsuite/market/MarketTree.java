package org.marketsuite.market;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

//A tree representation of market from Finviz import of 6900+ symbols organized via sector / industry / symbol
class MarketTree extends JTree {
    MarketTree() {
        _Root = new DefaultMutableTreeNode("MARKET");
        _TreeModel = new DefaultTreeModel(_Root);
        setModel(_TreeModel);
        setShowsRootHandles(true);
        long start = System.currentTimeMillis();//measure load time

        //walk fundamental map, create tree structure
        HashMap<String,Fundamental> fundamental_map = MainModel.getInstance().getFundamentals();
        Iterator itor = fundamental_map.keySet().iterator();
        while(itor.hasNext()) {
            String key = (String)itor.next();
            Fundamental fundamental = fundamental_map.get(key);
            String sector = fundamental.getSector();
            String industry = fundamental.getIndustry();
            String symbol = fundamental.getSymbol();
            TreePath sector_path = findSectorNode(sector);
            if (sector_path != null) {
                TreePath industry_path = findIndustryNode(sector, industry);
                if (industry_path != null) {
                    DefaultMutableTreeNode industry_node = (DefaultMutableTreeNode)industry_path.getLastPathComponent();
                    addObject(industry_node, new NodeInfo(symbol, NODETYPE_SYMBOL));
                }
                else {//add industry branch, symbol leaf
                    DefaultMutableTreeNode sector_node = (DefaultMutableTreeNode)sector_path.getLastPathComponent();
                    DefaultMutableTreeNode industry_node = addObject(sector_node, new NodeInfo(industry, NODETYPE_INDUSTRY));
                    addObject(industry_node, new NodeInfo(symbol, NODETYPE_SYMBOL));
                }
            }
            else {//add sector branch, industry branch, symbol leaf
                DefaultMutableTreeNode sector_node = addObject(null, new NodeInfo(sector, NODETYPE_SECTOR));
                DefaultMutableTreeNode industry_node = addObject(sector_node, new NodeInfo(industry, NODETYPE_INDUSTRY));
                addObject(industry_node, new NodeInfo(symbol, NODETYPE_SYMBOL));
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("It takes " + (end - start) / 1000 + " second(s) to build Market Tree from finviz export for " + _nNodeCounter + " symbols");
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = ((JTree) e.getSource()).getSelectionPath();
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                    NodeInfo info = (NodeInfo)node.getUserObject();
                    switch(info.getNodeType()) {
                        case NODETYPE_INDUSTRY://retrieve all children symbols
                            ArrayList<String> list = new ArrayList<>();
                            Enumeration emu = node.depthFirstEnumeration();
                            while (emu.hasMoreElements()) {
                                DefaultMutableTreeNode emu_node = (DefaultMutableTreeNode)emu.nextElement();
                                NodeInfo obj = (NodeInfo)emu_node.getUserObject();
                                if (obj.getNodeType() == NODETYPE_SYMBOL)//skip others
                                    list.add(emu_node.toString());
                            }

                            //create watch list, set main model
                            MainModel.getInstance().setWatchListModel(new WatchListModel(list, node.toString()));
                            Props.IndustryChange.setChanged();
                            break;

                        case NODETYPE_SECTOR://click on sector node
                            break;

                        case NODETYPE_SYMBOL://send message to chart frame, also pop up form of fundamentals
                            try {
                                MarketInfo mki = MarketUtil.calcMarketInfo(node.toString(),
                                        FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
                                Props.MarketInfoChange.setValue(mki);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            break;
                    }
                }
            }
        });
    }

    //Remove all nodes except the root node.
    public void clear() {
        _Root.removeAllChildren();
        _TreeModel.reload();
    }

    //Add child to the currently selected node
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath();
        if (parentPath == null)
            parentNode = _Root;
        else
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        return addObject(parentNode, child, true);
    }
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
        return addObject(parent, child, false);
    }
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
        if (parent == null)
            parent = _Root;

        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        _TreeModel.insertNodeInto(childNode, parent, parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible)
            scrollPathToVisible(new TreePath(childNode.getPath()));
        _nNodeCounter++;
        return childNode;
    }

    void showNode(String symbol) {
        TreePath node_path = findNode((DefaultMutableTreeNode) _TreeModel.getRoot(), symbol);
        setSelectionPath(node_path);
        scrollPathToVisible(node_path);
    }

    //----- private method -----
    private TreePath findSectorNode(String sector) {
        Enumeration em = _Root.depthFirstEnumeration();
        while(em.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)em.nextElement();
            if (node.toString().equals(sector))
                return new TreePath(node.getPath());
        }
        return null;
    }
    private TreePath findIndustryNode(String sector, String industry) {
        Enumeration em = _Root.depthFirstEnumeration();
        while(em.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)em.nextElement();
            if (node.toString().equals(industry)) {
                if (node.getParent().toString().equals(sector))
                    return new TreePath(node.getPath());
            }
        }
        return null;
    }
    private TreePath findNode(DefaultMutableTreeNode root, String s) {
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(s))
                return new TreePath(node.getPath());
        }
        return null;
    }

    //----- variables -----
    private DefaultTreeModel _TreeModel;
    private DefaultMutableTreeNode _Root;
    private int _nNodeCounter;

    //----- literals -----
    public static final int NODETYPE_SECTOR = 0;
    public static final int NODETYPE_INDUSTRY = 1;
    public static final int NODETYPE_SYMBOL = 2;
}