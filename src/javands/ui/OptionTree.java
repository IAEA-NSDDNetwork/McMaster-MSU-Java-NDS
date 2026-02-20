package javands.ui;

import javax.swing.tree.*;

import javax.swing.event.*;

/**
 *A tree used by the interface to allow the user to select which file to work on
 * @author Roy Zywina
 */
public class OptionTree implements TreeModel{
    final static String head = "Mass Chain";
    final static String msec = "All Nucleii";
    //MassChain mass;
    EnsdfWrap[] ew;
    
    /** Creates a new instance of OptionTree */
    public OptionTree() {
        //mass=null;
    }
    public void reset(EnsdfWrap[] ews){
        ew = ews;
        
    }
    /*public void reset(MassChain mc){
        mass = mc;
    }*/
    public void addTreeModelListener(TreeModelListener l){
        
    }
    /// Adds a listener for the TreeModelEvent posted after the tree changes.
    public Object getChild(Object parent, int index){
        if (head.equals(parent))
            return ew[index];
        return null;
    }
    /// Returns the child of parent at index index in the parent's child array.
    public int getChildCount(Object parent){
        if (!head.equals(parent))
            return 0;
        if (ew==null)
            return 0;
        return ew.length;
    }
    /// Returns the number of children of parent.
    public int getIndexOfChild(Object parent, Object child){
        for (int x=0; x<ew.length; x++){
            if (child.equals(ew[x]))
                return x;
        }
        return -1;
    }
    /// Returns the index of child in parent.
    public Object getRoot(){
        return head;
    }
    /// Returns the root of the tree.
    public boolean isLeaf(Object node){
        if (head.equals(node))
            return false;
        return true;
    }
    /// Returns true if node is a leaf.
    public void removeTreeModelListener(TreeModelListener l){
        
    }
    /// Removes a listener previously added with addTreeModelListener.
    public void valueForPathChanged(TreePath path, Object newValue) {
        
    }
}
