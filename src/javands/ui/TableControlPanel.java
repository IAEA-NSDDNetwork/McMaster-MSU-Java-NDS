package javands.ui;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;

import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.UIManager;

import java.awt.Color;

import javax.swing.JSeparator;

import java.awt.SystemColor;

import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.util.Str;
import javands.main.Run;

import javax.swing.event.ChangeEvent;
import javax.swing.JComboBox;

public class TableControlPanel extends BaseSettingPanel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	EnsdfWrap curEnsdf; //the ensdf we're working with
    MassChain data;
    Run run;
    
    TableControl ltc,gtc,dtc,ptc;
    DrawingControl bdc;//band drawing control
    DrawingControl ddc;//decay/level drawing control
    
    MoreLevelSettingFrame levelSettingFrame;
    MoreGammaSettingFrame gammaSettingFrame;
    MoreDecaySettingFrame decaySettingFrame;
    MoreDelaySettingFrame delaySettingFrame;

    
    //settings in an EnsdfTableData object
    boolean isCombineLG, isNewPage,isCommentWidthReset;
    String altID="";
    int nXrefs=0;
    
    int currentLevelPanelNo=0;
    int currentGammaPanelNo=0;
    int currentDecayPanelNo=0;
    int currentDelayPanelNo=0;
    
    
	public TableControlPanel(MassChain mass,EnsdfWrap curEnsdf,javands.main.Run r) {
		checkBox_4.setToolTipText("");

        run=r;
        this.curEnsdf=curEnsdf;
        data=mass;
        
        isCommentWidthReset=false;
        
		initComponents();
		
        if(curEnsdf!=null){
        	ltc=curEnsdf.etd.getLevelTableControl().clone();
        	gtc=curEnsdf.etd.getGammaTableControl().clone();
        	dtc=curEnsdf.etd.getDecayTableControl().clone();
        	ptc=curEnsdf.etd.getDelayTableControl().clone();
        	bdc=curEnsdf.etd.getBandDrawingControl().clone();
        	ddc=curEnsdf.etd.getDecayDrawingControl().clone();   
        	displayTableControl();
        }
                
	}
	
	
	public void setENSDF(EnsdfWrap ew){
		
		curEnsdf=ew;
		
        if(curEnsdf!=null){        	
        	ltc=curEnsdf.etd.getLevelTableControl().clone();
        	gtc=curEnsdf.etd.getGammaTableControl().clone();
        	dtc=curEnsdf.etd.getDecayTableControl().clone();
        	ptc=curEnsdf.etd.getDelayTableControl().clone();
        	bdc=curEnsdf.etd.getBandDrawingControl().clone();
        	ddc=curEnsdf.etd.getDecayDrawingControl().clone();  
        	
        	displayTableControl();
        }               
	}
	
	//called in MoreLevelSettingPanel, MoreGammaSettingPanel, ...
    public void setTableControl(String name,TableControl tc){
    	if(name.toUpperCase().equals("LEVEL")){
    		ltc=tc.clone();
    	}
    	else if(name.toUpperCase().equals("GAMMA"))
    		gtc=tc.clone();
    	else if(name.toUpperCase().equals("DECAY"))
    		dtc=tc.clone();
    	else if(name.toUpperCase().equals("DELAY"))
    		ptc=tc.clone();    	
    }
	
	/*display control settings for selected data set in control panel**/
	@SuppressWarnings("unchecked")
    private void displayTableControl(){
				
    	
		DefaultComboBoxModel<String> levelModel=new DefaultComboBoxModel<String>();
		for(int i=0;i<=ltc.getBreakPoints().size();i++)
			levelModel.addElement("panel"+i);
		comboBoxLevel.setModel(levelModel);
		comboBoxLevel.setSelectedIndex(0);
		
		DefaultComboBoxModel<String> gammaModel=new DefaultComboBoxModel<String>();
		for(int i=0;i<=gtc.getBreakPoints().size();i++) 
			gammaModel.addElement("panel"+i);
		comboBoxGamma.setModel(gammaModel);
		comboBoxGamma.setSelectedIndex(0);
        
		DefaultComboBoxModel<String> decayModel=new DefaultComboBoxModel<String>();
		for(int i=0;i<=dtc.getBreakPoints().size();i++)
			decayModel.addElement("panel"+i);
		comboBoxDecay.setModel(decayModel);
		comboBoxDecay.setSelectedIndex(0);
		
		DefaultComboBoxModel<String> delayModel=new DefaultComboBoxModel<String>();
		for(int i=0;i<=ptc.getBreakPoints().size();i++)
			delayModel.addElement("panel"+i);
		comboBoxDelay.setModel(delayModel);
		comboBoxDelay.setSelectedIndex(0);
        
    	
		//level TableControl (column names must be consistent with those in LevelTableMaker)
		displayLevelTableControl();
		
		
		//gamma TableControl (column names must be consistent with those in GammaTableMaker)
        displayGammaTableControl();
    	
		//decay TableControl (column names must be consistent with those in DecayTableMaker)
        displayDecayTableControl();
		
		//delayed particle TableControl (column names must be consistent with those in DelayTableMaker)
        displayDelayTableControl();
		
		//other settings
		setEnabledAndSelected(chckbxCombineLAnd,curEnsdf.etd.isCombineLG());
		setEnabledAndSelected(chckbxNewpage_1,curEnsdf.etd.isNewPage());
		setEnabledAndSelected(chckbxBandDrawing,bdc.isDrawn());
		
		if(ddc.isDrawn()){
			chckbxIncludeDecaylevelDrawing.setSelected(true);
			setEnabledAndSelected(chckbxLandscape,!ddc.isPortrait());
			setEnabledAndSelected(chckbxNewpage,ddc.isNewPage());
						
        	chckbxLandscape.setEnabled(true);
        	chckbxNewpage.setEnabled(true);
        	
        	//not used, disable them for now
        	textField_4.setEnabled(false);
        	textField_5.setEnabled(false);
        	textField_8.setEnabled(false);		
        	
        	textField_4.setText(String.valueOf(ddc.getWidth()));
        	textField_5.setText(String.valueOf(ddc.getHeight()));
        	textField_8.setText(String.valueOf(ddc.getSpacing()));
		}
		else{
			chckbxIncludeDecaylevelDrawing.setSelected(false);
        	chckbxLandscape.setSelected(false);chckbxLandscape.setEnabled(false);
        	chckbxNewpage.setSelected(false);chckbxNewpage.setEnabled(false);
        	textField_4.setEnabled(false);
        	textField_5.setEnabled(false);
        	textField_8.setEnabled(false);
        }
		
		String altID=curEnsdf.etd.getAltID();
		if(altID.length()>0)
			textField_6.setText(altID);
		
		textField_7.setText(String.valueOf(curEnsdf.etd.getNXRefs()));
	}
	
	/*display control settings for selected data set in control panel**/
	//does not work properly and behaves weird
	//does not update
	@SuppressWarnings({ "unchecked", "unused" })
    private void displayTableControl_old(){
				
    	
		comboBoxLevel.removeAllItems();
		for(int i=0;i<=ltc.getBreakPoints().size();i++)
			comboBoxLevel.addItem("panel"+i);
		comboBoxLevel.setSelectedIndex(0);
				
		
		comboBoxGamma.removeAllItems();
		for(int i=0;i<=gtc.getBreakPoints().size();i++) 
			comboBoxGamma.addItem("panel"+i);
		
		comboBoxGamma.setSelectedIndex(0);
        
		
		comboBoxDecay.removeAllItems();
		for(int i=0;i<=dtc.getBreakPoints().size();i++)
			comboBoxDecay.addItem("panel"+i);
		comboBoxDecay.setSelectedIndex(0);
		
		comboBoxDelay.removeAllItems();
		for(int i=0;i<=ptc.getBreakPoints().size();i++)
			comboBoxDelay.addItem("panel"+i);	
		comboBoxDelay.setSelectedIndex(0);
    	
		
		//level TableControl (column names must be consistent with those in LevelTableMaker)
		displayLevelTableControl();
		
		
		//gamma TableControl (column names must be consistent with those in GammaTableMaker)
        displayGammaTableControl();
    	
		//decay TableControl (column names must be consistent with those in DecayTableMaker)
        displayDecayTableControl();
		
		//delayed particle TableControl (column names must be consistent with those in DelayTableMaker)
        displayDelayTableControl();
		
		//other settings
		setEnabledAndSelected(chckbxCombineLAnd,curEnsdf.etd.isCombineLG());
		setEnabledAndSelected(chckbxNewpage_1,curEnsdf.etd.isNewPage());
		setEnabledAndSelected(chckbxBandDrawing,bdc.isDrawn());
		
		if(ddc.isDrawn()){
			chckbxIncludeDecaylevelDrawing.setSelected(true);
			setEnabledAndSelected(chckbxLandscape,!ddc.isPortrait());
			setEnabledAndSelected(chckbxNewpage,ddc.isNewPage());
						
        	chckbxLandscape.setEnabled(true);
        	chckbxNewpage.setEnabled(true);
        	textField_4.setEnabled(true);
        	textField_5.setEnabled(true);
        	textField_8.setEnabled(true);		
        	
        	textField_4.setText(String.valueOf(ddc.getWidth()));
        	textField_5.setText(String.valueOf(ddc.getHeight()));
        	textField_8.setText(String.valueOf(ddc.getSpacing()));
		}
		else{
			chckbxIncludeDecaylevelDrawing.setSelected(false);
        	chckbxLandscape.setSelected(false);chckbxLandscape.setEnabled(false);
        	chckbxNewpage.setSelected(false);chckbxNewpage.setEnabled(false);
        	textField_4.setEnabled(false);
        	textField_5.setEnabled(false);
        	textField_8.setEnabled(false);
        }
		
		String altID=curEnsdf.etd.getAltID();
		if(altID.length()>0)
			textField_6.setText(altID);
		
		textField_7.setText(String.valueOf(curEnsdf.etd.getNXRefs()));
	}
	
	@SuppressWarnings("unused")
	private void displayTableControl(String type){
		if(type.toUpperCase().equals("L"))
			displayLevelTableControl();
		else if(type.toUpperCase().equals("G"))
			displayGammaTableControl();
		else if(type.toUpperCase().equals("D"))
			displayDecayTableControl();
		else if(type.toUpperCase().equals("P"))
			displayDelayTableControl();
		
	}
	
	private void displayTableControl(String type,int panel){
		if(type.toUpperCase().equals("L"))
			displayLevelTableControl(panel);
		else if(type.toUpperCase().equals("G"))
			displayGammaTableControl(panel);
		else if(type.toUpperCase().equals("D"))
			displayDecayTableControl(panel);
		else if(type.toUpperCase().equals("P"))
			displayDelayTableControl(panel);
		
	}
	
	private void displayLevelTableControl(){
		int panel=0;
    	String selected=(String) comboBoxLevel.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	
    	displayLevelTableControl(panel);
	}
	
	private void displayLevelTableControl(int panel){
		
    	ltc.setCurrentPanelNo(panel);
    	
		setEnabled(lblIncludeColumns,ltc.isDrawn());
		setEnabled(btnMoreLevelSetting,ltc.isDrawn());
		setEnabled(lblWidth,ltc.isDrawn());
		setEnabled(comboBoxLevel,ltc.isDrawn());
		
		setEnabledAndSelected(checkBox_EL,ltc,"E");
		setEnabledAndSelected(checkBox_JP,ltc,"J");
		setEnabledAndSelected(checkBox_T,ltc,"T");		
		setEnabledAndSelected(checkBox_L,ltc,"L");
		setEnabledAndSelected(checkBox_S,ltc,"S");
		setEnabledAndSelected(chckbxXref,ltc,"XREF");
		setEnabledAndSelected(chckbxBand,ltc,"BND");
		setEnabledAndSelected(checkBox_LCOM,ltc,"COMM");			
		setEnabledAndSelected(chckbxSmallTable,ltc.isDrawn(),ltc.isSmall()&&ltc.isDrawn());
		setEnabledAndSelected(chckbxNewPage,ltc.isDrawn(),ltc.isNewPage()&&ltc.isDrawn());	
		setEnabledAndSelected(chckbxLandscape_1,ltc.isDrawn(),!ltc.isPortrait()&&ltc.isDrawn());	
		setEnabledAndSelected(chckbxDrawTable_1,ltc.isDrawn());	
		
		//setEnabledAndSelected(chckbxShowUncertaintiesIn,ltc.isDrawn(),ltc.showDE()&&ltc.isDrawn());	
		setEnabledAndSelected(chckbxShowUncertaintiesIn,false,true);//for level table, always show uncertainty in level energy	
		if(checkBox_LCOM.isSelected()) {
			textFieldLevelComWidth.setEnabled(true);
			
			if(ltc.isNewCommentWidth())
				textFieldLevelComWidth.setText(String.format("%.2f",ltc.getCommentColumnWidth(panel)));
			else
				textFieldLevelComWidth.setText("");
		}else{
			textFieldLevelComWidth.setEnabled(false);
		}
	}

	private void displayGammaTableControl(){
		
		int panel=0;
    	String selected=(String) comboBoxGamma.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	
    	displayGammaTableControl(panel);
	}
	
	private void displayGammaTableControl(int panel){

    	gtc.setCurrentPanelNo(panel);
    	
		setEnabled(label,gtc.isDrawn());
		setEnabled(button,gtc.isDrawn());
		setEnabled(lblWidth_3,gtc.isDrawn());
		setEnabled(comboBoxGamma,gtc.isDrawn());
		
		setEnabledAndSelected(chckbxEg,gtc,"E");
		setEnabledAndSelected(checkBox_RI,gtc,"RI");
		setEnabledAndSelected(chckbxNewCheckBox,gtc,"LEV");//initial level		
		setEnabledAndSelected(chckbxJi,gtc,"JI");         
		setEnabledAndSelected(chckbxEf,gtc,"LEVF");//final level
		setEnabledAndSelected(chckbxJf,gtc,"JF");
		setEnabledAndSelected(checkBox_MUL,gtc,"M");//MUL
		setEnabledAndSelected(checkBox_MR,gtc,"MR");		
		setEnabledAndSelected(checkBox_CC,gtc,"CC");
		setEnabledAndSelected(checkBox_TI,gtc,"TI");
		setEnabledAndSelected(chckbxComment,gtc,"COMM");	
		setEnabledAndSelected(checkBox_6,gtc.isDrawn(),gtc.isSmall()&&gtc.isDrawn());
		setEnabledAndSelected(checkBox_7,gtc.isDrawn(),gtc.isNewPage()&&gtc.isDrawn());	
		setEnabledAndSelected(checkBox_1,gtc.isDrawn(),!gtc.isPortrait()&&gtc.isDrawn());	
		setEnabledAndSelected(chckbxShowUncertaintyIn,gtc.isDrawn(),gtc.showDE()&&gtc.isDrawn());
		setEnabledAndSelected(chckbxShowAllLevels,gtc.showAll()&&gtc.isDrawn());
		setEnabledAndSelected(chckbxDrawTable_2,gtc.isDrawn());	
		if(chckbxComment.isSelected()) {
			textFieldGammaComWidth.setEnabled(true);
			if(gtc.isNewCommentWidth())
				textFieldGammaComWidth.setText(String.format("%.2f",gtc.getCommentColumnWidth(panel)));
			else
				textFieldGammaComWidth.setText("");
		}else{
			textFieldGammaComWidth.setEnabled(false);
		}
	}
	
	private void displayDecayTableControl(){
		
		int panel=0;
    	String selected=(String) comboBoxDecay.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	
    	displayDecayTableControl(panel);
	}
	
	private void displayDecayTableControl(int panel){
		
    	dtc.setCurrentPanelNo(panel);
    	
		setEnabled(label_2,dtc.isDrawn());
		setEnabled(button_1,dtc.isDrawn());
		setEnabled(lblWidth_4,dtc.isDrawn());
		setEnabled(comboBoxDecay,dtc.isDrawn());
		
		setEnabledAndSelected(checkBox_ED,dtc,"E");
		setEnabledAndSelected(chckbxLev,dtc,"LEV");
		setEnabledAndSelected(checkBox_IB,dtc,"IB");	
		setEnabledAndSelected(checkBox_IE,dtc,"IE");         
		setEnabledAndSelected(checkBox_IA,dtc,"IA");
		setEnabledAndSelected(chckbxIp,dtc,"IP");
		setEnabledAndSelected(checkBox_LOGFT,dtc,"LOGFT");	
		setEnabledAndSelected(chckbxHf,dtc,"HF");		
		setEnabledAndSelected(checkBox_15,dtc,"COMM");	
		setEnabledAndSelected(checkBox_9,dtc.isDrawn(),dtc.isSmall()&&dtc.isDrawn());
		setEnabledAndSelected(checkBox_10,dtc.isDrawn(),dtc.isNewPage()&&dtc.isDrawn());	
		setEnabledAndSelected(checkBox_2,dtc.isDrawn(),!dtc.isPortrait()&&dtc.isDrawn());	
		setEnabledAndSelected(chckbxShowUncertaintyIn_2,dtc.isDrawn(),dtc.showDE()&&dtc.isDrawn());
		setEnabledAndSelected(chckbxDrawTable_3,dtc.isDrawn());	
		if(checkBox_15.isSelected()) {
			textFieldDecayComWidth.setEnabled(true);
			if(dtc.isNewCommentWidth())
				textFieldDecayComWidth.setText(String.format("%.2f",dtc.getCommentColumnWidth(panel)));
			else
				textFieldDecayComWidth.setText("");
			
		}else{
			textFieldDecayComWidth.setEnabled(false);
		}
	}
	
	
	private void displayDelayTableControl(){
		
		int panel=0;
    	String selected=(String) comboBoxDelay.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
        displayDelayTableControl(panel);
	}
	private void displayDelayTableControl(int panel){

    	ptc.setCurrentPanelNo(panel);
    	
		setEnabled(label_3,ptc.isDrawn());
		setEnabled(button_2,ptc.isDrawn());
		setEnabled(lblWidth_2,ptc.isDrawn());
		setEnabled(comboBoxDelay,ptc.isDrawn());
		
		
		setEnabledAndSelected(checkBox_EP,ptc,"E");
		setEnabledAndSelected(chckbxRi,ptc,"RI");
		setEnabledAndSelected(chckbxEi,ptc,"LEV");		
		setEnabledAndSelected(chckbxEi_1,ptc,"EI");
		setEnabledAndSelected(checkBox,ptc,"COMM");		
		setEnabledAndSelected(checkBox_12,ptc.isDrawn(),ptc.isSmall()&&ptc.isDrawn());
		setEnabledAndSelected(checkBox_13,ptc.isDrawn(),ptc.isNewPage()&&ptc.isDrawn());	
		setEnabledAndSelected(checkBox_3,ptc.isDrawn(),!ptc.isPortrait()&&ptc.isDrawn());	
		setEnabledAndSelected(chckbxShowUncertaintyIn_1,ptc.isDrawn(),ptc.showDE()&&ptc.isDrawn());		
		setEnabledAndSelected(chckbxDrawTable_4,ptc.isDrawn());	
		
		if(checkBox.isSelected()) {
			textFieldDelayComWidth.setEnabled(true);
			if(ptc.isNewCommentWidth())
				textFieldDelayComWidth.setText(String.format("%.2f",ptc.getCommentColumnWidth(panel)));
			else
				textFieldDelayComWidth.setText("");
		}else{
			textFieldDelayComWidth.setEnabled(false);
		}
	}
	
    /* apply control settings from control panel to TableControl objects of
     * selected dataset
     */
    private void applyTableControl(){
	    if(curEnsdf==null){
	    	JOptionPane.showMessageDialog(this,"You haven't selected a dataset yet.");
	    	return;
	    }
	    
        updateTableControl();
        
		curEnsdf.etd.setTableControl("LEVEL", ltc);
		curEnsdf.etd.setTableControl("GAMMA", gtc);
		curEnsdf.etd.setTableControl("DECAY", dtc);
		curEnsdf.etd.setTableControl("DELAY", ptc);
		curEnsdf.etd.setDrawingControl("BAND", bdc);
		curEnsdf.etd.setDrawingControl("DECAY", ddc);

        curEnsdf.etd.setCombineLG(isCombineLG);//combine L and G table
        curEnsdf.etd.setNewPage(isNewPage);   
        curEnsdf.etd.setAltID(altID);
        curEnsdf.etd.setNXrefs(nXrefs);
        
        //isModified=true only when table control settings have been modified.
        //isModified=false even if the header settings have been changed but no modifications are made for table settings.
		NDSControl.isModified=true;
		NDSControl.isTableControlModified=true;
		
    	
		NDSControl.isCommentWidthReset=isCommentWidthReset;
		isCommentWidthReset=false;
    }
    
	/*update TableControl objects based on inputs in control panel**/
    //NOTE: These objects are clones of the ones of selected data set
    //Setting here doesn't affect the TableControl objects associated
    //with the selected dataset (EnsdfTableData), untill these settings
    //are applied to them.
	private void updateTableControl(){
		String text="";
		
        //level TableControl
        updateLevelTableControl();
		 			
	
		//gamma TableControl 
        updateGammaTableControl();
		
		//decay TableControl (column names must be consistent with those in DecayTableMaker)
        updateDecayTableControl();
        
		//delayed particle TableControl (column names must be consistent with those in DelayTableMaker)
        updateDelayTableControl();
        
		
        //reset all break points if gamma table is re-ordered, since that will affect all previously-set break points.
		if(gtc.reorderGamma()){
			//reset break points if ordering is changed
			ltc.getBreakPoints().clear();
			ltc.getBreaks().clear();
			
			dtc.getBreakPoints().clear();
			dtc.getBreaks().clear();
			
			ptc.getBreakPoints().clear();
			ptc.getBreaks().clear();
			
			gtc.getBreakPoints().clear();
			gtc.getBreaks().clear();
			
		}
        
        //other settings        
        bdc.setDrawn(chckbxBandDrawing.isSelected());//show band drawing
        ddc.setDrawn(chckbxIncludeDecaylevelDrawing.isSelected());//show level/decay scheme
        if(chckbxIncludeDecaylevelDrawing.isSelected()){//show level/decay scheme
        	ddc.setPortrait(!chckbxLandscape.isSelected());
        	ddc.setNewPage(chckbxNewpage.isSelected()); 
        	
        	//debug
        	//System.out.println("In TableControlPanel Line 412: ddc.isportrait="+ddc.isPortrait());
        	
            text=textField_4.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text))
            		ddc.setWidth(Float.parseFloat(text));
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for decay-drawing width!");
            }
            text=textField_5.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text))
            		ddc.setHeight(Float.parseFloat(text));
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for decay-drawing height!");
            }
            text=textField_8.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text))
            		ddc.setSpacing(Float.parseFloat(text));
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for decay-drawing spacing!");
            }
        }
        
        isCombineLG=chckbxCombineLAnd.isSelected();//combine L and G table
        isNewPage=chckbxNewpage_1.isSelected();           
        altID=textField_6.getText().trim();
    
    	text=textField_7.getText().trim();
        if(text.length()>0){
        	if(Str.isNumeric(text))
        		nXrefs=Integer.parseInt(text);
        	else
        		JOptionPane.showMessageDialog(this, "Wrong input for the number of XREFs!");
        }

	}

	
	@SuppressWarnings("unused")
	private void updateTableControl(String type){
		if(type.toUpperCase().equals("L"))
			updateLevelTableControl();
		else if(type.toUpperCase().equals("G"))
			updateGammaTableControl();
		else if(type.toUpperCase().equals("D"))
			updateDecayTableControl();
		else if(type.toUpperCase().equals("P"))
			updateDelayTableControl();
		
	}
	
	private void updateTableControl(String type,int panel){
		if(type.toUpperCase().equals("L"))
			updateLevelTableControl(panel);
		else if(type.toUpperCase().equals("G"))
			updateGammaTableControl(panel);
		else if(type.toUpperCase().equals("D"))
			updateDecayTableControl(panel);
		else if(type.toUpperCase().equals("P"))
			updateDelayTableControl(panel);
		
	}
	
	private void updateLevelTableControl(){
		int panel=0;
    	String selected=(String) comboBoxLevel.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	updateLevelTableControl(panel);
	}
	
	private void updateLevelTableControl(int panel){
		ltc.setCurrentPanelNo(panel);
		ltc.getPanelColumns(panel).clear();
		
		if(checkBox_EL.isSelected())  addColumn(ltc,"E","DE");
		if(checkBox_JP.isSelected())  addColumn(ltc,"J");
		if(checkBox_T.isSelected())   addColumn(ltc,"T","DT");	
		if(checkBox_L.isSelected())   addColumn(ltc,"L");
		if(checkBox_S.isSelected())   addColumn(ltc,"S","DS");
		if(chckbxXref.isSelected())   addColumn(ltc,"XREF");
		if(chckbxBand.isSelected())   addColumn(ltc,"BND");
		if(checkBox_LCOM.isSelected())addColumn(ltc,"COMM");
		
		ltc.setIsSmall(chckbxSmallTable.isSelected());
		ltc.setNewPage(chckbxNewPage.isSelected());
		ltc.setShowDE(chckbxShowUncertaintiesIn.isSelected());
		ltc.setPortait(!chckbxLandscape_1.isSelected());
		ltc.setDrawn(chckbxDrawTable_1.isSelected());
		
        //text=textFieldLevelComWidth.getText().trim();
        //if(text.length()>0){
        //	if(Str.isNumeric(text))
        //		ltc.setCommentColumnWidth(Float.parseFloat(text));
        //	else
        //		JOptionPane.showMessageDialog(this, "Wrong input for comment column width of level table!");
        //}
	}

	
	private void updateGammaTableControl(){
		int panel=0;
    	String selected=(String) comboBoxGamma.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	
    	updateGammaTableControl(panel);
	}
	
	private void updateGammaTableControl(int panel){
        //for decay:  E,RI columns before initial level column
        //for others: E,RI columns after initial level columns
        
		gtc.setCurrentPanelNo(panel);
		gtc.getPanelColumns(panel).clear();
		
		if(chckbxEg.isSelected())         addColumn(gtc,"E","DE");
		if(checkBox_RI.isSelected())      addColumn(gtc,"RI","DRI");
		
		if(chckbxNewCheckBox.isSelected()){
			addColumn(gtc,"LEV","DLEV");//initial level		
		}
		if(chckbxJi.isSelected()){
			addColumn(gtc,"JI");  
		}
		
		if(!curEnsdf.ens.fullDSId().contains("DECAY")){
			if(gtc.getPanelColumns(panel).contains("E")){
				removeColumn(gtc,"E","DE");
				addColumn(gtc,"E","DE");
			}
			if(gtc.getPanelColumns(panel).contains("RI")){
				removeColumn(gtc,"RI","DRI");
				addColumn(gtc,"RI","DRI");
			}
		
		}
		
		if(chckbxEf.isSelected())         addColumn(gtc,"LEVF","DLEVF");//final level
		if(chckbxJf.isSelected())         addColumn(gtc,"JF");
		if(checkBox_MUL.isSelected())     addColumn(gtc,"M");//MUL
		if(checkBox_MR.isSelected())      addColumn(gtc,"MR","DMR");		
		if(checkBox_CC.isSelected())      addColumn(gtc,"CC","DCC");
		if(checkBox_TI.isSelected())      addColumn(gtc,"TI","DTI");
		if(chckbxComment.isSelected())    addColumn(gtc,"COMM");	
		
		gtc.setIsSmall(checkBox_6.isSelected());
		gtc.setNewPage(checkBox_7.isSelected());
		gtc.setShowDE(chckbxShowUncertaintyIn.isSelected());		
		gtc.setShowAll(chckbxShowAllLevels.isSelected());
		gtc.setPortait(!checkBox_1.isSelected());
		gtc.setDrawn(chckbxDrawTable_2.isSelected());
		
        //text=textFieldGammaComWidth.getText().trim();
        //if(text.length()>0){
        //	if(Str.isNumeric(text))
        //		gtc.setCommentColumnWidth(Float.parseFloat(text));
        //	else
        //		JOptionPane.showMessageDialog(this, "Wrong input for comment column width of gamma table!");
        //}
	}

	private void updateDecayTableControl(){
		int panel=0;
    	String selected=(String) comboBoxDecay.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	updateDecayTableControl(panel);
	}
	
	private void updateDecayTableControl(int panel){
        dtc.setCurrentPanelNo(panel);
        dtc.getPanelColumns(panel).clear();
        
		if(checkBox_ED.isSelected())   addColumn(dtc,"E","DE");
		if(chckbxLev.isSelected())     addColumn(dtc,"LEV","DLEV");
		if(checkBox_IB.isSelected())   addColumn(dtc,"IB","DIB");	
		if(checkBox_IE.isSelected())   addColumn(dtc,"IE","DIE");         
		if(checkBox_IA.isSelected())   addColumn(dtc,"IA","DIA");
		if(chckbxIp.isSelected())      addColumn(dtc,"TI","DTI");
		if(checkBox_LOGFT.isSelected())addColumn(dtc,"LOGFT","DLOGFT");	
		if(chckbxHf.isSelected())      addColumn(dtc,"HF","DHF");		
		if(checkBox_15.isSelected())   addColumn(dtc,"COMM");		
		dtc.setIsSmall(checkBox_9.isSelected());
		dtc.setNewPage(checkBox_10.isSelected());
		dtc.setShowDE(chckbxShowUncertaintyIn_2.isSelected());	
		dtc.setPortait(!checkBox_2.isSelected());
		dtc.setDrawn(chckbxDrawTable_3.isSelected());
		
        //text=textFieldDecayComWidth.getText().trim();
        //if(text.length()>0){
        //	if(Str.isNumeric(text))
        //		dtc.setCommentColumnWidth(Float.parseFloat(text));
        //	else
        //		JOptionPane.showMessageDialog(this, "Wrong input for comment column width of decay table!");
        //}
		
	}

	private void updateDelayTableControl(){
		int panel=0;
    	String selected=(String) comboBoxDelay.getSelectedItem().toString(); 	
    	if(selected!=null && selected.trim().length()>0){
    		String no=selected.replace("panel", "").trim();
    		if(Str.isNumeric(no))
    			panel=Integer.parseInt(no);
    	}
    	updateDelayTableControl(panel);
	}
	
	private void updateDelayTableControl(int panel){
        ptc.setCurrentPanelNo(panel);
        ptc.getPanelColumns(panel).clear();
        
		if(checkBox_EP.isSelected())addColumn(ptc,"E","DE");
		if(chckbxRi.isSelected())   addColumn(ptc,"IP","DIP");
		if(chckbxEi.isSelected())   addColumn(ptc,"LEV","DLEV");		
		if(chckbxEi_1.isSelected()) addColumn(ptc,"EI");
		if(checkBox.isSelected())   addColumn(ptc,"COMM");				
		ptc.setIsSmall(checkBox_12.isSelected());
		ptc.setNewPage(checkBox_13.isSelected());
		ptc.setShowDE(chckbxShowUncertaintyIn_1.isSelected());	
		ptc.setPortait(!checkBox_3.isSelected());
		ptc.setDrawn(chckbxDrawTable_4.isSelected());
		
        //text=textFieldDelayComWidth.getText().trim();
        //if(text.length()>0){
        //	if(Str.isNumeric(text))
        //		ptc.setCommentColumnWidth(Float.parseFloat(text));
        //	else
        //		JOptionPane.showMessageDialog(this, "Wrong input for comment column width of delayed-particle table!");
        //}
	}

	
	private void writeSelected(java.awt.event.MouseEvent evt){

		//back up Control.controls
		Vector<String> tempControls=new Vector<String>();
		boolean autoAdjust=NDSControl.autoAdjust;
		boolean needToFindBreaks=NDSControl.needToFindBreaks;
		boolean hasReference=NDSControl.hasReference;
		boolean isModified=NDSControl.isModified;
		boolean isTableControlModified=NDSControl.isTableControlModified;
		boolean includeAllDrawings=NDSControl.includeAllDrawings;
		boolean drawFigureOnly=NDSControl.drawFigureOnly;
		
    	if(evt.getModifiers()!=InputEvent.BUTTON1_MASK)//left-button, BUTTON2-middle, BUTTON3-right
    		return;
    	
	    if(curEnsdf==null){
	    	JOptionPane.showMessageDialog(this,"You haven't selected a dataset yet.");
	    	return;
	    }
	    
    	
		if(chckbxWithAutoSettings.isSelected()){		
			if(NDSControl.controls!=null) 
				tempControls.addAll(NDSControl.controls);
			
			NDSControl.reset();
			NDSControl.autoAdjust=true;
			NDSControl.needToFindBreaks=true;
			NDSControl.isModified=false;
			NDSControl.isTableControlModified=false;
			NDSControl.includeAllDrawings=true;
        }
		else{
			updateTableControl();
			NDSControl.autoAdjust=false;
			NDSControl.isModified=true;
			NDSControl.isTableControlModified=true;
			NDSControl.includeAllDrawings=true;
		}
			
		if(chckbxFigureOnly.isSelected())
			NDSControl.drawFigureOnly=true;
		else
			NDSControl.drawFigureOnly=false;
		
		if(!NDSControl.drawFigureOnly)
			NDSControl.hasReference=true;
       
        boolean validOutdir=true;
        String message="";
        if(javands.main.Setup.outdir.trim().length()==0){
        	message="Error: output path is empty. ";           
        	validOutdir=false;
        }else{ 
            File f=new File(javands.main.Setup.outdir.trim());
        	if(!f.exists()){
        		message="Error: output path does not exist. ";
            	validOutdir=false;
        	}
        }
        
        if(!validOutdir){
        	message+="Please specify output path.";
        	run.printMessage(message);
            JOptionPane.showMessageDialog(this, message);
            return;
        }
        
        
        try{
          	run.clear();
          	
        	//tableControl of each etd (dataset) in data will be changed and set in "writeLatex" with auto mode.
        	MassChain tempData=new MassChain(NDSControl.printDocumentRecord);     
        	
        	//default TableControls are set in this call when creating EnsdfTableData object for each data set.
        	//To use TableControl settings set in this panel which will be applied to TableControls of each data
        	//set in MassChain "data", tableControl of etd of selected dataset should be set after this call.
        	
        	try{
            	tempData.load(curEnsdf.ens.lines());     		
        	}catch(IOException e){
                JOptionPane.showMessageDialog(this, "Error when loading file! Please check your input file.");
                run.printMessage("Error when loading file! Please check your input file!");
            }

        	
        	tempData.getETD(0).setTableControl("LEVEL", ltc);
        	tempData.getETD(0).setTableControl("GAMMA", gtc);
        	tempData.getETD(0).setTableControl("DECAY", dtc);
        	tempData.getETD(0).setTableControl("DELAY", ptc);
        	
        	tempData.getETD(0).setDrawingControl("BAND", bdc);
        	tempData.getETD(0).setDrawingControl("DECAY", ddc);
    		
        	NDSConfig.latex=javands.main.Setup.outdir+run.dirSeparator()+Integer.toString(tempData.getA())+".tex";       	
        	
        	run.setData(tempData);
            run.writeLatex(NDSConfig.latex,tempData);    
            
       	
        }catch(Exception e){
            JOptionPane.showMessageDialog(this, e);
            e.printStackTrace();
        }
        
        
        //restore Control.controls
        if(tempControls.size()>0 && NDSControl.controls==null) NDSControl.controls.addAll(tempControls);
        NDSControl.autoAdjust=autoAdjust;
        NDSControl.needToFindBreaks=needToFindBreaks;
        NDSControl.hasReference=hasReference;
        NDSControl.isModified=isModified;
        NDSControl.isTableControlModified=isTableControlModified;
        NDSControl.includeAllDrawings=includeAllDrawings;
        NDSControl.drawFigureOnly=drawFigureOnly;
        
        
        //run shell command to create PDF file
    	String script="NDS.bat";
    	String path=javands.main.Setup.outdir+"\\"+script;
    	String os=System.getProperty("os.name").toLowerCase();
    	
    	if(os.contains("linux")||os.contains("mac")){
            script="NDS.sh";
            path=javands.main.Setup.outdir+"/"+script;
    	}
    	
    	message="\nTo create PDF file, please run the following script:\n";
    	message+=path;
    	
        try{
            if(evt.getClickCount()>=2){             
            	run.runScript();
            }else{
            	run.printMessage(message);
            }
            
        }catch(Exception e){
			String msg=e.getMessage();
			if(msg.length()>0) {
				run.printMessage(msg);
				JOptionPane.showMessageDialog(this,msg);						         
				return;
			}
            e.printStackTrace();
        }
        
	}
    
    private void launchMoreSettingDialog(String panelName,ActionEvent evt) {
    	
        BaseMoreSettingFrame settingFrame = null;
        
        JComponent c=(JComponent)evt.getSource();
        if(!c.isEnabled())
        	return;
        
        if(curEnsdf==null){
        	JOptionPane.showMessageDialog(this,"You must select a dataset first.");
        	return;
        }
        

        
        try{    
        	if(panelName.toUpperCase().equals("LEVEL")){                                    
                levelSettingFrame=new MoreLevelSettingFrame(data,curEnsdf,run);    
                levelSettingFrame.setParentPanel(this);
        		levelSettingFrame.setTableControl(ltc);        		
        		settingFrame=((MoreLevelSettingFrame) levelSettingFrame);                        		              
        	}
        	else if(panelName.toUpperCase().equals("GAMMA")){   
        		gammaSettingFrame=new MoreGammaSettingFrame(data,curEnsdf,run);
        		gammaSettingFrame.setParentPanel(this);
        		gammaSettingFrame.setTableControl(gtc);
        		settingFrame=(MoreGammaSettingFrame) gammaSettingFrame;
        	}
        	else if(panelName.toUpperCase().equals("DECAY")){
        		decaySettingFrame=new MoreDecaySettingFrame(data,curEnsdf,run);
        		decaySettingFrame.setParentPanel(this);
        		decaySettingFrame.setTableControl(dtc);
        		settingFrame=(MoreDecaySettingFrame) decaySettingFrame;
        	}
        	else if(panelName.toUpperCase().equals("DELAY")) {      
        		delaySettingFrame=new MoreDelaySettingFrame(data,curEnsdf,run);
        		delaySettingFrame.setParentPanel(this);
        		delaySettingFrame.setTableControl(ptc);
        		settingFrame=(MoreDelaySettingFrame) delaySettingFrame;
        	}
        	
         	settingFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);          
         	settingFrame.setVisible(true);
         	
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Something is wrong in settings for "+panelName+" table.");
            e.printStackTrace();
        }
    }
    
	@SuppressWarnings({ "rawtypes" })
	private void initComponents(){
        	
		JPanel levelSettingPanel = new JPanel();
		levelSettingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Level Table", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		btnOk = new JButton("Set");
		btnOk.setToolTipText("Once set, it will apply to both auto and manual modes.");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				applyTableControl();
			}
		});
		
		/*
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				applyTableControl();
			}
		});
		*/
		JPanel panel_4 = new JPanel();
		
		JPanel gammaSettingPanel = new JPanel();
		gammaSettingPanel.setBorder(new TitledBorder(null, "Gamma Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel gammaDrawPanel = new JPanel();
		
		chckbxEg = new JCheckBox("E");
		
		checkBox_RI = new JCheckBox("RI");
		
		checkBox_MUL = new JCheckBox("MUL");
		
		checkBox_MR = new JCheckBox("MR");
		
		checkBox_CC = new JCheckBox("CC");
		
		checkBox_TI = new JCheckBox("TI");
		
		chckbxComment = new JCheckBox("comment:");
		
		label = new JLabel("Columns in table:");
		
		chckbxNewCheckBox = new JCheckBox("EI");
		chckbxNewCheckBox.setToolTipText("decaying level energy");
		
		chckbxJi = new JCheckBox("JI");
		chckbxJi.setToolTipText("decaying level spin-parity");
		
		chckbxEf = new JCheckBox("EF");
		chckbxEf.setToolTipText("final level energy");
		
		chckbxJf = new JCheckBox("JF");
		chckbxJf.setToolTipText("final level spin-parity");
		
		lblWidth_3 = new JLabel("width:");
		
		textFieldGammaComWidth = new JTextField();
		textFieldGammaComWidth.setColumns(10);
		textFieldGammaComWidth.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusLost(java.awt.event.FocusEvent evt) {
            	textFieldFocusLost(evt,comboBoxGamma,gtc);
            }
		});
		
		
		checkBox_6 = new JCheckBox("Small table");
		checkBox_6.setToolTipText("This is used to fix tables with very few columns (one to three) which are not too long enough to be split. The table must not span multiple pages.");
		
		checkBox_7 = new JCheckBox("New page");
		checkBox_7.setToolTipText("This table starts from a new page");
		
		chckbxShowUncertaintyIn = new JCheckBox("Show uncertainty in E(Level)");
		
		chckbxShowAllLevels = new JCheckBox("Show all levels");
		
		JPanel decaySettingPanel = new JPanel();
		decaySettingPanel.setBorder(new TitledBorder(null, "Decay Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel decayDrawPanel = new JPanel();
		
		checkBox_ED = new JCheckBox("E");
		
		checkBox_IB = new JCheckBox("IB");
		
		checkBox_IE = new JCheckBox("IE");
		
		checkBox_IA = new JCheckBox("IA");
		
		checkBox_LOGFT = new JCheckBox("LOGFT");
		
		label_2 = new JLabel("Columns in table:");
		
		chckbxIp = new JCheckBox("TI");
		
		checkBox_15 = new JCheckBox("comment:");
		
		lblWidth_4 = new JLabel("width:");
		
		textFieldDecayComWidth = new JTextField();
		textFieldDecayComWidth.setColumns(10);
		textFieldDecayComWidth.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusLost(java.awt.event.FocusEvent evt) {
            	textFieldFocusLost(evt,comboBoxDecay,dtc);
            }
		});
		

		
		chckbxLev = new JCheckBox("LEV");
		chckbxLev.setToolTipText("decay daughter level energy");
		
		chckbxHf = new JCheckBox("HF");
		
		checkBox_9 = new JCheckBox("Small table");
		checkBox_9.setToolTipText("This is used to fix tables with very few columns (one to three) which are not too long enough to be split. The table must not span multiple pages.");
		
		checkBox_10 = new JCheckBox("New page");
		checkBox_10.setToolTipText("This table starts from a new page");
		
		chckbxShowUncertaintyIn_2 = new JCheckBox("Show uncertainty in E(Level)");
		
		button_1 = new JButton("more");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchMoreSettingDialog("DECAY",e);
			}
		});
		
		comboBoxLevel = new JComboBox();
		comboBoxLevel.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				comboBoxItemChanged(e,textFieldLevelComWidth,ltc);
			}
		});
		
		comboBoxGamma = new JComboBox();
		comboBoxGamma.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				comboBoxItemChanged(e,textFieldGammaComWidth,gtc);
			}
		});
		
		comboBoxDecay = new JComboBox();
		comboBoxDecay.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				comboBoxItemChanged(e,textFieldDecayComWidth,dtc);
			}
		});
		
		comboBoxDelay = new JComboBox();
		comboBoxDelay.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				comboBoxItemChanged(e,textFieldDelayComWidth,ptc);
			}
		});
		
		checkBox_2 = new JCheckBox("Landscape");
		checkBox_2.setToolTipText("");
		
		chckbxDrawTable_3 = new JCheckBox("Draw");
		chckbxDrawTable_3.setToolTipText("");
		GroupLayout gl_decaySettingPanel = new GroupLayout(decaySettingPanel);
		gl_decaySettingPanel.setHorizontalGroup(
			gl_decaySettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_decaySettingPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_decaySettingPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_decaySettingPanel.createSequentialGroup()
							.addComponent(checkBox_9, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(checkBox_10, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(checkBox_2, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxShowUncertaintyIn_2, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
							.addComponent(chckbxDrawTable_3, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(button_1, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
						.addComponent(decayDrawPanel, GroupLayout.PREFERRED_SIZE, 780, GroupLayout.PREFERRED_SIZE))
					.addGap(1))
		);
		gl_decaySettingPanel.setVerticalGroup(
			gl_decaySettingPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_decaySettingPanel.createSequentialGroup()
					.addGap(1)
					.addComponent(decayDrawPanel, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addGap(13)
					.addGroup(gl_decaySettingPanel.createParallelGroup(Alignment.BASELINE, false)
						.addComponent(checkBox_9)
						.addComponent(checkBox_10)
						.addComponent(checkBox_2)
						.addComponent(chckbxShowUncertaintyIn_2)
						.addComponent(chckbxDrawTable_3)
						.addComponent(button_1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
		);
		GroupLayout gl_decayDrawPanel = new GroupLayout(decayDrawPanel);
		gl_decayDrawPanel.setHorizontalGroup(
			gl_decayDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_decayDrawPanel.createSequentialGroup()
					.addGap(1)
					.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(comboBoxDecay, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(checkBox_ED, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(chckbxLev)
					.addGap(6)
					.addComponent(checkBox_IB)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBox_IE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBox_IA)
					.addGap(6)
					.addComponent(chckbxIp)
					.addGap(6)
					.addComponent(checkBox_LOGFT, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxHf, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(checkBox_15, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(lblWidth_4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textFieldDecayComWidth, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))
		);
		gl_decayDrawPanel.setVerticalGroup(
			gl_decayDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_decayDrawPanel.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_decayDrawPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBoxDecay, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_ED, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxLev, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_IB, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_IE, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_IA, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxIp, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_LOGFT, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxHf, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_15, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblWidth_4, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_2)
						.addComponent(textFieldDecayComWidth, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
		);
		decayDrawPanel.setLayout(gl_decayDrawPanel);
		decaySettingPanel.setLayout(gl_decaySettingPanel);
		
		JPanel delaySettingPanel = new JPanel();
		delaySettingPanel.setBorder(new TitledBorder(null, "Delayed Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel delayDrawPanel = new JPanel();
		
		checkBox_EP = new JCheckBox("E");
		checkBox_EP.setToolTipText("particle energy");
		
		chckbxRi = new JCheckBox("TI");
		
		chckbxEi = new JCheckBox("LEV");
		chckbxEi.setToolTipText("decay daughter level energy");
		
		chckbxEi_1 = new JCheckBox("EI");
		chckbxEi_1.setToolTipText("intermediate level energy");
		
		label_3 = new JLabel("Columns in table:");
		
		checkBox = new JCheckBox("comment:");
		
		lblWidth_2 = new JLabel("width:");
		
		textFieldDelayComWidth = new JTextField();
		textFieldDelayComWidth.setColumns(10);
		textFieldDelayComWidth.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusLost(java.awt.event.FocusEvent evt) {
            	textFieldFocusLost(evt,comboBoxDelay,ptc);
            }
		});
		
		
		checkBox_12 = new JCheckBox("Small table");
		checkBox_12.setToolTipText("This is used to fix tables with very few columns (one to three) which are not too long enough to be split. The table must not span multiple pages.");
		
		checkBox_13 = new JCheckBox("New page");
		checkBox_13.setToolTipText("This table starts from a new page");
		
		chckbxShowUncertaintyIn_1 = new JCheckBox("Show uncertainty in E(Level)");
		
		panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Other settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		panel_6 = new JPanel();
		
		chckbxCombineLAnd = new JCheckBox("Combine L and G");
		chckbxCombineLAnd.setToolTipText("combine level and gamma tables beside each other");
		
		chckbxBandDrawing = new JCheckBox("Band drawing");
		
		chckbxNewpage_1 = new JCheckBox("Newpage");
		chckbxNewpage_1.setToolTipText("this dataset starts a new page");
		
		chckbxAltid = new JCheckBox("ALTID:");
		chckbxAltid.setToolTipText("replace the old DSID of the dataset with the one given");
		
		textField_6 = new JTextField();
		textField_6.setColumns(10);
		
		lblNxrefs = new JLabel("nXREFs:");
		
		textField_7 = new JTextField();
		textField_7.setColumns(10);
		

		
		panel = new JPanel();
		
		chckbxIncludeDecaylevelDrawing = new JCheckBox("Decay/level drawing:");
		chckbxIncludeDecaylevelDrawing.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				boolean enable=(curEnsdf!=null && ((JCheckBox)evt.getSource()).isSelected());

				setEnabled(chckbxLandscape,enable);
				setEnabled(chckbxNewpage,enable);
				
				/*
				setEnabled(lblWidth_1,enable);
				setEnabled(textField_4,enable);
				setEnabled(lblHeight,enable);
				setEnabled(textField_5,enable);
				setEnabled(lblSpacing,enable);
				setEnabled(textField_8,enable);
				*/
				//not used, disabled for now
				setEnabled(lblWidth_1,false);
				setEnabled(textField_4,false);
				setEnabled(lblHeight,false);
				setEnabled(textField_5,false);
				setEnabled(lblSpacing,false);
				setEnabled(textField_8,false);
			}
		});
		
		chckbxIncludeDecaylevelDrawing.setToolTipText("");
		
		chckbxLandscape = new JCheckBox("landscape");
		chckbxLandscape.setHorizontalTextPosition(SwingConstants.LEADING);
		
		lblWidth_1 = new JLabel("width (cm)");
		
		textField_4 = new JTextField();
		textField_4.setColumns(10);
		
		lblHeight = new JLabel("height");
		
		textField_5 = new JTextField();
		textField_5.setColumns(10);
		
		chckbxNewpage = new JCheckBox("newpage");
		chckbxNewpage.setToolTipText("start this drawing on a new page");
		chckbxNewpage.setHorizontalTextPosition(SwingConstants.LEADING);
		
		lblSpacing = new JLabel("spacing (in pt)");
		lblSpacing.setToolTipText("minimum spacing in pt between levels in the drawing");
		
		textField_8 = new JTextField();
		textField_8.setColumns(10);
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_6, GroupLayout.DEFAULT_SIZE, 749, Short.MAX_VALUE)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 749, Short.MAX_VALUE))
					.addGap(1))
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
		);
		GroupLayout gl_panel_6 = new GroupLayout(panel_6);
		gl_panel_6.setHorizontalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_6.createSequentialGroup()
					.addGap(6)
					.addComponent(chckbxCombineLAnd, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(chckbxBandDrawing, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(chckbxNewpage_1, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(chckbxAltid, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textField_6, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(lblNxrefs, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textField_7, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
		);
		gl_panel_6.setVerticalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_6.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_panel_6.createParallelGroup(Alignment.LEADING, false)
						.addComponent(chckbxCombineLAnd, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxBandDrawing, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxNewpage_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxAltid, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(textField_6, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNxrefs, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(textField_7, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
		);
		panel_6.setLayout(gl_panel_6);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(6)
					.addComponent(chckbxIncludeDecaylevelDrawing, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(chckbxLandscape, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(chckbxNewpage, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(lblWidth_1, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textField_5, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(8)
					.addComponent(lblSpacing, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(textField_8, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(chckbxIncludeDecaylevelDrawing, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(chckbxLandscape, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(chckbxNewpage, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(lblWidth_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(textField_5, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(2)
					.addComponent(lblSpacing))
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(textField_8, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
		);
		panel.setLayout(gl_panel);
		panel_3.setLayout(gl_panel_3);
		
		separator_1 = new JSeparator();
		separator_1.setForeground(SystemColor.controlShadow);
		
		button_2 = new JButton("more");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchMoreSettingDialog("DELAY",e);
			}
		});
		
		checkBox_3 = new JCheckBox("Landscape");
		checkBox_3.setToolTipText("");
		panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
		JPanel levelDrawPanel = new JPanel();
		
		checkBox_EL = new JCheckBox("E");
		
		checkBox_JP = new JCheckBox("J");
		
		checkBox_T = new JCheckBox("T");
		
		checkBox_S = new JCheckBox("S");
		
		checkBox_LCOM = new JCheckBox("comment:");
		
		checkBox_L = new JCheckBox("L");
		
		lblIncludeColumns = new JLabel("Columns in table:");
		
		lblWidth = new JLabel("width (cm):");
		
		textFieldLevelComWidth = new JTextField();
		textFieldLevelComWidth.setColumns(10);
		textFieldLevelComWidth.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusLost(java.awt.event.FocusEvent evt) {
            	textFieldFocusLost(evt,comboBoxLevel,ltc);
            }
		});
		
		
		chckbxXref = new JCheckBox("XREF");
		
		chckbxBand = new JCheckBox("Band");
		
		chckbxSmallTable = new JCheckBox("Small table");
		chckbxSmallTable.setToolTipText("This is used to fix tables with very few columns (one to three) which are not too long enough to be split. The table must not span multiple pages.");
		
		chckbxNewPage = new JCheckBox("New page");
		chckbxNewPage.setToolTipText("This table starts from a new page");
		
		btnMoreLevelSetting = new JButton("more");
		btnMoreLevelSetting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				launchMoreSettingDialog("LEVEL",evt);
			}
		});
		
		chckbxShowUncertaintiesIn = new JCheckBox("Show uncertainty in E(Level)");
		
		chckbxLandscape_1 = new JCheckBox("Landscape");
		chckbxLandscape_1.setToolTipText("");
		


		
		GroupLayout gl_levelDrawPanel = new GroupLayout(levelDrawPanel);
		gl_levelDrawPanel.setHorizontalGroup(
			gl_levelDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_levelDrawPanel.createSequentialGroup()
					.addGap(1)
					.addComponent(lblIncludeColumns, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(comboBoxLevel, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(checkBox_EL, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(checkBox_JP)
					.addGap(5)
					.addComponent(checkBox_T)
					.addGap(12)
					.addComponent(checkBox_L)
					.addGap(6)
					.addComponent(checkBox_S)
					.addGap(12)
					.addComponent(chckbxXref)
					.addGap(6)
					.addComponent(chckbxBand)
					.addGap(6)
					.addComponent(checkBox_LCOM, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textFieldLevelComWidth, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_levelDrawPanel.setVerticalGroup(
			gl_levelDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_levelDrawPanel.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_levelDrawPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBoxLevel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblIncludeColumns)
						.addComponent(textFieldLevelComWidth, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_LCOM, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxBand, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxXref, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_S, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_L, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_T, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_JP, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_EL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		levelDrawPanel.setLayout(gl_levelDrawPanel);
		
		button = new JButton("more");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchMoreSettingDialog("GAMMA",e);
			}
		});
		
		checkBox_1 = new JCheckBox("Landscape");
		checkBox_1.setToolTipText("");
		
		chckbxDrawTable_2 = new JCheckBox("Draw");
		chckbxDrawTable_2.setToolTipText("");
		GroupLayout gl_gammaSettingPanel = new GroupLayout(gammaSettingPanel);
		gl_gammaSettingPanel.setHorizontalGroup(
			gl_gammaSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_gammaSettingPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_gammaSettingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(gammaDrawPanel, GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
						.addGroup(gl_gammaSettingPanel.createSequentialGroup()
							.addComponent(checkBox_6, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(checkBox_7, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(checkBox_1, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxShowUncertaintyIn, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxShowAllLevels)
							.addPreferredGap(ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
							.addComponent(chckbxDrawTable_2, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))))
		);
		gl_gammaSettingPanel.setVerticalGroup(
			gl_gammaSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_gammaSettingPanel.createSequentialGroup()
					.addComponent(gammaDrawPanel, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addGap(13)
					.addGroup(gl_gammaSettingPanel.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(gl_gammaSettingPanel.createSequentialGroup()
							.addGroup(gl_gammaSettingPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(checkBox_6, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_gammaSettingPanel.createParallelGroup(Alignment.BASELINE)
									.addComponent(chckbxShowUncertaintyIn)
									.addComponent(checkBox_1)
									.addComponent(checkBox_7))
								.addComponent(chckbxShowAllLevels))
							.addGap(5))
						.addGroup(gl_gammaSettingPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxDrawTable_2))))
		);
		GroupLayout gl_gammaDrawPanel = new GroupLayout(gammaDrawPanel);
		gl_gammaDrawPanel.setHorizontalGroup(
			gl_gammaDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_gammaDrawPanel.createSequentialGroup()
					.addComponent(label, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(comboBoxGamma, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(chckbxEg, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(checkBox_RI)
					.addGap(6)
					.addComponent(chckbxNewCheckBox)
					.addGap(6)
					.addComponent(chckbxJi)
					.addGap(6)
					.addComponent(chckbxEf)
					.addGap(6)
					.addComponent(chckbxJf)
					.addGap(6)
					.addComponent(checkBox_MUL)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBox_MR)
					.addGap(6)
					.addComponent(checkBox_CC)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBox_TI)
					.addGap(6)
					.addComponent(chckbxComment, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(lblWidth_3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addGap(3)
					.addComponent(textFieldGammaComWidth, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))
		);
		gl_gammaDrawPanel.setVerticalGroup(
			gl_gammaDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_gammaDrawPanel.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_gammaDrawPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBoxGamma, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxEg, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_RI, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxNewCheckBox)
						.addComponent(chckbxJi, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxEf, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxJf, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_MUL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_MR, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_CC, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox_TI, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxComment, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblWidth_3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldGammaComWidth, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(label)))
		);
		gammaDrawPanel.setLayout(gl_gammaDrawPanel);
		gammaSettingPanel.setLayout(gl_gammaSettingPanel);
		
		btnDrawSelected = new JButton("Draw selected");
		btnDrawSelected.setToolTipText("double-click to create and view a PDF output.");
		//btnDrawSelected.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		writeSelected();
		//	}
		//});
		
        btnDrawSelected.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent evt) {
        		writeSelected(evt);
        	}
        });
        
		chckbxWithAutoSettings = new JCheckBox("auto settings");
		chckbxWithAutoSettings.setToolTipText("<HTML>In auto mode, layout is adjusted automatically by the program and the change here will not take "
				                              + "<br>effect unless 'set' button is clicked, while in manual mode it can be controlled in a control file "
				                              + "<br>or control settings here.</HTML>");
		chckbxWithAutoSettings.setSelected(true);
		
		chckbxFigureOnly = new JCheckBox("figure only");
		chckbxFigureOnly.setToolTipText("draw only figures in the output file.");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addComponent(decaySettingPanel, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addComponent(delaySettingPanel, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 890, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(levelSettingPanel, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE)
								.addComponent(gammaSettingPanel, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(btnDrawSelected)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(chckbxWithAutoSettings)
									.addGap(10)
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(chckbxFigureOnly, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED, 511, Short.MAX_VALUE)
											.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED, 668, Short.MAX_VALUE))))
								.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE))))
					.addGap(9))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(247)
					.addComponent(checkBox_4, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(670, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(checkBox_4, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(levelSettingPanel, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(gammaSettingPanel, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(decaySettingPanel, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(delaySettingPanel, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(10)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(btnOk)
								.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addComponent(btnDrawSelected))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(17)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(chckbxWithAutoSettings)
								.addComponent(chckbxFigureOnly))))
					.addGap(15))
		);
		
		chckbxDrawTable_1 = new JCheckBox("Draw");
		chckbxDrawTable_1.setToolTipText("");
		GroupLayout gl_levelSettingPanel = new GroupLayout(levelSettingPanel);
		gl_levelSettingPanel.setHorizontalGroup(
			gl_levelSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_levelSettingPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_levelSettingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(levelDrawPanel, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_levelSettingPanel.createSequentialGroup()
							.addComponent(chckbxSmallTable, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxNewPage, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxLandscape_1, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxShowUncertaintiesIn, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
							.addComponent(chckbxDrawTable_1, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnMoreLevelSetting, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))))
		);
		gl_levelSettingPanel.setVerticalGroup(
			gl_levelSettingPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_levelSettingPanel.createSequentialGroup()
					.addComponent(levelDrawPanel, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
					.addGap(13)
					.addGroup(gl_levelSettingPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxSmallTable)
						.addComponent(chckbxNewPage)
						.addComponent(chckbxLandscape_1)
						.addComponent(chckbxShowUncertaintiesIn))
					.addGap(5))
				.addGroup(gl_levelSettingPanel.createParallelGroup(Alignment.BASELINE)
					.addComponent(btnMoreLevelSetting, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addComponent(chckbxDrawTable_1))
		);
		levelSettingPanel.setLayout(gl_levelSettingPanel);
		
		chckbxDrawTable_4 = new JCheckBox("Draw");
		chckbxDrawTable_4.setToolTipText("");
		GroupLayout gl_delaySettingPanel = new GroupLayout(delaySettingPanel);
		gl_delaySettingPanel.setHorizontalGroup(
			gl_delaySettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_delaySettingPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_delaySettingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(delayDrawPanel, GroupLayout.PREFERRED_SIZE, 642, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_delaySettingPanel.createSequentialGroup()
							.addComponent(checkBox_12, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(checkBox_13, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(checkBox_3, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxShowUncertaintyIn_1, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 227, Short.MAX_VALUE)
							.addComponent(chckbxDrawTable_4, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addGap(13)
							.addComponent(button_2, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))))
		);
		gl_delaySettingPanel.setVerticalGroup(
			gl_delaySettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_delaySettingPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(delayDrawPanel, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
					.addGap(13)
					.addGroup(gl_delaySettingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(checkBox_12)
						.addGroup(gl_delaySettingPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(checkBox_3)
							.addComponent(checkBox_13))
						.addGroup(gl_delaySettingPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(chckbxShowUncertaintyIn_1)
							.addComponent(button_2, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxDrawTable_4)))
					.addContainerGap())
		);
		GroupLayout gl_delayDrawPanel = new GroupLayout(delayDrawPanel);
		gl_delayDrawPanel.setHorizontalGroup(
			gl_delayDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_delayDrawPanel.createSequentialGroup()
					.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(comboBoxDelay, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(checkBox_EP, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxRi)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxEi)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxEi_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblWidth_2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(textFieldDelayComWidth, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
					.addGap(106))
		);
		gl_delayDrawPanel.setVerticalGroup(
			gl_delayDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_delayDrawPanel.createSequentialGroup()
					.addGroup(gl_delayDrawPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_delayDrawPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(label_3))
						.addGroup(gl_delayDrawPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(comboBoxDelay, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addComponent(checkBox_EP, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxRi, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxEi, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxEi_1)
							.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblWidth_2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
						.addComponent(comboBoxDelay, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldDelayComWidth, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
					.addGap(24))
		);
		delayDrawPanel.setLayout(gl_delayDrawPanel);
		delaySettingPanel.setLayout(gl_delaySettingPanel);
		setLayout(groupLayout);
		

	}
	
	private void comboBoxItemChanged(java.awt.event.ItemEvent evt, JTextField tf,TableControl tc) {
    	String item=(String) evt.getItem().toString();
    	String type=tc.getTableType();//L,G,D,P
    	
    	if(item!=null && item.trim().length()>0){
    		String no=item.replace("panel", "").trim();
    		
    		if(Str.isNumeric(no)){
    			int panel=Integer.parseInt(no);
    			
    			//when item state changes, two events are fired and it is the "DESELECTED" fired first.
    			if(evt.getStateChange()==ItemEvent.DESELECTED){
    				updateTableControl(type,panel);
    			}
    			else if(evt.getStateChange()==ItemEvent.SELECTED){
        			tf.setText(String.format("%.2f",tc.getCommentColumnWidth(panel)));
        			displayTableControl(type,panel);
    			}
    			
    		}
    	}
    	
   	
    }
	
	@SuppressWarnings("rawtypes")
	private void textFieldFocusLost(java.awt.event.FocusEvent evt,JComboBox cb,TableControl tc){
		String selectedPanel=(String) cb.getSelectedItem().toString();
		if(selectedPanel==null || selectedPanel.trim().length()<=0 || !selectedPanel.trim().contains("panel"))
			return;
		
		selectedPanel=selectedPanel.trim().replace("panel","");
		
		if(!Str.isNumeric(selectedPanel))
			return;

		int panel=Integer.parseInt(selectedPanel);
		
		float oldComWidth=tc.getCommentColumnWidth(panel);
		float newComWidth=-1;
		
		JTextField tf=(JTextField) evt.getSource();
		String text=tf.getText().trim();
		
		if(text.length()<=0){
			//tf.setText(String.format("%.2f", oldComWidth));
			String message="Width for "+tc.getTableTypeName()+" table in "+curEnsdf.getENSDF().nucleus().nameENSDF()+":"+curEnsdf.getENSDF().fullDSIdS();
			message+=" is not set and will be calculated automatically.";
			run.printMessage(message);
			tc.setUseAutoComWidth(true);
			return;
		}
		
		try{
			newComWidth=Float.parseFloat(text);
			if(newComWidth==oldComWidth)
				return;

        	tc.setCommentColumnWidth(panel,Float.parseFloat(text));
        	isCommentWidthReset=true;
        	
        	
		}catch (Exception e){
			JOptionPane.showMessageDialog(this, "Wrong input for comment column width of decay table!");
			e.printStackTrace();
			return;
		}
	}



class ComboItem
{
    private String key;
    private String value;

    public ComboItem(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return key;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
}

    private JButton btnDrawSelected;
    private JCheckBox chckbxWithAutoSettings;
    private JButton btnOk;
    private JCheckBox checkBox_EL;
    private JCheckBox checkBox_L;
    private JCheckBox chckbxEg;
    private JCheckBox checkBox_RI;
    private JCheckBox checkBox_MUL;
    private JCheckBox checkBox_MR;
    private JCheckBox checkBox_CC;
    private JCheckBox checkBox_TI;
    private JCheckBox chckbxComment;
    private JCheckBox checkBox_ED;
    private JCheckBox checkBox_IB;
    private JCheckBox checkBox_IE;
    private JCheckBox checkBox_IA;
    private JCheckBox checkBox_LOGFT;
    private JCheckBox checkBox_EP;
    private JCheckBox chckbxRi;
    private JCheckBox chckbxEi;
    private JCheckBox chckbxEi_1;
    private JCheckBox checkBox_JP;
    private JCheckBox checkBox_T;
    private JCheckBox checkBox_S;
    private JCheckBox checkBox_LCOM;
    private JLabel lblIncludeColumns;
    private JCheckBox chckbxSmallTable;
    private JCheckBox chckbxNewPage;
    private JPanel panel_3;
    private JPanel panel_6;
    private JCheckBox chckbxCombineLAnd;
    private JCheckBox chckbxBandDrawing;
    private JCheckBox chckbxNewpage_1;
    private JSeparator separator_1;
    private JLabel lblWidth;
    private JTextField textFieldLevelComWidth;
    private JLabel label;
    private JLabel label_2;
    private JLabel label_3;
    private JCheckBox chckbxNewCheckBox;
    private JCheckBox chckbxJi;
    private JCheckBox chckbxEf;
    private JCheckBox chckbxJf;
    private JCheckBox chckbxShowUncertaintiesIn;
    private JCheckBox chckbxXref;
    private JCheckBox chckbxBand;
    private JCheckBox checkBox_6;
    private JCheckBox checkBox_7;
    private JCheckBox chckbxShowUncertaintyIn;
    private JCheckBox checkBox_9;
    private JCheckBox checkBox_10;
    private JCheckBox chckbxShowUncertaintyIn_2;
    private JCheckBox checkBox_12;
    private JCheckBox checkBox_13;
    private JCheckBox chckbxShowUncertaintyIn_1;
    private JCheckBox chckbxShowAllLevels;
    private JCheckBox chckbxIp;
    private JCheckBox checkBox_15;
    private JLabel lblWidth_4;
    private JTextField textFieldDecayComWidth;
    private JCheckBox chckbxLev;
    private JLabel lblWidth_3;
    private JTextField textFieldGammaComWidth;
    private JCheckBox checkBox;
    private JLabel lblWidth_2;
    private JTextField textFieldDelayComWidth;
    private JPanel panel;
    private JCheckBox chckbxIncludeDecaylevelDrawing;
    private JCheckBox chckbxLandscape;
    private JLabel lblWidth_1;
    private JTextField textField_4;
    private JLabel lblHeight;
    private JTextField textField_5;
    private JCheckBox chckbxNewpage;
    private JCheckBox chckbxAltid;
    private JTextField textField_6;
    private JLabel lblNxrefs;
    private JTextField textField_7;
    private JLabel lblSpacing;
    private JTextField textField_8;
    private JCheckBox chckbxHf;
    private JCheckBox chckbxLandscape_1;
    private JCheckBox checkBox_1;
    private JCheckBox checkBox_2;
    private JCheckBox checkBox_3;
    private JButton btnMoreLevelSetting;
    private JButton button;
    private JButton button_1;
    private JButton button_2;
    private final JCheckBox checkBox_4 = new JCheckBox("Landscape");
    private JCheckBox chckbxDrawTable_1;
    private JCheckBox chckbxDrawTable_2;
    private JCheckBox chckbxDrawTable_3;
    private JCheckBox chckbxDrawTable_4;
    @SuppressWarnings("rawtypes")
	private JComboBox comboBoxLevel;
    @SuppressWarnings("rawtypes")
	private JComboBox comboBoxGamma;
    @SuppressWarnings("rawtypes")
	private JComboBox comboBoxDecay;
    @SuppressWarnings("rawtypes")
	private JComboBox comboBoxDelay;
    private JCheckBox chckbxFigureOnly;
}
