package utils.QS1802;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


public class HelpPanel {
	
	public static class SelectPanel extends JPanel
	{
		public SelectPanel()
		{
			this.setLayout(null);
			//this.setBackground(Color.red); 
			//this.setPreferredSize(new Dimension(800,300));
			this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			
			JLabel label_RTMonitor = new JLabel("Select");
			label_RTMonitor.setBounds(new Rectangle(10, 0, 100, 20));
			this.add(label_RTMonitor);

			JCheckBox checkbox_AutoAddMonitor = new JCheckBox(" AutoAddMonitor");
			checkbox_AutoAddMonitor.setBounds(new Rectangle(100, 10, 250, 20));
			this.add(checkbox_AutoAddMonitor);
			
			{
				m_SelectTable = new JTable();
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setViewportView(m_SelectTable);
				scrollPane.setBounds(new Rectangle(10, 40, 1155, 150));
				this.add(scrollPane);

				Vector vName = new Vector();
				vName.add("StockID");
				vName.add("Priority");
				vName.add("Date");
				Vector vData = new Vector();
				DefaultTableModel model = new DefaultTableModel(vData, vName);
				m_SelectTable.setModel(model);
			}

		}
		
		private JTable m_SelectTable;
	}
	
	public static class RTMonitorPanel extends JPanel
	{
		public static class AddBtnListener implements ActionListener
		{
			public AddBtnListener(RTMonitorPanel rtmt)
			{
				m_RTMonitorPanel = rtmt;
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				m_RTMonitorPanel.onClickAdd();
			}
			private RTMonitorPanel m_RTMonitorPanel;
		}
		public static class RemoveBtnListener implements ActionListener
		{
			public RemoveBtnListener(RTMonitorPanel rtmt)
			{
				m_RTMonitorPanel = rtmt;
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				m_RTMonitorPanel.onClickRemove();
			}
			private RTMonitorPanel m_RTMonitorPanel;
		}
		public static class CommitBtnListener implements ActionListener
		{
			public CommitBtnListener(RTMonitorPanel rtmt)
			{
				m_RTMonitorPanel = rtmt;
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				m_RTMonitorPanel.onClickCommit();
			}
			private RTMonitorPanel m_RTMonitorPanel;
		}
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
		        m_RTMonitorTable = new JTable();
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setViewportView(m_RTMonitorTable);
				scrollPane.setBounds(new Rectangle(10, 30, 1155, 200));
				this.add(scrollPane);

				Vector vName = new Vector();
				vName.add("stockID");
				vName.add("strategy");
				vName.add("buyTriggerPrice");
				vName.add("sellTriggerPrice");
				vName.add("minCommitInterval");
				vName.add("oneCommitAmount");
				vName.add("maxHoldAmount");
				vName.add("targetProfitPrice");
				vName.add("targetProfitMoney");
				vName.add("stopLossPrice");
				vName.add("stopLossMoney");
				vName.add("maxHoldDays");
				Vector vData = new Vector();
				DefaultTableModel model = new DefaultTableModel(vData, vName);
				m_RTMonitorTable.setModel(model);
			}

			JButton btn_add = new JButton("Add");
			btn_add.setBounds(new Rectangle(10, 235, 80, 20));
			btn_add.addActionListener(new AddBtnListener(this));
			this.add(btn_add);
			
			JButton btn_remove = new JButton("Remove");
			btn_remove.setBounds(new Rectangle(100, 235, 80, 20));
			btn_remove.addActionListener(new RemoveBtnListener(this));
			this.add(btn_remove);
			
			JButton btn_commit = new JButton("Commit");
			btn_commit.setBounds(new Rectangle(190, 235, 80, 20));
			btn_commit.addActionListener(new CommitBtnListener(this));
			this.add(btn_commit);
		}
		
		public void onClickAdd()
		{
			DefaultTableModel dftModel = (DefaultTableModel)m_RTMonitorTable.getModel();
			dftModel.addRow(new Vector());
		}
		
		public void onClickRemove()
		{
			DefaultTableModel dftModel = (DefaultTableModel)m_RTMonitorTable.getModel();
			int numrow=m_RTMonitorTable.getSelectedRows().length;
            for (int i=0;i<numrow;i++){
                   //É¾³ýËùÑ¡ÐÐ;
            	dftModel.removeRow(m_RTMonitorTable.getSelectedRow());
            }
		}
		
		public void onClickCommit()
		{
			DefaultTableModel dftModel = (DefaultTableModel)m_RTMonitorTable.getModel();
		}
		
		private JTable m_RTMonitorTable;
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
				JTable table_holdstock = new JTable();
				JScrollPane scrollPane_holdstock = new JScrollPane();
				scrollPane_holdstock.setViewportView(table_holdstock);
				scrollPane_holdstock.setSize(0, 0);
				scrollPane_holdstock.setBounds(new Rectangle(10, 90, 1155, 200));
				this.add(scrollPane_holdstock);
				
				Vector vName = new Vector();
				vName.add("stockID");
				vName.add("createDate");
				vName.add("totalAmount");
				vName.add("availableAmount");
				vName.add("totalBuyCost");
				vName.add("curPrice");
				vName.add("refPrimeCostPrice");
				Vector vData = new Vector();
				DefaultTableModel model = new DefaultTableModel(vData, vName);
				table_holdstock.setModel(model);
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
			
			SelectPanel selectPane = new SelectPanel();
			selectPane.setBounds(new Rectangle(10, 20, 1175, 200));
			this.add(selectPane);
			
			RTMonitorPanel panelRTMonitor = new RTMonitorPanel();
			panelRTMonitor.setBounds(new Rectangle(10, 240, 1175, 270));
			this.add(panelRTMonitor);
			
			AccountInfoPanel panelAccountInfo = new AccountInfoPanel();
			panelAccountInfo.setBounds(new Rectangle(10, 530, 1175, 300));
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
		jfrm.setSize(1200, 900);
		jfrm.setResizable(false);
		jfrm.setLocation(10,10);
		jfrm.setContentPane(new MainFramePanel());
		//jfrm.pack();
		jfrm.addWindowListener(new WindowListener());
		jfrm.setVisible(true);
	}
}
