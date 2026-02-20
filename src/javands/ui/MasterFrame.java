/*
 * masterFrame.java
 *
 * Created on May 10, 2010, 9:38 AM
 */

package javands.ui;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.*;

import java.util.Vector;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultCaret;

import ensdfparser.ensdf.ENSDF;
import ensdfparser.nds.config.NDSConfig;
import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.ensdf.MassChain;
import ensdfparser.nds.util.Str;
import ensdfparser.ui.CustomFrame;
import ensdfparser.ui.TextDisplayFrame;
import javands.main.Setup;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.EmptyBorder;



/**
 * The top-level user interface 
 * @author scott
 */
@SuppressWarnings("unused")
public class MasterFrame extends CustomFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MassChain data;//all of the data in the file
    EnsdfWrap [] ensw;//wrappers for all of the endsfs in the file
    boolean isFileLoaded;
    boolean isFileProcessed;
    boolean isControlLoaded;
    javands.main.Run run;
    File dataFile;

    
    public MasterFrame() {


        isFileLoaded=false;
        isControlLoaded=false;
        isFileProcessed=false;
        
        initComponents();
        
        run=new javands.main.Run(textArea);
        pathField.setText(javands.main.Setup.outdir);
       
        data=new MassChain();
        
        textArea.setTransferHandler(new TransferHandler() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean canImport(TransferHandler.TransferSupport support) {
                if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                } 
                return true;
            }
     
            @SuppressWarnings("unchecked")
    		public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                 
                Transferable t = support.getTransferable();
   
                try {
                    reset(); 
                    
                    java.util.List<File> fileList=(java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
     
                    javands.main.Setup.filedir=fileList.get(0).getAbsolutePath();
                    javands.main.Setup.save();
                    
           
                    //run.printMessage(run.title());
                    
                    File[] files=new File[fileList.size()];
                    fileList.toArray(files);                    

                    loadFiles(files);

                    
                } catch (UnsupportedFlavorException e) {
                    //e.printStackTrace();
                    return false;
                } catch (Exception e) {
                	e.printStackTrace();
                    return false;
                }
     
                return true;
            }
        });
        run.printMessage("*** Run in current folder: "+System.getProperty("user.dir"));
        
        //create a popup for option to delete figure files
    	this.addMouseListener(new FrameMouseAdapter());
    }
    
	private void loadFiles(File[] files) throws Exception{
		try {
	         isFileLoaded=false;
	         isFileProcessed=false;
	         
			if(files.length>1) {
	
				String tempName=javands.main.Setup.outdir+run.dirSeparator()+"temp_input.ens";
				File tempFile=new File(tempName);
				PrintWriter out=new PrintWriter(tempFile);
				
				run.printMessage("Multiple input files:");
				
				Vector<String> lines=new Vector<String>();
				for(int i=0;i<files.length;i++) {
					run.printMessage("  "+files[i].getAbsolutePath());
					lines.addAll(Str.readFile(files[i]));
					lines.add("                              ");
				}
				run.printMessage("will be merged to a single file for processing: temp_input.ens");
				
				Str.write(out,lines);
				out.flush();
				out.close();
				
				dataFile=new File(tempName);
				
		         isFileLoaded=false;
		         isFileProcessed=false;
		         //reset();
				data=run.loadFile(dataFile);
			}else {
				data=run.loadFiles(files);   
				dataFile=files[0];
			}
			
	        int n=data.nENSDF();
	        boolean hasErrorMsg=false;
	        
	        ensw=new EnsdfWrap[n];
	        for(int i=0;i<n;i++){
	            ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
	            ensw[i]=new EnsdfWrap(etd);
	            if(!hasErrorMsg&&data.getENSDF(i).errorMsg().length()>0)
	         	   hasErrorMsg=true;
	        }
	        
	        if(hasErrorMsg)
	     	   run.printMessage("See log.txt for warning messages when loading ENSDF");
	        
	        run.setEnsdfWraps(ensw);
	        run.setData(data);
	        
	        isFileLoaded=true;
	        
	        NDSConfig.ensdfFile=dataFile.getAbsolutePath();
	        
	        
	        //set default output names after file is loaded and it can be override afterwards
	        //Config.latex is set in this call
	        String s=Str.fileNamePrefix(dataFile.getName());
	        s=Str.cleanFilename(s);
	        
	        run.setOutputFilename(s);
	
		}catch(Exception e) {
			
		}

	}
	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loadButton = new javax.swing.JToggleButton();
        pathLabel = new javax.swing.JLabel();
        pathField = new javax.swing.JTextField();
        controlSettingButton = new javax.swing.JToggleButton();
        controlSettingButton.setToolTipText("view/modify control and band settings for individual dataset");
        controlSettingButton.setEnabled(false);
        tableButton = new javax.swing.JToggleButton();
        tableButton.setToolTipText("<HTML>create tables and drawings in LaTeX with auto settings by default, unless control is manually set.<br>double click to run also shell script to create PDF file.</HTML>");
        controlFileButton = new javax.swing.JButton();
        controlFileButton.setToolTipText("load a control file for manual display settings for tables and figures");
        controlFileButton.setEnabled(false);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        loadButton.setText("Load ENSDF File");
        /*
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });
        */
        loadButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent evt) {
        		loadButtonMouseClicked(evt);
        	}
        });
        
        pathLabel.setText("Output path:");

        pathField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                pathFieldPropertyChange(evt);
            }
        });
        pathField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pathFieldKeyReleased(evt);
            }
        });

        controlSettingButton.setText("Control Settings");
        controlSettingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controlSettingButtonActionPerformed(evt);
            }
        });

        tableButton.setText("Create LaTeX File");
        
        //tableButton.addActionListener(new java.awt.event.ActionListener() {
        //    public void actionPerformed(java.awt.event.ActionEvent evt) {
        //        tableButtonActionPerformed(evt);
        //    }
        //});
        
        tableButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent evt) {
        		tableButtonMouseClicked(evt);
        	}
        });
        
        controlFileButton.setText("Load control file");
        controlFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controlFileButtonActionPerformed(evt);
            }
        });
        
        controlFileCheckBox = new JCheckBox("use control file");
        controlFileCheckBox.setToolTipText("use a control file for manual settings");
        controlFileCheckBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
                controlFileCheckBoxStateChanged(e);
        	}
        });
     
        controlSettingCheckBox = new JCheckBox("use control panel");
        controlSettingCheckBox.setToolTipText("use graphical control panel for manual settings");
        controlSettingCheckBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
                controlSettingCheckBoxStateChanged(e);
        	}
        });
        
        scrollPane = new JScrollPane();
        //scrollPane.setViewportBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "message", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),"message"));
        scrollPane.setToolTipText("display the processing status and message.");
        
        browserButton = new JButton("Browse");
        browserButton.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent arg0) {
        		browseButtonMouseClicked(arg0);
        	}
        });
        /*
        browserButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		browseButtonActionPerformed(evt);
        	}
        });
        */
        
        panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Global Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
        
        manual_label = new JLabel("<HTML><center>Custom<br>Settings</center></HTML>");
        manual_label.setToolTipText("<HTML>Manual settings for the layout in output file. If not used, the program will run in automatic mode <br>by default and the output settings can be viewed and adjusted here afterwards.");
        manual_label.setHorizontalAlignment(SwingConstants.CENTER);
        manual_label.setBorder(null);
        //manual_label.setForeground(SystemColor.windowText);
        manual_label.setBackground(UIManager.getColor("ArrowButton.background"));
        
        drawingCheckBox = new JCheckBox("include all drawings");
        
        drawingCheckBox.setSelected(true);
        
        drawingCheckBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		drawingCheckBoxStateChanged(e);
        	}
        });
        drawingCheckBox.setToolTipText("<html> include all band and level/decay drawings in Adopted and individual data sets.<br>If not checked, only band drawings in Adopted and level/decay drawings in individual data sets are included.</html> ");
        
        refCheckBox = new JCheckBox("include reference list");
        refCheckBox.setToolTipText("print reference list at the end of the output");
        
        //drawingCheckBox.setSelected(false);
        //refCheckBox.setSelected(true);
        
        nodrawingCheckBox = new JCheckBox("include no drawings"); 
        nodrawingCheckBox.setSelected(false);
        
        nodrawingCheckBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		nodrawingCheckBoxStateChanged(e);
        	}
        });
        nodrawingCheckBox.setToolTipText("Include no drawings for any dataset");
        
                
        suppressCheckBox = new JCheckBox("suppress all \"S\" records");
        suppressCheckBox.setSelected(true);
        suppressCheckBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		suppressCheckBoxStateChanged(e);
        	}
        });
        suppressCheckBox.setToolTipText("suppress S records in all data sets");
        
        includeTitleCheckBox = new JCheckBox("include title in reference");
        includeTitleCheckBox.setEnabled(false);
        includeTitleCheckBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		includeTitleCheckBoxStateChanged(e);
        	}
        });

        
        moreGlobalSettingButton = new JButton("More");
        moreGlobalSettingButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		moreGlobalSettingButtonActionPerformed(e);
        	}
        });
        //moreGlobalSettingButton.setVisible(false);
        
        showAllAuthorsCheckBox = new JCheckBox("show all authors in reference");
        showAllAuthorsCheckBox.setEnabled(false);
        showAllAuthorsCheckBox.setToolTipText("By default, names of the first four authors are printed out if more than 5.");
		showAllAuthorsCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showAllAuthorsCheckBoxStateChanged(e);
			}
		});
        
        readmeLabel = new JLabel("?");
        readmeLabel.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e0) {
        		
        		final JPopupMenu popupMenu=new JPopupMenu();
    			//popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
    			//popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    			popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
                popupMenu.setForeground(SystemColor.windowText);
                popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));
                
    			 
                JMenuItem instruction=new JMenuItem("Instruction");	 
                instruction.setToolTipText("Java-NDS user's instuction"); 			 
                instruction.addActionListener(new ActionListener(){
    				//item.addMouseListener(new MouseAdapter(){
    			    //public void mouseClicked(MouseEvent evt){				
                	public void actionPerformed(ActionEvent evt){
    						
                   		try {
			                String filename="McMaster_MSU_JAVA_NDS_README.pdf";
			                InputStream is=null;
					                
			                is=Str.getInputStream(filename);
			                
			                if (is!=null){
			                    try{      
			               
			                        //Path tempOutput = Files.createTempFile("TempManual", ".pdf");
			                        //File tempFile=tempOutput.toFile();

			                        //File tempFile=File.createTempFile("TempManual", ".pdf");
			                        File tempFile=new File(System.getProperty("java.io.tmpdir"),filename);
			                        Path tempOutput=tempFile.toPath();
			                        
			                        System.out.println("tempOutput: " + tempOutput);
			                        
			                        tempFile.deleteOnExit();
			                        Files.copy(is, tempOutput, StandardCopyOption.REPLACE_EXISTING);
			                        
			                        Desktop.getDesktop().open(tempFile);
			                        
			                    }catch(Exception e1){
			                    	//e1.printStackTrace();
			                        run.printMessage("Error: cannot open instruction file\n"+filename);
			                        run.printMessage("     : it could be already open\n");
			                    }
			                }else{
			                    run.printMessage("Error: instruction file not found:\n    "+filename);
			                }
					
                   		} catch (Exception e) {						
                   			// TODO Auto-generated catch block					
                   			e.printStackTrace();									
                   		}               	
                	}			 
               
                });
                
      			 popupMenu.add(instruction);
    			 
                 JMenuItem latexSymbol=new JMenuItem("LaTeX symbols");	 
                 latexSymbol.setToolTipText("ENSDF expressions for LaTeX symbols"); 			 
                 latexSymbol.addActionListener(new ActionListener(){

    				@Override
    				public void actionPerformed(ActionEvent e) {
    					String text="";
    					String title="ENSDF formats of LaTeX symbols";
    					
		                String filename="latex_dic.dat";
		                InputStream is=null;
		  
		                is=Str.getInputStream(filename);
		                
		                try {
			                text=Str.readInputStreamToString(is);
	    					showTextFrame(latexSymbolFrame,title,text);
		                }catch(Exception e1) {
		                	//e1.printStackTrace();
		                	
		                	run.printMessage("Error: latex symbol file not found:\n    "+filename);
		                }

    					
    				}
                	 
                 });
                 
                 popupMenu.add(latexSymbol); 
                 
    			 popupMenu.show(e0.getComponent(), e0.getX(), e0.getY());
	 
        	}
        });
        readmeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        readmeLabel.setToolTipText("read me");
        readmeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        readmeLabel.setForeground(new Color(0, 0, 255));
        
        //////////////////////////////////////////
        // LAYOUT
        /////////////////////////////////////////
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addGap(0)
        			.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(4)
        					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(6)
        					.addComponent(pathLabel)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(pathField, GroupLayout.PREFERRED_SIZE, 446, GroupLayout.PREFERRED_SIZE)
        					.addGap(4)
        					.addComponent(browserButton, GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        					.addGap(1))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(4)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
        						.addComponent(loadButton, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
        						.addComponent(tableButton, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(readmeLabel, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
        					.addComponent(manual_label, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        						.addComponent(controlSettingCheckBox)
        						.addComponent(controlFileCheckBox))
        					.addGap(18)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
        						.addComponent(controlSettingButton, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
        						.addComponent(controlFileButton, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(4)
        					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)))
        			.addGap(4))
        );
        groupLayout.setVerticalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        			.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(loadButton)
        						.addComponent(controlFileButton)
        						.addComponent(controlFileCheckBox, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
        						.addComponent(readmeLabel))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        							.addComponent(controlSettingButton)
        							.addComponent(controlSettingCheckBox, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
        						.addComponent(tableButton))
        					.addGap(6))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addComponent(manual_label, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
        					.addGap(15)))
        			.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(6)
        			.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(pathField, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
        				.addComponent(browserButton)
        				.addComponent(pathLabel, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
        			.addGap(6)
        			.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
        );
        
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(2)
        			.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        				.addComponent(drawingCheckBox)
        				.addComponent(nodrawingCheckBox, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        				.addComponent(refCheckBox)
        				.addComponent(suppressCheckBox, GroupLayout.PREFERRED_SIZE, 181, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel.createSequentialGroup()
        					.addComponent(includeTitleCheckBox)
        					.addPreferredGap(ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
        					.addComponent(moreGlobalSettingButton, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
        				.addComponent(showAllAuthorsCheckBox, GroupLayout.PREFERRED_SIZE, 219, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap())
        );
        gl_panel.setVerticalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(3)
        			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        				.addComponent(refCheckBox)
        				.addComponent(drawingCheckBox)
        				.addComponent(includeTitleCheckBox))
        			.addGap(3)
        			.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        					.addComponent(suppressCheckBox, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
        					.addComponent(showAllAuthorsCheckBox))
        				.addComponent(nodrawingCheckBox, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
        			.addContainerGap())
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(1)
        			.addComponent(moreGlobalSettingButton, GroupLayout.PREFERRED_SIZE, 23, Short.MAX_VALUE)
        			.addGap(29))
        );

                
               
                
             
        panel.setLayout(gl_panel);     
        
        
        refCheckBox.addItemListener(new ItemListener() {               	
        	public void itemStateChanged(ItemEvent e) {                        
        		refCheckBoxStateChanged(e);                	
        	}                
        });
              

        refCheckBox.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent evt) {
        		refCheckBoxMouseClicked(evt);
        	}
        });
        
        
        groupLayout.setHonorsVisibility(false);
        
        textArea = new JTextArea();
        //textArea.addCaretListener(new CaretListener() {
        //	public void caretUpdate(CaretEvent e) {
        //		textArea.update(textArea.getGraphics());
        //		scrollPane.update(scrollPane.getGraphics());
        //	}
       // });
        
		textArea.setMargin(new Insets(5,5,5,5));
		textArea.setEditable(false);

		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
		//caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		//PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		//System.setOut(printStream);
		
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.add(textArea);
        scrollPane.setViewportView(textArea);

        getContentPane().setLayout(groupLayout);
        
        pack();
		textArea.setVisible(true);
        scrollPane.setVisible(true);
    }// </editor-fold>//GEN-END:initComponents


	private void resetUI(){
    	drawingCheckBox.setSelected(true);
    	nodrawingCheckBox.setSelected(false);
    	refCheckBox.setSelected(false);
    	suppressCheckBox.setSelected(true);
    	
    	includeTitleCheckBox.setEnabled(false);
    	includeTitleCheckBox.setSelected(false);
    	
    	showAllAuthorsCheckBox.setEnabled(false);
    	showAllAuthorsCheckBox.setSelected(false);
    	
    	controlFileCheckBox.setSelected(false);
    	controlFileButton.setEnabled(false);
    	
    	controlSettingCheckBox.setSelected(false);
    	controlSettingButton.setEnabled(false);
    	
    
    	if(globalSettingFrame!=null) 
    		globalSettingFrame.resetUI();
    	
    	   	
    }


    
    protected void refCheckBoxMouseClicked(MouseEvent evt) {
    	
    	if(!refCheckBox.isEnabled() || !refCheckBox.isSelected())
    		return;
    	
		if(evt.getModifiers()==InputEvent.BUTTON1_MASK){//left-button, BUTTON2-middle, BUTTON3-right
			//do nothing
			return;
		}else if(evt.getModifiers()==InputEvent.BUTTON3_MASK){
			
			final JPopupMenu popupMenu=new JPopupMenu();
			//popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
			//popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
            popupMenu.setForeground(SystemColor.windowText);
            popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));
			 
			 JMenuItem loadNSRFileMenuItem=new JMenuItem("Load NSR file");
			 loadNSRFileMenuItem.setToolTipText("<HTML>Open an NSR file containing NSR information of keynumbers in Exchange format.<br>"
			 		+ "This file can be manually generated from a list of keynumers using NSR search page.</HTML>");
			 loadNSRFileMenuItem.addActionListener(new ActionListener(){
			 //itemLoadKeynumbers.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 if(true){//if(evt.getButton()==1){//left clicked
						 //debug
						 //System.out.println("In MasterFrame: figurefiles="+run.getLatexWriter().getFigureFiles().size());
						 try {									 
							 loadNSRFileBrowser();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							run.printMessage("Error when loading the NSR file.\n");
							e.printStackTrace();
						}
					 }
				 }
			 });
			 popupMenu.add(loadNSRFileMenuItem); 
			 
			 popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			 
		}
		
	}
    
    private void reset() {
		try{
	        run.clear();
	        NDSControl.reset();      
	        resetUI();			
		}catch(Exception e){	
			e.printStackTrace();
		}
		data.setPrintDocumentRecord(false);
    }
    
	private void loadButtonMouseClicked(java.awt.event.MouseEvent evt) {


		if(evt.getModifiers()==InputEvent.BUTTON1_MASK){//left-button, BUTTON2-middle, BUTTON3-right
			reset();
			
			loadDataFileBrowser();

		}
		else if(evt.getModifiers()==InputEvent.BUTTON3_MASK){
			
			final JPopupMenu popupMenu=new JPopupMenu();
			//popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
			//popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
            popupMenu.setForeground(SystemColor.windowText);
            popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));
			 
			 JMenuItem itemWebDisplay=new JMenuItem("Open as for web-display");
			 itemWebDisplay.setToolTipText("Open an ENSDF file with the same display settings as for web-display");
			 itemWebDisplay.addActionListener(new ActionListener(){
			 //itemWebDisplay.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 if(true){//if(evt.getButton()==1){//left clicked
						 //debug
						 //System.out.println("In MasterFrame: figurefiles="+run.getLatexWriter().getFigureFiles().size());
						 try {
							 reset();
							 
							 NDSControl.isForWebDisplay=true;
							 NDSControl.printDocumentRecord=true;
							 data.setPrintDocumentRecord(true);
							 
							 loadDataFileBrowser();
							 run.printMessage("ENSDF file is loaded as for web-display (also print out document records).\n");

						} catch (Exception e) {
							// TODO Auto-generated catch block
							run.printMessage("Error when loading ENSDF file as for web-display.\n");
							e.printStackTrace();
						}
					 }
				 }
			 });
			 popupMenu.add(itemWebDisplay); 
			 
			 JMenuItem itemLoadKeynumbers=new JMenuItem("Load a list of keynumbers");
			 itemLoadKeynumbers.setToolTipText("Open a list of keynumbers or an ENSDF file for genering reference list only");
			 itemLoadKeynumbers.addActionListener(new ActionListener(){
			 //itemLoadKeynumbers.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 if(true){//if(evt.getButton()==1){//left clicked
						 //debug
						 //System.out.println("In MasterFrame: figurefiles="+run.getLatexWriter().getFigureFiles().size());
						 try {			
							 reset();
							 
							 NDSControl.isLoadedKeynumberFile=true;
							 NDSControl.removeAllHeading=true;
							 
							 refCheckBox.setSelected(true);
							 
							 loadDataFileBrowser();
							 run.printMessage("A text file of a list of keynumbers is loaded for generating reference list only.\n");

						} catch (Exception e) {
							// TODO Auto-generated catch block
							run.printMessage("Error when loading the list of keybumbers.\n");
							e.printStackTrace();
						}
					 }
				 }
			 });
			 popupMenu.add(itemLoadKeynumbers); 
			 
			 JMenuItem itemReloadDefault=new JMenuItem("Reload last file (reset settings)");
			 itemReloadDefault.setToolTipText("Reload last-loaded file with default settings");
			 itemReloadDefault.addActionListener(new ActionListener(){
			 //itemLoadKeynumbers.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 reset();
					 
					 reloadLastFile();
				 }
			 });
			 popupMenu.add(itemReloadDefault); 
			 
			 JMenuItem itemReload=new JMenuItem("Reload last file (keep settings)");
			 itemReload.setToolTipText("Reload last-loaded file");
			 itemReload.addActionListener(new ActionListener(){
			 //itemLoadKeynumbers.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 reloadLastFile();
				 }
			 });
			 popupMenu.add(itemReload); 
			 
			 popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			 
		}
		
		
		
	}
    /*
     * reload last-loaded file
     * used when a file is changed
     */
    private void reloadLastFile() {

        try{    
        	if(dataFile==null) {
        		run.printMessage("Warning: no file to be reloaded. Please load a file first");
        		return;
        	}
            isFileLoaded=false;
            isFileProcessed=false;
            
            data.clear();            
            run.printMessage("Reloading file: "+dataFile.getAbsolutePath());
            
            data.load(dataFile);
            
            run.printMessage("Done reloading");
  
            int n=data.nENSDF();
            boolean hasErrorMsg=false;
            
            ensw=new EnsdfWrap[n];
            for(int i=0;i<n;i++){
                ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
                ensw[i]=new EnsdfWrap(etd);
                if(!hasErrorMsg&&data.getENSDF(i).errorMsg().length()>0)
             	   hasErrorMsg=true;
            }
            
            if(hasErrorMsg)
         	   run.printMessage("See log.txt for warning messages when loading ENSDF");
            
            run.setEnsdfWraps(ensw);
            run.setData(data);
            
            isFileLoaded=true;
            
            NDSConfig.ensdfFile=dataFile.getAbsolutePath();
            
            
            //set default output names after file is loaded and it can be override afterwards
            //Config.latex is set in this call
            String s=Str.fileNamePrefix(dataFile.getName());
            s=Str.cleanFilename(s);
            
            run.setOutputFilename(s);
              
          
        }catch(FileNotFoundException e){
           	String message="Error: cannot create log file:"+run.getLogFilePath()+"\nPlease check output path.";
           	JOptionPane.showMessageDialog(this,message);
           	run.printMessage("***"+message);
           	e.printStackTrace();
           
        }catch(Exception e){
           	
           	String message="Error when loading file! Please check the input file:"+dataFile.getName();
               JOptionPane.showMessageDialog(this,message);
               run.printMessage("***"+message);
               run.printMessage("***"+e.getMessage());
               e.printStackTrace(); 
           	               
           
        }
    }
    
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed

        isFileLoaded=false;
        isFileProcessed=false;
        try{
            run.clear();
            NDSControl.reset();
            resetUI();            
            
            //JFileChooser fc=new JFileChooser();
            JFileChooser fc;
            try {
                fc = new JFileChooser(".");
             }catch (Exception e) {
                fc = new JFileChooser(".", new RestrictedFileSystemView());
             }
               if(javands.main.Setup.filedir!=null)
                   fc.setCurrentDirectory(new File(javands.main.Setup.filedir));
               
               int ret=fc.showOpenDialog(this);
               if(ret==JFileChooser.APPROVE_OPTION){
                   javands.main.Setup.filedir=fc.getCurrentDirectory().toString();
                   javands.main.Setup.save();
                   data.clear();
                   dataFile=fc.getSelectedFile();
                   
                   run.printMessage("Loading file: "+dataFile.getAbsolutePath());
                   data.load(dataFile);
                   		          
                   run.printMessage("Done loading");
                   
                   int n=data.nENSDF();
                   boolean hasErrorMsg=false;
                   
                   ensw=new EnsdfWrap[n];
                   for(int i=0;i<n;i++){
                       ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
                       ensw[i]=new EnsdfWrap(etd);
                       run.printMessage("***"+data.getENSDF(i).errorMsg());
                       if(!hasErrorMsg&&data.getENSDF(i).errorMsg().length()>0)
                    	   hasErrorMsg=true;
                   }
                   
                   if(hasErrorMsg)
                	   run.printMessage("See log.txt for warning messages when loading ENSDF");
                   
                   run.setEnsdfWraps(ensw);
                   run.setData(data);
                   isFileLoaded=true;

                   
                   NDSConfig.ensdfFile=dataFile.getAbsolutePath();
                   
                   
                   //set default output names after file is loaded and it can be override afterwards
                   //Config.latex is set in this call
                   run.setOutputFilename(Str.fileNamePrefix(dataFile.getName()));
               }
              
           }catch(FileNotFoundException e){
           	String message="Error: cannot create log file:"+run.getLogFilePath()+"\nPlease check output path.";
           	JOptionPane.showMessageDialog(this,message);
           	run.printMessage("***"+message);
           	e.printStackTrace();
           }catch(Exception e){
           	
           	String message="Error when loading file! Please check the input file:"+dataFile.getName();
               JOptionPane.showMessageDialog(this,message);
               run.printMessage("***"+message);
               run.printMessage("***"+e.getMessage());
               e.printStackTrace(); 
           	               
           }

    }
    
    private void loadDataFileBrowser() {
    	
         isFileLoaded=false;
         isFileProcessed=false;
         try{          
            
             //JFileChooser fc=new JFileChooser();
             JFileChooser fc;
             try {
                 fc = new JFileChooser(".");
              }catch (Exception e) {
                 fc = new JFileChooser(".", new RestrictedFileSystemView());
              }
                if(javands.main.Setup.filedir!=null)
                    fc.setCurrentDirectory(new File(javands.main.Setup.filedir));
                
                int ret=fc.showOpenDialog(this);
                if(ret==JFileChooser.APPROVE_OPTION){
                    javands.main.Setup.filedir=fc.getCurrentDirectory().toString();
                    javands.main.Setup.save();
                    data.clear();
                    dataFile=fc.getSelectedFile();
                    
                    run.printMessage("Loading file: "+dataFile.getAbsolutePath());
                    data.load(dataFile);
                    run.printMessage("Done loading");
                    
                    int n=data.nENSDF();
                    boolean hasErrorMsg=false;
                    
                    ensw=new EnsdfWrap[n];
                    for(int i=0;i<n;i++){
                        ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
                        ensw[i]=new EnsdfWrap(etd);
                        if(!hasErrorMsg&&data.getENSDF(i).errorMsg().length()>0)
                     	   hasErrorMsg=true;
                    }
                    
                    if(hasErrorMsg)
                 	   run.printMessage("See log.txt for warning messages when loading ENSDF");
                    
                    run.setEnsdfWraps(ensw);
                    run.setData(data);
                    
                    isFileLoaded=true;
                    
                    NDSConfig.ensdfFile=dataFile.getAbsolutePath();
                    
                    
                    //set default output names after file is loaded and it can be override afterwards
                    //Config.latex is set in this call
                    String s=Str.fileNamePrefix(dataFile.getName());
                    s=Str.cleanFilename(s);
                    
                    run.setOutputFilename(s);
                }
               
            }catch(FileNotFoundException e){
            	String message="Error: cannot create log file:"+run.getLogFilePath()+"\nPlease check output path.";
            	JOptionPane.showMessageDialog(this,message);
            	run.printMessage("***"+message);
            	e.printStackTrace();
            }catch(Exception e){
            	
            	String message="Error when loading file! Please check the input file:"+dataFile.getName();
                JOptionPane.showMessageDialog(this,message);
                run.printMessage("***"+message);
                run.printMessage("***"+e.getMessage());
                e.printStackTrace(); 
            	               
            }

    }//GEN-LAST:event_loadButtonActionPerformed

    private void loadNSRFileBrowser() {
        try{          
           
            //JFileChooser fc=new JFileChooser();
            JFileChooser fc;
            try {
                fc = new JFileChooser(".");
             }catch (Exception e) {
                fc = new JFileChooser(".", new RestrictedFileSystemView());
             }
               if(javands.main.Setup.filedir!=null)
                   fc.setCurrentDirectory(new File(javands.main.Setup.filedir));
               
               int ret=fc.showOpenDialog(this);
               if(ret==JFileChooser.APPROVE_OPTION){

                   File nsrFile=fc.getSelectedFile();                  
                   run.printMessage("selected NSR file: "+nsrFile.getAbsolutePath());
                   NDSControl.userNSRFilename=nsrFile.getAbsolutePath();

               }
              
           }catch(Exception e){
               e.printStackTrace(); 
           }

   }//GEN-LAST:event_loadButtonActionPerformed
    
    
    private void browseButtonMouseClicked(MouseEvent evt){
		if(evt.getModifiers()==InputEvent.BUTTON1_MASK){//left-button, BUTTON2-middle, BUTTON3-right
			loadOutputBrowser();
		}else if(evt.getModifiers()==InputEvent.BUTTON3_MASK){
			final JPopupMenu popupMenu=new JPopupMenu();
			//popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
			//popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
            popupMenu.setForeground(SystemColor.windowText);
            popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));

            
			 JMenuItem useCurrentFolder=new JMenuItem("Use Current Folder");
			 useCurrentFolder.setToolTipText("set output path to be the current working folder where the code is started");
			 useCurrentFolder.addActionListener(new ActionListener(){
				 //item.addMouseListener(new MouseAdapter(){
					 //public void mouseClicked(MouseEvent evt){
					 public void actionPerformed(ActionEvent evt){
						 try {
							 setOutpathAsCurrentActionPerformed(evt); 
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 }
				 });

			 popupMenu.add(useCurrentFolder);
			 
			 popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
    }
    protected void setOutpathAsCurrentActionPerformed(ActionEvent arg0) {
    	setOutpath(System.getProperty("user.dir"));		
	}
    
    protected void setOutpath(String outdir) {
        try{
        	Setup.outdir=outdir;
        	
        	run.updateRedirectOutput();
        	
            System.out.println();
            
            Setup.save();       
            pathField.setText(outdir);

        }catch(Exception e){
            e.printStackTrace();
           	String message="Error when setting output path!";
           	JOptionPane.showMessageDialog(this,message);
           	run.printMessage(message);           	                         
        }		
	}
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {
    
    	loadOutputBrowser();
    }
    

    private void loadOutputBrowser() {
        try{
            
            //JFileChooser fc=new JFileChooser();
            JFileChooser fc;
            try {
                fc = new JFileChooser(".");            
             }catch (Exception e) {
                fc = new JFileChooser(".", new RestrictedFileSystemView());
             }

            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setApproveButtonText("Select");
            fc.setApproveButtonToolTipText("Select this directory");
            
            if(javands.main.Setup.outdir!=null)
                fc.setCurrentDirectory(new File(javands.main.Setup.outdir));
            
            int ret=fc.showOpenDialog(this);
            if(ret==JFileChooser.APPROVE_OPTION){
                //nds.Setup.outdir=fc.getSelectedFile().toString();
            	
            	File file = fc.getSelectedFile();
            	javands.main.Setup.outdir=file.getAbsolutePath();
            	
                System.out.println();
                javands.main.Setup.save();
                
                pathField.setText(javands.main.Setup.outdir);
            }
            
           
        }catch(Exception e){
            //e.printStackTrace();
           	String message="Error when selecting output path!";
           	JOptionPane.showMessageDialog(this,message);
           	run.printMessage(message);       
           	e.printStackTrace();
        }

   }
    
	private void reloadFile(){
		
    	if(dataFile==null)
    		return;
    	
    	try{
    		run.clear();
            data.clear();
            data.load(dataFile);

            int n=data.nENSDF();

            ensw=new EnsdfWrap[n];
            for(int i=0;i<n;i++){
            	ensdfparser.nds.ensdf.EnsdfTableData etd=data.getETD(i);
                ensw[i]=new EnsdfWrap(etd);
            }
            
            isFileLoaded=true;
            
            run.setEnsdfWraps(ensw);
            run.setData(data);
            
            run.printMessage("Input file has been reloaded!");
            
    	}catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,e);
            
        }
    	
    }
    
    private void controlSettingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingButtonActionPerformed
      
        if(!isFileLoaded){
            JOptionPane.showMessageDialog(this,"You should load a file before trying to setting control and/or creating bands");
            return;
        }
        
        String title="Band and Control Settings";
        MainSettingFrame settingFrame=null;
        
        java.awt.Frame[] frames=java.awt.Frame.getFrames();
        for(int i=0;i<frames.length;i++) {
            if(frames[i].getTitle().equals(title))
                settingFrame=(MainSettingFrame)frames[i];
            
            //System.out.println(frames[i].getName()+"  "+frames[i].getTitle());
        }
            
        if(settingFrame==null) {
            settingFrame=new MainSettingFrame(data,ensw,run);
            settingFrame.setTitle(title);
            settingFrame.setLocation(this.getX()+this.getWidth(),this.getY());
            settingFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        settingFrame.setVisible(true);
        
        //notes which dataset need to have bands drawn for them
        
    }//GEN-LAST:event_settingButtonActionPerformed

    private void tableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableButtonActionPerformed

        if(!isFileLoaded){
            JOptionPane.showMessageDialog(this,"You should load a file before trying to create tables");
            return;
        }
        else if(controlFileCheckBox.isSelected() && !isControlLoaded){
        	 JOptionPane.showMessageDialog(this,"Control file is not loaded and automatic settings will be used.");
        	 NDSControl.autoAdjust=true;
        	 NDSControl.needToFindBreaks=true;
        }

        if(refCheckBox.isSelected())
        	NDSControl.hasReference=true;
        else
        	NDSControl.hasReference=false;
        
        if(drawingCheckBox.isSelected()){
        	NDSControl.includeAllDrawings=true;
        	NDSControl.nodrawing=false;
        }
        else
        	NDSControl.includeAllDrawings=false;
        
        if(nodrawingCheckBox.isSelected()){
        	NDSControl.nodrawing=true;
        	NDSControl.includeAllDrawings=false;
        }
        else
        	NDSControl.nodrawing=false;
        
        if(suppressCheckBox.isSelected())
        	NDSControl.showSuppressed=false;
        else
        	NDSControl.showSuppressed=true;
        
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
        
        
        //default Config.latex is already set when the file is loaded
        //Config.latex=nds.Setup.outdir+run.dirSeparator()+run.getDefaultOutputFilename()+".tex";
        
        try{
        	//tableControl of each etd (dataset) in data will be changed and set in "writeLatex".
        	//So every time the "tableButton" is clicked, the data should be reloaded to reset all tableControl 
        	//to default settings
        	if(isFileProcessed && NDSControl.autoAdjust && !NDSControl.isTableControlModified && !NDSControl.isBandSettingModified)//re-process the same file 
        		reloadFile();
        	

        	run.clear();
         
        	run.setData(data);
            run.writeLatex(NDSConfig.latex,data);
            
            isFileProcessed=true;
        }catch(Exception e){
        	e.printStackTrace();
            JOptionPane.showMessageDialog(this, e);
        }
    }//GEN-LAST:event_tableButtonActionPerformed

    private void createLatex(boolean alsoCreatePDF,boolean cleanFiles) {

    	String message="";
    	
        if(!isFileLoaded){
            JOptionPane.showMessageDialog(this,"You should load a file before trying to create tables");
            return;
        }
        else if(controlFileCheckBox.isSelected() && !isControlLoaded){
        	 JOptionPane.showMessageDialog(this,"Control file is not loaded and automatic settings will be used.");
        	 NDSControl.autoAdjust=true;
        	 NDSControl.needToFindBreaks=true;
        }

        if(refCheckBox.isSelected())
        	NDSControl.hasReference=true;
        else
        	NDSControl.hasReference=false;
        
        if(drawingCheckBox.isSelected()){
        	NDSControl.includeAllDrawings=true;
        	NDSControl.nodrawing=false;
        }
        else
        	NDSControl.includeAllDrawings=false;
        
        if(nodrawingCheckBox.isSelected()){
        	NDSControl.nodrawing=true;
        	NDSControl.includeAllDrawings=false;
        }
        else
        	NDSControl.nodrawing=false;
        
        if(suppressCheckBox.isSelected())
        	NDSControl.showSuppressed=false;
        else
        	NDSControl.showSuppressed=true;
        
        
        boolean validOutdir=true;
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
        

    	//debug
    	//System.out.println("In MasterFrame 730: "+Config.latex);
    	//System.out.println("In MasterFrame 731: "+run.getDefaultOutputFilename());
    	
        //default Config.latex is already set when the file is loaded
    	//set default output filename
        //Config.latex=nds.Setup.outdir+run.dirSeparator()+run.getDefaultOutputFilename()+".tex";
        
        try{
        	//tableControl of each etd (dataset) in data will be changed and set in "writeLatex".
        	//So every time the "tableButton" is clicked, the data should be reloaded to reset all tableControl 
        	//to default settings
    		//System.out.println("In MasterFrame line 1018: nsrfilename="+data.ref().userLoadedNSRFilename());
    		
        	if(isFileProcessed && NDSControl.autoAdjust && !NDSControl.isTableControlModified && !NDSControl.isBandSettingModified){//re-process the same file 
        		reloadFile();
        	}
            
        	run.clear();       	      

        	//System.out.println("1 output dir="+Setup.outdir);
     
        	run.setData(data);

        	//System.out.println("2 output dir="+Setup.outdir);
     
        	System.out.println("###### "+NDSConfig.latex);
        	
            run.writeLatex(NDSConfig.latex,data);
            
            isFileProcessed=true;
        }catch(Exception e){
        	message+="Error when writing LaTeX output file.";
        	run.printMessage(message);
            JOptionPane.showMessageDialog(this, message);
            e.printStackTrace();
        }
         
        
        //run shell command to create PDF file
    	String script="NDS.bat";
    	String path=javands.main.Setup.outdir+"\\"+script;
    	String command="";
    	String os=System.getProperty("os.name").toLowerCase();
    	String outdir="";  
    	
    	if(os.contains("linux")||os.contains("mac")){
            script="NDS.sh";
            path=javands.main.Setup.outdir+"/"+script;
    	}
    	
    	message="\nTo create PDF file, double-click \"Create\" button or run the following script:\n";
    	message+=path;
    	message+="\n(right-click blank area near \"Create\" button for a pop-up menu)\n\n";
    	
        try{
            if(alsoCreatePDF){
               
				 long startTime=System.currentTimeMillis();
				 
				 run.runScript();

				 if(cleanFiles)
					 run.cleanupFilesWithMessage(startTime);

            }else{
            	run.printMessage(message);
            }
            
        }catch(Exception e){
			String msg=e.getMessage();
			if(msg.length()>0) {
				run.printMessage(msg);
				JOptionPane.showMessageDialog(popupMenu,msg);						         
				return;
			}
            e.printStackTrace();
        }    	
    }
    
    private void tableButtonMouseClicked(java.awt.event.MouseEvent evt) {
    	
    	int nClick=evt.getClickCount();
    	boolean alsoCreatePDF=(nClick>=2);
    	
    	if(evt.getModifiers()==InputEvent.BUTTON1_MASK) {//BUTON1 left, BUTTON2 middle, BUTTON3 right
		
    		createLatex(alsoCreatePDF,true);//cleanFiles=true to clean auxiliary files after creating PDF

    	}
    	/*
    	else if(evt.getModifiers()==InputEvent.BUTTON3_MASK){  
        	JPopupMenu popupMenuForCreateButton=new JPopupMenu();
  			 //popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
  			 //popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      		popupMenuForCreateButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
      		popupMenuForCreateButton.setForeground(SystemColor.windowText);
      		popupMenuForCreateButton.setBackground(UIManager.getColor("ArrowButton.background"));
      		
      		JMenuItem itemCreatePDFAutoKey=new JMenuItem("Create PDF correcting keynumber");
  			 itemCreatePDFAutoKey.setToolTipText("<HTML>Create PDF file with auto-correction of incorrect letter case in keynumbers.<br>"
  			 	                                    	+ "Right-click to keep auxiliary files after PDF is created</HTML>.");
  			 
  			 //itemCreatePDFAutoKey.addMouseListener(new MouseAdapter(){//menu item doesn't work with MouseListener
  		     itemCreatePDFAutoKey.addActionListener(new ActionListener() {
  				 //public void mouseClicked(MouseEvent e){	
  		    	public void actionPerformed(ActionEvent e) {
  					 NDSControl.autoCorrectionForKeynumber=true;
 
  					 if(e.getModifiers()==InputEvent.BUTTON3_MASK) {
  	  					 createLatex(true,false);//right-click to keep auxiliary after creating PDF
  					 }else {						
  						 createLatex(true,true);
  					 }
  					NDSControl.autoCorrectionForKeynumber=false;
  				 }
  			 });
  			 popupMenuForCreateButton.add(itemCreatePDFAutoKey);    		
  			 popupMenuForCreateButton.show(evt.getComponent(),evt.getX(), evt.getY()); 
    	}
    	*/  	
    }
    
    private void controlFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controlButtonActionPerformed

        Vector<String> v=new Vector<String>();
        JFileChooser fc;
        String s;
         try{
                //JFileChooser fc=new JFileChooser();
                
                try {
                    fc = new JFileChooser(".");
                 }catch (Exception e) {
                    fc = new JFileChooser(".", new RestrictedFileSystemView());
                 }
                 
                if(javands.main.Setup.filedir!=null)
                    fc.setCurrentDirectory(new File(javands.main.Setup.filedir));
                int ret=fc.showOpenDialog(this);
                if(ret==JFileChooser.APPROVE_OPTION){
                    javands.main.Setup.filedir=fc.getCurrentDirectory().toString();
                    javands.main.Setup.save();
                    
                    BufferedReader br=new BufferedReader(new FileReader(fc.getSelectedFile()));
                    while(true){
                        s=br.readLine();
                        if(s==null) break;
                        v.add(s);
                    }
                    NDSControl.setControl(v);
                    br.close();
                    isControlLoaded=true;
                }
                
            }catch(Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,e);
            }

    }//GEN-LAST:event_controlButtonActionPerformed
    
    private void moreGlobalSettingButtonActionPerformed(java.awt.event.ActionEvent evt) {
             
        if(globalSettingFrame==null) {
            globalSettingFrame=new GlobalSettingFrame(data,this);
            
            //globalSettingFrame.setLocationRelativeTo(moreSettingButton);//put the center of opening frame at the center of current frame
            globalSettingFrame.setLocation(this.getX()+this.getWidth(),this.getY());
            globalSettingFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);               
        }
       
     	globalSettingFrame.setVisible(true);
    }

    private void controlFileCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.autoAdjust=false;
    		NDSControl.needToFindBreaks=false;//will be set to be true if comment column with is set in control file
    		NDSControl.useControlFile=true;
    		controlFileButton.setEnabled(true);
    	    //resetButton.setEnabled(true);
    	}
    	else{
    		NDSControl.autoAdjust=true;
    		NDSControl.needToFindBreaks=true;
    		NDSControl.useControlFile=false;
    		controlFileButton.setEnabled(false);
    	    //resetButton.setEnabled(false);
    	}
    }
    
    private void controlSettingCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		controlSettingButton.setEnabled(true);
    	}
    	else{
    		controlSettingButton.setEnabled(false);
    		NDSControl.isModified=false;
    		NDSControl.isHeaderSettingModified=false;
    		NDSControl.isTableControlModified=false;
    	}
    }
    
    private void refCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.hasReference=true;
    		includeTitleCheckBox.setEnabled(true);
    		showAllAuthorsCheckBox.setEnabled(true);
    		if(globalSettingFrame!=null)
    			globalSettingFrame.setNoOfAuthorsBoxEnabled(true);
    	}
    	else{
    		NDSControl.hasReference=false;    		
    		includeTitleCheckBox.setEnabled(false);
    		includeTitleCheckBox.setSelected(false);
    		showAllAuthorsCheckBox.setEnabled(false);
    		showAllAuthorsCheckBox.setSelected(false);
    		
    		if(globalSettingFrame!=null){
        		this.globalSettingFrame.setNoOfAuthorsBoxEnabled(false);
        		this.globalSettingFrame.setNoOfAuthorsBoxSelected(false);
    		}

    	}
    }
    
    private void includeTitleCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.includeRefTitle=true;
    	}
    	else{
    		NDSControl.includeRefTitle=false;
    	}
    }
    
	private void showAllAuthorsCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.showAllAuthors=true;
    		if(this.globalSettingFrame!=null)
    			globalSettingFrame.setNoOfAuthorsBoxSelected(false);
    	}
    	else
    		NDSControl.showAllAuthors=false;
    } 
	
    private void suppressCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.showSuppressed=false;
    	}
    	else{
    		NDSControl.showSuppressed=true;
    	}
    }
    
    private void drawingCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected())
    		nodrawingCheckBox.setSelected(false);
    }    
    
    private void nodrawingCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected())
    		drawingCheckBox.setSelected(false);
    } 
    
    private void pathFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_pathFieldPropertyChange
        
    }//GEN-LAST:event_pathFieldPropertyChange

    private void pathFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pathFieldKeyReleased
        //specifies the path that output will be created in
        javands.main.Setup.outdir=pathField.getText();
        javands.main.Setup.save();
    }//GEN-LAST:event_pathFieldKeyReleased
    
    public void setShowAllAuthorsBoxSelected(boolean b){
    	if(this.refCheckBox.isSelected()){
        	this.showAllAuthorsCheckBox.setSelected(b);
    	}

    }

	protected void showTextFrame(TextDisplayFrame textFrame,String title,String text) {
        try {
    	    int width=600;
    	    int height=795;

            java.awt.Frame[] frames=java.awt.Frame.getFrames();
            for(int i=0;i<frames.length;i++) {
                if(frames[i].getTitle().equals(title))
                    textFrame=(TextDisplayFrame)frames[i];
                
                //System.out.println(frames[i].getName()+"  "+frames[i].getTitle());
            }
            
            if(textFrame==null) {
                textFrame=new TextDisplayFrame(text);
                textFrame.setTitle(title);
                textFrame.setLocation(this.getLocationOnScreen().x+this.getWidth()+2,this.getLocationOnScreen().y);
                textFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                textFrame.setVisible(true); 
                textFrame.pack();              
                textFrame.setResizable(false);
                
            }else {
                textFrame.updateResult(text);
                if(!textFrame.isVisible()) {
                    textFrame.setLocation(this.getLocationOnScreen().x+this.getWidth()+2,this.getLocationOnScreen().y);
                    textFrame.setVisible(true); 
                }
            }
            
            textFrame.requestFocus();
            
            
            
        }catch(Exception e) {
	    	JOptionPane.showMessageDialog(null, "Error when openning window of latex synbols.");    
            return;
        }

        
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MasterFrame().setVisible(true);
            }
        });
    }
    
    public class CustomOutputStream extends OutputStream {
    	private JTextArea textArea;

    	public CustomOutputStream(JTextArea textArea) {
    		this.textArea = textArea;
    	}

    	@Override
    	public void write(int b) throws IOException {
    		// redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
    	}
    }
    
    public class FrameMouseAdapter extends MouseAdapter{
    	
	  @Override
	  public void mouseClicked(MouseEvent e) {
			 
		if(e.getButton()==3){//right clicked
			 popupMenu=new JPopupMenu();
			 //popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
			 //popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			 popupMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, SystemColor.windowText,UIManager.getColor("ArrowButton.background")));
             popupMenu.setForeground(SystemColor.windowText);
             popupMenu.setBackground(UIManager.getColor("ArrowButton.background"));
			 
			 JMenuItem itemCreatePDF=new JMenuItem("Create PDF");
			 itemCreatePDF.setToolTipText("Create PDF file and clean up all generated auxiliary files including figure and LaTeX files");
			 itemCreatePDF.addActionListener(new ActionListener(){
			 //item.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 
					 if(true){//if(evt.getButton()==1){//left clicked
						 //debug
						 //System.out.println("In MasterFrame: figurefiles="+run.getLatexWriter().getFigureFiles().size());
						 //run.printMessage("\nCreating PDF file from LaTex file.");
						 try {
							 
							 if(!isFileLoaded){						     
								 JOptionPane.showMessageDialog(popupMenu,"You haven't loaded an ENSDF file and created LaTeX file from it.");						         
								 return;						        
							 }else if(!isFileProcessed){
								 JOptionPane.showMessageDialog(popupMenu,"You haven't created LaTeX from the loaded ENSDF file.");						         
								 return;	
							 }
							 
							 
							 long startTime=System.currentTimeMillis();
							 
							 run.runScript();

							 run.cleanupFilesWithMessage(startTime);
							 							 
						} catch (Exception e) {
							// TODO Auto-generated catch block
							run.printMessage("Error when running the script to create pdf file.\n");
							String msg=e.getMessage();
							if(msg.length()>0) {
								run.printMessage(msg);
								JOptionPane.showMessageDialog(popupMenu,msg);						         
								return;
							}
								
							//e.printStackTrace();
						}
					 }
				 }
			 });
			 popupMenu.add(itemCreatePDF); 
			 			 
			 JMenuItem itemCreatePDFWithFigFiles=new JMenuItem("Create PDF with auxiliary files");
			 itemCreatePDFWithFigFiles.setToolTipText("Create PDF file and keep all generated auxiliary files including figure and LaTeX files");
			 itemCreatePDFWithFigFiles.addActionListener(new ActionListener(){
			 //item.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 
					 if(true){//if(evt.getButton()==1){//left clicked
						 //debug
						 //System.out.println("In MasterFrame: figurefiles="+run.getLatexWriter().getFigureFiles().size());
						 //run.printMessage("\nCreating PDF file from LaTex file.");
						 try {
							 
							 if(!isFileLoaded){						     
								 JOptionPane.showMessageDialog(popupMenu,"You haven't loaded an ENSDF file and created LaTeX file from it.");						         
								 return;						        
							 }else if(!isFileProcessed){
								 JOptionPane.showMessageDialog(popupMenu,"You haven't created LaTeX from the loaded ENSDF file.");						         
								 return;	
							 }
							 
							 run.runScript();
							 
						} catch (Exception e) {
							// TODO Auto-generated catch block
							run.printMessage("Error when running the script to create pdf file.\n"+e.getMessage());
							String msg=e.getMessage();
							if(msg.length()>0) {
								run.printMessage(msg);
								JOptionPane.showMessageDialog(popupMenu,msg);						         
								return;
							}
	
							//e.printStackTrace();
						}
					 }
				 }
			 });
			 popupMenu.add(itemCreatePDFWithFigFiles); 
			 
			 /*
			 JMenuItem itemDeleteFiles=new JMenuItem("Delete figure files");
			 itemDeleteFiles.addActionListener(new ActionListener(){
			 //item.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 
					 if(true){//if(evt.getButton()==1){//left clicked
						 //debug
						 //System.out.println("In MasterFrame: figurefiles="+run.getLatexWriter().getFigureFiles().size());
						 if(isFileProcessed){
							 run.printMessage("\nDeleting MetaPost figure files. ");
							 
							 run.deleteFigureFiles();
							 
							 //run.cleanupFiles();//delete all generated files except for the pdf
							 
							 run.printMessage("Done.\n");
						 }
					 }
				 }
			 });
			 			 
    		 popupMenu.add(itemDeleteFiles); 
    		 */
			 
			 
			 JMenuItem itemSaveDatasets=new JMenuItem("Split and Save datasets");
			 itemSaveDatasets.setToolTipText("Split a mass-chain file by dataset and save each into a file.");
			 itemSaveDatasets.addActionListener(new ActionListener(){
			 //itemSaveDatasets.addMouseListener(new MouseAdapter(){//it is recommended not to use MouseListener for menu item
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 
					 if(true){
						 try{
							 if(!isFileLoaded){						     
								 JOptionPane.showMessageDialog(popupMenu,"You haven't loaded an ENSDF file.");						         
								 return;						        
							 }
							 
							 run.printMessage("\nSaving each dataset to individual ENSDF file...");
							 
							 int modifier=evt.getModifiers();
							 int mask=ActionEvent.CTRL_MASK;
							 if((modifier&mask)==mask){
								 run.saveAllENSDF(data,"old");
								 
								 //System.out.println("extension=old");
							 }else{
								 run.saveAllENSDF(data);
								 
								 //System.out.println("extension=ens");
							 }
							 run.printMessage("Done.");
						 }catch (Exception e) {
							  // TODO Auto-generated catch block
							 run.printMessage("Error when spliting datasets and saving each dataset into individual ENSDF file.\n");
							 e.printStackTrace();
						 }						 
					 }
				 }
			 });
    		 popupMenu.add(itemSaveDatasets); 
    		 
			 JMenuItem itemSetupEvaluation=new JMenuItem("Setup evaluation folder");
			 itemSetupEvaluation.setToolTipText("Set up and create folders for a new mass-chain evaluation from the input mass-chain file.");
			 itemSetupEvaluation.addActionListener(new ActionListener(){
			 //item.addMouseListener(new MouseAdapter(){
				 //public void mouseClicked(MouseEvent evt){
				 public void actionPerformed(ActionEvent evt){
					 
					 if(true){//if(evt.getButton()==1){//left clicked
						 try{
							 if(!isFileLoaded){						     
								 JOptionPane.showMessageDialog(popupMenu,"You haven't loaded an ENSDF file.");						         
								 return;						        
							 }else if(data.nENSDF()<5 || data.nChains()!=1 || data.nNucleus()<5){//not a mass-chain file
								 JOptionPane.showMessageDialog(popupMenu,"You must load a mass-chain file for setting-up evaluation folders.");	
								 return;
							 }
							 
							 run.printMessage("\nSetting up and creating folders for a new mass-chain evaluation and");
							 run.printMessage(  "spliting mass-chain file into individual datasets and");
							 run.printMessage(  "saving datasets into corresponding folders...");
							 run.setupEvaluation(data);
							 run.printMessage("Done.");
						 }catch (Exception e) {
							  // TODO Auto-generated catch block
							 run.printMessage("Error when setting up evaluation folders.\n");
							 e.printStackTrace();
						 }						 
					 }
				 }
			 });
    		 popupMenu.add(itemSetupEvaluation); 
    		 
	         popupMenu.show(e.getComponent(),e.getX(), e.getY());
		 }
		}
    }
    
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton controlSettingButton;
    private javax.swing.JButton controlFileButton;
    private javax.swing.JToggleButton loadButton;
    private javax.swing.JTextField pathField;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JToggleButton tableButton;
    private javax.swing.JToggleButton resetButton;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JCheckBox controlFileCheckBox;
    private JCheckBox controlSettingCheckBox;
    private JCheckBox refCheckBox;
    private JCheckBox drawingCheckBox;
    private JCheckBox suppressCheckBox;
    private JButton browserButton;
    private JCheckBox nodrawingCheckBox;
    /**
     * @wbp.nonvisual location=301,379
     */

    private JPanel panel;
    private JCheckBox includeTitleCheckBox;
    private JButton moreGlobalSettingButton;
    private JLabel manual_label;
    
	private JPopupMenu popupMenu;
    
    private GlobalSettingFrame globalSettingFrame;
    private JCheckBox showAllAuthorsCheckBox;
    private JLabel readmeLabel;
    
    private TextDisplayFrame latexSymbolFrame=null;
}