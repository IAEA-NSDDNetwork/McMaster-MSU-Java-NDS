package javands.ui;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.border.TitledBorder;

import ensdfparser.nds.control.NDSControl;
import ensdfparser.nds.ensdf.MassChain;

import javax.swing.border.EtchedBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;

import java.awt.event.ActionListener;

import javax.swing.JTextField;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JComponent;


@SuppressWarnings("unused")
public class GlobalSettingFrame extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel mainPanel1;
	private JPanel mainPanel2;
	private JCheckBox showNoOfAuthorsCheckBox;
	private JCheckBox showIntraBandCheckBox;
	private JCheckBox reorderGammaCheckBox;
	private javax.swing.ButtonGroup buttonGroup1;
	private JRadioButton orderByGammaRadioButton;
	private JRadioButton orderByLevelRadioButton;
	private JCheckBox drawFigNewPageCheckBox;
	private JButton drawingSelectorButton;
	private DrawingSelectorFrame drawingSelectorFrame;
	
	private MassChain massChain;
	private JCheckBox chckbxShowHeading;
	private JTextField textFieldHeading;
	private JTextField noOfAuthorsTextField;

	private MasterFrame parentFrame;
	private JCheckBox footmarkCheckBox;
	private JLabel policyMonthLabel;
	private JLabel policyURLLabel;
	private JTextField policyURLTextField;
	@SuppressWarnings("rawtypes")
	private JComboBox policyMonthComboBox;
	private JCheckBox drawBandOnlyCheckBox;
	private JCheckBox showAllhistoriesCheckBox;
    public GlobalSettingFrame(MassChain massChain,MasterFrame masterFrame) {
        
    	this.massChain=massChain;
    	this.parentFrame=masterFrame;
        initComponents();
        
    }
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initComponents() {
		setTitle("Global Settings");
		
		mainPanel1 = new JPanel();
		mainPanel1.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Others", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
				
		showIntraBandCheckBox = new JCheckBox("show inter-band transitions");
		showIntraBandCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showIntraBandCheckBoxStateChanged(e);
			}
		});
		showIntraBandCheckBox.setToolTipText("By default, only in-band transitions are drawn.");
		
		drawFigNewPageCheckBox = new JCheckBox("draw figures in new page");
		drawFigNewPageCheckBox.setToolTipText("force to draw all figures in separate pages");
		drawFigNewPageCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				drawFigNewPageCheckBoxStateChanged(e);
			}
		});
		
		
		mainPanel2 = new JPanel();
		mainPanel2.setBorder(new TitledBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)), "Drawings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Tables", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
		
		reorderGammaCheckBox = new JCheckBox("reorder gamma table by:");
		reorderGammaCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				reorderGammaCheckBoxItemStateChanged(arg0);
			}

		});
		reorderGammaCheckBox.setToolTipText("By default, the ordering of gamma table follow PN record (=6 or 7 by level and by gamma otherwise).");
		
		orderByGammaRadioButton = new JRadioButton("Gamma");
		orderByGammaRadioButton.setEnabled(false);
		
		orderByLevelRadioButton = new JRadioButton("Level");
		orderByLevelRadioButton.setEnabled(false);
		
		buttonGroup1=new javax.swing.ButtonGroup();
        buttonGroup1.add(orderByGammaRadioButton);
        orderByGammaRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	orderByGammaRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(orderByLevelRadioButton);
        orderByLevelRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	orderByLevelRadioButtonActionPerformed(evt);
            }

        });
		
		footmarkCheckBox = new JCheckBox("use letter footmark first");
		footmarkCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
		    	if(((JCheckBox)e.getSource()).isSelected()){
		    		NDSControl.useLetterFootnoteMarkFirst=true;
		    	}
		    	else{
		    		NDSControl.useLetterFootnoteMarkFirst=false;
		    	}
			}
		});
		footmarkCheckBox.setToolTipText("If not checked, the normal special footmark symbols like #,@,&, etc, will be used");
        
        
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(6)
							.addComponent(reorderGammaCheckBox)
							.addGap(31)
							.addComponent(orderByGammaRadioButton)
							.addGap(18)
							.addComponent(orderByLevelRadioButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(footmarkCheckBox, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(40, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(reorderGammaCheckBox)
						.addComponent(orderByLevelRadioButton)
						.addComponent(orderByGammaRadioButton))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(footmarkCheckBox)
					.addContainerGap(34, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(mainPanel1, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
						.addComponent(mainPanel2, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(mainPanel2, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(mainPanel1, GroupLayout.PREFERRED_SIZE, 231, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		
		drawingSelectorButton = new JButton("Drawings Selector");
		drawingSelectorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingSelectorButtonActionPerformed(e);
			}
		});
		
		drawBandOnlyCheckBox = new JCheckBox("show no drawings except adopted bands");
		drawBandOnlyCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				showOnlyDrawingsOfAdoptedBandsCheckBoxStateChanged(arg0);
			}
		});
		drawBandOnlyCheckBox.setToolTipText("Show no drawings in the pdf other than the adopted band drawings in Adopted Levels, Gammas");
		


		GroupLayout gl_mainPanel2 = new GroupLayout(mainPanel2);
		gl_mainPanel2.setHorizontalGroup(
			gl_mainPanel2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_mainPanel2.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel2.createSequentialGroup()
							.addComponent(drawBandOnlyCheckBox, GroupLayout.PREFERRED_SIZE, 262, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())
						.addGroup(gl_mainPanel2.createSequentialGroup()
							.addGroup(gl_mainPanel2.createParallelGroup(Alignment.LEADING, false)
								.addComponent(drawFigNewPageCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(showIntraBandCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
							.addComponent(drawingSelectorButton, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE)
							.addGap(13))))
		);
		gl_mainPanel2.setVerticalGroup(
			gl_mainPanel2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel2.createSequentialGroup()
					.addGap(1)
					.addGroup(gl_mainPanel2.createParallelGroup(Alignment.LEADING)
						.addComponent(drawingSelectorButton)
						.addGroup(gl_mainPanel2.createSequentialGroup()
							.addComponent(showIntraBandCheckBox)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(drawFigNewPageCheckBox)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(drawBandOnlyCheckBox)
					.addContainerGap(13, Short.MAX_VALUE))
		);
		mainPanel2.setLayout(gl_mainPanel2);
		
		chckbxShowHeading = new JCheckBox("show page heading:");
		chckbxShowHeading.setSelected(true);
		chckbxShowHeading.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showHeadingCheckBoxStateChanged(e);
			}
		});
		chckbxShowHeading.setToolTipText("By default, the heading of \"NUCLEAR DATA SHEETS\" is printed at page top.");
		
		showNoOfAuthorsCheckBox = new JCheckBox("show specified number of authors in reference:");
		showNoOfAuthorsCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showNoOfAuthorsCheckBoxStateChanged(e);
			}
		});
		showNoOfAuthorsCheckBox.setToolTipText("By default, the names of the first four authors are shown in refenrece list.");
		
		textFieldHeading = new JTextField();
		textFieldHeading.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				textFieldHeadingKeyReleased(e);
			}
		});
		textFieldHeading.setText("NUCLEAR DATA SHEETS");
		textFieldHeading.setColumns(10);
		
		noOfAuthorsTextField = new JTextField();
		noOfAuthorsTextField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				noOfAuthorsTextFieldKeyReleased(e);
			}
		});
		if(NDSControl.hasReference){
			showNoOfAuthorsCheckBox.setEnabled(true);
			noOfAuthorsTextField.setText("4");
			noOfAuthorsTextField.setEnabled(true);
			
			showNoOfAuthorsCheckBox.setSelected(!NDSControl.showAllAuthors);

		}else{
			showNoOfAuthorsCheckBox.setEnabled(false);	
			noOfAuthorsTextField.setEnabled(false);
		}

		noOfAuthorsTextField.setColumns(10);
		
		policyMonthLabel = new JLabel("In Abstract: policy page in the Issue of month:");
				
		String[] months= {"January","February","March","April","May","June","July","August","September",
				"October","November","December"};
        

        policyMonthComboBox = new JComboBox(new Object[]{});
        policyMonthComboBox.setModel(new DefaultComboBoxModel(months)); 
        
        String month=NDSControl.NDSIssueMonthForPolicy;
        int index=0;
        if(!month.trim().isEmpty()) {
        	month=month.toUpperCase().charAt(0)+month.toLowerCase().substring(1);
            ArrayList<String> monthsV=new ArrayList<String>(Arrays.asList(months));
            index=monthsV.indexOf(month);
            if(index<0)
            	index=0;
        }
        
        policyMonthComboBox.setSelectedIndex(index);
        
        /*
        policyMonthComboBox.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent event) {
     	       if (event.getStateChange() == ItemEvent.SELECTED) {
          	       if(policyMonthComboBox.getSelectedItem()!=null) {             
          	    	   NDSControl.NDSIssueMonthForPolicy=policyMonthComboBox.getSelectedItem().toString().trim();
                   } 	       
     	       }
	        
        	}
        });
        */
        
        policyMonthComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
      	       if(policyMonthComboBox.getSelectedItem()!=null) {             
      	    	   NDSControl.NDSIssueMonthForPolicy=policyMonthComboBox.getSelectedItem().toString().trim();
      	    	   javands.main.Setup.save();
               } 
            }
        });
        
             
        policyMonthComboBox.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {

    
                JComponent comp = (JComponent) super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);

    
                return comp;

            }            
        });
         
		policyURLLabel = new JLabel("URL of policy document:");
		
		policyURLTextField = new JTextField();
		policyURLTextField.setText(NDSControl.NDSPolicyURL);
		policyURLTextField.setColumns(10);
		policyURLTextField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				policyURLTextFieldKeyReleased(e);
			}
		});
		
		showAllhistoriesCheckBox = new JCheckBox("show all history records in pdf output");
		showAllhistoriesCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showAllHistoriesCheckBoxStateChanged(e);
			}
		});
		showAllhistoriesCheckBox.setToolTipText("By default, only the first non-errata history record is printed out in the pdf output.");
		


		GroupLayout gl_mainPanel1 = new GroupLayout(mainPanel1);
		gl_mainPanel1.setHorizontalGroup(
			gl_mainPanel1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel1.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_mainPanel1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel1.createSequentialGroup()
							.addGap(6)
							.addGroup(gl_mainPanel1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel1.createSequentialGroup()
									.addGap(9)
									.addComponent(policyURLTextField, GroupLayout.PREFERRED_SIZE, 372, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_mainPanel1.createSequentialGroup()
									.addComponent(policyURLLabel, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 218, GroupLayout.PREFERRED_SIZE)))
							.addContainerGap())
						.addGroup(gl_mainPanel1.createSequentialGroup()
							.addGroup(gl_mainPanel1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel1.createSequentialGroup()
									.addComponent(showNoOfAuthorsCheckBox)
									.addPreferredGap(ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
									.addComponent(noOfAuthorsTextField, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_mainPanel1.createSequentialGroup()
									.addComponent(chckbxShowHeading, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textFieldHeading, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_mainPanel1.createSequentialGroup()
									.addComponent(policyMonthLabel, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(policyMonthComboBox, 0, 110, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)))
							.addGap(6))))
				.addGroup(gl_mainPanel1.createSequentialGroup()
					.addContainerGap()
					.addComponent(showAllhistoriesCheckBox, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(122, Short.MAX_VALUE))
		);
		gl_mainPanel1.setVerticalGroup(
			gl_mainPanel1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel1.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_mainPanel1.createParallelGroup(Alignment.BASELINE)
						.addComponent(showNoOfAuthorsCheckBox)
						.addComponent(noOfAuthorsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_mainPanel1.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxShowHeading)
						.addComponent(textFieldHeading, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_mainPanel1.createParallelGroup(Alignment.BASELINE)
						.addComponent(policyMonthLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(policyMonthComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(policyURLLabel, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(policyURLTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(showAllhistoriesCheckBox)
					.addContainerGap(12, Short.MAX_VALUE))
		);
		mainPanel1.setLayout(gl_mainPanel1);
		getContentPane().setLayout(groupLayout);
		
		pack();
	}
	
	
	public void resetUI(){
		showNoOfAuthorsCheckBox.setSelected(false);
		showIntraBandCheckBox.setSelected(false);
		return;
	}
	
	private void showIntraBandCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected())
    		NDSControl.showInterBand=true;
    	else
    		NDSControl.showInterBand=false;
    }    
	
	private void drawFigNewPageCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected())
    		NDSControl.isNewFigurePage=true;
    	else
    		NDSControl.isNewFigurePage=false;
    }
	
	private void showOnlyDrawingsOfAdoptedBandsCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()) {
    		NDSControl.nodrawingExceptAdoptedBands=true;
    		this.drawingSelectorButton.setEnabled(false);
    	}else {
    		NDSControl.nodrawingExceptAdoptedBands=false;
    		this.drawingSelectorButton.setEnabled(true);
    	}
    }
	
	private void showAllHistoriesCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.showAllHistories=true;
    	}
    	else{
    		NDSControl.showAllHistories=false;
    	}
    } 
	
	private void showNoOfAuthorsCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.showAllAuthors=false;
    		this.noOfAuthorsTextField.setEnabled(true);
    		this.parentFrame.setShowAllAuthorsBoxSelected(false);
    	}
    	else{
    		NDSControl.numberOfAuthorsToShow=4;
    		this.noOfAuthorsTextField.setEnabled(false);
    	}
    } 

	private void showHeadingCheckBoxStateChanged(ItemEvent e){
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.showHeading=true;
    		textFieldHeading.setEnabled(true);
    		//Control.heading=textFieldHeading.getText();
    	}
    	else{
    		NDSControl.showHeading=false;
    		textFieldHeading.setEnabled(false);
    	}
    } 

	private void policyURLTextFieldKeyReleased(KeyEvent evt){
		if(!this.policyURLTextField.hasFocus() || !policyURLTextField.isEnabled())
			return;
		
		NDSControl.NDSPolicyURL=policyURLTextField.getText();
		javands.main.Setup.save();//save URL 
	}
	
	private void textFieldHeadingKeyReleased(KeyEvent evt){
		if(!textFieldHeading.hasFocus() || !textFieldHeading.isEnabled())
			return;
		
		NDSControl.heading=textFieldHeading.getText();
	}
	
	private void noOfAuthorsTextFieldKeyReleased(KeyEvent evt){
		if(!noOfAuthorsTextField.hasFocus() || !noOfAuthorsTextField.isEnabled())
			return;
		
		try{
			NDSControl.numberOfAuthorsToShow=Integer.parseInt(noOfAuthorsTextField.getText());
			if(NDSControl.numberOfAuthorsToShow<1){
				JOptionPane.showMessageDialog(this,"Invalid negative number of authors has been entered!\nPlease try again! Otherwise, number=4 will be used!");
				NDSControl.numberOfAuthorsToShow=4;
			}
		}catch(NumberFormatException e){
			NDSControl.numberOfAuthorsToShow=4;
			JOptionPane.showMessageDialog(this,"Invalid text for number of authors has been entered!\nPlease try again! Otherwise, number=4 will be used!");

		}

	}
	
    private void orderByGammaRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if(NDSControl.reorderGamma){
            NDSControl.reorderByGamma=true;
            NDSControl.reorderByLevel=false;
    	}

    }
    
    private void orderByLevelRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if(NDSControl.reorderGamma){
            NDSControl.reorderByLevel=true;
            NDSControl.reorderByGamma=false;
    	}
    }
    

	private void reorderGammaCheckBoxItemStateChanged(ItemEvent e) {
    	if(((JCheckBox)e.getSource()).isSelected()){
    		NDSControl.reorderGamma=true;
    		orderByGammaRadioButton.setEnabled(true);
    		orderByLevelRadioButton.setEnabled(true);
    	}
    	else{
    		NDSControl.reorderGamma=false;
    		NDSControl.reorderByGamma=false;
    		NDSControl.reorderByLevel=false;
    		orderByGammaRadioButton.setEnabled(false);
    		orderByLevelRadioButton.setEnabled(false);
    		
    		orderByGammaRadioButton.setSelected(false);
    		orderByLevelRadioButton.setSelected(false);
    	}
	}
	
	private void drawingSelectorButtonActionPerformed(java.awt.event.ActionEvent evt){
        drawingSelectorFrame=new DrawingSelectorFrame(massChain);
        
        //globalSettingFrame.setLocationRelativeTo(moreSettingButton);//put the center of opening frame at the center of current frame
        drawingSelectorFrame.setLocation(this.getX()+this.getWidth(),this.getY());
        drawingSelectorFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);          
        drawingSelectorFrame.setVisible(true);
	}
	
	public void setNoOfAuthorsBoxSelected(boolean b){
		this.showNoOfAuthorsCheckBox.setSelected(b);
	}
	
	public void setNoOfAuthorsBoxEnabled(boolean b){
		this.showNoOfAuthorsCheckBox.setEnabled(b);
		this.noOfAuthorsTextField.setEnabled(b);
	}
}
