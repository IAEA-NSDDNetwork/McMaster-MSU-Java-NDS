package javands.ui;


import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ensdfparser.nds.control.TableControl;

public class BaseSettingPanel extends JPanel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    public BaseSettingPanel(){};
	
	//select or de-select other settings
    protected void setSelected(AbstractButton ckb,boolean choice){
	
        if(choice)
        	ckb.setSelected(true);
        else
        	ckb.setSelected(false);
        
    }

    //!!!!NOTE: This causes trouble!
    protected void setSelected_wrong(AbstractButton ckb,boolean choice){
    	ckb.setSelected(false);//this files an unnecessary stateChanged event
        if(choice)
        	ckb.setSelected(true);
    }
    
    protected void setSelected(AbstractButton ckb,TableControl tc,String columnName){
    	if(tc==null){
        	ckb.setSelected(false);
    		return;
    	}
    	
    	Vector<String> columns=null;   	
    	if(tc.isDrawn()){
    		int panelNo=tc.getCurrentPanelNo();
    		if(panelNo>=0)
    			columns=tc.getPanelColumns(panelNo);
    		else
    			columns=tc.getDefaultColumns();
    	}
    	
        setSelected(ckb,columns,columnName);  
    }
    
    protected void setSelected(AbstractButton ckb,Vector<String> columns,String columnName){
    	if(columns==null){
        	ckb.setSelected(false);
    		return;
    	}

    	
        if(columns.contains(columnName))
        	ckb.setSelected(true);
        else
        	ckb.setSelected(false);
    }
        
    protected void setEnabledAndSelected(AbstractButton ckb,boolean choice){
    	
        if(choice){
        	ckb.setSelected(true);
        	ckb.setEnabled(true);
        }
        else{
        	ckb.setSelected(false);
        	ckb.setEnabled(false);
        }
        
    }
    
    protected void setEnabledAndSelected(AbstractButton ckb,boolean isEnabled,boolean isSelected){
    	
        if(isEnabled)
        	ckb.setEnabled(true);
        
        else
        	ckb.setEnabled(false);
   
        if(isSelected)
        	ckb.setSelected(true);
        
        else
        	ckb.setSelected(false);
        
        
    }
    
	//enable or disable columns shown in table based on defaultColumns
    protected void setEnabledAndSelected(AbstractButton ckb,TableControl tc,String columnName){
		
    	if(tc==null){
    		ckb.setSelected(false);
    		ckb.setEnabled(false);
    		return;
    	}
   	   
    	Vector<String> columns=null;   	
    	if(tc.isDrawn()){
    		int panelNo=tc.getCurrentPanelNo();
    		if(panelNo>=0)
    			columns=tc.getPanelColumns(panelNo);
    		else
    			columns=tc.getDefaultColumns();
    	}
    	
    	setEnabledAndSelected(ckb,columns,columnName);

    }
    
    protected void setEnabledAndSelected(AbstractButton ckb,Vector<String> columns,String columnName){

    	if(columns==null){
    		ckb.setSelected(false);
    		ckb.setEnabled(false);
    		return;
    	}
    	
        if(columns.contains(columnName)){
        	ckb.setEnabled(true);
        	ckb.setSelected(true);
        }
        else{
        	ckb.setEnabled(false);
        	ckb.setSelected(false);
        }
        	
    }
    
    protected void setEnabled(JComponent ckb,boolean choice){
    	
    	if(choice)
    		ckb.setEnabled(true);
    	else
    		ckb.setEnabled(false);
        	
    }
    
    
    protected void setEnabled(JComponent ckb,TableControl tc,String columnName){
    	
    	if(tc==null){
    		ckb.setEnabled(false);
    		return;
    	}
    	
        setEnabled(ckb,tc.getDefaultColumns(),columnName);      	
    }
    
    protected void setEnabled(JComponent ckb,Vector<String> columns,String columnName){
    	
    	if(columns==null){
    		ckb.setEnabled(false);
    		return;
    	}

    	
        if(columns.contains(columnName))
        	ckb.setEnabled(true);   
        else
        	ckb.setEnabled(false);
    }
    
	protected void addColumn(Vector<String> columns,String columnName){
		if(columns==null || columnName==null || columnName.length()==0)
			return;
		
		if(columns.contains(columnName))
			return;
		
		columns.add(columnName);		
	}
	
	protected void addColumn(TableControl tc,String columnName){
		if(tc==null || columnName==null || columnName.length()==0)
			return;
		
		int panelNo=tc.getCurrentPanelNo();
		Vector<String> columns=null;
		if(panelNo>=0)
			columns=tc.getPanelColumns(panelNo);
		else
			columns=tc.getDefaultColumns();
		
		if(!columns.contains(columnName))
			columns.add(columnName);	
	
	}
		
	protected void addColumn(TableControl tc,String valColumnName,String uncColumnName){
		if(tc==null || valColumnName==null || valColumnName.length()==0)
			return;
		
		addColumn(tc,valColumnName);
		addColumn(tc,uncColumnName);	
	}
	
	protected void removeColumn(TableControl tc,String columnName){
		if(tc==null || columnName==null || columnName.length()==0)
			return;
		
		int panelNo=tc.getCurrentPanelNo();
		Vector<String> columns=null;
		if(panelNo>=0)
			columns=tc.getPanelColumns(panelNo);
		else
			columns=tc.getDefaultColumns();
		
		if(columns.contains(columnName))
			columns.remove(columnName);
			
	}
	
	protected void removeColumn(TableControl tc,String valColumnName,String uncColumnName){
		if(tc==null || valColumnName==null || valColumnName.length()==0)
			return;
		
		removeColumn(tc,valColumnName);
		removeColumn(tc,uncColumnName);	
	}
}
