package test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;
/**
 * ����JTable������ݣ�ɾ������Ƶ��������JTable��������Խ��Ĵ���
 * �ڹ������������Ƶ���Ĳ���Jtable�����ݣ��ر������ʺܿ������£�����������
 * Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException
 * ����������Խ����쳣,��������Swing��һ���̣߳��ܺܺõĽ���������
 * ��ͬ����������������˲ο���
 *  *  *
 */
public class TestThreadTable extends JTable {
	
	static String[] header = new String[] { "id", "name", "sex", "age" };

	public TestThreadTable() {
		DefaultTableModel model = new DefaultTableModel(header, 0);
		this.setModel(model);
	}

	public void updateTable()
	{
		final Vector<String> value = new Vector<String>();
		value.add("0");
		value.add("simon");
		value.add("boy");
		value.add("21");
		
		DefaultTableModel model = (DefaultTableModel)this.getModel();
		model.addRow(value);
		
		if (model.getRowCount() > 5) {
			
			int rmRowCnt = 3;
			if (rmRowCnt < model.getColumnCount()) 
			{   
				for (int i = rmRowCnt - 1; i >= 0; i--) 
				{ 
					model.removeRow(i); 
				} 
			}
		}
	}
	
	public void testInsertValue() {
		for (int i = 0; i < 100000; i++) {
			
//			updateTable();
			
			Runnable runnable = new Runnable() {
				public void run() {
					updateTable();
				}
			};
			SwingUtilities.invokeLater(runnable);
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
	
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());

		TestThreadTable table = new TestThreadTable();
		JScrollPane scroll = new JScrollPane(table);
		f.getContentPane().add(scroll, BorderLayout.CENTER);
	
		f.setSize(800, 600);
		f.setLocation(250, 250);
		f.setVisible(true);
	
		table.testInsertValue();
	}
}