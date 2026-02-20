
package javands.ui;

import javax.swing.*;

import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.MassChain;
import javands.main.Run;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;

public class BaseMoreSettingFrame extends javax.swing.JFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected EnsdfWrap curEnsdf; //the ensdf we're working with
	protected MassChain data;
	protected Run run;
	protected TableControl tc;
    
	protected String type="";//"LEVEL","GAMMA","DECAY","DELAY" set in subclass
    
	protected TableControlPanel parentPanel;
    
	protected HashMap<String,JPopupMenu> popupMenus;
	protected HashMap<String,JButton> brButtons;
	protected HashMap<String,JTextField> brTextFields;
	protected HashMap<String,JCheckBox>  brCheckBoxes;
	
	protected boolean isBreakPointReset;
    
    public BaseMoreSettingFrame(MassChain mass,EnsdfWrap curEnsdf,javands.main.Run r) {
        run=r;
        this.curEnsdf=curEnsdf;
        data=mass;
        isBreakPointReset=false;
        
        popupMenus=new HashMap<String,JPopupMenu>();
        brButtons=new HashMap<String,JButton>();
        brTextFields  =new  HashMap<String,JTextField>();
        brCheckBoxes=new  HashMap<String,JCheckBox>();
        
		initComponents();
		
        if(curEnsdf!=null){
        	tc=curEnsdf.etd.getLevelTableControl().clone();
        	displayTableControl();
        }

    }
    

	
	protected void displayTableControl(){};
	protected void updateTableControl(){};
	
	protected void displayBreakPoints(){
		int ibreak=-1;
		ensdfparser.nds.control.Break br;
		String text,key;
		
		if(tc.getBreaks().size()>15){
			JOptionPane.showMessageDialog(this, "Warnings: Too many break points (maximum=15)! This happens very rarely! Please use control file instead.");
			return;
		}
		
		int size=tc.getBreaks().size();
		String[] lbr=new String[size],gbr=new String[size];//level and gamma breakk points
		
		int n=-1;
		for(int i=0;i<size;i++){
			lbr[i]="";
			gbr[i]="";
			
			br=tc.getBreaks().elementAt(i);
			lbr[i]=br.pos;
			gbr[i]=String.valueOf(br.gammaPos);
			
			n=lbr[i].indexOf(".");
			if(n>=0 && (n+4)<=lbr[i].length())
				lbr[i]=lbr[i].substring(0, n+4);
			
			n=gbr[i].indexOf(".");
			if(n>=0 && (n+4)<=gbr[i].length())
				gbr[i]=gbr[i].substring(0, n+4);
		}
		
		if(type.toUpperCase().equals("GAMMA") && tc.isSortGammas()){//if gamma is sorted, br.pos is from gamma energy
			for(int i=0;i<size;i++){
				gbr[i]=lbr[i];
				lbr[i]="";
			}
		}	
			
		for(int i=0;i<size;i++){
			br=tc.getBreaks().elementAt(i);
			text=lbr[i];
			
			ibreak=i+1;
			key=String.valueOf(ibreak);
			brTextFields.get(key).setText(text);
						
			brCheckBoxes.get(key).setEnabled(true);
			setSelected(brCheckBoxes.get(key),br.isNewPage);
						
			//System.out.println(" nbreaks="+tc.getBreaks()+" i="+i+" key="+key+" text="+text);
			
			//for(java.util.Map.Entry<String,JButton> e: brButtons.entrySet()){
			//   System.out.println("In BaseMoreSettingPanel line 91: brButtons="+e.getKey()+" "+e.getValue().getText());
			//}
			
			getBreakPointButton(ibreak,"S").setEnabled(true);
			getBreakPointButton(ibreak,"R").setEnabled(true);	
			

			
			//for gamma table only, key for gamma break pos =ibreak+15			
			if(type.toUpperCase().equals("GAMMA")){
				key=String.valueOf(ibreak+15);
				text=gbr[i];
				brTextFields.get(key).setText(text);
			}
			
		}

		
	}
	
	public void setENSDF(EnsdfWrap ew){
		curEnsdf=ew;
	}
	
	public void setParentPanel(TableControlPanel p){
		parentPanel=p;
	}
	
	
	public void setTableControl(TableControl tableControl){
    	
		tc=tableControl.clone();
		displayTableControl();
		
		//debug
    	//System.out.println("In Dialog setTC curEnsdf TC: "+" size="+curEnsdf.etd.getLevelTableControl().getBreaks().size());
    	//System.out.println("              tablePanel TC: "+" size="+tableControl.getBreaks().size());
    	//System.out.println("                  dialog TC: "+" size="+tc.getBreaks().size());		
	}
	
	
    protected void applyTableControl(String name){
	    if(curEnsdf==null){
	    	JOptionPane.showMessageDialog(this,"You haven't selected a dataset yet.");
	    	return;
	    }
	         
	    updateTableControl();
	    
		parentPanel.setTableControl(name, tc);		
    }
    	
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
    	if(tc.isDrawn())
    		columns=tc.getDefaultColumns();
    	
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
    
	//enable or disable columns shown in table based on defaultColumns
    protected void setEnabledAndSelected(AbstractButton ckb,TableControl tc,String columnName){
		
    	if(tc==null){
    		ckb.setSelected(false);
    		ckb.setEnabled(false);
    		return;
    	}
   	   
    	Vector<String> columns=null;   	
    	if(tc.isDrawn())
    		columns=tc.getDefaultColumns();
    	
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
	
	
	protected JPopupMenu getPopupMenu(int ibreak,String option){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in getPopupMenu: ibreak must be >=1");
			System.exit(0);
		}
		
		String key=option+ibreak;
		JPopupMenu popupMenu=popupMenus.get(key);
				
		return popupMenu;
	}
	
	protected JTextField getBreakPointTextField(int n){
		//index of break point=n-1  (=n-15-1 for gamma pos field)
		
		int index=n-1;
		if(index<0){
			System.out.println("Error in getBreakPointTextField: ibreak must be >=1");
			System.exit(0);
		}
		
		String key=""+n;
		return (JTextField) brTextFields.get(key);
	}
	
	protected JCheckBox getBreakPointCheckBox(int ibreak){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in getBreakPointCheckBox: ibreak must be >=1");
			System.exit(0);
		}
		
		String key=""+ibreak;
		return (JCheckBox) brCheckBoxes.get(key);
	}
	
	protected JButton getBreakPointButton(int ibreak,String option){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in getBreakPointButton: ibreak must be >=1");
			System.exit(0);
		}
		
		String key=option+ibreak;
		return (JButton) brButtons.get(key);
	}
	

    protected JTextField makeBreakPointTextField(int n){
        //gamma pos of a break point is stored at n=(ibreak + 15)
    	
		//index of break point=n-1; (it is (n-15)-1 for gamma pos of a break point)
		int index=n-1;
		if(index<0){
			System.out.println("Error in makeBreakPointTextFieldn: ibreak must be >=1");
			System.exit(0);
		}
		
		final JTextField textField=new JTextField();
		
		setBreakPointTextField(textField,n);
        
		return textField;
	}
	
	protected void setBreakPointTextField(JTextField textField, int n){
        //gamma pos of a break point is stored at ibreak=(#breakpoint + 15)
		
		
		//index of break point=ibreak-1;
		int index=n-1;
		if(index<0){
			System.out.println("Error in setBreakPointTextField: ibreak must be >=1");
			System.exit(0);
		}
		
		String name="brTextField_"+n;
		textField.setName(name);

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                String text,name;
                int n=0;
                int ibreak;
                int index=-1;
                float prevBreakPoint=0;

        		JTextField tf;
        		tf=(JTextField)evt.getSource();
                try{               	
                	text=tf.getText().trim();
                	name=tf.getName().replace("brTextField_", "").trim();
                	
            		
                	n=Integer.parseInt(name);
                	ibreak=n;
                	if(ibreak>15) ibreak=ibreak -15; //gamma pos field
                	index=ibreak-1;
                	int nbreaks=tc.getBreaks().size();
                                       		
                	float breakPoint=-1f;;
                	if(text.length()>0){              		
                		breakPoint=Float.parseFloat(text);
                	}
                	else{   
                		if(index<nbreaks-1){
                			
                			//is gamma is sorted, b.pos is from gamma energy and b.gammasPos=0
                			String pos=tc.getBreaks().elementAt(index).pos;
                			if(type.toUpperCase().equals("GAMMA") && tc.isSortGammas())
                				pos="";
                			
                			if(n>15){ 
                				pos=String.valueOf(tc.getBreaks().elementAt(index).gammaPos);
                				if(tc.isSortGammas())
                					pos=tc.getBreaks().elementAt(index).pos;
                			}
                			
                			if(!tc.isSortGammas() || n>15)
                				JOptionPane.showMessageDialog(tf, "Error: can't remove break point No."+ibreak+"!Next break point is not empty!");
                			
                			tf.setText(pos);
                			return;
                		}else if(index==nbreaks-1){//remove last break point
                			tc.getBreaks().remove(index);
                		}
                		
                		//debug
                		//System.out.println("In MoreSettingBaseFrame: focuLost: ibreak="+ibreak+" "+brCheckBoxes.size());
                		
                        getBreakPointCheckBox(ibreak).setEnabled(false);
                        getBreakPointButton(ibreak,"S").setEnabled(false);
                        getBreakPointButton(ibreak,"R").setEnabled(false);
                        return;
                	}
                	          
              	
                	ensdfparser.nds.control.Break br;
                    if(index>nbreaks){
                      	if(text.length()>0) 
                      		JOptionPane.showMessageDialog(tf, "Error: previous break point is empty!");
                       	return;	
                    }
                    else if(index==nbreaks){//adding new break point                        	
                        br=new ensdfparser.nds.control.Break();
                        br.panelColumns=new Vector<String>();
                        br.panelColumns.addAll(tc.getDefaultColumns());
                    }
                    else
                    	br=tc.getBreaks().elementAt(index);                  
                   
                    
                    if(index>0){
                		try{
                    	    prevBreakPoint=Float.parseFloat(tc.getBreaks().elementAt(index-1).pos);
                		}catch (NumberFormatException e){
                        	prevBreakPoint=0;
                        }
                    }
                    
                	if(breakPoint<0){
                		JOptionPane.showMessageDialog(tf, "Error: input value of break point is negative!");
                		return;
                	}else if(breakPoint==0){
                		JOptionPane.showMessageDialog(tf, "Error: input value of break point is zero!");
                		return;
                	}else if(breakPoint<=prevBreakPoint){
                		JOptionPane.showMessageDialog(tf, "Error: input value of break point is not greater than previous break point!");
                		return;
                	}
                	
                    if(n<=15){//level break points                   
                    	if(!type.toUpperCase().equals("GAMMA") || !tc.isSortGammas())
                    	 	br.pos=String.valueOf(breakPoint);                     	
                    }
                    else{//gamma break point
                        if(!tc.isSortGammas())
                        	br.gammaPos=breakPoint;
                        else
                        	br.pos=String.valueOf(breakPoint);//is gamma is sorted, br.pos is from gamma energy
                    }
                    
                	if(index<nbreaks)
                    	tc.getBreaks().remove(index);
                	
                	tc.getBreaks().add(index, br);
                	
                	isBreakPointReset=true;
                	
                    getBreakPointCheckBox(ibreak).setEnabled(true);
                    getBreakPointButton(ibreak,"S").setEnabled(true);
                    getBreakPointButton(ibreak,"R").setEnabled(true);
                    
                }catch (NumberFormatException e){
                	JOptionPane.showMessageDialog(tf, "Error: input value of break point has wrong format!");              
                }
            }
        });
        
        String key=String.valueOf(n);
        brTextFields.put(key, textField);
		
	}
	
	protected JCheckBox makeBreakPointCheckBox(int ibreak){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in makeBreakPointCheckBox: ibreak must be >=1");
			System.exit(0);
		}	
		
        final JCheckBox checkBox=new JCheckBox();
		setBreakPointCheckBox(checkBox,ibreak);
		
		return checkBox;
		
	}
	
	protected void setBreakPointCheckBox(JCheckBox checkBox,int ibreak){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in setBreakPointCheckBox: ibreak must be >=1");
			System.exit(0);
		}
		

        String name="brCheckBox_"+ibreak;
        checkBox.setName(name);
        checkBox.setEnabled(false);
        
        //NOTE: ChnageListener gets events on mouse over as focus gained/lost represents 
        //a change to the state of the component. So use ItemListener instead
        //checkBox.addChangeListener(new ChangeListener() {
        //	public void stateChanged(ChangeEvent e) {
        
        checkBox.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e){
                JCheckBox checkBox=(JCheckBox)e.getSource();
            	String name=checkBox.getName().replace("brCheckBox_", "").trim();
            	int ibreak=Integer.parseInt(name);
            	int index=ibreak-1;
            	
                if(index>=tc.getBreaks().size()){
              		JOptionPane.showMessageDialog(checkBox, "Error: null break point!");
                   	return;	
                }
                
                if(checkBox.isSelected())
                	tc.getBreaks().elementAt(index).isNewPage=true;
                else
                	tc.getBreaks().elementAt(index).isNewPage=false;
        	}
        }); 
        
        String key=String.valueOf(ibreak);
        brCheckBoxes.put(key, checkBox);
	}
	
	
	//make "S" and "R" button
	//option must be "S" or "R"
	//WindowBuilder problem: for some reaction I don't know, WindowBuilder can not display the UI 
	//when using this method to create button. It seems that only the return object from the first
	//call is accepted in WindowBuilder and repeated calls return the "same" object variable recognized
	//by WindowBuilder. However, the program runs ok using this call and it is just the problem with
	//WindowBuilder. In order to still take advantage of UI design in WindowBuilder, all objects are
	//created in place in corresponding SettingDialog but properties are set using method in BaseDialog.
	protected JButton makeBreakPointnButton(int ibreak,String option){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in makeBreakPointButton: ibreak must be >=1");
			System.exit(0);
		}
		
		final JButton button=new JButton();
		
		setBreakPointButton(button,ibreak,option);
        
        
        return button;
	}
	
	protected void setBreakPointButton(JButton button,int ibreak,String option){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in setBreakPointCheckBox: ibreak must be >=1");
			System.exit(0);
		}
		
		button.setText(option);
		button.setName("brButton_"+option+ibreak);
		button.setEnabled(false);
		
		button.setMargin(new Insets(2, 5, 2, 4));
        button.addMouseListener(new MouseAdapter(){
	    	public void mouseClicked(MouseEvent e){
	    		JButton btn=(JButton)e.getSource();
	    		if(!btn.isEnabled())
	    			return;
	    		
	    		String name=btn.getName().replace("brButton_", "").trim();
	    		String option=name.substring(0, 1);
	    		int ibreak=Integer.parseInt(name.substring(1));
	    		
	    		
	    		JPopupMenu popupMenu=getPopupMenu(ibreak,option);	

	    		
	    		for(Component c : popupMenu.getComponents()){
	    			JCheckBoxMenuItem item=(JCheckBoxMenuItem)c;
	    			setEnabled(item,tc,item.getName());
	    			
	    			if(option.equals("R")){
		    			Vector<String> sups=null;
		    			if(tc.getBreaks().size()>0) 
		    				sups=tc.getBreaks().elementAt(ibreak-1).sups;

		    			
	    				setEnabled(item,sups,item.getName());
	    			}

	    		}
	    		
	    		popupMenu.show(e.getComponent(),e.getX(),e.getY());
	    	}
	    });
        
        JPopupMenu popupMenu=makePopupMenu(ibreak,option);
        
        String key=option+ibreak;
        popupMenus.put(key, popupMenu);
        brButtons.put(key, button);
        
	}
	
	//option must be "S" for suppress or "R" for restore
	//type="LEVEL","GAMMA","DECAY","DELAY" for different subclass setting dialogs
	protected JPopupMenu makePopupMenu(int ibreak,String option){
		//index of break point=ibreak-1;
		int index=ibreak-1;
		if(index<0){
			System.out.println("Error in makePopupMenu: ibreak must be >=1");
			System.exit(0);
		}
		
		final JPopupMenu popupMenu=new JPopupMenu();
		if(option!="S" && option!="R"){
			System.out.println("Error: wrong option in createPopupMeanu in BaseDialog");
			System.exit(0);
		}
		
        String name=option+ibreak;
        if(type.toUpperCase().equals("LEVEL")){
    		popupMenu.setName(name);
    		popupMenu.add(makeCheckBoxMenuItem("E"));
    		popupMenu.add(makeCheckBoxMenuItem("J"));
    		popupMenu.add(makeCheckBoxMenuItem("T"));
    		popupMenu.add(makeCheckBoxMenuItem("L"));
    		popupMenu.add(makeCheckBoxMenuItem("S"));
    		popupMenu.add(makeCheckBoxMenuItem("XREF"));
    		popupMenu.add(makeCheckBoxMenuItem("BND","Band"));
    		popupMenu.add(makeCheckBoxMenuItem("COMM","Comment"));       	
        }else if(type.toUpperCase().equals("GAMMA")){
    		popupMenu.setName(name);
    		popupMenu.add(makeCheckBoxMenuItem("E"));
    		popupMenu.add(makeCheckBoxMenuItem("RI"));
    		popupMenu.add(makeCheckBoxMenuItem("LEV","EI"));
    		popupMenu.add(makeCheckBoxMenuItem("JI"));
    		popupMenu.add(makeCheckBoxMenuItem("LEVF","EF"));
    		popupMenu.add(makeCheckBoxMenuItem("JF"));
    		popupMenu.add(makeCheckBoxMenuItem("M","MUL"));
      		popupMenu.add(makeCheckBoxMenuItem("CC"));
      		popupMenu.add(makeCheckBoxMenuItem("TI"));
    		popupMenu.add(makeCheckBoxMenuItem("COMM","Comment")); 
        }else if(type.toUpperCase().equals("DECAY")){
    		popupMenu.setName(name);
    		popupMenu.add(makeCheckBoxMenuItem("E"));
    		popupMenu.add(makeCheckBoxMenuItem("LEV"));
    		popupMenu.add(makeCheckBoxMenuItem("IB"));
    		popupMenu.add(makeCheckBoxMenuItem("IE"));
    		popupMenu.add(makeCheckBoxMenuItem("IA"));
    		popupMenu.add(makeCheckBoxMenuItem("IP"));
      		popupMenu.add(makeCheckBoxMenuItem("LOGFT"));
      		popupMenu.add(makeCheckBoxMenuItem("HF"));
    		popupMenu.add(makeCheckBoxMenuItem("COMM","Comment")); 
        }else if(type.toUpperCase().equals("DELAY")){
    		popupMenu.setName(name);
    		popupMenu.add(makeCheckBoxMenuItem("E","EP"));
    		popupMenu.add(makeCheckBoxMenuItem("RI","IP"));
    		popupMenu.add(makeCheckBoxMenuItem("LEV","ED"));
    		popupMenu.add(makeCheckBoxMenuItem("EI"));
    		popupMenu.add(makeCheckBoxMenuItem("COMM","Comment")); 
        }else{
    		popupMenu.setName(name);
    		popupMenu.add(makeCheckBoxMenuItem("E"));
    		System.out.println("Error: type is empty!should be LEVEL/GAMMA/DECAY/DELAY.");
        }

	        
		int nrow=10;
		int ncol=1;
		nrow=popupMenu.getComponentCount()/ncol;
		popupMenu.setLayout(new GridLayout(nrow,ncol));
		
		return popupMenu;
   }


	protected JCheckBoxMenuItem makeCheckBoxMenuItem(String name){	
		return makeCheckBoxMenuItem(name,name);
	}
	
	protected JCheckBoxMenuItem makeCheckBoxMenuItem(String name,String text){	
		final JCheckBoxMenuItem item=new JCheckBoxMenuItem(text);
		item.setName(name);
		item.setBorder(BorderFactory.createLoweredBevelBorder());
        item.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent evt) {
        		popupItemStateChanged(evt);
            }
        });
        //item.addMouseListener(new PopupMenuMouseListener());
        return item;
	}

	
	protected void popupItemStateChanged(ItemEvent event){
		 JCheckBoxMenuItem item=(JCheckBoxMenuItem)(event.getSource());
		 JPopupMenu popupMenu=(JPopupMenu)(item.getParent());
		 			 
		 popupMenu.setVisible(true);
		 
		 String itemName=item.getName().trim();
		 String menuName=item.getParent().getName().trim();//menuName must be "S" or "R"+ibreak (index of a break point)
         String option="";
         int ibreak=-1;
         int index=-1;
         try{
        	 option=menuName.substring(0,1);
        	
        	 ibreak=Integer.parseInt(menuName.substring(1));
      		
        	 //index of break point=ibreak-1;
      		
        	 index=ibreak-1;     	    
        	 if(index<0){     		
        		 System.out.println("Error: something wrong with menu name: "+menuName+"! ibreak must be >=1");      			
        		 System.exit(0);      	
        	 }
     		
         }catch (NumberFormatException e){
        	 System.out.println("Error: something wrong with menu name: "+menuName+"! It should be S or R + a number.");
         }
         
         int nbreaks=tc.getBreaks().size();
         if(index>=nbreaks){
        	 System.out.println("Error: something wrong (index>nbreaks) in popupItemStateChanged.");
        	 return;
         }
         
         ensdfparser.nds.control.Break br=tc.getBreaks().elementAt(index);
         
		 if(item.isSelected()){
			 if(option.equals("S")){	
				 if(br.sups==null)
					 br.sups=new Vector<String>();				 
				 if(!br.sups.contains(itemName))
					 br.sups.add(itemName);
				 if(br.restore!=null && br.restore.contains(itemName))
					 br.restore.remove(itemName);
			 }else if(option.equals("R")){
				 if(br.restore==null)
					 br.restore=new Vector<String>();
				 if(!br.restore.contains(itemName))
					 br.restore.add(itemName);
				 if(br.sups!=null && br.sups.contains(itemName))
					 br.sups.remove(itemName);
			 }
		 }else{
			 if(option.equals("S")){	
				 if(br.sups.contains(itemName))
					 br.sups.remove(itemName);
			 }else if(option.equals("R")){
				 if(br.restore.contains(itemName))
					 br.restore.remove(itemName);
			 }
		 }

	}
	

    protected void OKActionPerformed(java.awt.event.ActionEvent evt) {
        applyTableControl(type);
        NDSControl.isBreakPointReset=isBreakPointReset;
        isBreakPointReset=false;
        
        this.dispose();
    }

    protected void cancelActionPerformed(java.awt.event.ActionEvent evt){
    	this.dispose();
    }
    
    protected void formWindowClosing(java.awt.event.WindowEvent evt) {
    	//JOptionPane.showMessageDialog(this,"You did not hit OK, no change will be made.");
    	
        this.dispose();
    }
    
    protected void initComponents() {}
    
}
