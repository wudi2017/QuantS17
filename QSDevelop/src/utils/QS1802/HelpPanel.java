package utils.QS1802;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

public class HelpPanel {
	
	public static class RTMonitorPanel extends JPanel
	{
		public RTMonitorPanel()
		{
			this.setLayout(null);
			//this.setBackground(Color.red); 
			//this.setPreferredSize(new Dimension(800,300));
			this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			
			JLabel label_RTMonitor = new JLabel("RTMonitor");
			label_RTMonitor.setBounds(new Rectangle(10, 0, 100, 20));
			this.add(label_RTMonitor);
			
			{
				String[] columnNames = {"StockID","Strategy","BuyTriggerPrice","SellTriggerPrice",
						"MinCommitVal","MaxHoldAmount", "A1", "A2", "A3", "A4", "A5", "A6"};
		        String[][] tableValues = {};
				JTable table_holdstock = new JTable(tableValues,columnNames);
				JScrollPane scrollPane_holdstock = new JScrollPane();
				scrollPane_holdstock.setViewportView(table_holdstock);
				scrollPane_holdstock.setSize(0, 0);
				scrollPane_holdstock.setBounds(new Rectangle(10, 30, 780, 200));
				this.add(scrollPane_holdstock);
			}
			
			
			JButton btn_add = new JButton("Add");
			btn_add.setBounds(new Rectangle(10, 235, 80, 20));
			this.add(btn_add);
			
			JButton btn_remove = new JButton("Remove");
			btn_remove.setBounds(new Rectangle(100, 235, 80, 20));
			this.add(btn_remove);
			
			JButton btn_commit = new JButton("Commit");
			btn_commit.setBounds(new Rectangle(190, 235, 80, 20));
			this.add(btn_commit);
		}
	}
	
	public static class AccountInfoPanel extends JPanel
	{
		public AccountInfoPanel()
		{
			this.setLayout(null);
			//this.setBackground(Color.red); 
			//this.setPreferredSize(new Dimension(800,300));
			this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			
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
				scrollPane_holdstock.setBounds(new Rectangle(10, 90, 780, 200));
				this.add(scrollPane_holdstock);
			}
		}
	}
	

	public static class MainFramePanel extends JPanel
	{
		public MainFramePanel()
		{
			this.setLayout(null);
			//this.setBackground(Color.CYAN);
			this.setSize(800, 600);
			
			RTMonitorPanel panelRTMonitor = new RTMonitorPanel();
			panelRTMonitor.setBounds(new Rectangle(10, 20, 800, 270));
			this.add(panelRTMonitor);
			
			AccountInfoPanel panelAccountInfo = new AccountInfoPanel();
			panelAccountInfo.setBounds(new Rectangle(10, 300, 800, 300));
			this.add(panelAccountInfo);
		}
	}
	static class WindowListener extends WindowAdapter {  
		public void windowClosing(WindowEvent e) {  
			System.exit(0);  
		}  
	}  
	
	public void start()
	{
		JFrame jfrm = new JFrame();
		jfrm.setTitle("HelpPanel");
		jfrm.setSize(840, 650);
		jfrm.setResizable(false);
		jfrm.setLocation(100,100);
		jfrm.setContentPane(new MainFramePanel());
		//jfrm.pack();
		jfrm.addWindowListener(new WindowListener());
		jfrm.setVisible(true);
	}
}
