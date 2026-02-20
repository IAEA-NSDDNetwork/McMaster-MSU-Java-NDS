package javands.ui;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ensdfparser.nds.ensdf.MassChain;

public class DrawingSelectorFrame extends javax.swing.JFrame {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private JScrollPane scrollPane;
	private JTable drawingSelectorTable;
	
    public String[] datasetNames;
    public boolean[] drawBand;
    public boolean[] drawLevel;
    public MassChain massChain;
    
	public DrawingSelectorFrame(MassChain massChain) {
		
		this.massChain=massChain;
		initComponents();

	}

	
    private void setTableRenderer(JTable bandTable){
    	
    	
    	DrawingSelectorTableCellRenderer centerRenderer = new DrawingSelectorTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DrawingSelectorTableCellRenderer leftRenderer = new DrawingSelectorTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        
        bandTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        bandTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        bandTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        bandTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        bandTable.getColumnModel().getColumn(1).setPreferredWidth(20);
        bandTable.getColumnModel().getColumn(2).setPreferredWidth(20);   

    	((DefaultTableCellRenderer)bandTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }
    
    private void initComponents() {
    	setTitle("Selector for band/level drawings");
    	
		scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		drawingSelectorTable = new JTable();
		
		
		drawingSelectorTable.setModel(new DefaultTableModel(
            	new Object[][] {
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            		{null, null, null},
            	},
            	new String[] {
            		"Dataset ID", "Draw band?","Draw level?"
            	}
            ));

            
		drawingSelectorTable.setModel(new DrawingSelectorTableModel(massChain));
		drawingSelectorTable.getColumnModel().getColumn(0).setPreferredWidth(11);
        
        
        JTextField jtext=new JTextField();
        
        jtext.setFont(new Font("Serif",Font.PLAIN,11));
        
        javax.swing.table.TableCellEditor editor=new DefaultCellEditor(jtext);

        drawingSelectorTable.getColumnModel().getColumn(0).setCellEditor(editor);
        //drawingSelectorTable.getColumnModel().getColumn(2).setCellEditor(editor);
        
        drawingSelectorTable.setRowHeight(18);
        drawingSelectorTable.setRowMargin(-1);
        
        setTableRenderer(drawingSelectorTable); 
        
		scrollPane.setViewportView(drawingSelectorTable);
		
		//javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        //getContentPane().setLayout(layout);

        pack();
    }
    
    //this is the format for the table where band selection is done
    //check out the java documentations for TableModels if you want to know how it fits into the GUI
    public class DrawingSelectorTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.table.TableModel{
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int numRows;
		MassChain massChain;
		
        DrawingSelectorTableModel(MassChain massChain){
        	this.massChain=massChain;
            numRows=massChain.nENSDF();
            datasetNames=new String[numRows];
            drawBand=new boolean[numRows];
            drawLevel=new boolean[numRows];

        }
        public void addTableModelListener(javax.swing.event.TableModelListener l){
            
        }
        public Class<?> getColumnClass(int columnIndex){
            if (columnIndex==1)
                return Boolean.class;
            else if(columnIndex==2) 
                return Boolean.class;
            return String.class;
            //return null;
        }
        public int getColumnCount(){
            return 3;
        }
        public String getColumnName(int columnIndex){
            switch(columnIndex){
                case 0:
                    return "Dataset ID";
                case 1:
                    return "Draw band?";
                case 2:
                    return "Draw level?";
                default:
                    return null;
            }
        }
        public int getRowCount(){
        	if(numRows<20)
        		return 20;
        	
            return numRows;
        }
        public Object getValueAt(int rowIndex, int columnIndex){
        	if(rowIndex>=massChain.nENSDF())
        		return null;
        	
            if (columnIndex==1){
            	drawBand[rowIndex]=massChain.getETD(rowIndex).getBandDrawingControl().isDrawn();
                return new Boolean(drawBand[rowIndex]);// ? "Yes" : "No");
            }
            else if (columnIndex==0){
            	datasetNames[rowIndex]=massChain.getENSDF(rowIndex).nucleus().nameENSDF()+":"+massChain.getENSDF(rowIndex).fullDSIdS();
                return datasetNames[rowIndex];
            }
            else if (columnIndex==2){
            	drawLevel[rowIndex]=massChain.getETD(rowIndex).getDecayDrawingControl().isDrawn();
                return new Boolean(drawLevel[rowIndex]);
            }
            
            return null;
        }
        public boolean isCellEditable(int rowIndex, int columnIndex){
        	if(columnIndex==0 || rowIndex>=massChain.nENSDF())
        		return false;
        	
        	else if (columnIndex==1){
            	if(massChain.getENSDF(rowIndex).nBands()<=0)
            		return false;
            	else
            		return true;
            }
            else if (columnIndex==2){
            	ensdfparser.ensdf.ENSDF ens=massChain.getENSDF(rowIndex);
            	if(ens.nGamWFL()>0 || ens.nAlpWDL()>0 || ens.nBetWDL()>0 || ens.nECBPWDL()>0 ||ens.nDPWEI()>0)
            		return true;
            	else
            		return false;

            }
            return false;
        }
        public void removeTableModelListener(javax.swing.event.TableModelListener l){
            
        }
        public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        	if(rowIndex>=massChain.nENSDF())
        		return;
        	
            if(columnIndex==1){
            	drawBand[rowIndex]=(Boolean)aValue;
            	massChain.getETD(rowIndex).getBandDrawingControl().setDrawn(drawBand[rowIndex]);
            }
            else if(columnIndex==0){
            	datasetNames[rowIndex]=(String)aValue;
            }
            else if(columnIndex==2){
            	drawLevel[rowIndex]=(Boolean)aValue;
            	massChain.getETD(rowIndex).getDecayDrawingControl().setDrawn(drawLevel[rowIndex]);
            }
            
            fireTableCellUpdated(rowIndex,columnIndex);
        }        
    }
    
    class DrawingSelectorTableCellRenderer extends DefaultTableCellRenderer 
    {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
			
            if(column==1 || column==2){
            	JCheckBox cellComponent=new JCheckBox();
            	cellComponent.setEnabled(table.isCellEditable(row, column));
            	cellComponent.setSelected(value!=null && (Boolean)value);
            	cellComponent.setHorizontalAlignment(JLabel.CENTER);
            	return cellComponent;

            }else
            	return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


        }
    } 
}
