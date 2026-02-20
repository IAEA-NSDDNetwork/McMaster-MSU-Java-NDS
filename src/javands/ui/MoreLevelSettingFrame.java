
package javands.ui;


import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.LayoutStyle.ComponentPlacement;

import ensdfparser.nds.ensdf.MassChain;

public class MoreLevelSettingFrame extends BaseMoreSettingFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MoreLevelSettingFrame(MassChain mass,EnsdfWrap curEnsdf,javands.main.Run r) {
        super(mass,curEnsdf,r);
        type="LEVEL";
    }
	
	/*display control settings for selected data set in control panel**/
	protected void displayTableControl(){

		Vector<String> columns;

		if(tc==null)
			return;
			
		
		columns=tc.getDataAsComments();
		setEnabledAndSelected(checkBox_7, tc,"E");setSelected(checkBox_7,columns,"E"); 
		setEnabledAndSelected(checkBox_8, tc,"J");setSelected(checkBox_8,columns,"J"); 
		setEnabledAndSelected(checkBox_9, tc,"T");setSelected(checkBox_9,columns,"T"); 
		setEnabledAndSelected(checkBox_10,tc,"J");setSelected(checkBox_10,columns,"L");
		setEnabledAndSelected(checkBox_11,tc,"S");setSelected(checkBox_11,columns,"S");
		
		columns=tc.getFcolumns();
		setEnabledAndSelected(checkBox,   tc,"E"); setSelected(checkBox,columns,"E");  
		setEnabledAndSelected(checkBox_1, tc,"J"); setSelected(checkBox_1,columns,"J");
		setEnabledAndSelected(checkBox_2, tc,"T"); setSelected(checkBox_2,columns,"T");
		setEnabledAndSelected(checkBox_3, tc,"L"); setSelected(checkBox_3,columns,"L");
		setEnabledAndSelected(checkBox_4, tc,"S");	setSelected(checkBox_4,columns,"S");
		setEnabledAndSelected(chckbxXref, tc,"XREF");setSelected(chckbxXref,columns,"XREF");

		
		setSelected(chckbxPutFootnotesIn,tc.isSeparateFoots());
		
		displayBreakPoints();
	}
	

	   
	/*set control settings for selected data set based on inputs in control panel**/
	protected void updateTableControl(){

		Vector<String> columns;

		if(tc==null)
			return;
		
		//set dataAsComment columns
		columns=new Vector<String>();
		if(checkBox_7.isSelected()) addColumn(columns,"E");
		if(checkBox_8.isSelected()) addColumn(columns,"J");
		if(checkBox_9.isSelected()) addColumn(columns,"T");
		if(checkBox_10.isSelected()) addColumn(columns,"L");
		if(checkBox_11.isSelected()) addColumn(columns,"S");
		if(columns.size()>0) 
			tc.setDataAsComments(columns);
				
		columns=new Vector<String>();
		if(checkBox.isSelected()) addColumn(columns,"E");
		if(checkBox_1.isSelected()) addColumn(columns,"J");
		if(checkBox_2.isSelected()) addColumn(columns,"T");
		if(checkBox_3.isSelected()) addColumn(columns,"L");
		if(checkBox_4.isSelected()) addColumn(columns,"S");
		if(chckbxXref.isSelected()) addColumn(columns,"XREF");
		if(columns.size()>0) 
			tc.setFcolumns(columns);
		

		tc.setIsSeparateFoots(chckbxPutFootnotesIn.isSelected());
		
		if(this.curEnsdf.etd.getGammaTableControl().reorderGamma()){
			//reset break points if ordering is changed
			tc.getBreakPoints().clear();
			tc.getBreaks().clear();
		}
		//update for break points is handled by corresponding listeners of components

	}
	
    protected void initComponents() {
        type="LEVEL";
    	popupMenus.clear();
    	brTextFields.clear();
    	brCheckBoxes.clear();
    	brButtons.clear();
    	
    	
    	//////////////////////////
    	//create objects
    	/////////////////////////
    	
        JPanel panel_2 = new JPanel();       
        label_2 = new JLabel("Data as comment:");       
        checkBox_7 = new JCheckBox("E");        
        checkBox_8 = new JCheckBox("J");        
        checkBox_9 = new JCheckBox("T");        
        checkBox_10 = new JCheckBox("L");       
        checkBox_11 = new JCheckBox("S");
        
        lblOnlyPrintRow = new JLabel("Print row if contain any:");
        checkBox = new JCheckBox("E");       
        checkBox_1 = new JCheckBox("J");       
        checkBox_2 = new JCheckBox("T");    
        checkBox_3 = new JCheckBox("L");        
        checkBox_4 = new JCheckBox("S");        
        chckbxXref = new JCheckBox("XREF");
        
        
        JLabel lblBreakPointslevel = new JLabel("Break points (level energies), check the box if starting a new page:");
        lblBreakPointslevel.setToolTipText("This table breaks up before given energies");
      
        chckbxPutFootnotesIn = new JCheckBox("Put footnotes in a new column");
        
        JPanel panel_4 = new JPanel();        
        checkBox_b1 = new JCheckBox("");setBreakPointCheckBox(checkBox_b1,1);       
        checkBox_b2 = new JCheckBox("");setBreakPointCheckBox(checkBox_b2,2);         
        textField_b3 = new JTextField();setBreakPointTextField(textField_b3,3);
        textField_b3.setColumns(10); 
        
        checkBox_b3 = new JCheckBox("");setBreakPointCheckBox(checkBox_b3,3);             
        JLabel label = new JLabel("1");        
        JLabel label_3 = new JLabel("3");        
        JPanel panel_5 = new JPanel();        
        JLabel label_6 = new JLabel("7");        
        textField_b7 = new JTextField();setBreakPointTextField(textField_b7,7);
        textField_b7.setColumns(10);        
        checkBox_b7 = new JCheckBox("");setBreakPointCheckBox(checkBox_b7,7);       
        JLabel label_7 = new JLabel("8");        
        textField_b8 = new JTextField();setBreakPointTextField(textField_b8,8);
        textField_b8.setColumns(10);        
        checkBox_b8 = new JCheckBox("");setBreakPointCheckBox(checkBox_b8,8);         
        JLabel label_10 = new JLabel("9");      
        textField_b9 = new JTextField();setBreakPointTextField(textField_b9,9);
        textField_b9.setColumns(10);
        
        checkBox_b9 = new JCheckBox("");setBreakPointCheckBox(checkBox_b9,9); 
        
        button_S7 = new JButton("S");button_S7.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S7,7,"S");       
        button_R7 = new JButton("R");button_R7.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R7,7,"R");
        button_S8 = new JButton("S");button_S8.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S8,8,"S");       
        button_R8 = new JButton("R");button_R8.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R8,8,"R");
                
        panel_6 = new JPanel();   
        
        label_11 = new JLabel("10");        
        textField_b10 = new JTextField();setBreakPointTextField(textField_b10,10);
        textField_b10.setColumns(10);       
        checkBox_b10 = new JCheckBox("");setBreakPointCheckBox(checkBox_b10,10);   
        
        label_12 = new JLabel("11");       
        textField_b11 = new JTextField();setBreakPointTextField(textField_b11,11);
        textField_b11.setColumns(10);        
        checkBox_b11 = new JCheckBox("");setBreakPointCheckBox(checkBox_b11,11); 
        
        label_13 = new JLabel("12");        
        textField_b12 = new JTextField();setBreakPointTextField(textField_b12,12);
        textField_b12.setColumns(10);       
        checkBox_b12 = new JCheckBox("");setBreakPointCheckBox(checkBox_b12,12); 
        
        lblsSuppress = new JLabel("(S - suppress columns after a break point, R - restore previously suppressed columns)");
        lblsSuppress.setToolTipText("This table breaks up before given energies");
        
        panel_1 = new JPanel();
        
        label_16 = new JLabel("4");
        
        textField_b4 = new JTextField();setBreakPointTextField(textField_b4,4);
        textField_b4.setColumns(10);
        
        checkBox_b4 = new JCheckBox("");setBreakPointCheckBox(checkBox_b4,4); 
        
        label_17 = new JLabel("5");
        
        textField_b5 = new JTextField();setBreakPointTextField(textField_b5,5);
        textField_b5.setColumns(10);
        
        checkBox_b5 = new JCheckBox("");setBreakPointCheckBox(checkBox_b5,5); 
        
        label_18 = new JLabel("6");
        
        textField_b6 = new JTextField();setBreakPointTextField(textField_b6,6);
        textField_b6.setColumns(10);
        
        checkBox_b6 = new JCheckBox("");setBreakPointCheckBox(checkBox_b6,6); 
        
        button_S4 = new JButton("S");button_S4.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S4,4,"S");       
        button_R4 = new JButton("R");button_R4.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R4,4,"R");
        
        button_S5 = new JButton("S");button_S5.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S5,5,"S");       
        button_R5 = new JButton("R");button_R5.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R5,5,"R");
        
        button_S6 = new JButton("S");button_S6.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S6,6,"S");       
        button_R6 = new JButton("R");button_R6.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R6,6,"R");
        
        panel_3 = new JPanel();
        
        button_S15 = new JButton("S");button_S15.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S15,15,"S");       
        button_R15 = new JButton("R");button_R15.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R15,15,"R");
        
        label_4 = new JLabel("13");
        
        textField_b13 = new JTextField();setBreakPointTextField(textField_b13,13);
        textField_b13.setColumns(10);
        
        checkBox_b13 = new JCheckBox("");setBreakPointCheckBox(checkBox_b13,13); 
        
        button_S13 = new JButton("S");button_S13.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S13,13,"S");       
        button_R13 = new JButton("R");button_R13.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R13,13,"R");
        
        label_5 = new JLabel("14");
        
        textField_b14 = new JTextField();setBreakPointTextField(textField_b14,14);
        textField_b14.setColumns(10);
        
        checkBox_b14 = new JCheckBox("");setBreakPointCheckBox(checkBox_b14,14); 
        
        button_S14 = new JButton("S");button_S14.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S14,14,"S");       
        button_R14 = new JButton("R");button_R14.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R14,14,"R");
        
        label_8 = new JLabel("15");
        
        textField_b15 = new JTextField();setBreakPointTextField(textField_b15,15);
        textField_b15.setColumns(10);
        
        checkBox_b15 = new JCheckBox("");setBreakPointCheckBox(checkBox_b15,15); 
        
        button_S10 = new JButton("S");button_S10.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S10,10,"S");       
        button_R10 = new JButton("R");button_R10.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R10,10,"R");
        
        button_S11 = new JButton("S");button_S11.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S11,11,"S");       
        button_R11 = new JButton("R");button_R11.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R11,11,"R");
        
        button_S12 = new JButton("S");button_S12.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S12,12,"S");       
        button_R12 = new JButton("R");button_R12.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R12,12,"R");
        
        button_S9 = new JButton("S");button_S9.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S9,9,"S");       
        button_R9 = new JButton("R");button_R9.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R9,9,"R");
        
        JLabel label_1 = new JLabel("2");
        
        textField_b2 = new JTextField();setBreakPointTextField(textField_b2,2);
        textField_b2.setColumns(10);
        
        textField_b1 = new JTextField();setBreakPointTextField(textField_b1,1);
        textField_b1.setColumns(10);
        
        button_S1 = new JButton("S");button_S1.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S1,1,"S");       
        button_R1 = new JButton("R");button_R1.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R1,1,"R");
        
        button_S2 = new JButton("S");button_S2.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S2,2,"S");       
        button_R2 = new JButton("R");button_R2.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R2,2,"R");
        
        button_S3 = new JButton("S");button_S3.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_S3,3,"S");       
        button_R3 = new JButton("R");button_R3.setMargin(new Insets(2, 5, 2, 4));setBreakPointButton(button_R3,3,"R");
        
    	/////////////////////////
    	    	
	
		
        OK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        this.setTitle("More settings for level table control.");
        
        OK.setText("OK");
        OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKActionPerformed(evt);
            }
        });
        
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		cancelActionPerformed(evt);
        	}
        });
        

        
        GroupLayout gl_panel_2 = new GroupLayout(panel_2);
        gl_panel_2.setHorizontalGroup(
        	gl_panel_2.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_2.createSequentialGroup()
        			.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
        			.addGap(31)
        			.addComponent(checkBox_7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_8)
        			.addGap(5)
        			.addComponent(checkBox_9)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(checkBox_10)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_11)
        			.addGap(152))
        );
        gl_panel_2.setVerticalGroup(
        	gl_panel_2.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_2.createSequentialGroup()
        			.addGap(1)
        			.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
        				.addComponent(checkBox_7, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_2.createSequentialGroup()
        					.addGap(1)
        					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
        						.addComponent(checkBox_8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        						.addGroup(gl_panel_2.createSequentialGroup()
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(label_2, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))))
        				.addComponent(checkBox_9, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addComponent(checkBox_11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(checkBox_10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        			.addGap(0))
        );
        panel_2.setLayout(gl_panel_2);
        
        JPanel panel = new JPanel();
                
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(0, 0, Short.MAX_VALUE)
        			.addComponent(lblOnlyPrintRow, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
        			.addGap(6)
        			.addComponent(checkBox_1)
        			.addGap(5)
        			.addComponent(checkBox_2)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(checkBox_3)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_4)
        			.addGap(1)
        			.addComponent(chckbxXref)
        			.addGap(53))
        );
        gl_panel.setVerticalGroup(
        	gl_panel.createParallelGroup(Alignment.TRAILING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel.createSequentialGroup()
        					.addGap(3)
        					.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE))
        				.addGroup(gl_panel.createSequentialGroup()
        					.addGap(1)
        					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
        						.addComponent(checkBox_2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        							.addComponent(checkBox_4, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        							.addComponent(chckbxXref, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        						.addComponent(checkBox_3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        						.addGroup(gl_panel.createSequentialGroup()
        							.addGap(1)
        							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        								.addComponent(lblOnlyPrintRow, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
        								.addComponent(checkBox_1, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))))))
        			.addGap(0))
        );
        panel.setLayout(gl_panel);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addGap(0, 0, Short.MAX_VALUE)
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addGroup(groupLayout.createSequentialGroup()
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 575, GroupLayout.PREFERRED_SIZE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 575, GroupLayout.PREFERRED_SIZE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 575, GroupLayout.PREFERRED_SIZE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(204)
        					.addComponent(btnCancel)
        					.addGap(54)
        					.addComponent(OK, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(13)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 424, GroupLayout.PREFERRED_SIZE)
        						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 438, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(10)
        					.addComponent(chckbxPutFootnotesIn, GroupLayout.PREFERRED_SIZE, 269, GroupLayout.PREFERRED_SIZE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(11)
        					.addComponent(lblBreakPointslevel, GroupLayout.PREFERRED_SIZE, 508, GroupLayout.PREFERRED_SIZE))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        						.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 576, GroupLayout.PREFERRED_SIZE)
        						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 575, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(11)
        					.addComponent(lblsSuppress, GroupLayout.PREFERRED_SIZE, 535, GroupLayout.PREFERRED_SIZE)))
        			.addGap(9))
        );
        groupLayout.setVerticalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        			.addGap(5)
        			.addComponent(panel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxPutFootnotesIn)
        			.addGap(5)
        			.addComponent(lblBreakPointslevel)
        			.addGap(1)
        			.addComponent(lblsSuppress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(15)
        			.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(OK)
        				.addComponent(btnCancel))
        			.addContainerGap())
        );
        GroupLayout gl_panel_1 = new GroupLayout(panel_1);
        gl_panel_1.setHorizontalGroup(
        	gl_panel_1.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_1.createSequentialGroup()
        			.addGap(9)
        			.addComponent(label_16)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField_b4, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b4)
        			.addGap(4)
        			.addComponent(button_S4, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R4)
        			.addGap(18)
        			.addComponent(label_17)
        			.addGap(3)
        			.addComponent(textField_b5, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b5)
        			.addGap(4)
        			.addComponent(button_S5, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R5)
        			.addGap(18)
        			.addComponent(label_18)
        			.addGap(4)
        			.addComponent(textField_b6, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b6)
        			.addGap(4)
        			.addComponent(button_S6, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R6))
        );
        gl_panel_1.setVerticalGroup(
        	gl_panel_1.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_1.createSequentialGroup()
        			.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(1)
        					.addComponent(label_16, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(textField_b4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b4, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(2)
        					.addComponent(label_17, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b5, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_18, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_1.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b6, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_1.setLayout(gl_panel_1);
        GroupLayout gl_panel_3 = new GroupLayout(panel_3);
        gl_panel_3.setHorizontalGroup(
        	gl_panel_3.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_3.createSequentialGroup()
        			.addGap(1)
        			.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
        			.addGap(5)
        			.addComponent(textField_b13, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b13)
        			.addGap(4)
        			.addComponent(button_S13, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R13)
        			.addGap(11)
        			.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b14, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b14)
        			.addGap(4)
        			.addComponent(button_S14, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R14)
        			.addGap(12)
        			.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(textField_b15, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b15)
        			.addGap(4)
        			.addComponent(button_S15, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R15))
        );
        gl_panel_3.setVerticalGroup(
        	gl_panel_3.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_3.createSequentialGroup()
        			.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b13))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b14))
        				.addComponent(button_S14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_3.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b15, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_3.setLayout(gl_panel_3);
        

        
        GroupLayout gl_panel_6 = new GroupLayout(panel_6);
        gl_panel_6.setHorizontalGroup(
        	gl_panel_6.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
        			.addGap(4)
        			.addComponent(textField_b10, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b10)
        			.addGap(4)
        			.addComponent(button_S10, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R10)
        			.addGap(11)
        			.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b11, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b11)
        			.addGap(4)
        			.addComponent(button_S11, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R11)
        			.addGap(12)
        			.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
        			.addGap(3)
        			.addComponent(textField_b12, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b12)
        			.addGap(4)
        			.addComponent(button_S12, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R12, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(11, Short.MAX_VALUE))
        );
        gl_panel_6.setVerticalGroup(
        	gl_panel_6.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGroup(gl_panel_6.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
        				.addComponent(textField_b10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b10))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b11))
        				.addComponent(button_S11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createParallelGroup(Alignment.BASELINE)
        					.addComponent(button_S12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(button_R12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b12, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_6.setLayout(gl_panel_6);
        GroupLayout gl_panel_5 = new GroupLayout(panel_5);
        gl_panel_5.setHorizontalGroup(
        	gl_panel_5.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(9)
        			.addComponent(label_6)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField_b7, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b7)
        			.addGap(4)
        			.addComponent(button_S7, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R7)
        			.addGap(18)
        			.addComponent(label_7)
        			.addGap(3)
        			.addComponent(textField_b8, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b8)
        			.addGap(4)
        			.addComponent(button_S8, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R8)
        			.addGap(18)
        			.addComponent(label_10)
        			.addGap(4)
        			.addComponent(textField_b9, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b9)
        			.addGap(4)
        			.addComponent(button_S9, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R9))
        );
        gl_panel_5.setVerticalGroup(
        	gl_panel_5.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGroup(gl_panel_5.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
        				.addComponent(textField_b7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b7))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(2)
        					.addComponent(textField_b8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b8, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b9, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_5.setLayout(gl_panel_5);
        

        
        GroupLayout gl_panel_4 = new GroupLayout(panel_4);
        gl_panel_4.setHorizontalGroup(
        	gl_panel_4.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(9)
        			.addComponent(label)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField_b1, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b1)
        			.addGap(4)
        			.addComponent(button_S1, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R1)
        			.addGap(18)
        			.addComponent(label_1)
        			.addGap(3)
        			.addComponent(textField_b2, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b2)
        			.addGap(4)
        			.addComponent(button_S2, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R2)
        			.addGap(18)
        			.addComponent(label_3)
        			.addGap(4)
        			.addComponent(textField_b3, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b3)
        			.addGap(4)
        			.addComponent(button_S3, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R3, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(0, Short.MAX_VALUE))
        );
        gl_panel_4.setVerticalGroup(
        	gl_panel_4.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(label, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addComponent(button_S1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_R1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(textField_b2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addComponent(button_S2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_R2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 31, Short.MAX_VALUE))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(textField_b3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING, false)
        				.addComponent(button_S3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(1)
        			.addComponent(textField_b1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(9, Short.MAX_VALUE))
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(3)
        			.addComponent(checkBox_b3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(11, Short.MAX_VALUE))
        );
        panel_4.setLayout(gl_panel_4);
        getContentPane().setLayout(groupLayout);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables   
    private javax.swing.JButton OK;
    private JButton btnCancel;
    
    private JTextField textField_b3;
    private JTextField textField_b2;
    private JTextField textField_b1;
    private JTextField textField_b7;
    private JTextField textField_b8;
    private JTextField textField_b9;
    private JCheckBox checkBox_7;
    private JCheckBox checkBox_8;
    private JCheckBox checkBox_9;
    private JCheckBox checkBox_10;
    private JCheckBox checkBox_11;
    private JCheckBox checkBox_1;
    private JCheckBox checkBox_2;
    private JCheckBox checkBox_3;
    private JCheckBox checkBox_4;
    private JCheckBox checkBox;
    private JCheckBox chckbxPutFootnotesIn;
    private JCheckBox checkBox_b1;
    private JCheckBox checkBox_b2;
    private JCheckBox checkBox_b3;
    private JCheckBox checkBox_b9;
    private JCheckBox checkBox_b8;
    private JCheckBox checkBox_b7;
    private JPanel panel_6;
    private JLabel label_11;
    private JTextField textField_b10;
    private JCheckBox checkBox_b10;
    private JLabel label_12;
    private JTextField textField_b11;
    private JCheckBox checkBox_b11;
    private JLabel label_13;
    private JTextField textField_b12;
    private JCheckBox checkBox_b12;
    private JCheckBox chckbxXref;
    private JLabel lblsSuppress;
    private JPanel panel_1;
    private JLabel label_16;
    private JTextField textField_b4;
    private JCheckBox checkBox_b4;
    private JLabel label_17;
    private JTextField textField_b5;
    private JCheckBox checkBox_b5;
    private JLabel label_18;
    private JTextField textField_b6;
    private JCheckBox checkBox_b6;
    private JButton button_S1;
    private JButton button_R1;
    private JButton button_S2;
    private JButton button_R2;
    private JButton button_S3;
    private JButton button_R3;
    private JButton button_S4;
    private JButton button_R4;
    private JButton button_S5;
    private JButton button_R5;
    private JButton button_S6;
    private JButton button_R6;
    private JButton button_S7;
    private JButton button_R7;
    private JButton button_S8;
    private JButton button_R8;
    private JButton button_S9;
    private JButton button_R9;
    private JButton button_S10;
    private JButton button_R10;
    private JButton button_S11;
    private JButton button_R11;
    private JButton button_S12;
    private JButton button_R12;
    private JPanel panel_3;
    private JButton button_S15;
    private JButton button_R15;
    private JLabel label_4;
    private JTextField textField_b13;
    private JCheckBox checkBox_b13;
    private JButton button_S13;
    private JButton button_R13;
    private JLabel label_5;
    private JTextField textField_b14;
    private JCheckBox checkBox_b14;
    private JButton button_S14;
    private JButton button_R14;
    private JLabel label_8;
    private JTextField textField_b15;
    private JCheckBox checkBox_b15;
    private JLabel label_2;
    private JLabel lblOnlyPrintRow;
    // End of variables declaration//GEN-END:variables
    
}
