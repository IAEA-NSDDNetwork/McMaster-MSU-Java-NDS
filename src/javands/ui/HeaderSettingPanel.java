package javands.ui;

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
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.UIManager;

import java.awt.Color;
import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class HeaderSettingPanel extends BaseSettingPanel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    MassChain data;
    Run run;
    
    TableControl ltc,gtc,dtc,ptc;
    DrawingControl bdc;//band drawing control
    DrawingControl ddc;//decay/level drawing control
    
    /* Data related to the abstract for the data set*/
    private boolean hasAbstract;
    private String abstractVolume;
    private String year="****";
    private String receivedDate="****";
    private String revisedDate="****";
    private String page="****";
    private String Z="";
    
    /*other data for header */
    private boolean hasIndex;
    private boolean blankPage; //put a blank page after the index
    private boolean hasReference;
    private String referenceA="";
    private String type="REVIEW";
    private boolean hasSkeleton;
    private String skelOptions="";
    private boolean isUpdate;
    private String updCom1="";//editor's comment on cover page, see LatexWriter
    private String updCom2="";//editor's comment in individual data set (level table only), see LevelTableMaker
    
    private boolean isPortrait=true;
    private boolean isNewPage=false;
    
    private boolean isBandCreated=false;
    

    
    boolean isSkelPortrait;
    float ladderWidth,ladderSpacing,parentSpace,tableX,tableY;
    int pageNo;
    String forcedBreaks;
    
    boolean isDefaultSetting=true;
	public HeaderSettingPanel(MassChain mass,javands.main.Run r) {

        run=r;
        data=mass;
        
		initComponents();
		
		displayHeaderSetting();      
		
		isDefaultSetting=false;
		
	}
	
	private void readControl(){
		hasAbstract=NDSControl.hasAbstract;
		abstractVolume=NDSControl.abstractVolume;
		year=NDSControl.year;
		receivedDate=NDSControl.receivedDate;
		revisedDate=NDSControl.revisedDate;
		page=NDSControl.page;
		Z=NDSControl.Z;
		
		hasIndex=NDSControl.hasIndex;
		blankPage=NDSControl.blankPage;
		hasReference=NDSControl.hasReference;
		referenceA=NDSControl.referenceA;
		type=NDSControl.type;
		hasSkeleton=NDSControl.hasSkeleton;
		skelOptions=NDSControl.skelOptions;
		isUpdate=NDSControl.isUpdate;
		updCom1=NDSControl.updCom1;
		updCom2=NDSControl.updCom2;
		isPortrait=NDSControl.isPortrait;
		isNewPage=NDSControl.isNewPage;
		isBandCreated=NDSControl.isBandCreated;
		
		if(data.getComBlock().size()==0) 
			hasAbstract=false;
		
        int nENSDFs=data.nENSDF();
         
        if(nENSDFs<=2){
        	hasIndex=false;
        	hasSkeleton=false;
        	blankPage=false;
        }
	}
	
	private void setControl(){
		
		NDSControl.hasAbstract=hasAbstract;
		NDSControl.abstractVolume=abstractVolume;
		NDSControl.year=year;
		NDSControl.receivedDate=receivedDate;
		NDSControl.revisedDate=revisedDate;
		NDSControl.page=page;
		NDSControl.Z=Z;
		
		NDSControl.hasIndex=hasIndex;
		NDSControl.blankPage=blankPage;
		NDSControl.hasReference=hasReference;
		NDSControl.referenceA=referenceA;
		NDSControl.type=type;
		NDSControl.hasSkeleton=hasSkeleton;
		NDSControl.skelOptions=skelOptions;
		NDSControl.isUpdate=isUpdate;
		NDSControl.updCom1=updCom1;
		NDSControl.updCom2=updCom2;
		NDSControl.isPortrait=isPortrait;
		NDSControl.isNewPage=isNewPage;
		NDSControl.isBandCreated=isBandCreated;
	}
	
	/*display control settings for selected data set in control panel**/
	private void displayHeaderSetting(){
		
		readControl();//Control.skelOptions is updated after calling setControl() here 
		              //as well as after calling drawSkeletronChart() in writeLatex() in Run.java,
		              //which is called here after clicking "Draw header only" button

		if(type.toUpperCase().equals("PUBLICATION")){
			reviewRadioButton.setSelected(false);
			publicationRadioButton.setSelected(true);
		}
		else{
			type="REVIEW";
			reviewRadioButton.setSelected(true);
			publicationRadioButton.setSelected(false);
			
		}
		
		
		boolean isSelected;
		
		//System.out.println(" Control.isHeaderSettingModified="+Control.isHeaderSettingModified);
		
		if(isDefaultSetting) {
		    setEnabled(chckbxAbstract,NDSControl.hasAbstract);        
	        setEnabled(indexCheckBox,NDSControl.hasIndex);
	        setEnabled(blankPageCheckBox,NDSControl.hasIndex);
	        setEnabled(referenceCheckBox,NDSControl.hasReference);
	        setEnabled(skeletonCheckBox,NDSControl.hasSkeleton&&!NDSControl.isHeaderSettingModified);
		}
		
		setSelected(chckbxAbstract,hasAbstract);
		isSelected=(chckbxAbstract.isSelected()&&hasAbstract);
		
		setEnabled(lblVol,isSelected);
		setEnabled(volTextField,isSelected);
		setEnabled(lblYear,isSelected);
		setEnabled(yearTextField,isSelected);
		setEnabled(lblZ,isSelected);
		setEnabled(ZTextField,isSelected);
		setEnabled(lblReceivedDate,isSelected);
		setEnabled(lblPage,isSelected);
		setEnabled(pageTextField,isSelected);
		setEnabled(receivedDateTextField,isSelected);
		setEnabled(lblRevisedDate,isSelected);
		setEnabled(revisedDateTextField,isSelected);
		if(isSelected){
			volTextField.setText(abstractVolume);
			yearTextField.setText(year);
			ZTextField.setText(Z);
			receivedDateTextField.setText(receivedDate);
			revisedDateTextField.setText(revisedDate);
			pageTextField.setText(page);
		}

        
	    setSelected(indexCheckBox,hasIndex);
	    setSelected(blankPageCheckBox,hasIndex);
	    setSelected(referenceCheckBox,hasReference);
	    
	    setSelected(massListCheckBox,(referenceA.length()>0));
	    setEnabled(massListTextField,(referenceA.length()>0));
	    if(referenceA.length()>0)
	    	massListTextField.setText(referenceA);
	    
	    
	    //for skeleton

        if(skelOptions.contains("/H:")) isSkelPortrait=false;
        else isSkelPortrait=true;
        
        try{ ladderWidth=Float.parseFloat(NDSControl.getLineAsString(skelOptions,"/W:"));
        }catch(Exception e){ ladderWidth=2.1f; 
        }
        
        try{ ladderSpacing=Float.parseFloat(NDSControl.getLineAsString(skelOptions,"/S:"));
        }catch(Exception e){ ladderSpacing=0.5f;}
        
        try{pageNo=Integer.parseInt(NDSControl.getLineAsString(skelOptions,"/P:"));
        }catch(Exception e){ pageNo=-1;}

        try{ parentSpace=Float.parseFloat(NDSControl.getLineAsString(skelOptions,"/T:"));
        }catch(Exception e){ parentSpace=1.5f;}
        
        @SuppressWarnings("unused")
		float H,W;
        if(isSkelPortrait){
            H=NDSConfig.MAX_TEXT_HEIGHT-0.3f;
            W=NDSConfig.MAX_TEXT_WIDTH;
        }else{
            W=NDSConfig.MAX_TEXT_HEIGHT-0.3f;
            H=NDSConfig.MAX_TEXT_WIDTH;
        }
        
        //get location of summary table
        try{ tableX=Float.parseFloat(NDSControl.getLineAsString(skelOptions,"/X:"));
        }catch(Exception e){ tableX=W;}

        try{ tableY=Float.parseFloat(NDSControl.getLineAsString(skelOptions,"/Y:"));
        }catch(Exception e){ tableY=-0.8f;}

        
        if(skelOptions.contains("/B:")) forcedBreaks=NDSControl.getLineAsString(skelOptions,"/B:");
        

	    setSelected(skeletonCheckBox,hasSkeleton);
	      
	    isSelected=(skeletonCheckBox.isSelected()&&hasSkeleton);
	     
		setEnabledAndSelected(landscapeCheckBox,isSelected,isSelected&&!isSkelPortrait);
		
		setEnabled(lblPages,isSelected);
		setEnabled(summaryPageNoTextField,isSelected);
		setEnabled(ladderWidthLabel,isSelected);
		setEnabled(ladderWidthTextField,isSelected);
		setEnabled(spacingLabel,isSelected);
		setEnabled(spacingTextField,isSelected);
		setEnabled(parentSpacingLabel,isSelected);
		setEnabled(parentSpacingTextField,isSelected);
		setEnabled(ladderIndexLabel,isSelected);
		setEnabled(ladderIndexTextField,isSelected);
		setEnabled(summaryTableLocLabel,isSelected);
		setEnabled(summaryTableXTextField,isSelected);
		setEnabled(lblX,isSelected);
		setEnabled(lblY,isSelected);
		setEnabled(summaryTableYTextField,isSelected);
		if(isSelected){
			summaryPageNoTextField.setText(String.valueOf(pageNo));
			ladderWidthTextField.setText(String.format("%.2f",ladderWidth));
			spacingTextField.setText(String.format("%.2f",ladderSpacing));
			parentSpacingTextField.setText(String.format("%.2f",parentSpace));
			summaryTableXTextField.setText(String.format("%.2f",tableX));
			summaryTableYTextField.setText(String.format("%.2f",tableY));
			if(skelOptions.contains("/B:"))
				ladderIndexTextField.setText(forcedBreaks);
		}
		
		
		if(isUpdate &&updCom1.length()>0)
			editorNoteTextField.setText(updCom1);
		if(isUpdate&&updCom2.length()>0)
			editorNoteXREFTextField.setText(updCom2);
	}
	
    
	/*set header control settings based on inputs in control panel**/
	private void applyHeaderSetting(){
		
        updateHeaderSetting();        
        setControl();

        
        //isModified=Control.isTableControlModified || Control.isHeaderSettingModified
		NDSControl.isModified=true;
		NDSControl.isHeaderSettingModified=true;
		
        displayHeaderSetting();
	}

	
	private void updateHeaderSetting(){
		String text="";
		
        if(publicationRadioButton.isSelected())
        	type="PUBLICATION";
        else
        	type="REVIEW";
        
        if(chckbxAbstract.isSelected()){
        	
        	//volume
            text=volTextField.getText().trim();
            if(text.length()>0){
            	abstractVolume=text;
            	if(!Str.isNumeric(text))
            		//JOptionPane.showMessageDialog(this, "Warning: wrong input for abstract volume!");
            		run.printMessage("Warning: non-numerical input for abstract volume!");
           
            }	
            
            //year
            text=yearTextField.getText().trim();
            if(text.length()>0){
            	year=text;
            	if(!Str.isNumeric(text) || text.contains("."))
            		//JOptionPane.showMessageDialog(this, "Warning: Wrong input for abstract year!");
            	    run.printMessage("Warning: non-numerical input for abstract year!");
            }
            
            //revised date
            text=pageTextField.getText().trim();
            if(text.length()>0){
            		page=text;
            }
            
            //Z
            text=ZTextField.getText().trim();
            if(text.length()>0){
            	Z=text;
            	if(!Str.isNumeric(text) || text.contains("."))
            		//JOptionPane.showMessageDialog(this, "Warning: Wrong input for abstracct Z!");
            		run.printMessage("Warning: non-numerical input for Z!");
            }
            
            //received date
            text=receivedDateTextField.getText().trim();
            if(text.length()>0){
            		receivedDate=text;
            }
            
            //revised date
            text=revisedDateTextField.getText().trim();
            if(text.length()>0){
            		revisedDate=text;
            }
        }
        
		if(indexCheckBox.isSelected())
			hasIndex=true;
		else
			hasIndex=false;
		
		if(blankPageCheckBox.isSelected())
			blankPage=true;
		else
			blankPage=false;
		
		if(referenceCheckBox.isSelected())
			hasReference=true;
		else
			hasReference=false;
				
		if(massListCheckBox.isSelected()){
            text=massListTextField.getText().trim();
            if(text.length()>0){
            		referenceA=text;
            }
		}
        
		
		//for skeleton
		String options="";//to be written in a final control file
		hasSkeleton=false;
        if(skeletonCheckBox.isSelected()){
        	hasSkeleton=true;
        	
        	options+="SKELETON";
        	
        	//portrait or landscape
            if(landscapeCheckBox.isSelected()){
            	isSkelPortrait=false;
            	options+="/H:";
            }
            else
            	isSkelPortrait=true;
            
            //pageNo
            text=summaryPageNoTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text) && !text.contains(".")){
            		pageNo=Integer.parseInt(text);
            		options+="/P:"+pageNo;
            	}
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for skeleton page number!");
            }
            
            //ladder width
            text=ladderWidthTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text)){
            		ladderWidth=Float.parseFloat(text);
            		options+="/W:"+ladderWidth;
            	}
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for skeleton ladder width!");
            }
            
            //ladder spacing
            text=spacingTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text)){
            		ladderSpacing=Float.parseFloat(text);
            		options+="/S:"+ladderSpacing;
            	}
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for skeleton ladder spacing!");
            }
            
            //parent spacing
            text=parentSpacingTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text)){
            		parentSpace=Float.parseFloat(text);
            		options+="/T:"+parentSpace;
            	}
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for skeleton parent ladder spacing!");
            }
         
            //table location (bottom right):X
            text=summaryTableXTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text)){
            		tableX=Float.parseFloat(text);
            		options+="/X:"+tableX;
            	}
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for skeleton table location X!");
            }
            
            //table location (bottom right):Y
            text=summaryTableYTextField.getText().trim();
            if(text.length()>0){
            	if(Str.isNumeric(text)){
            		tableY=Float.parseFloat(text);
            		options+="/Y:"+tableY;
            	}
            	else
            		JOptionPane.showMessageDialog(this, "Wrong input for skeleton table location Y!");
            }
            
            //forced breaks
            text=ladderIndexTextField.getText().trim();
            if(text.length()>0){
            	forcedBreaks=text;
            	options+="/B:"+forcedBreaks;
            }
            
        }
        
        if(options.length()>0)
        	skelOptions=options;
        
        
        //editor's note
        text=editorNoteTextField.getText().trim();
        if(text.length()>0){
        	updCom1=text;
        	isUpdate=true;
        }
        
        text=editorNoteXREFTextField.getText().trim();
        if(text.length()>0){
        	updCom2=text;
        	isUpdate=true;
        }
	}
    
	
	private void writeHeaderOnly(java.awt.event.MouseEvent evt){
		
    	if(evt.getModifiers()!=InputEvent.BUTTON1_MASK)//left-button, BUTTON2-middle, BUTTON3-right
    		return;
     	
       
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
        	
        	if(data.isEmpty()){
                JOptionPane.showMessageDialog(this,"The input file is not loaded or empty and nothing will be drawn!");
                return;
        	}
    		
        	NDSControl.writeHeaderOnly=true;
        	
        	NDSConfig.latex=javands.main.Setup.outdir+run.dirSeparator()+Integer.toString(data.getA())+".tex";       	
        	
        	run.setData(data);
            run.writeLatex(NDSConfig.latex,data);    
            
            NDSControl.writeHeaderOnly=false;
       	
        }catch(Exception e){
            JOptionPane.showMessageDialog(this, e);
            e.printStackTrace();
        }
        
        
        //run shell command to create PDF file
    	String script="NDS.bat";
    	String path=javands.main.Setup.outdir+"\\"+script;
    	String os=System.getProperty("os.name").toLowerCase();
    	
    	if(os.contains("linux")||os.contains("mac")){
            script="NDS.sh";
            path=javands.main.Setup.outdir+"/"+script;
    	}
    	
    	message="\nTo create PDF file, double click the <Draw header only> button or run the following script manually:\n";
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
	
	
	private void initComponents(){
        	
		JPanel includeSettingPanel = new JPanel();
		includeSettingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Include in output", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		btnOk = new JButton("Set");
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				applyHeaderSetting();
			}
		});
		
		JPanel otherSettingPanel = new JPanel();
		otherSettingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Other settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		editorNoteTextField = new JTextField();
		editorNoteTextField.setColumns(10);
		
		editorNoteXREFTextField = new JTextField();
		editorNoteXREFTextField.setColumns(10);
		
		editorNoteLabel = new JLabel("Editor's Note for update data sets (on cover page):");
		editorNoteLabel.setToolTipText("This is an update dataset. A comment will be placed on the abstract page saying that some datasets are not included. Any dataset in an XREF but not inlucded will generate a comment on the XREF table saying that the dataset is available online.");
		
		editorNoteXREFLabel = new JLabel("Editor's Note for update data sets (on XREF table):");
		editorNoteXREFLabel.setToolTipText("This is an update dataset. A comment will be placed on the abstract page saying that some datasets are not included. Any dataset in an XREF but not inlucded will generate a comment on the XREF table saying that the dataset is available online.");
		
		lblOutputType = new JLabel("Output type:");
		
		reviewRadioButton = new JRadioButton("Review");		
		reviewRadioButton.setSelected(true);
		publicationRadioButton = new JRadioButton("Publication");
		outputTypeButtonGroup = new ButtonGroup();
		outputTypeButtonGroup.add(reviewRadioButton);
		outputTypeButtonGroup.add(publicationRadioButton);
		
		JPanel abstractDrawPanel = new JPanel();
		
		chckbxAbstract = new JCheckBox("Abstract:");
		
		indexDrawPanel = new JPanel();
		
		indexCheckBox = new JCheckBox("Index");
		
		blankPageCheckBox = new JCheckBox("blank page");
		blankPageCheckBox.setToolTipText("include a blank page after the table of contents");
		
		referenceCheckBox = new JCheckBox("Reference");
		
		massListCheckBox = new JCheckBox("mass list:");
		massListCheckBox.setEnabled(false);
		
		massListTextField = new JTextField();
		massListTextField.setEnabled(false);
		massListTextField.setColumns(10);
		GroupLayout gl_indexDrawPanel = new GroupLayout(indexDrawPanel);
		gl_indexDrawPanel.setHorizontalGroup(
		    gl_indexDrawPanel.createParallelGroup(Alignment.LEADING)
		        .addGroup(gl_indexDrawPanel.createSequentialGroup()
		            .addContainerGap()
		            .addComponent(indexCheckBox)
		            .addPreferredGap(ComponentPlacement.RELATED)
		            .addComponent(blankPageCheckBox, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
		            .addGap(4)
		            .addComponent(referenceCheckBox, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
		            .addPreferredGap(ComponentPlacement.RELATED)
		            .addComponent(massListCheckBox)
		            .addPreferredGap(ComponentPlacement.RELATED)
		            .addComponent(massListTextField, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
		            .addContainerGap(237, Short.MAX_VALUE))
		);
		gl_indexDrawPanel.setVerticalGroup(
		    gl_indexDrawPanel.createParallelGroup(Alignment.LEADING)
		        .addGroup(gl_indexDrawPanel.createSequentialGroup()
		            .addGap(4)
		            .addGroup(gl_indexDrawPanel.createParallelGroup(Alignment.BASELINE)
		                .addComponent(indexCheckBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(blankPageCheckBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(massListCheckBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(referenceCheckBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(massListTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
		            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		indexDrawPanel.setLayout(gl_indexDrawPanel);
		
		panel_1 = new JPanel();
		
		skeletonCheckBox = new JCheckBox("Skeleton:");
		
		ladderWidthLabel = new JLabel("ladder width (cm)");
		
		landscapeCheckBox = new JCheckBox("landscape");
		
		ladderWidthTextField = new JTextField();
		ladderWidthTextField.setEnabled(false);
		ladderWidthTextField.setColumns(10);
		
		spacingLabel = new JLabel("spacing");
		spacingLabel.setToolTipText("The spacing between adjacent ladders (in cm)");
		
		spacingTextField = new JTextField();
		spacingTextField.setEnabled(false);
		spacingTextField.setColumns(10);
		
		lblPages = new JLabel("on page#");
		lblPages.setToolTipText("The index of the page to put the summary table on within skeleton pages. Index=1 for the first skeleton page.");
		
		summaryPageNoTextField = new JTextField();
		summaryPageNoTextField.setEnabled(false);
		summaryPageNoTextField.setColumns(10);
		
		parentSpacingLabel = new JLabel("parent spacing");
		parentSpacingLabel.setToolTipText("The spacing between the nuclei and the parent nuclei drawn above");
		
		parentSpacingTextField = new JTextField();
		parentSpacingTextField.setEnabled(false);
		parentSpacingTextField.setColumns(10);
		
		ladderIndexLabel = new JLabel("page-break ladder index");
		ladderIndexLabel.setToolTipText("the indexes of ladders which start a new page; input numbers are separated by comma.");
		
		ladderIndexTextField = new JTextField();
		ladderIndexTextField.setEnabled(false);
		ladderIndexTextField.setColumns(10);
		
		summaryTableLocLabel = new JLabel("summary table location (bottom-right):");
		summaryTableLocLabel.setToolTipText("");
		
		lblY = new JLabel("Y");
		lblY.setToolTipText("");
		
		summaryTableXTextField = new JTextField();
		summaryTableXTextField.setEnabled(false);
		summaryTableXTextField.setColumns(10);
		
		summaryTableYTextField = new JTextField();
		summaryTableYTextField.setEnabled(false);
		summaryTableYTextField.setColumns(10);
		
		lblX = new JLabel("X");
		lblX.setToolTipText("");
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
		    gl_panel_1.createParallelGroup(Alignment.LEADING)
		        .addGroup(gl_panel_1.createSequentialGroup()
		            .addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
		                .addGroup(gl_panel_1.createSequentialGroup()
		                    .addGap(27)
		                    .addComponent(summaryTableLocLabel, GroupLayout.PREFERRED_SIZE, 253, GroupLayout.PREFERRED_SIZE)
		                    .addPreferredGap(ComponentPlacement.RELATED)
		                    .addComponent(lblPages, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
		                    .addGap(5)
		                    .addComponent(summaryPageNoTextField, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
		                    .addGap(10)
		                    .addComponent(lblX, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
		                    .addPreferredGap(ComponentPlacement.RELATED)
		                    .addComponent(summaryTableXTextField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
		                    .addGap(9)
		                    .addComponent(lblY, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
		                    .addGap(4)
		                    .addComponent(summaryTableYTextField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
		                    .addGap(25)
		                    .addComponent(ladderIndexLabel, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
		                    .addGap(6)
		                    .addComponent(ladderIndexTextField, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE))
		                .addGroup(gl_panel_1.createSequentialGroup()
		                    .addContainerGap()
		                    .addComponent(skeletonCheckBox, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
		                    .addPreferredGap(ComponentPlacement.RELATED)
		                    .addComponent(landscapeCheckBox, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
		                    .addGap(12)
		                    .addComponent(ladderWidthLabel, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
		                    .addGap(6)
		                    .addComponent(ladderWidthTextField, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
		                    .addGap(12)
		                    .addComponent(spacingLabel, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
		                    .addPreferredGap(ComponentPlacement.RELATED)
		                    .addComponent(spacingTextField, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
		                    .addGap(18)
		                    .addComponent(parentSpacingLabel, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
		                    .addPreferredGap(ComponentPlacement.RELATED)
		                    .addComponent(parentSpacingTextField, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)))
		            .addContainerGap(39, Short.MAX_VALUE))
		);
		gl_panel_1.setVerticalGroup(
		    gl_panel_1.createParallelGroup(Alignment.TRAILING)
		        .addGroup(gl_panel_1.createSequentialGroup()
		            .addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
		                .addComponent(ladderWidthTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(spacingTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(parentSpacingTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(parentSpacingLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(spacingLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(skeletonCheckBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(landscapeCheckBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(ladderWidthLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
		            .addPreferredGap(ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
		            .addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
		                .addComponent(summaryTableLocLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(summaryPageNoTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(lblX, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(summaryTableXTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(lblY, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(summaryTableYTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(ladderIndexTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
		                .addComponent(lblPages, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
		                .addComponent(ladderIndexLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
		            .addContainerGap())
		);
		panel_1.setLayout(gl_panel_1);
		
		lblVol = new JLabel("Vol.");
		
		volTextField = new JTextField();
		volTextField.setEnabled(false);
		volTextField.setColumns(10);
		
		lblYear = new JLabel("Year");
		
		yearTextField = new JTextField();
		yearTextField.setEnabled(false);
		yearTextField.setColumns(10);
		
		lblReceivedDate = new JLabel("Received date (eg., 24 June 2015)");
		
		receivedDateTextField = new JTextField();
		receivedDateTextField.setEnabled(false);
		receivedDateTextField.setColumns(10);
		
		lblRevisedDate = new JLabel("Revised date");
		
		revisedDateTextField = new JTextField();
		revisedDateTextField.setEnabled(false);
		revisedDateTextField.setColumns(10);
		
		lblZ = new JLabel("Z (if only one nucleus)");
		lblZ.setToolTipText("if this is a publication on only one nucleus");
		
		ZTextField = new JTextField();
		ZTextField.setEnabled(false);
		ZTextField.setColumns(10);
		
		lblPage = new JLabel("Page");
		
		pageTextField = new JTextField();
		pageTextField.setEnabled(false);
		pageTextField.setColumns(10);
		GroupLayout gl_abstractDrawPanel = new GroupLayout(abstractDrawPanel);
		gl_abstractDrawPanel.setHorizontalGroup(
			gl_abstractDrawPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_abstractDrawPanel.createSequentialGroup()
					.addGap(6)
					.addComponent(chckbxAbstract)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_abstractDrawPanel.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addComponent(lblReceivedDate, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(receivedDateTextField, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addComponent(lblVol)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(volTextField, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblYear)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(yearTextField, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
							.addGap(11)
							.addComponent(lblPage, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(pageTextField, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)))
					.addGap(46)
					.addGroup(gl_abstractDrawPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addComponent(lblRevisedDate, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(revisedDateTextField, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
							.addGap(36))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addComponent(lblZ)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(ZTextField, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addGap(89))))
		);
		gl_abstractDrawPanel.setVerticalGroup(
			gl_abstractDrawPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_abstractDrawPanel.createSequentialGroup()
					.addGroup(gl_abstractDrawPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(10)
							.addGroup(gl_abstractDrawPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(ZTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblZ, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
								.addComponent(pageTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(11)
							.addComponent(lblVol, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(10)
							.addComponent(volTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(11)
							.addComponent(lblYear, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(10)
							.addComponent(yearTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(11)
							.addComponent(lblPage, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
					.addGroup(gl_abstractDrawPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(14)
							.addComponent(receivedDateTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(15)
							.addComponent(lblRevisedDate, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(14)
							.addComponent(revisedDateTextField, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_abstractDrawPanel.createSequentialGroup()
							.addGap(15)
							.addComponent(lblReceivedDate, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(27, Short.MAX_VALUE))
				.addGroup(gl_abstractDrawPanel.createSequentialGroup()
					.addGap(11)
					.addComponent(chckbxAbstract, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addGap(70))
		);
		abstractDrawPanel.setLayout(gl_abstractDrawPanel);
		
		
		btnDrawHeaderOnly = new JButton("Draw header only");
		btnDrawHeaderOnly.setToolTipText("Draw the header only with settings here into a LaTeX file. Double click to create and view a PDF output. ");
		btnDrawHeaderOnly.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				writeHeaderOnly(evt);
				
				displayHeaderSetting();
			}
		});
		
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(513)
					.addComponent(lblOutputType)
					.addGap(18)
					.addComponent(reviewRadioButton)
					.addGap(18)
					.addComponent(publicationRadioButton)
					.addGap(73))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(includeSettingPanel, GroupLayout.DEFAULT_SIZE, 911, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnDrawHeaderOnly, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 679, Short.MAX_VALUE)
							.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
						.addComponent(otherSettingPanel, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(21)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblOutputType)
						.addComponent(reviewRadioButton)
						.addComponent(publicationRadioButton))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(includeSettingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(otherSettingPanel, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
					.addGap(55)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnDrawHeaderOnly, Alignment.TRAILING)
						.addComponent(btnOk, Alignment.TRAILING))
					.addContainerGap())
		);
		GroupLayout gl_otherSettingPanel = new GroupLayout(otherSettingPanel);
		gl_otherSettingPanel.setHorizontalGroup(
			gl_otherSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_otherSettingPanel.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_otherSettingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(editorNoteLabel, GroupLayout.PREFERRED_SIZE, 393, GroupLayout.PREFERRED_SIZE)
						.addComponent(editorNoteXREFLabel, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
						.addGroup(Alignment.TRAILING, gl_otherSettingPanel.createSequentialGroup()
							.addGroup(gl_otherSettingPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(editorNoteXREFTextField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
								.addComponent(editorNoteTextField, GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE))
							.addContainerGap())))
		);
		gl_otherSettingPanel.setVerticalGroup(
			gl_otherSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_otherSettingPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(editorNoteLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(editorNoteTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(editorNoteXREFLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(editorNoteXREFTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(5))
		);
		otherSettingPanel.setLayout(gl_otherSettingPanel);
		GroupLayout gl_includeSettingPanel = new GroupLayout(includeSettingPanel);
		gl_includeSettingPanel.setHorizontalGroup(
			gl_includeSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_includeSettingPanel.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_includeSettingPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(indexDrawPanel, GroupLayout.PREFERRED_SIZE, 718, GroupLayout.PREFERRED_SIZE)
						.addComponent(abstractDrawPanel, GroupLayout.DEFAULT_SIZE, 8885, Short.MAX_VALUE)
						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 866, GroupLayout.PREFERRED_SIZE))
					.addGap(0))
		);
		gl_includeSettingPanel.setVerticalGroup(
			gl_includeSettingPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_includeSettingPanel.createSequentialGroup()
					.addComponent(abstractDrawPanel, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(indexDrawPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		includeSettingPanel.setLayout(gl_includeSettingPanel);
		setLayout(groupLayout);
		

	}
    private JButton btnOk;
    private JCheckBox chckbxAbstract;
    private JLabel lblOutputType;
    private JRadioButton publicationRadioButton;
    private ButtonGroup outputTypeButtonGroup;
    private JRadioButton reviewRadioButton;
    private JPanel indexDrawPanel;
    private JCheckBox indexCheckBox;
    private JCheckBox blankPageCheckBox;
    private JCheckBox referenceCheckBox;
    private JCheckBox massListCheckBox;
    private JLabel lblVol;
    private JTextField volTextField;
    private JTextField yearTextField;
    private JTextField receivedDateTextField;
    private JTextField revisedDateTextField;
    private JLabel lblZ;
    private JTextField ZTextField;
    private JPanel panel_1;
    private JCheckBox skeletonCheckBox;
    private JLabel ladderWidthLabel;
    private JCheckBox landscapeCheckBox;
    private JTextField ladderWidthTextField;
    private JLabel spacingLabel;
    private JTextField spacingTextField;
    private JLabel lblPages;
    private JTextField summaryPageNoTextField;
    private JLabel parentSpacingLabel;
    private JTextField parentSpacingTextField;
    private JLabel lblYear;
    private JLabel lblReceivedDate;
    private JLabel lblRevisedDate;
    private JTextField massListTextField;
    private JTextField ladderIndexTextField;
    private JLabel ladderIndexLabel;
    private JTextField editorNoteTextField;
    private JTextField editorNoteXREFTextField;
    private JLabel editorNoteLabel;
    private JLabel editorNoteXREFLabel;
    private JLabel summaryTableLocLabel;
    private JLabel lblY;
    private JTextField summaryTableXTextField;
    private JTextField summaryTableYTextField;
    private JLabel lblPage;
    private JTextField pageTextField;
    private JLabel lblX;
    private JButton btnDrawHeaderOnly;
}
