package javands.ui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;

import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.DrawingControl;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.control.TableControl;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.util.Str;
import javands.main.Run;

import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.InputEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;


import java.awt.Color;


import javax.swing.JTextField;

import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.LayoutStyle.ComponentPlacement;

public class DrawingCreatorPanel extends BaseSettingPanel {


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
    
    
	public DrawingCreatorPanel(MassChain mass,EnsdfWrap curEnsdf,javands.main.Run r) {
		checkBox_4.setToolTipText("");

        run=r;
        this.curEnsdf=curEnsdf;
        data=mass;
        
        isCommentWidthReset=false;
        
		initComponents();
		
        if(curEnsdf!=null){
        	ddc=curEnsdf.etd.getDecayDrawingControl().clone();   
        	ddc.setDrawn(true);
        	displayDrawingControl();
        }
                
	}
	
	
	public void setENSDF(EnsdfWrap ew){
		
		curEnsdf=ew;
		
        if(curEnsdf!=null){        	
        	ddc=curEnsdf.etd.getDecayDrawingControl().clone();  
        	ddc.setDrawn(true);
        	displayDrawingControl();
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
	
    private void displayDrawingControl(){
				
		if(ddc.isDrawn()){

			setSelected(portraitRadioButton,ddc.isPortrait());
			setSelected(landscapeRadioButton,!ddc.isPortrait());
			
			
			float w=ddc.getWidth();
			float h=ddc.getHeight();
			float gap=ddc.getSpacing();
			if(w<=0 || h<=0){
				w=16.5f;
				h=20.0f;
				if(!ddc.isPortrait()){
					h=16.5f;
					w=20.0f;
				}
			}
			
        	heightTextField.setText(String.valueOf(h));
        	widthTextField.setText(String.valueOf(w));
        	spacingTextField.setText(String.valueOf(gap));
        	
        	boolean isDecay=false;
        	if(curEnsdf!=null&&curEnsdf.getENSDF().fullDSId().contains("DECAY"))
        		isDecay=true;
        	
        	
			setEnabled(showGSQValueCheckBox,isDecay);
			setEnabled(showQValueUnitCheckBox,isDecay);
			setEnabled(tableXTextField,isDecay);
			
			if(isDecay){
				setSelected(showGSQValueCheckBox,ddc.isShowGSQValue());
				setSelected(showQValueUnitCheckBox,ddc.isShowQValueUnit());
				tableXTextField.setText(String.format("%.1f",ddc.getDecayTableXOffset()));
			}

			
			setSelected(showLegendCheckBox,ddc.isShowLegend());
			setSelected(showTitleCheckBox,ddc.isShowTitle());
						       	

		}

	}
    

	@SuppressWarnings("unused")
	private void applyDrawingControl(){
	    if(curEnsdf==null){
	    	JOptionPane.showMessageDialog(this,"You haven't selected a dataset yet.");
	    	return;
	    }
	    
        updateDrawingControl();
        
		//curEnsdf.etd.setTableControl("LEVEL", ltc);
		//curEnsdf.etd.setTableControl("GAMMA", gtc);
		//curEnsdf.etd.setTableControl("DECAY", dtc);
		//curEnsdf.etd.setTableControl("DELAY", ptc);
		//curEnsdf.etd.setDrawingControl("BAND", bdc);
		curEnsdf.etd.setDrawingControl("DECAY", ddc);

        //curEnsdf.etd.setCombineLG(isCombineLG);//combine L and G table
        //curEnsdf.etd.setNewPage(isNewPage);   
        //curEnsdf.etd.setAltID(altID);
        //curEnsdf.etd.setNXrefs(nXrefs);
        
        //isModified=true only when table control settings have been modified.
        //isModified=false even if the header settings have been changed but no modifications are made for table settings.
		//Control.isModified=true;
		//Control.isTableControlModified=true;
		
    	
		//Control.isCommentWidthReset=isCommentWidthReset;
		//isCommentWidthReset=false;
    }
    
	private void updateDrawingControl(){
		String text="";

    	ddc.setPortrait(portraitRadioButton.isSelected());
    	ddc.setShowTitle(showTitleCheckBox.isSelected());
    	ddc.setShowLegend(showLegendCheckBox.isSelected());
    	
    	if(curEnsdf!=null&&curEnsdf.getENSDF().fullDSId().contains("DECAY")){
        	ddc.setShowGSQValue(showGSQValueCheckBox.isSelected());
        	ddc.setShowQValueUnit(showQValueUnitCheckBox.isSelected());
            text=tableXTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text))
            		ddc.setDecayTableXOffset(Float.parseFloat(text));
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for table X offset!");
            }
    	}

    	
        text=widthTextField.getText().trim();
        if(text.length()>0){
        	if(Str.isNumeric(text))
        		ddc.setWidth(Float.parseFloat(text));
        	else
        		JOptionPane.showMessageDialog(this, "Wrong input for decay-drawing width!");
        }
        text=heightTextField.getText().trim();
        if(text.length()>0){
        	if(Str.isNumeric(text))
        		ddc.setHeight(Float.parseFloat(text));
        	else
        		JOptionPane.showMessageDialog(this, "Wrong input for decay-drawing height!");
        }
        text=spacingTextField.getText().trim();
        if(text.length()>0){
        	if(Str.isNumeric(text))
        		ddc.setSpacing(Float.parseFloat(text));
        	else
        		JOptionPane.showMessageDialog(this, "Wrong input for decay-drawing spacing!");
        }


	}
	
	private void drawSelected(java.awt.event.MouseEvent evt){

		//back up Control.controls
		Vector<String> tempControls=new Vector<String>();
		boolean autoAdjust=NDSControl.autoAdjust;
		boolean needToFindBreaks=NDSControl.needToFindBreaks;
		boolean hasReference=NDSControl.hasReference;
		boolean isModified=NDSControl.isModified;
		boolean isTableControlModified=NDSControl.isTableControlModified;
		boolean includeAllDrawings=NDSControl.includeAllDrawings;
		boolean drawFigureOnly=NDSControl.drawFigureOnly;
		boolean removeAllHeading=NDSControl.removeAllHeading;
		boolean removeAllFooting=NDSControl.removeAllFooting;
		
		NDSControl.drawFigureOnly=true;
		NDSControl.showHeading=false;
		NDSControl.removeAllFooting=true;
		NDSControl.removeAllHeading=true;
		
    	if(evt.getModifiers()!=InputEvent.BUTTON1_MASK)//left-button, BUTTON2-middle, BUTTON3-right
    		return;
    	
	    if(curEnsdf==null){
	    	JOptionPane.showMessageDialog(this,"You haven't selected a dataset yet.");
	    	return;
	    }
	    
    	
       
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
                e.printStackTrace();
            }

        	
        	updateDrawingControl();

        	
        	//tempData.getETD(0).setTableControl("LEVEL", ltc);
        	//tempData.getETD(0).setTableControl("GAMMA", gtc);
        	//tempData.getETD(0).setTableControl("DECAY", dtc);
        	//tempData.getETD(0).setTableControl("DELAY", ptc);
        	
        	//tempData.getETD(0).setDrawingControl("BAND", bdc);
        	tempData.getETD(0).setDrawingControl("DECAY", ddc);
    		
        	NDSConfig.latex=javands.main.Setup.outdir+run.dirSeparator()+Integer.toString(tempData.getA())+".tex";       	
        	
        	run.setData(tempData);
            run.writeLatex(NDSConfig.latex,tempData);    
            
        	//System.out.println("line 270:"+ddc.isShowTitle());
        	
        }catch(Exception e){
        	e.printStackTrace();
            JOptionPane.showMessageDialog(this, e);
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
        NDSControl.removeAllHeading=removeAllHeading;
        NDSControl.removeAllFooting=removeAllFooting;
        
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
    
	
    private void portraitRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	ddc.setPortrait(portraitRadioButton.isSelected());
    	String hs=heightTextField.getText();
    	String ws=widthTextField.getText();
    	
    	heightTextField.setText(ws);
    	widthTextField.setText(hs);
    }
    
    private void landscapeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	ddc.setPortrait(portraitRadioButton.isSelected());
    	
    	String hs=heightTextField.getText();
    	String ws=widthTextField.getText();
    	
    	heightTextField.setText(ws);
    	widthTextField.setText(hs);
    }
    
	private void initComponents(){
		
		/*
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				applyTableControl();
			}
		});
		*/
		JPanel panel_4 = new JPanel();
		
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder(null, "Decay/Level scheme settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		spacingTextField = new JTextField();
		spacingTextField.setColumns(10);
		
		heightTextField = new JTextField();
		heightTextField.setColumns(10);
		
		lblHeight = new JLabel("height=");
		
		lblSpacing = new JLabel("spacing (in pt)");
		lblSpacing.setToolTipText("minimum spacing in pt between levels in the drawing");
		
		JLabel label = new JLabel("Orientation:");
		
		buttonGroup1 = new javax.swing.ButtonGroup();
		
		portraitRadioButton = new JRadioButton();
		portraitRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				portraitRadioButtonActionPerformed(e);
			}
		});
		portraitRadioButton.setText("Portrait");
        
		
		landscapeRadioButton = new JRadioButton();
		landscapeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				landscapeRadioButtonActionPerformed(e);
			}
		});
		landscapeRadioButton.setText("Landscape");
		
		buttonGroup1.add(portraitRadioButton);
		buttonGroup1.add(landscapeRadioButton);
		
		
		JLabel lblDimensions = new JLabel("<HTML><center>Dimensions:<br>(limits)</center></HTML>");
		lblDimensions.setToolTipText("The actual size is determined and optimized according to the complexity of the level scheme.");
		
		lblCm = new JLabel("cm");
		
		JLabel lblWidth = new JLabel("width=");
		
		widthTextField = new JTextField();
		widthTextField.setColumns(10);
		
		JLabel label_2 = new JLabel("cm");
		
		JLabel lblmaxHeightWidth = new JLabel("(maximum height=22.0 cm width=16.5 cm minimum level spacing=6 pt in portrait orientation)");
		
		JSeparator separator1 = new JSeparator();
		
		lblDecayScheme = new JLabel("Decay scheme:");
		
		showGSQValueCheckBox = new JCheckBox(" show GS to GS Q-value");
		showGSQValueCheckBox.setEnabled(false);
		showGSQValueCheckBox.setToolTipText("If not checked, the real Q-value is calculated and shown for metastable to ground decay.");
		
		showQValueUnitCheckBox = new JCheckBox("show unit (keV) in Q-value");
		showQValueUnitCheckBox.setEnabled(false);
		
		JLabel lblBranchingTableX = new JLabel("branching table X offset:");
		lblBranchingTableX.setToolTipText("<HTML>X offset of the left edge of the adjacent column relative to the edges of daughter levels. <br> If set too small, the default value will be used to avoid overlap.</HTML>");
		
		tableXTextField = new JTextField();
		tableXTextField.setEnabled(false);
		tableXTextField.setColumns(10);
		
		JLabel label_1 = new JLabel("cm");
		
		showTitleCheckBox = new JCheckBox("show title");
		showTitleCheckBox.setToolTipText("");
		
		JSeparator separator = new JSeparator();
		
		showLegendCheckBox = new JCheckBox("show legend");
		showLegendCheckBox.setToolTipText("");
		panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
		btnDrawSelected = new JButton("Generate level scheme");
		btnDrawSelected.setToolTipText("double-click to create and view a PDF output. ");
		//btnDrawSelected.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		writeSelected();
		//	}
		//});
		
        btnDrawSelected.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent evt) {
        		drawSelected(evt);
        	}
        });
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(247)
							.addComponent(checkBox_4, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(12)
							.addComponent(mainPanel, GroupLayout.PREFERRED_SIZE, 911, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(276)
							.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(12)
							.addComponent(btnDrawSelected)))
					.addGap(0))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(checkBox_4, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(mainPanel, GroupLayout.PREFERRED_SIZE, 519, GroupLayout.PREFERRED_SIZE)
					.addGap(10)
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(btnDrawSelected))
		);
		GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
		gl_mainPanel.setHorizontalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(6)
					.addComponent(label, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(portraitRadioButton, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(landscapeRadioButton))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(6)
					.addComponent(separator1, GroupLayout.PREFERRED_SIZE, 870, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(8)
					.addComponent(lblDecayScheme, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(showGSQValueCheckBox, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(showQValueUnitCheckBox, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(124)
					.addComponent(lblBranchingTableX, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(tableXTextField, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(6)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 870, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(6)
					.addComponent(showTitleCheckBox, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(showLegendCheckBox, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(6)
					.addComponent(lblDimensions, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(lblCm, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(lblSpacing, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
							.addGap(5)
							.addComponent(spacingTextField, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblmaxHeightWidth, GroupLayout.PREFERRED_SIZE, 533, GroupLayout.PREFERRED_SIZE)))
		);
		gl_mainPanel.setVerticalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(label))
						.addComponent(portraitRadioButton)
						.addComponent(landscapeRadioButton))
					.addGap(9)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addGap(2)
									.addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
								.addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addGap(2)
									.addComponent(lblCm, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addGap(2)
									.addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
								.addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addGap(2)
									.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_mainPanel.createSequentialGroup()
									.addGap(2)
									.addComponent(lblSpacing, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
								.addComponent(spacingTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
							.addGap(6)
							.addComponent(lblmaxHeightWidth))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(2)
							.addComponent(lblDimensions, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)))
					.addGap(9)
					.addComponent(separator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblDecayScheme, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(showGSQValueCheckBox))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(showQValueUnitCheckBox)))
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(6)
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(tableXTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
								.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(8)
							.addComponent(lblBranchingTableX, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
					.addGap(12)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
					.addGap(7)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(showTitleCheckBox)
						.addComponent(showLegendCheckBox))
					.addGap(279))
		);
		mainPanel.setLayout(gl_mainPanel);
		setLayout(groupLayout);
		

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
    private JPanel mainPanel;
    private JTextField heightTextField;
    private JLabel lblHeight;
    private JLabel lblSpacing;
    private JTextField spacingTextField;
    private final JCheckBox checkBox_4 = new JCheckBox("Landscape");
    private JLabel lblCm;
    private JTextField widthTextField;
    private JLabel lblDecayScheme;
    private JTextField tableXTextField;
    private JRadioButton portraitRadioButton;
    private JRadioButton landscapeRadioButton;
    private JCheckBox showLegendCheckBox;
    private JCheckBox showTitleCheckBox;
    private JCheckBox showGSQValueCheckBox;
    private JCheckBox showQValueUnitCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
}
