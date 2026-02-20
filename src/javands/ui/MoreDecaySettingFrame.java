
package javands.ui;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.LayoutStyle.ComponentPlacement;

import ensdfparser.nds.ensdf.MassChain;

import java.awt.Insets;

public class MoreDecaySettingFrame extends BaseMoreSettingFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    
    public MoreDecaySettingFrame(MassChain mass,EnsdfWrap curEnsdf,javands.main.Run r) {
        super(mass,curEnsdf,r);
        type="DECAY";
    }
	
	
	/*display control settings for selected data set in control panel**/
	protected void displayTableControl(){

		Vector<String> columns;

		if(tc==null)
			return;
			
		
		columns=tc.getDataAsComments();
		setEnabledAndSelected(checkBox_7, tc,"E");setSelected(checkBox_7,columns,"E"); 
		setEnabledAndSelected(chckbxLev, tc,"LEV");setSelected(chckbxLev,columns,"LEV"); 
		setEnabledAndSelected(chckbxIb, tc,"IB");setSelected(chckbxIb,columns,"IB"); 
		setEnabledAndSelected(chckbxIe,tc,"IE");setSelected(chckbxIe,columns,"IE");
		setEnabledAndSelected(chckbxIa,tc,"IA");setSelected(chckbxIa,columns,"IA");
		setEnabledAndSelected(chckbxIp,tc,"IP");setSelected(chckbxIp,columns,"IP");
		setEnabledAndSelected(chckbxLogft,tc,"LOGFT");setSelected(chckbxIa,columns,"LOGFT");
		setEnabledAndSelected(chckbxHf,tc,"HF");setSelected(chckbxHf,columns,"HF");
		
		columns=tc.getFcolumns();
		setEnabledAndSelected(checkBox, tc,"E");setSelected(checkBox,columns,"E"); 
		setEnabledAndSelected(checkBox_1, tc,"LEV");setSelected(checkBox_1,columns,"LEV"); 
		setEnabledAndSelected(checkBox_2, tc,"IB");setSelected(checkBox_2,columns,"IB"); 
		setEnabledAndSelected(checkBox_3,tc,"IE");setSelected(checkBox_3,columns,"IE");
		setEnabledAndSelected(checkBox_4,tc,"IA");setSelected(checkBox_4,columns,"IA");
		setEnabledAndSelected(checkBox_8,tc,"IP");setSelected(checkBox_8,columns,"IP");
		setEnabledAndSelected(checkBox_9,tc,"LOGFT");setSelected(checkBox_9,columns,"LOGFT");
		setEnabledAndSelected(chckbxHf_1,tc,"HF");setSelected(chckbxHf_1,columns,"HF");

		
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
		if(chckbxLev.isSelected()) addColumn(columns,"LEV");
		if(chckbxIb.isSelected()) addColumn(columns,"IB");
		if(chckbxIe.isSelected()) addColumn(columns,"IE");
		if(chckbxIa.isSelected()) addColumn(columns,"IA");
		if(chckbxIp.isSelected()) addColumn(columns,"IP");
		if(chckbxLogft.isSelected()) addColumn(columns,"LOGFT");
		if(chckbxHf.isSelected()) addColumn(columns,"HF");
		if(columns.size()>0) 
			tc.setDataAsComments(columns);
				
		columns=new Vector<String>();
		if(checkBox.isSelected()) addColumn(columns,"E");
		if(checkBox_1.isSelected()) addColumn(columns,"LEV");
		if(checkBox_2.isSelected()) addColumn(columns,"IB");
		if(checkBox_3.isSelected()) addColumn(columns,"IE");
		if(checkBox_4.isSelected()) addColumn(columns,"IA");
		if(checkBox_8.isSelected()) addColumn(columns,"IP");
		if(checkBox_9.isSelected()) addColumn(columns,"LOGFT");
		if(chckbxHf_1.isSelected()) addColumn(columns,"HF");
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
    	
        type="DECAY";
    	popupMenus.clear();
    	brTextFields.clear();
    	brCheckBoxes.clear();
    	brButtons.clear();
    	
    	
        OK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        this.setTitle("More settings for decay table control.");
        
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
        
        JPanel panel_2 = new JPanel();
        
        JLabel label_2 = new JLabel("Data as comment:");
        
        checkBox_7 = new JCheckBox("E");
        
        chckbxLev = new JCheckBox("LEV");
        
        chckbxIb = new JCheckBox("IB");
        
        chckbxIe = new JCheckBox("IE");
        
        chckbxIa = new JCheckBox("IA");
        
        chckbxIp = new JCheckBox("IP");
        
        chckbxLogft = new JCheckBox("LOGFT");
        
        chckbxHf = new JCheckBox("HF");
        GroupLayout gl_panel_2 = new GroupLayout(panel_2);
        gl_panel_2.setHorizontalGroup(
        	gl_panel_2.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_2.createSequentialGroup()
        			.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_7, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxLev)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxIb)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxIe)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxIa)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxIp, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxLogft, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(chckbxHf, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(37, Short.MAX_VALUE))
        );
        gl_panel_2.setVerticalGroup(
        	gl_panel_2.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_2.createSequentialGroup()
        			.addGap(1)
        			.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
        				.addComponent(checkBox_7, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
        					.addComponent(chckbxLev, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(label_2)
        					.addComponent(chckbxIb, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
        					.addComponent(chckbxIa, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(chckbxIp, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(chckbxLogft, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(chckbxHf, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(chckbxIe, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        			.addGap(1))
        );
        panel_2.setLayout(gl_panel_2);
        
        JLabel lblBreakPointslevel = new JLabel("Break points (decay energies), check the box if starting a new page:");
        lblBreakPointslevel.setToolTipText("This table breaks up before given energies");
        
        chckbxPutFootnotesIn = new JCheckBox("Put footnotes in a new column");
        
        panel = new JPanel();
        
        lblOnlyPrintRow = new JLabel("Print row if contain any:");
        
        checkBox = new JCheckBox("E");
        
        checkBox_1 = new JCheckBox("LEV");
        
        checkBox_2 = new JCheckBox("IB");
        
        checkBox_3 = new JCheckBox("IE");
        
        checkBox_4 = new JCheckBox("IA");
        
        checkBox_8 = new JCheckBox("IP");
        
        checkBox_9 = new JCheckBox("LOGFT");
        
        chckbxHf_1 = new JCheckBox("HF");
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addComponent(lblOnlyPrintRow, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_1)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_2)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_3)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_4)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_8, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_9, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
        			.addGap(6)
        			.addComponent(chckbxHf_1, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(52, Short.MAX_VALUE))
        );
        gl_panel.setVerticalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(1)
        			.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
        				.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(lblOnlyPrintRow))
        				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_4, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_8, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_9, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(chckbxHf_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
        			.addGap(1))
        );
        panel.setLayout(gl_panel);
        
        panel_4 = new JPanel();
        
        label = new JLabel("1");
        
        textField_b1 = new JTextField();
        textField_b1.setToolTipText("put values sepated by comma or space");
        textField_b1.setColumns(10);
        
        checkBox_b1 = new JCheckBox("");
        
        button_S1 = new JButton("S");
        button_S1.setMargin(new Insets(2, 5, 2, 4));
        
        button_R1 = new JButton("R");
        button_R1.setMargin(new Insets(2, 5, 2, 4));
        
        label_1 = new JLabel("2");
        
        textField_b2 = new JTextField();
        textField_b2.setToolTipText("put values sepated by comma or space");
        textField_b2.setColumns(10);
        
        checkBox_b2 = new JCheckBox("");
        
        button_S2 = new JButton("S");
        button_S2.setMargin(new Insets(2, 5, 2, 4));
        
        button_R2 = new JButton("R");
        button_R2.setMargin(new Insets(2, 5, 2, 4));
        
        label_3 = new JLabel("3");
        
        textField_b3 = new JTextField();
        textField_b3.setToolTipText("put values sepated by comma or space");
        textField_b3.setColumns(10);
        
        checkBox_b3 = new JCheckBox("");
        
        button_S3 = new JButton("S");
        button_S3.setMargin(new Insets(2, 5, 2, 4));
        
        button_R3 = new JButton("R");
        button_R3.setMargin(new Insets(2, 5, 2, 4));
        
        label_4 = new JLabel("(S - suppress columns after a break point, R - restore previously suppressed columns)");
        label_4.setToolTipText("This table breaks up before given energies");
        
        panel_5 = new JPanel();
        
        label_5 = new JLabel("4");
        
        textField_b4 = new JTextField();
        textField_b4.setToolTipText("put values sepated by comma or space");
        textField_b4.setColumns(10);
        
        checkBox_b4 = new JCheckBox("");
        
        button_S4 = new JButton("S");
        button_S4.setMargin(new Insets(2, 5, 2, 4));
        
        button_R4 = new JButton("R");
        button_R4.setMargin(new Insets(2, 5, 2, 4));
        
        label_6 = new JLabel("5");
        
        textField_b5 = new JTextField();
        textField_b5.setToolTipText("put values sepated by comma or space");
        textField_b5.setColumns(10);
        
        checkBox_b5 = new JCheckBox("");
        
        button_S5 = new JButton("S");
        button_S5.setMargin(new Insets(2, 5, 2, 4));
        
        button_R5 = new JButton("R");
        button_R5.setMargin(new Insets(2, 5, 2, 4));
        
        label_7 = new JLabel("6");
        
        textField_b6 = new JTextField();
        textField_b6.setToolTipText("put values sepated by comma or space");
        textField_b6.setColumns(10);
        
        checkBox_b6 = new JCheckBox("");
        
        button_S6 = new JButton("S");
        button_S6.setMargin(new Insets(2, 5, 2, 4));
        
        button_R6 = new JButton("R");
        button_R6.setMargin(new Insets(2, 5, 2, 4));
        
        panel_6 = new JPanel();
        
        label_8 = new JLabel("7");
        
        textField_b7 = new JTextField();
        textField_b7.setToolTipText("put values sepated by comma or space");
        textField_b7.setColumns(10);
        
        checkBox_b7 = new JCheckBox("");
        
        button_S7 = new JButton("S");
        button_S7.setMargin(new Insets(2, 5, 2, 4));
        
        button_R7 = new JButton("R");
        button_R7.setMargin(new Insets(2, 5, 2, 4));
        
        label_9 = new JLabel("8");
        
        textField_b8 = new JTextField();
        textField_b8.setToolTipText("put values sepated by comma or space");
        textField_b8.setColumns(10);
        
        checkBox_b8 = new JCheckBox("");
        
        button_S8 = new JButton("S");
        button_S8.setMargin(new Insets(2, 5, 2, 4));
        
        button_R8 = new JButton("R");
        button_R8.setMargin(new Insets(2, 5, 2, 4));
        
        label_10 = new JLabel("9");
        
        textField_b9 = new JTextField();
        textField_b9.setToolTipText("put values sepated by comma or space");
        textField_b9.setColumns(10);
        
        checkBox_b9 = new JCheckBox("");
        
        button_S9 = new JButton("S");
        button_S9.setMargin(new Insets(2, 5, 2, 4));
        
        button_R9 = new JButton("R");
        button_R9.setMargin(new Insets(2, 5, 2, 4));
        GroupLayout gl_panel_6 = new GroupLayout(panel_6);
        gl_panel_6.setHorizontalGroup(
        	gl_panel_6.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(9)
        			.addComponent(label_8)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField_b7, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b7)
        			.addGap(4)
        			.addComponent(button_S7, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R7)
        			.addGap(18)
        			.addComponent(label_9)
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
        			.addComponent(button_R9, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(19, Short.MAX_VALUE))
        );
        gl_panel_6.setVerticalGroup(
        	gl_panel_6.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGroup(gl_panel_6.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b7, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(2)
        					.addComponent(textField_b8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(checkBox_b8, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_S8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(checkBox_b9, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_S9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_6.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_6.setLayout(gl_panel_6);
        
        panel_7 = new JPanel();
        
        label_11 = new JLabel("10");
        
        textField_b10 = new JTextField();
        textField_b10.setToolTipText("put values sepated by comma or space");
        textField_b10.setColumns(10);
        
        checkBox_b10 = new JCheckBox("");
        
        button_S10 = new JButton("S");
        button_S10.setMargin(new Insets(2, 5, 2, 4));
        
        button_R10 = new JButton("R");
        button_R10.setMargin(new Insets(2, 5, 2, 4));
        
        label_12 = new JLabel("11");
        
        textField_b11 = new JTextField();
        textField_b11.setToolTipText("put values sepated by comma or space");
        textField_b11.setColumns(10);
        
        checkBox_b11 = new JCheckBox("");
        
        button_S11 = new JButton("S");
        button_S11.setMargin(new Insets(2, 5, 2, 4));
        
        button_R11 = new JButton("R");
        button_R11.setMargin(new Insets(2, 5, 2, 4));
        
        label_13 = new JLabel("12");
        
        textField_b12 = new JTextField();
        textField_b12.setToolTipText("put values sepated by comma or space");
        textField_b12.setColumns(10);
        
        checkBox_b12 = new JCheckBox("");
        
        button_S12 = new JButton("S");
        button_S12.setMargin(new Insets(2, 5, 2, 4));
        
        button_R12 = new JButton("R");
        button_R12.setMargin(new Insets(2, 5, 2, 4));
        GroupLayout gl_panel_7 = new GroupLayout(panel_7);
        gl_panel_7.setHorizontalGroup(
        	gl_panel_7.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_7.createSequentialGroup()
        			.addGap(2)
        			.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(textField_b10, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b10)
        			.addGap(4)
        			.addComponent(button_S10, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R10)
        			.addGap(9)
        			.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b11, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b11)
        			.addGap(4)
        			.addComponent(button_S11, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R11)
        			.addGap(8)
        			.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(textField_b12, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b12)
        			.addGap(4)
        			.addComponent(button_S12, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R12, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(17, Short.MAX_VALUE))
        );
        gl_panel_7.setVerticalGroup(
        	gl_panel_7.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_7.createSequentialGroup()
        			.addGroup(gl_panel_7.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b10))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b11))
        				.addComponent(button_S11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(checkBox_b12)
        				.addGroup(gl_panel_7.createParallelGroup(Alignment.BASELINE)
        					.addComponent(button_S12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(button_R12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_7.setLayout(gl_panel_7);
        
        panel_8 = new JPanel();
        
        label_14 = new JLabel("13");
        
        textField_b13 = new JTextField();
        textField_b13.setToolTipText("put values sepated by comma or space");
        textField_b13.setColumns(10);
        
        checkBox_b13 = new JCheckBox("");
        
        button_S13 = new JButton("S");
        button_S13.setMargin(new Insets(2, 5, 2, 4));
        
        button_R13 = new JButton("R");
        button_R13.setMargin(new Insets(2, 5, 2, 4));
        
        label_15 = new JLabel("14");
        
        textField_b14 = new JTextField();
        textField_b14.setToolTipText("put values sepated by comma or space");
        textField_b14.setColumns(10);
        
        checkBox_b14 = new JCheckBox("");
        
        button_S14 = new JButton("S");
        button_S14.setMargin(new Insets(2, 5, 2, 4));
        
        button_R14 = new JButton("R");
        button_R14.setMargin(new Insets(2, 5, 2, 4));
        
        label_16 = new JLabel("15");
        
        textField_b15 = new JTextField();
        textField_b15.setToolTipText("put values sepated by comma or space");
        textField_b15.setColumns(10);
        
        checkBox_b15 = new JCheckBox("");
        
        button_S15 = new JButton("S");
        button_S15.setMargin(new Insets(2, 5, 2, 4));
        
        button_R15 = new JButton("R");
        button_R15.setMargin(new Insets(2, 5, 2, 4));
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)
        				.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)
        				.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)
        				.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)))
        		.addGroup(groupLayout.createSequentialGroup()
        			.addGap(231)
        			.addComponent(btnCancel)
        			.addGap(34)
        			.addComponent(OK, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addComponent(chckbxPutFootnotesIn, GroupLayout.PREFERRED_SIZE, 321, GroupLayout.PREFERRED_SIZE)
        				.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 580, GroupLayout.PREFERRED_SIZE)))
        		.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
        			.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
        				.addGap(12)
        				.addComponent(label_4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        			.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
        				.addGap(10)
        				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 548, GroupLayout.PREFERRED_SIZE)
        					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 533, GroupLayout.PREFERRED_SIZE))))
        		.addGroup(groupLayout.createSequentialGroup()
        			.addGap(13)
        			.addComponent(lblBreakPointslevel, GroupLayout.PREFERRED_SIZE, 528, GroupLayout.PREFERRED_SIZE))
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
        			.addGap(7)
        			.addComponent(lblBreakPointslevel)
        			.addGap(1)
        			.addComponent(label_4)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(6)
        			.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(6)
        			.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(3)
        			.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(15)
        			.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(OK)
        				.addComponent(btnCancel))
        			.addContainerGap())
        );
        GroupLayout gl_panel_5 = new GroupLayout(panel_5);
        gl_panel_5.setHorizontalGroup(
        	gl_panel_5.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(9)
        			.addComponent(label_5)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField_b4, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b4)
        			.addGap(4)
        			.addComponent(button_S4, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R4)
        			.addGap(18)
        			.addComponent(label_6)
        			.addGap(3)
        			.addComponent(textField_b5, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b5)
        			.addGap(4)
        			.addComponent(button_S5, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R5)
        			.addGap(18)
        			.addComponent(label_7)
        			.addGap(4)
        			.addComponent(textField_b6, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b6)
        			.addGap(4)
        			.addComponent(button_S6, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R6))
        );
        gl_panel_5.setVerticalGroup(
        	gl_panel_5.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGroup(gl_panel_5.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b4, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(2)
        					.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b5, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b6, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(button_S6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_5.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
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
        			.addComponent(checkBox_b1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
        			.addGap(4)
        			.addComponent(button_S1, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R1)
        			.addGap(18)
        			.addComponent(label_1)
        			.addGap(3)
        			.addComponent(textField_b2, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
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
        			.addComponent(button_R3))
        );
        gl_panel_4.setVerticalGroup(
        	gl_panel_4.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(0, 0, Short.MAX_VALUE)
        			.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
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
        					.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_4.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_4.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_4.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_4.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_4.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap())
        );
        panel_4.setLayout(gl_panel_4);
        GroupLayout gl_panel_8 = new GroupLayout(panel_8);
        gl_panel_8.setHorizontalGroup(
        	gl_panel_8.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(2)
        			.addComponent(label_14, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(textField_b13, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b13)
        			.addGap(4)
        			.addComponent(button_S13, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R13)
        			.addGap(9)
        			.addComponent(label_15, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b14, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(checkBox_b14)
        			.addGap(4)
        			.addComponent(button_S14, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R14)
        			.addGap(8)
        			.addComponent(label_16, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(textField_b15, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b15)
        			.addGap(4)
        			.addComponent(button_S15, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R15))
        );
        gl_panel_8.setVerticalGroup(
        	gl_panel_8.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGroup(gl_panel_8.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_14, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b13))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label_15, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b14))
        				.addComponent(button_S14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_16, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_8.createSequentialGroup()
        					.addGap(1)
        					.addComponent(textField_b15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(checkBox_b15)
        				.addComponent(button_S15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel_8.setLayout(gl_panel_8);
        
        
        setBreakPointTextField(textField_b1,1);
        setBreakPointTextField(textField_b2,2);
        setBreakPointTextField(textField_b3,3);
        setBreakPointTextField(textField_b4,4);
        setBreakPointTextField(textField_b5,5);
        setBreakPointTextField(textField_b6,6);
        setBreakPointTextField(textField_b7,7);
        setBreakPointTextField(textField_b8,8);
        setBreakPointTextField(textField_b9,9);
        setBreakPointTextField(textField_b10,10);
        setBreakPointTextField(textField_b11,11);
        setBreakPointTextField(textField_b12,12);
        setBreakPointTextField(textField_b13,13);
        setBreakPointTextField(textField_b14,14);
        setBreakPointTextField(textField_b15,15);
        
        setBreakPointCheckBox(checkBox_b1,1);
        setBreakPointCheckBox(checkBox_b2,2);
        setBreakPointCheckBox(checkBox_b3,3);
        setBreakPointCheckBox(checkBox_b4,4);
        setBreakPointCheckBox(checkBox_b5,5);
        setBreakPointCheckBox(checkBox_b6,6);
        setBreakPointCheckBox(checkBox_b7,7);
        setBreakPointCheckBox(checkBox_b8,8);
        setBreakPointCheckBox(checkBox_b9,9);
        setBreakPointCheckBox(checkBox_b10,10);
        setBreakPointCheckBox(checkBox_b11,11);
        setBreakPointCheckBox(checkBox_b12,12);
        setBreakPointCheckBox(checkBox_b13,13);
        setBreakPointCheckBox(checkBox_b14,14);
        setBreakPointCheckBox(checkBox_b15,15);
        
        
        setBreakPointButton(button_R1,1,"R"); 
        setBreakPointButton(button_R2,2,"R"); 
        setBreakPointButton(button_R3,3,"R"); 
        setBreakPointButton(button_R4,4,"R"); 
        setBreakPointButton(button_R5,5,"R"); 
        setBreakPointButton(button_R6,6,"R"); 
        setBreakPointButton(button_R7,7,"R"); 
        setBreakPointButton(button_R8,8,"R"); 
        setBreakPointButton(button_R9,9,"R"); 
        setBreakPointButton(button_R10,10,"R"); 
        setBreakPointButton(button_R11,11,"R"); 
        setBreakPointButton(button_R12,12,"R"); 
        setBreakPointButton(button_R13,13,"R"); 
        setBreakPointButton(button_R14,14,"R"); 
        setBreakPointButton(button_R15,15,"R"); 
        
        setBreakPointButton(button_S1,1,"S");    
        setBreakPointButton(button_S2,2,"S");
        setBreakPointButton(button_S3,3,"S");
        setBreakPointButton(button_S4,4,"S");    
        setBreakPointButton(button_S5,5,"S");
        setBreakPointButton(button_S6,6,"S");
        setBreakPointButton(button_S7,7,"S");    
        setBreakPointButton(button_S8,8,"S");
        setBreakPointButton(button_S9,9,"S");
        setBreakPointButton(button_S10,10,"S");    
        setBreakPointButton(button_S11,11,"S");
        setBreakPointButton(button_S12,12,"S");
        setBreakPointButton(button_S13,13,"S");    
        setBreakPointButton(button_S14,14,"S");
        setBreakPointButton(button_S15,15,"S");
        
        
        getContentPane().setLayout(groupLayout);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OK;
    private JButton btnCancel;
    private JCheckBox checkBox_7;
    private JCheckBox chckbxLev;
    private JCheckBox chckbxIb;
    private JCheckBox chckbxIe;
    private JCheckBox chckbxIa;
    private JCheckBox chckbxPutFootnotesIn;
    private JCheckBox chckbxIp;
    private JCheckBox chckbxLogft;
    private JPanel panel;
    private JLabel lblOnlyPrintRow;
    private JCheckBox checkBox;
    private JCheckBox checkBox_1;
    private JCheckBox checkBox_2;
    private JCheckBox checkBox_3;
    private JCheckBox checkBox_4;
    private JCheckBox checkBox_8;
    private JCheckBox checkBox_9;
    private JPanel panel_4;
    private JLabel label;
    private JTextField textField_b1;
    private JCheckBox checkBox_b1;
    private JButton button_S1;
    private JButton button_R1;
    private JLabel label_1;
    private JTextField textField_b2;
    private JCheckBox checkBox_b2;
    private JButton button_S2;
    private JButton button_R2;
    private JLabel label_3;
    private JTextField textField_b3;
    private JCheckBox checkBox_b3;
    private JButton button_S3;
    private JButton button_R3;
    private JLabel label_4;
    private JPanel panel_5;
    private JLabel label_5;
    private JTextField textField_b4;
    private JCheckBox checkBox_b4;
    private JButton button_S4;
    private JButton button_R4;
    private JLabel label_6;
    private JTextField textField_b5;
    private JCheckBox checkBox_b5;
    private JButton button_S5;
    private JButton button_R5;
    private JLabel label_7;
    private JTextField textField_b6;
    private JCheckBox checkBox_b6;
    private JButton button_S6;
    private JButton button_R6;
    private JPanel panel_6;
    private JLabel label_8;
    private JTextField textField_b7;
    private JCheckBox checkBox_b7;
    private JButton button_S7;
    private JButton button_R7;
    private JLabel label_9;
    private JTextField textField_b8;
    private JCheckBox checkBox_b8;
    private JButton button_S8;
    private JButton button_R8;
    private JLabel label_10;
    private JTextField textField_b9;
    private JCheckBox checkBox_b9;
    private JButton button_S9;
    private JButton button_R9;
    private JPanel panel_7;
    private JLabel label_11;
    private JTextField textField_b10;
    private JCheckBox checkBox_b10;
    private JButton button_S10;
    private JButton button_R10;
    private JLabel label_12;
    private JTextField textField_b11;
    private JCheckBox checkBox_b11;
    private JButton button_S11;
    private JButton button_R11;
    private JLabel label_13;
    private JTextField textField_b12;
    private JCheckBox checkBox_b12;
    private JButton button_S12;
    private JButton button_R12;
    private JPanel panel_8;
    private JLabel label_14;
    private JTextField textField_b13;
    private JCheckBox checkBox_b13;
    private JButton button_S13;
    private JButton button_R13;
    private JLabel label_15;
    private JTextField textField_b14;
    private JCheckBox checkBox_b14;
    private JButton button_S14;
    private JButton button_R14;
    private JLabel label_16;
    private JTextField textField_b15;
    private JCheckBox checkBox_b15;
    private JButton button_S15;
    private JButton button_R15;
    private JCheckBox chckbxHf;
    private JCheckBox chckbxHf_1;
}
