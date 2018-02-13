package test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;
/**
 * 测试JTable添加数据，删除数据频繁操作，JTable出现数组越界的处理
 * 在工作中如果遇到频繁的操作Jtable的数据，特别是速率很快的情况下，经常会遇到
 * Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException
 * 这样的数组越界的异常,这里引入Swing的一个线程，能很好的解决这个问题
 * 供同样遇到这样问题的人参考。
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