package test;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

public class TestUISample {
	public static class FramePane extends JPanel
	{
		public FramePane()
		{
			// 绝对布局
			this.setLayout(null);
			// border
			this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			
			// JTabbedPane
			// JTabbedPane jTabbedpane = new JTabbedPane();
			// JPanel jpanelFirst = new JPanel();
			// jTabbedpane.addTab("firstTap", null, jpanelFirst, "first"); 
			// JPanel jpanelSecond = new JPanel();
			// jTabbedpane.addTab("secondTap", null, jpanelSecond, "second");
			// jTabbedpane.setSize(200, 200);
			
			// JCheckBox
			// JCheckBox pwdKeep = new JCheckBox("记住密码");
			
			// JComboBox
			// JComboBox adminType = new JComboBox(new String[] { "普通职员", "管理员", "高级管理员" });
			
			// JPasswordField tfPwd = new JPasswordField();
			
			JLabel label_AccountInfo = new JLabel("AccountInfo");
			label_AccountInfo.setBounds(new Rectangle(10, 0, 100, 20));
			this.add(label_AccountInfo);
			
			JLabel label_totalassets = new JLabel("TotalAssets:");
			label_totalassets.setBounds(new Rectangle(10, 20, 100, 20));
			this.add(label_totalassets);
			
			JTextField tfTotalAssets = new JTextField();
			tfTotalAssets.setText("");
			tfTotalAssets.setEditable(false);
			tfTotalAssets.setBounds(new Rectangle(100, 20, 100, 18));
			this.add(tfTotalAssets);
			
			JLabel label_money = new JLabel("Money:");
			label_money.setBounds(new Rectangle(10, 40, 100, 20));
			this.add(label_money);
			
			JTextField tfMoney = new JTextField();
			tfMoney.setText("");
			tfMoney.setEditable(false);
			tfMoney.setBounds(new Rectangle(100, 40, 100, 18));
			this.add(tfMoney);
			
			JLabel label_marketValue = new JLabel("MarketValue:");
			label_marketValue.setBounds(new Rectangle(10, 60, 100, 20));
			this.add(label_marketValue);
			
			JTextField tfMarketValue = new JTextField();
			tfMarketValue.setText("");
			tfMarketValue.setEditable(false);
			tfMarketValue.setBounds(new Rectangle(100, 60, 100, 18));
			this.add(tfMarketValue);
	
			{
				String[] columnNames = {"StockID","Strategy","BuyTriggerPrice","SellTriggerPrice",
						"MinCommitVal","MaxHoldAmount", "A1", "A2", "A3", "A4", "A5", "A6"};
		        String[][] tableValues = {};
				JTable table_holdstock = new JTable(tableValues,columnNames);
				JScrollPane scrollPane_holdstock = new JScrollPane();
				scrollPane_holdstock.setViewportView(table_holdstock);
				scrollPane_holdstock.setSize(0, 0);
				scrollPane_holdstock.setBounds(new Rectangle(0, 90, 800, 200));
				this.add(scrollPane_holdstock);
			}
			
			
			JLabel label_RTMonitor = new JLabel("RTMonitor");
			label_RTMonitor.setBounds(new Rectangle(10, 300, 100, 20));
			this.add(label_RTMonitor);
			
			{
				String[] columnNames = {"StockID","Strategy","BuyTriggerPrice","SellTriggerPrice",
						"MinCommitVal","MaxHoldAmount", "A1", "A2", "A3", "A4", "A5", "A6"};
		        String[][] tableValues = {};
				JTable table_holdstock = new JTable(tableValues,columnNames);
				JScrollPane scrollPane_holdstock = new JScrollPane();
				scrollPane_holdstock.setViewportView(table_holdstock);
				scrollPane_holdstock.setSize(0, 0);
				scrollPane_holdstock.setBounds(new Rectangle(0, 330, 800, 200));
				this.add(scrollPane_holdstock);
			}
			
			
			JButton btn_add = new JButton("Add");
			btn_add.setBounds(new Rectangle(10, 535, 80, 20));
			this.add(btn_add);
			
			JButton btn_remove = new JButton("Remove");
			btn_remove.setBounds(new Rectangle(100, 535, 80, 20));
			this.add(btn_remove);
			
			JButton btn_commit = new JButton("Commit");
			btn_commit.setBounds(new Rectangle(190, 535, 80, 20));
			this.add(btn_commit);
		}
	}
	
	
	static class MyWindowListener extends WindowAdapter {  
		public void windowClosing(WindowEvent e) {  
			System.exit(0);  
		}  
	}  
	
	public static void main(String[] args) {
		JFrame jfrm = new JFrame();
		jfrm.setTitle("TestUI");
		jfrm.setSize(815, 600);
		jfrm.setContentPane(new FramePane());
		jfrm.addWindowListener(new MyWindowListener());
		jfrm.setVisible(true);
	}
}
