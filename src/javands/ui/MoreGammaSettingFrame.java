
package javands.ui;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.LayoutStyle.ComponentPlacement;

import ensdfparser.nds.ensdf.MassChain;

import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class MoreGammaSettingFrame extends BaseMoreSettingFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    public MoreGammaSettingFrame(MassChain mass,EnsdfWrap curEnsdf,javands.main.Run r) {
    	super(mass,curEnsdf,r);
        type="GAMMA";
    }
    
	
	/*display control settings for selected data set in control panel**/
	protected void displayTableControl(){

		Vector<String> columns;

		if(tc==null)
			return;
			
		
		columns=tc.getDataAsComments();
		setEnabledAndSelected(checkBox_7, tc,"E");setSelected(checkBox_7,columns,"E"); 
		setEnabledAndSelected(checkBox_8, tc,"RI");setSelected(checkBox_8,columns,"RI"); 
		setEnabledAndSelected(checkBox_9, tc,"LEV");setSelected(checkBox_9,columns,"LEV"); 
		setEnabledAndSelected(checkBox_10,tc,"JI");setSelected(checkBox_10,columns,"JI");
		setEnabledAndSelected(checkBox_11,tc,"LEVF");setSelected(checkBox_11,columns,"LEVF");		
		setEnabledAndSelected(checkBox_30, tc,"JF");setSelected(checkBox_30,columns,"JF"); 
		setEnabledAndSelected(checkBox_31, tc,"M");setSelected(checkBox_31,columns,"M"); 
		setEnabledAndSelected(checkBox_32, tc,"MR");setSelected(checkBox_32,columns,"MR"); 
		setEnabledAndSelected(checkBox_33,tc,"CC");setSelected(checkBox_33,columns,"CC");
		setEnabledAndSelected(checkBox_34,tc,"TI");setSelected(checkBox_34,columns,"TI");
		
		
		columns=tc.getFcolumns();
		setEnabledAndSelected(checkBox, tc,"E");setSelected(checkBox,columns,"E"); 
		setEnabledAndSelected(checkBox_1, tc,"RI");setSelected(checkBox_1,columns,"RI"); 
		setEnabledAndSelected(checkBox_2, tc,"LEV");setSelected(checkBox_2,columns,"LEV"); 
		setEnabledAndSelected(checkBox_3,tc,"JI");setSelected(checkBox_3,columns,"JI");
		setEnabledAndSelected(checkBox_4,tc,"LEVF");setSelected(checkBox_4,columns,"LEVF");		
		setEnabledAndSelected(checkBox_5, tc,"JF");setSelected(checkBox_5,columns,"JF"); 
		setEnabledAndSelected(checkBox_6, tc,"M");setSelected(checkBox_6,columns,"M"); 
		setEnabledAndSelected(checkBox_12, tc,"MR");setSelected(checkBox_12,columns,"MR"); 
		setEnabledAndSelected(checkBox_13,tc,"CC");setSelected(checkBox_13,columns,"CC");
		setEnabledAndSelected(checkBox_14,tc,"TI");setSelected(checkBox_14,columns,"TI");

		
		setSelected(chckbxPutFootnotesIn,tc.isSeparateFoots());
		
		setSelected(chckbxReorder,tc.reorderGamma());
		
		setEnabled(orderByGammaRadioButton,tc.reorderGamma());
		setEnabled(orderByGammaRadioButton,tc.reorderGamma());
		
		setSelected(orderByGammaRadioButton,tc.isSortGammas());
		setSelected(orderByLevelRadioButton,!tc.isSortGammas());
		
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
		if(checkBox_8.isSelected()) addColumn(columns,"RI");
		if(checkBox_9.isSelected()) addColumn(columns,"LEV");
		if(checkBox_10.isSelected()) addColumn(columns,"JI");
		if(checkBox_11.isSelected()) addColumn(columns,"LEVF");
		if(checkBox_30.isSelected()) addColumn(columns,"JF");
		if(checkBox_31.isSelected()) addColumn(columns,"M");
		if(checkBox_32.isSelected()) addColumn(columns,"MR");
		if(checkBox_33.isSelected()) addColumn(columns,"CC");
		if(checkBox_34.isSelected()) addColumn(columns,"TI");
		if(columns.size()>0) 
			tc.setDataAsComments(columns);
				
		columns=new Vector<String>();
		if(checkBox.isSelected()) addColumn(columns,"E");
		if(checkBox_1.isSelected()) addColumn(columns,"RI");
		if(checkBox_2.isSelected()) addColumn(columns,"LEV");
		if(checkBox_3.isSelected()) addColumn(columns,"JI");
		if(checkBox_4.isSelected()) addColumn(columns,"LEVF");
		if(checkBox_5.isSelected()) addColumn(columns,"JF");
		if(checkBox_6.isSelected()) addColumn(columns,"M");
		if(checkBox_12.isSelected()) addColumn(columns,"MR");
		if(checkBox_13.isSelected()) addColumn(columns,"CC");
		if(checkBox_14.isSelected()) addColumn(columns,"TI");
		if(columns.size()>0) 
			tc.setFcolumns(columns);
		

		tc.setIsSeparateFoots(chckbxPutFootnotesIn.isSelected());
		
		if(chckbxReorder.isSelected()){
			tc.setSortGammas(orderByGammaRadioButton.isSelected());
			
			//reset break points if ordering is changed
			tc.getBreakPoints().clear();
			tc.getBreaks().clear();
		}
		
		
		//update for break points is handled by corresponding listeners of components

	}
	

	
    protected void initComponents() {
        type="GAMMA";
    	popupMenus.clear();
    	brTextFields.clear();
    	brCheckBoxes.clear();
    	brButtons.clear();
    	
    	
    	//////////////////////////
    	//create objects
    	/////////////////////////
    	
        JPanel panel_2 = new JPanel();        
        JLabel lblDataAsComment = new JLabel("Data as comment:");       
        checkBox_7 = new JCheckBox("E");       
        checkBox_8 = new JCheckBox("RI");       
        checkBox_9 = new JCheckBox("EI");
        checkBox_9.setToolTipText("decaying level energy");       
        checkBox_10 = new JCheckBox("JI");
        checkBox_10.setToolTipText("decaying level spin-parity");        
        checkBox_11 = new JCheckBox("EF");
        checkBox_11.setToolTipText("final level energy");        
        checkBox_30 = new JCheckBox("JF");
        checkBox_30.setToolTipText("final level spin-parity");        
        checkBox_31 = new JCheckBox("MUL");        
        checkBox_32 = new JCheckBox("MR");        
        checkBox_33 = new JCheckBox("CC");        
        checkBox_34 = new JCheckBox("TI");
                       
        JPanel panel = new JPanel();        
        JLabel lblOnlyPrintRow = new JLabel("Print row if contain any:");        
        checkBox = new JCheckBox("E");       
        checkBox_1 = new JCheckBox("RI");       
        checkBox_2 = new JCheckBox("EI");
        checkBox_2.setToolTipText("decaying level energy");        
        checkBox_3 = new JCheckBox("JI");
        checkBox_3.setToolTipText("decaying level spin-parity");       
        checkBox_4 = new JCheckBox("EF");
        checkBox_4.setToolTipText("final level energy");       
        checkBox_5 = new JCheckBox("JF");
        checkBox_5.setToolTipText("final level spin-parity");        
        checkBox_6 = new JCheckBox("MUL");       
        checkBox_12 = new JCheckBox("MR");        
        checkBox_13 = new JCheckBox("CC");       
        checkBox_14 = new JCheckBox("TI");
        
        chckbxPutFootnotesIn = new JCheckBox("Put footnotes in a new column");       
        chckbxReorder = new JCheckBox("Reorder table by:");
        chckbxReorder.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		chckbxReorderItemStateChanged(e);
        	}
        });
        
        JLabel lblBreakPointslevel = new JLabel("Break points (level and gamma energies), check the box if starting a new page:");
        lblBreakPointslevel.setToolTipText("This table breaks up before given energies");
        JLabel label_15 = new JLabel("(S - suppress columns after a break point, R - restore previously suppressed columns)");
        
        
        JPanel panel_4 = new JPanel();
        
        checkBox_b1 = new JCheckBox("");setBreakPointCheckBox(checkBox_b1,1);           
        checkBox_b2 = new JCheckBox("");setBreakPointCheckBox(checkBox_b2,2);    
        
        textField_b3L = new JTextField();setBreakPointTextField(textField_b3L,3);
        textField_b3L.setToolTipText("");
        textField_b3L.setColumns(10);
        
        checkBox_b3 = new JCheckBox("");setBreakPointCheckBox(checkBox_b3,3);    
        
        JLabel lblL = new JLabel("1. L");
        
        JLabel lblL_2 = new JLabel("3. L");
               

        
        JPanel panel_5 = new JPanel();
        
        JLabel lblL_3 = new JLabel("4. L");
        
        textField_b4L = new JTextField();setBreakPointTextField(textField_b4L,4);
        textField_b4L.setToolTipText("");
        textField_b4L.setColumns(10);
        
        JLabel label_4 = new JLabel("G");
        
        textField_b4G = new JTextField();setBreakPointTextField(textField_b4G,4+15);
        textField_b4G.setToolTipText("");
        textField_b4G.setColumns(10);
        
        checkBox_b4 = new JCheckBox("");setBreakPointCheckBox(checkBox_b4,4);    
        
        button_S4 = new JButton("S");setBreakPointButton(button_S4,4,"S");
        button_S4.setMargin(new Insets(2, 5, 2, 4));
        
        button_R4 = new JButton("R");setBreakPointButton(button_R4,4,"R");
        button_R4.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_4 = new JLabel("5. L");
        
        textField_b5L = new JTextField();setBreakPointTextField(textField_b5L,5);
        textField_b5L.setToolTipText("");
        textField_b5L.setColumns(10);
        
        JLabel label_6 = new JLabel("G");
        
        textField_b5G = new JTextField();setBreakPointTextField(textField_b5G,5+15);
        textField_b5G.setToolTipText("");
        textField_b5G.setColumns(10);
        
        checkBox_b5 = new JCheckBox("");setBreakPointCheckBox(checkBox_b5,5);    
        
        button_S5 = new JButton("S");setBreakPointButton(button_S5,5,"S");
        button_S5.setMargin(new Insets(2, 5, 2, 4));
        
        button_R5 = new JButton("R");setBreakPointButton(button_R5,5,"R");
        button_R5.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_5 = new JLabel("6. L");
        
        textField_b6L = new JTextField();setBreakPointTextField(textField_b6L,6);
        textField_b6L.setToolTipText("");
        textField_b6L.setColumns(10);
        
        JLabel label_17 = new JLabel("G");
        
        textField_b6G = new JTextField();setBreakPointTextField(textField_b6G,6+15);
        textField_b6G.setToolTipText("");
        textField_b6G.setColumns(10);
        
        checkBox_b6 = new JCheckBox("");setBreakPointCheckBox(checkBox_b6,6);    
        
        button_S6 = new JButton("S");setBreakPointButton(button_S6,6,"S");
        button_S6.setMargin(new Insets(2, 5, 2, 4));
        
        button_R6 = new JButton("R");setBreakPointButton(button_R6,6,"R");
        button_R6.setMargin(new Insets(2, 5, 2, 4));
        
        JPanel panel_6 = new JPanel();
        
        JLabel lblL_6 = new JLabel("7. L");
        
        textField_b7L = new JTextField();setBreakPointTextField(textField_b7L,7);
        textField_b7L.setToolTipText("");
        textField_b7L.setColumns(10);
        
        JLabel label_5 = new JLabel("G");
        
        textField_b7G = new JTextField();setBreakPointTextField(textField_b7G,7+15);
        textField_b7G.setToolTipText("");
        textField_b7G.setColumns(10);
        
        checkBox_b7 = new JCheckBox("");setBreakPointCheckBox(checkBox_b7,7);    
        
        button_S7 = new JButton("S");setBreakPointButton(button_S7,7,"S");
        button_S7.setMargin(new Insets(2, 5, 2, 4));
        
        button_R7 = new JButton("R");setBreakPointButton(button_R7,7,"R");
        button_R7.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_7 = new JLabel("8. L");
        
        textField_b8L = new JTextField();setBreakPointTextField(textField_b8L,8);
        textField_b8L.setToolTipText("");
        textField_b8L.setColumns(10);
        
        JLabel label_8 = new JLabel("G");
        
        textField_b8G = new JTextField();setBreakPointTextField(textField_b8G,8+15);
        textField_b8G.setToolTipText("");
        textField_b8G.setColumns(10);
        
        checkBox_b8 = new JCheckBox("");setBreakPointCheckBox(checkBox_b8,8);    
        
        button_S8 = new JButton("S");setBreakPointButton(button_S8,8,"S");
        button_S8.setMargin(new Insets(2, 5, 2, 4));
        
        button_R8 = new JButton("R");setBreakPointButton(button_R8,8,"R");
        button_R8.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_8 = new JLabel("9. L");
        
        textField_b9L = new JTextField();setBreakPointTextField(textField_b9L,9);
        textField_b9L.setToolTipText("");
        textField_b9L.setColumns(10);
        
        JLabel label_10 = new JLabel("G");
        
        textField_b9G = new JTextField();setBreakPointTextField(textField_b9G,9+15);
        textField_b9G.setToolTipText("");
        textField_b9G.setColumns(10);
        
        checkBox_b9 = new JCheckBox("");setBreakPointCheckBox(checkBox_b9,9);    
        
        button_S9 = new JButton("S");setBreakPointButton(button_S9,9,"S");
        button_S9.setMargin(new Insets(2, 5, 2, 4));
        
        button_R9 = new JButton("R");setBreakPointButton(button_R9,9,"R");
        button_R9.setMargin(new Insets(2, 5, 2, 4));
        
        JPanel panel_7 = new JPanel();
        
        JLabel lblL_9 = new JLabel("10. L");
        
        textField_b10L = new JTextField();setBreakPointTextField(textField_b10L,10);
        textField_b10L.setToolTipText("");
        textField_b10L.setColumns(10);
        
        JLabel label_7 = new JLabel("G");
        
        textField_b10G = new JTextField();setBreakPointTextField(textField_b10G,10+15);
        textField_b10G.setToolTipText("");
        textField_b10G.setColumns(10);
        
        checkBox_b10 = new JCheckBox("");setBreakPointCheckBox(checkBox_b10,10);    
        
        button_S10 = new JButton("S");setBreakPointButton(button_S10,10,"S");
        button_S10.setMargin(new Insets(2, 5, 2, 4));
        
        button_R10 = new JButton("R");setBreakPointButton(button_R10,10,"R");
        button_R10.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_10 = new JLabel("11. L");
        
        textField_b11L = new JTextField();setBreakPointTextField(textField_b11L,11);
        textField_b11L.setToolTipText("");
        textField_b11L.setColumns(10);
        
        JLabel label_11 = new JLabel("G");
        
        textField_b11G = new JTextField();setBreakPointTextField(textField_b11G,11+15);
        textField_b11G.setToolTipText("");
        textField_b11G.setColumns(10);
        
        checkBox_b11 = new JCheckBox("");setBreakPointCheckBox(checkBox_b11,11);    
        
        button_S11 = new JButton("S");setBreakPointButton(button_S11,11,"S");
        button_S11.setMargin(new Insets(2, 5, 2, 4));
        
        button_R11 = new JButton("R");setBreakPointButton(button_R11,11,"R");
        button_R11.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_11 = new JLabel("12. L");
        
        textField_b12L = new JTextField();setBreakPointTextField(textField_b12L,12);
        textField_b12L.setToolTipText("");
        textField_b12L.setColumns(10);
        
        JLabel label_13 = new JLabel("G");
        
        textField_b12G = new JTextField();setBreakPointTextField(textField_b12G,12+15);
        textField_b12G.setToolTipText("");
        textField_b12G.setColumns(10);
        
        checkBox_b12 = new JCheckBox("");setBreakPointCheckBox(checkBox_b12,12);    
        
        button_S12 = new JButton("S");setBreakPointButton(button_S12,12,"S");
        button_S12.setMargin(new Insets(2, 5, 2, 4));
        
        button_R12 = new JButton("R");setBreakPointButton(button_R12,12,"R");
        button_R12.setMargin(new Insets(2, 5, 2, 4));
        
        JPanel panel_8 = new JPanel();
        
        JLabel lblL_12 = new JLabel("13. L");
        
        textField_b13L = new JTextField();setBreakPointTextField(textField_b13L,13);
        textField_b13L.setToolTipText("");
        textField_b13L.setColumns(10);
        
        JLabel label_9 = new JLabel("G");
        
        textField_b13G = new JTextField();setBreakPointTextField(textField_b13G,13+15);
        textField_b13G.setToolTipText("");
        textField_b13G.setColumns(10);
        
        checkBox_b13 = new JCheckBox("");setBreakPointCheckBox(checkBox_b13,13);    
        
        button_S13 = new JButton("S");setBreakPointButton(button_S13,13,"S");
        button_S13.setMargin(new Insets(2, 5, 2, 4));
        
        button_R13 = new JButton("R");setBreakPointButton(button_R13,13,"R");
        button_R13.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_13 = new JLabel("14. L");
        
        textField_b14L = new JTextField();setBreakPointTextField(textField_b14L,14);
        textField_b14L.setToolTipText("");
        textField_b14L.setColumns(10);
        
        JLabel label_14 = new JLabel("G");
        
        textField_b14G = new JTextField();setBreakPointTextField(textField_b14G,14+15);
        textField_b14G.setToolTipText("");
        textField_b14G.setColumns(10);
        
        checkBox_b14 = new JCheckBox("");setBreakPointCheckBox(checkBox_b14,14);    
        
        button_S14 = new JButton("S");setBreakPointButton(button_S14,14,"S");
        button_S14.setMargin(new Insets(2, 5, 2, 4));
        
        button_R14 = new JButton("R");setBreakPointButton(button_R14,14,"R");
        button_R14.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_14 = new JLabel("15. L");
        
        textField_b15L = new JTextField();setBreakPointTextField(textField_b15L,15);
        textField_b15L.setToolTipText("");
        textField_b15L.setColumns(10);
        
        JLabel label_18 = new JLabel("G");
        
        textField_b15G = new JTextField();setBreakPointTextField(textField_b15G,15+15);
        textField_b15G.setToolTipText("");
        textField_b15G.setColumns(10);
        
        checkBox_b15 = new JCheckBox("");setBreakPointCheckBox(checkBox_b15,15);    
        
        button_S15 = new JButton("S");setBreakPointButton(button_S15,15,"S");
        button_S15.setMargin(new Insets(2, 5, 2, 4));
        
        button_R15 = new JButton("R");setBreakPointButton(button_R15,15,"R");
        button_R15.setMargin(new Insets(2, 5, 2, 4));
        
        JLabel lblL_1 = new JLabel("2. L");
        
        textField_b2L = new JTextField();setBreakPointTextField(textField_b2L,2);
        textField_b2L.setToolTipText("");
        textField_b2L.setColumns(10);
        
        textField_b1L = new JTextField();setBreakPointTextField(textField_b1L,1);
        textField_b1L.setToolTipText("");
        textField_b1L.setColumns(10);
        
        textField_b1G = new JTextField();setBreakPointTextField(textField_b1G,1+15);
        textField_b1G.setToolTipText("");
        textField_b1G.setColumns(10);
        
        JLabel lblG = new JLabel("G");
        
        textField_b2G = new JTextField();setBreakPointTextField(textField_b2G,2+15);
        textField_b2G.setToolTipText("");
        textField_b2G.setColumns(10);
        
        label = new JLabel("G");
        
        label_1 = new JLabel("G");
        
        textField_b3G = new JTextField();setBreakPointTextField(textField_b3G,3+15);
        textField_b3G.setToolTipText("");
        textField_b3G.setColumns(10);
        
        button_S1 = new JButton("S");setBreakPointButton(button_S1,1,"S");
        button_S1.setMargin(new Insets(2, 5, 2, 4));
        
        button_R1 = new JButton("R");setBreakPointButton(button_R1,1,"R");
        button_R1.setMargin(new Insets(2, 5, 2, 4));
        
        button_S2 = new JButton("S");setBreakPointButton(button_S2,2,"S");
        button_S2.setMargin(new Insets(2, 5, 2, 4));
        
        button_R2 = new JButton("R");setBreakPointButton(button_R2,2,"R");
        button_R2.setMargin(new Insets(2, 5, 2, 4));
        
        button_S3 = new JButton("S");setBreakPointButton(button_S3,3,"S");
        button_S3.setMargin(new Insets(2, 5, 2, 4));
        
        button_R3 = new JButton("R");setBreakPointButton(button_R3,3,"R");
        button_R3.setMargin(new Insets(2, 5, 2, 4));
        
    	/////////////////////////
    	
        OK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        this.setTitle("More settings for gamma table control.");
        
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
        			.addGap(0, 0, Short.MAX_VALUE)
        			.addComponent(lblDataAsComment, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
        			.addGap(32)
        			.addComponent(checkBox_7, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_8)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_9)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_10)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_11)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_30)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_31)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(checkBox_32)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_33)
        			.addGap(5)
        			.addComponent(checkBox_34)
        			.addGap(166))
        );
        gl_panel_2.setVerticalGroup(
        	gl_panel_2.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_2.createSequentialGroup()
        			.addGap(1)
        			.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
        				.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_7, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(lblDataAsComment))
        				.addComponent(checkBox_8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_31, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_9))
        				.addComponent(checkBox_33, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_34, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_32)
        					.addComponent(checkBox_10)
        					.addComponent(checkBox_11)
        					.addComponent(checkBox_30)))
        			.addGap(1))
        );
        panel_2.setLayout(gl_panel_2);
        
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(0, 0, Short.MAX_VALUE)
        			.addComponent(lblOnlyPrintRow, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
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
        			.addComponent(checkBox_5)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_6)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(checkBox_12)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(checkBox_13)
        			.addGap(5)
        			.addComponent(checkBox_14)
        			.addGap(166))
        );
        gl_panel.setVerticalGroup(
        	gl_panel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel.createSequentialGroup()
        			.addGap(1)
        			.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
        				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(lblOnlyPrintRow))
        				.addComponent(checkBox_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_6, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_2))
        				.addComponent(checkBox_13, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
        					.addComponent(checkBox_14, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        					.addComponent(checkBox_12)
        					.addComponent(checkBox_3)
        					.addComponent(checkBox_4)
        					.addComponent(checkBox_5)))
        			.addGap(1))
        );
        panel.setLayout(gl_panel);
        

        GroupLayout gl_panel_7 = new GroupLayout(panel_7);
        gl_panel_7.setHorizontalGroup(
        	gl_panel_7.createParallelGroup(Alignment.LEADING)
        		.addGroup(Alignment.TRAILING, gl_panel_7.createSequentialGroup()
        			.addGap(1)
        			.addComponent(lblL_9, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b10L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b10G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S10, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R10)
        			.addGap(1)
        			.addComponent(lblL_10, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b11L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b11G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S11, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R11)
        			.addGap(1)
        			.addComponent(lblL_11, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b12L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b12G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S12, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R12, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gl_panel_7.setVerticalGroup(
        	gl_panel_7.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_7.createSequentialGroup()
        			.addGroup(gl_panel_7.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_7.createParallelGroup(Alignment.BASELINE)
        					.addComponent(textField_b10L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(lblL_9, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b10G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(checkBox_b10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_S10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblL_10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b11L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b11G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(checkBox_b11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_S11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addComponent(button_R11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(lblL_11, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b12L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b12G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_7.createSequentialGroup()
        					.addGap(1)
        					.addGroup(gl_panel_7.createParallelGroup(Alignment.LEADING)
        						.addComponent(button_S12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        						.addComponent(button_R12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        						.addComponent(checkBox_b12, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))))
        			.addContainerGap())
        );
        panel_7.setLayout(gl_panel_7);
        
        orderByGammaRadioButton = new JRadioButton("Gamma");
        orderByGammaRadioButton.setEnabled(false);
        
        orderByLevelRadioButton = new JRadioButton("Level");
        orderByLevelRadioButton.setEnabled(false);
        
		buttonGroup1=new javax.swing.ButtonGroup();
        buttonGroup1.add(orderByGammaRadioButton);
        buttonGroup1.add(orderByLevelRadioButton);
        
        /*
        orderByGammaRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	orderByGammaRadioButtonActionPerformed(evt);
            }
        });


        orderByLevelRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	orderByLevelRadioButtonActionPerformed(evt);
            }

        });
        */
        
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addGap(0, 0, Short.MAX_VALUE)
        			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(10)
        					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        						.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
        						.addGroup(groupLayout.createSequentialGroup()
        							.addComponent(chckbxPutFootnotesIn, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
        							.addGap(18)
        							.addComponent(chckbxReorder, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(orderByGammaRadioButton)
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addComponent(orderByLevelRadioButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE))
        						.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
        						.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
        						.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
        						.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
        						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
        							.addComponent(panel, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
        							.addComponent(panel_2, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 675, Short.MAX_VALUE))
        						.addComponent(lblBreakPointslevel, GroupLayout.PREFERRED_SIZE, 588, GroupLayout.PREFERRED_SIZE)
        						.addComponent(label_15, GroupLayout.PREFERRED_SIZE, 613, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(groupLayout.createSequentialGroup()
        					.addGap(279)
        					.addComponent(btnCancel)
        					.addGap(50)
        					.addComponent(OK, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap())
        );
        groupLayout.setVerticalGroup(
        	groupLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(groupLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(5)
        			.addComponent(panel, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(chckbxReorder)
        				.addComponent(chckbxPutFootnotesIn)
        				.addComponent(orderByGammaRadioButton)
        				.addComponent(orderByLevelRadioButton))
        			.addGap(4)
        			.addComponent(lblBreakPointslevel)
        			.addGap(1)
        			.addComponent(label_15)
        			.addGap(11)
        			.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 23, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        			.addGap(15)
        			.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(OK)
        				.addComponent(btnCancel))
        			.addContainerGap())
        );
        GroupLayout gl_panel_8 = new GroupLayout(panel_8);
        gl_panel_8.setHorizontalGroup(
        	gl_panel_8.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(lblL_12, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b13L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b13G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S13, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R13)
        			.addGap(1)
        			.addComponent(lblL_13, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b14L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_14, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b14G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S14, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R14)
        			.addGap(1)
        			.addComponent(lblL_14, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b15L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_18, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b15G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S15, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R15))
        );
        gl_panel_8.setVerticalGroup(
        	gl_panel_8.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(lblL_12, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addComponent(textField_b13L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b13G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(checkBox_b13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_S13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_R13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(lblL_13, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b14L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b14G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_S14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_R14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addComponent(lblL_14, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b15L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_18, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b15G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_S15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_8.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_R15, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        );
        panel_8.setLayout(gl_panel_8);
        GroupLayout gl_panel_6 = new GroupLayout(panel_6);
        gl_panel_6.setHorizontalGroup(
        	gl_panel_6.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(7)
        			.addComponent(lblL_6, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b7L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b7G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S7, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R7)
        			.addGap(8)
        			.addComponent(lblL_7, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b8L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b8G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S8, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R8)
        			.addGap(8)
        			.addComponent(lblL_8, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b9L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b9G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S9, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R9))
        );
        gl_panel_6.setVerticalGroup(
        	gl_panel_6.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(lblL_6, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addComponent(textField_b7L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b7G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(checkBox_b7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_S7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_R7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(lblL_7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b8L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b8G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_S8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_R8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addComponent(lblL_8, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b9L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b9G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_S9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_6.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_R9, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        );
        panel_6.setLayout(gl_panel_6);
        GroupLayout gl_panel_5 = new GroupLayout(panel_5);
        gl_panel_5.setHorizontalGroup(
        	gl_panel_5.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(7)
        			.addComponent(lblL_3, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b4L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b4G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S4, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R4)
        			.addGap(8)
        			.addComponent(lblL_4, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b5L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b5G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S5, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R5)
        			.addGap(8)
        			.addComponent(lblL_5, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b6L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_17, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b6G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S6, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(button_R6))
        );
        gl_panel_5.setVerticalGroup(
        	gl_panel_5.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(lblL_3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        		.addComponent(textField_b4L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b4G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(checkBox_b4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_S4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(button_R4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(lblL_4, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b5L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b5G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_S5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_R5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addComponent(lblL_5, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b6L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(label_17, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addComponent(textField_b6G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(checkBox_b6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_S6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        		.addGroup(gl_panel_5.createSequentialGroup()
        			.addGap(1)
        			.addComponent(button_R6, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        );
        panel_5.setLayout(gl_panel_5);
        GroupLayout gl_panel_4 = new GroupLayout(panel_4);
        gl_panel_4.setHorizontalGroup(
        	gl_panel_4.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGap(7)
        			.addComponent(lblL, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b1L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(lblG, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b1G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S1)
        			.addGap(1)
        			.addComponent(button_R1)
        			.addGap(8)
        			.addComponent(lblL_1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b2L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b2G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S2)
        			.addGap(2)
        			.addComponent(button_R2)
        			.addGap(8)
        			.addComponent(lblL_2, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b3L, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
        			.addGap(1)
        			.addComponent(textField_b3G, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(checkBox_b3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        			.addGap(2)
        			.addComponent(button_S3)
        			.addGap(2)
        			.addComponent(button_R3)
        			.addContainerGap())
        );
        gl_panel_4.setVerticalGroup(
        	gl_panel_4.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_panel_4.createSequentialGroup()
        			.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_panel_4.createSequentialGroup()
        					.addGap(1)
        					.addComponent(lblL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
        				.addComponent(textField_b1L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblG, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b1G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(checkBox_b1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_S1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(button_R1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblL_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b2L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b2G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblL_2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b3L, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(textField_b3G, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE, false)
        					.addComponent(button_S3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(button_R3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
        				.addComponent(checkBox_b3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(checkBox_b2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
        					.addComponent(button_S2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        					.addComponent(button_R2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap())
        );
        panel_4.setLayout(gl_panel_4);
        getContentPane().setLayout(groupLayout);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    protected void chckbxReorderItemStateChanged(ItemEvent e) {
    	if(((JCheckBox)e.getSource()).isSelected()){
    		tc.setRecorderGamma(true);
    		orderByGammaRadioButton.setEnabled(true);
    		orderByLevelRadioButton.setEnabled(true);
    	}
    	else{
    		tc.setRecorderGamma(false);

    		setSelected(orderByGammaRadioButton,tc.isSortGammas());
    		setSelected(orderByGammaRadioButton,!tc.isSortGammas());
    		
    		orderByGammaRadioButton.setEnabled(false);
    		orderByLevelRadioButton.setEnabled(false);
    		
    	}		
	}
    
    @SuppressWarnings("unused")
	private void orderByGammaRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if(tc.reorderGamma()){
            tc.setSortGammas(true);
    	}

    }
    
    @SuppressWarnings("unused")
	private void orderByLevelRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if(tc.reorderGamma()){
            tc.setSortGammas(false);
    	}
    }


	// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OK;
    private JButton btnCancel;
    private JTextField textField_b3L;
    private JTextField textField_b2L;
    private JTextField textField_b1L;
    private JCheckBox chckbxPutFootnotesIn;
    private JCheckBox checkBox_b1;
    private JCheckBox checkBox_b2;
    private JTextField textField_b1G;
    private JTextField textField_b2G;
    private JLabel label;
    private JLabel label_1;
    private JTextField textField_b3G;
    private JCheckBox checkBox_7;
    private JCheckBox checkBox_8;
    private JCheckBox checkBox_9;
    private JCheckBox checkBox_10;
    private JCheckBox checkBox_11;
    private JCheckBox checkBox_30;
    private JCheckBox checkBox_31;
    private JCheckBox checkBox_32;
    private JCheckBox checkBox_33;
    private JCheckBox checkBox_34;
    private JTextField textField_b4L;
    private JTextField textField_b4G;
    private JTextField textField_b5L;
    private JTextField textField_b5G;
    private JTextField textField_b6L;
    private JTextField textField_b6G;
    private JTextField textField_b7L;
    private JTextField textField_b7G;
    private JTextField textField_b8L;
    private JTextField textField_b8G;
    private JTextField textField_b9L;
    private JTextField textField_b9G;
    private JTextField textField_b10L;
    private JTextField textField_b10G;
    private JTextField textField_b11L;
    private JTextField textField_b11G;
    private JTextField textField_b12L;
    private JTextField textField_b12G;
    private JTextField textField_b13L;
    private JTextField textField_b13G;
    private JTextField textField_b14L;
    private JTextField textField_b14G;
    private JTextField textField_b15L;
    private JTextField textField_b15G;
    private JButton button_S1;
    private JButton button_R1;
    private JButton button_S2;
    private JButton button_R2;
    private JButton button_S4;
    private JButton button_R4;
    private JButton button_S7;
    private JButton button_R7;
    private JButton button_S10;
    private JButton button_R10;
    private JButton button_S13;
    private JButton button_R13;
    private JButton button_S5;
    private JButton button_R5;
    private JButton button_S8;
    private JButton button_R8;
    private JButton button_S11;
    private JButton button_R11;
    private JButton button_S14;
    private JButton button_R14;
    private JButton button_S6;
    private JButton button_R6;
    private JButton button_S9;
    private JButton button_R9;
    private JButton button_S12;
    private JButton button_R12;
    private JButton button_S15;
    private JButton button_R15;
    private JCheckBox checkBox_b15;
    private JCheckBox checkBox_b12;
    private JCheckBox checkBox_b9;
    private JCheckBox checkBox_b6;
    private JCheckBox checkBox_b3;
    private JCheckBox checkBox_b5;
    private JCheckBox checkBox_b8;
    private JCheckBox checkBox_b11;
    private JCheckBox checkBox_b14;
    private JCheckBox checkBox_b13;
    private JCheckBox checkBox_b10;
    private JCheckBox checkBox_b7;
    private JCheckBox checkBox_b4;
    private JCheckBox chckbxReorder;
    private JCheckBox checkBox;
    private JCheckBox checkBox_1;
    private JCheckBox checkBox_2;
    private JCheckBox checkBox_3;
    private JCheckBox checkBox_4;
    private JCheckBox checkBox_5;
    private JCheckBox checkBox_6;
    private JCheckBox checkBox_12;
    private JCheckBox checkBox_13;
    private JCheckBox checkBox_14;
    private JRadioButton orderByGammaRadioButton;
    private JRadioButton orderByLevelRadioButton;
	private javax.swing.ButtonGroup buttonGroup1;
	private JButton button_S3;
	private JButton button_R3;
}
