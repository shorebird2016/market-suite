package org.marketsuite.framework.model;

import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;

public class GroupStore {
    private static GroupStore _Instance;
    public static GroupStore getInstance() {
        if (_Instance == null)
            _Instance = new GroupStore();
        return _Instance;
    }
    private GroupStore() {
        loadGroups();
    }

    /**
     * Save group hash map into storage file
     * @param member_map a Hashmap of group information
     */
    public static void saveGroups(HashMap<String, ArrayList<String>> member_map) {
//        saveGroups(member_map, new File(FILE_NAME));
        if (member_map == null || member_map.size() == 0)
            return;
        FileOutputStream is = null;
        try {
        	is = new FileOutputStream(WATCHLIST_PREF_PATH);
            XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(is));
            enc.writeObject(member_map);
            enc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally{
        	try {
        		if(is != null){
        			is.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    public static void saveGroups(HashMap<String, ArrayList<String>> member_map, File file_name) {
        if (member_map == null || member_map.size() == 0)
            return;
        FileOutputStream is = null;
        try {
            is = new FileOutputStream(file_name);
            XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(is));
            enc.writeObject(member_map);
            enc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally{
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Read group information from serialized storage into hashmap
     * @return hashmap of string and array of strings, or null = no group or error
     */
    public static HashMap<String, ArrayList<String>> loadGroups() {
        FileInputStream is = null;
        try {
            is = new FileInputStream(WATCHLIST_PREF_PATH);
            XMLDecoder dec = new XMLDecoder(new BufferedInputStream(is));
            _mapMember = (HashMap<String, ArrayList<String>>) dec.readObject();
            dec.close();
            return _mapMember;
        } catch (FileNotFoundException ex1) {
            //ok not having this file
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally{
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void addGroup(String group_name, ArrayList<String> members) {
        //remove duplicate
        ArrayList<String> list = new ArrayList<>();
        for (String member : members)
            if (!list.contains(member))
                list.add(member);
        _mapMember.put(group_name, list);
        saveGroups(_mapMember);
        Props.GroupChange.setChanged();
    }
    public void removeGroup(String group_name) {
        _mapMember.remove(group_name);
        saveGroups(_mapMember);
        Props.GroupChange.setChanged();
    }
    //remove groups of a particular pattern
    public void removeGroups(String prefix) {
        ArrayList<String> to_delete = new ArrayList<>();
        Iterator<String> itor = _mapMember.keySet().iterator();
        while (itor.hasNext()) {
            String name = itor.next();
            if (name.startsWith(prefix))
                to_delete.add(name);
        }
        if (to_delete.size() == 0) return;
        for (String name : to_delete)
            _mapMember.remove(name);
        Props.WatchListsChange.setChanged();
    }
    //results are sorted for viewing
    public ArrayList<String> getGroup(String group_name) {
        ArrayList<String> names = _mapMember.get(group_name);
        Collections.sort(names);
        return names;
    }
    public void addMember(String group_name, String member_name) {
        _mapMember.get(group_name).add(member_name);
        saveGroups(_mapMember);
    }
    private void removeMember(String group_name, String member_name, boolean update_store) {
        _mapMember.get(group_name).remove(member_name);
        if (update_store)
            saveGroups(_mapMember);
    }
    public void removeMembers(String group_name, List<String> symbols) {
        ArrayList<String> members = _mapMember.get(group_name);
        for (Object sym : symbols)
            members.remove(sym);
        saveGroups(_mapMember);
    }
    public boolean isGroupExist(String group_name) {
        return !(_mapMember.get(group_name) == null);
    }
    public ArrayList<String> findGroupsByMember(String member) {
        ArrayList<String> ret = new ArrayList<>();
        Iterator<String> itor = _mapMember.keySet().iterator();
        while (itor.hasNext()) {
            String grp = itor.next();
            if (_mapMember.get(grp).contains(member))
                ret.add(grp);
        }
        return ret;
    }
    //results are sorted
    public ArrayList<String> getGroupNames() {
        Set<String> grp_names = _mapMember.keySet();
        ArrayList<String> grp_list = new ArrayList<>();
        for (String grp : grp_names)
            grp_list.add(grp);
        Collections.sort(grp_list);
        return grp_list;
    }
    //returns all the unique symbols from store
    public ArrayList<String> getUniqueStockSymbols() {
        ArrayList<String> all_symbols = new ArrayList<>();
        ArrayList<String> grp_names = GroupStore.getInstance().getGroupNames();
        HashMap<String,Fundamental> fm = MainModel.getInstance().getFundamentals();
        for (String grp_name : grp_names) {
            if (grp_name.startsWith("ETF")) continue;
            ArrayList<String> sym_names = GroupStore.getInstance().getGroup(grp_name);
            for (String sym : sym_names) {
                if (!all_symbols.contains(sym)) {//skip duplicate
                    if (fm.get(sym) != null && !fm.get(sym).isETF() || fm.get(sym) == null)
                        all_symbols.add(sym);
                    else
                        System.err.println("---ETF ---" + sym);
                }
                else
                    System.err.println("--DUP-- " + grp_name + " : " + sym);
            }
        }
        return all_symbols;
    }
    //remove specified symbols from all watch lists
    public void removeSymbols(List<String> symbols) {
        for (String member : symbols) {
            ArrayList<String> grp = findGroupsByMember(member);
            for (String g : grp)
                removeMember(g, member, false);
        }
        saveGroups(_mapMember);
    }
    //does watch list exist?
    public boolean doesGroupExist(String name) { return _mapMember.get(name) != null; }

    //----- variables -----
    private static HashMap<String, ArrayList<String>> _mapMember = new HashMap<>();
    public HashMap<String, ArrayList<String>> getGroups() { return _mapMember; }
    public ArrayList<String> getMembers(String group_name) { return _mapMember.get(group_name); }
//TODO move to constants ????
    //----- literals -----
    public static final String FILE_NAME = "apollo_watch_list.xml";
    public static final String WATCHLIST_PREF_PATH = System.getProperty("user.home") + File.separator + FILE_NAME;
}