package utils.QS1802;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Map;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import pers.di.common.CLog;
import pers.di.common.CSyncObj;
import pers.di.common.CThread;
import pers.di.quantplatform.AccountProxy;
import utils.QS1802.QURTMonitorTable.MonitorItem;


public class HelpPanel {
	
	public boolean bindQUObject(QUSelectTable selectTable, 
			QURTMonitorTable cQURTMonitorTable, 
			AccountProxy ap)
	{
		m_sync.Lock();
		
		m_QURTMonitorTable = cQURTMonitorTable;
		m_QURTMonitorTable.registerCallback(new QURTMonitorTableCB(this));
		FlushMonitorTable2JTable();
		
		m_sync.UnLock();
		return true;
	}

	private void FlushMonitorTable2JTable()
	{
		m_sync.Lock();
		
		Runnable runnable = new Runnable() {
			public void run() {
				DefaultTableModel dftModel = (DefaultTableModel)m_MainFramePanel.m_RTMonitorPanel.m_RTMonitorTable.getModel();
				// clear
				// dftModel.getDataVector().clear();
				// dftModel.setRowCount(0);
				while(dftModel.getRowCount()>0){
					dftModel.removeRow(dftModel.getRowCount()-1);
				}
				// add
				Map<String, MonitorItem> itemsMap = m_QURTMonitorTable.CopyOriginROMirrorMap();
				for (Map.Entry<String, MonitorItem> entry : itemsMap.entrySet()) { 
					String stockID = entry.getKey();
					MonitorItem cMonitorItem = entry.getValue();

					Vector newRot = new Vector();
					newRot.add(stockID);
					newRot.add(cMonitorItem.strategy());
					newRot.add(cMonitorItem.buyTriggerPrice());
					newRot.add(cMonitorItem.sellTriggerPrice());
					newRot.add(cMonitorItem.minCommitInterval());
					newRot.add(cMonitorItem.oneCommitAmount());
					newRot.add(cMonitorItem.maxHoldAmount());
					newRot.add(cMonitorItem.targetProfitPrice());
					newRot.add(cMonitorItem.targetProfitMoney());
					newRot.add(cMonitorItem.stopLossPrice());
					newRot.add(cMonitorItem.stopLossMoney());
					newRot.add(cMonitorItem.maxHoldDays());
					dftModel.addRow(newRot);
				}
			}
		};
		SwingUtilities.invokeLater(runnable);	
		
		m_sync.UnLock();
	}
	
	private void onRTMonitorPanelCommit()
	{
		FlushJTable2MonitorTable();
	}
	
	private void FlushJTable2MonitorTable()
	{
		m_sync.Lock();

		DefaultTableModel dftModel = (DefaultTableModel)m_MainFramePanel.m_RTMonitorPanel.m_RTMonitorTable.getModel();

		// 清除QURTMonitorTable数据，但要停止callback，否则导致UItable为NULL
		m_QURTMonitorTable.registerCallback(null);
		m_QURTMonitorTable.removeAllItem();
		m_QURTMonitorTable.registerCallback(new QURTMonitorTableCB(this));
		
		int iRowCnt = dftModel.getDataVector().size();
		for(int iRow=0; iRow<iRowCnt; iRow++)
		{
			String stockID = (String)dftModel.getValueAt(iRow, 0);
			String strategy = (String)dftModel.getValueAt(iRow, 1);
			String buyTriggerPrice = (String)dftModel.getValueAt(iRow, 2);
			String sellTriggerPrice = (String)dftModel.getValueAt(iRow, 3);
			String minCommitInterval = (String)dftModel.getValueAt(iRow, 4);
			String oneCommitAmount = (String)dftModel.getValueAt(iRow, 5);
			String maxHoldAmount = (String)dftModel.getValueAt(iRow, 6);
			String targetProfitPrice = (String)dftModel.getValueAt(iRow, 7);
			String targetProfitMoney = (String)dftModel.getValueAt(iRow, 8);
			String stopLossPrice = (String)dftModel.getValueAt(iRow, 9);
			String stopLossMoney = (String)dftModel.getValueAt(iRow, 10);
			String maxHoldDays = (String)dftModel.getValueAt(iRow, 11);
			
			m_QURTMonitorTable.addItem(stockID);
			MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
			cMonitorItem.setStrategy(strategy);
			if(null != buyTriggerPrice)
				cMonitorItem.setBuyTriggerPrice(Double.parseDouble(buyTriggerPrice));
			if(null != sellTriggerPrice)
				cMonitorItem.setSellTriggerPrice(Double.parseDouble(sellTriggerPrice));
			if(null != minCommitInterval)
				cMonitorItem.setMinCommitInterval(Long.parseLong(minCommitInterval));
			if(null != oneCommitAmount)
				cMonitorItem.setOneCommitAmount(Long.parseLong(oneCommitAmount));
			if(null != maxHoldAmount)
				cMonitorItem.setMaxHoldAmount(Long.parseLong(maxHoldAmount));
			if(null != targetProfitPrice)
				cMonitorItem.setTargetProfitPrice(Double.parseDouble(targetProfitPrice));
			if(null != targetProfitMoney)
				cMonitorItem.setTargetProfitMoney(Double.parseDouble(targetProfitMoney));
			if(null != stopLossPrice)
				cMonitorItem.setStopLossPrice(Double.parseDouble(stopLossPrice));
			if(null != stopLossMoney)
				cMonitorItem.setStopLossMoney(Double.parseDouble(stopLossMoney));
			if(null != maxHoldDays)
				cMonitorItem.setMaxHoldDays(Long.parseLong(maxHoldDays));
		}

		m_sync.UnLock();
	}
	
	public static class QURTMonitorTableCB implements QURTMonitorTable.ICallback
	{
		public QURTMonitorTableCB(HelpPanel cHelpPanel)
		{
			m_HelpPanel = cHelpPanel;
		}
		
		@Override
		public void onNotify(CALLBACKTYPE cb) {
			m_HelpPanel.FlushMonitorTable2JTable();
		}
		private HelpPanel m_HelpPanel;
	}
	
	private QURTMonitorTable m_QURTMonitorTable;
	
	/*
	 * =====================================================================================================
	 * 
	 * 
	 *  UI
	 * 
	 * 
	 * =====================================================================================================
	 */

	public HelpPanel ()
	{
		m_sync = new CSyncObj();
		m_MainFramePanel = new MainFramePanel(this);
	}
	public void start()
	{
		JFrame jfrm = new JFrame();
		jfrm.setTitle("HelpPanel");
		jfrm.setSize(1200, 900);
		jfrm.setResizable(false);
		jfrm.setLocation(10,10);
		jfrm.setContentPane(m_MainFramePanel);
		//jfrm.pack();
		jfrm.addWindowListener(new WindowListener());
		jfrm.setVisible(true);
	}
	
	static private void FitTableColumns(JTable myTable) {
	    JTableHeader header = myTable.getTableHeader();
	    int rowCount = myTable.getRowCount();

	    Enumeration columns = myTable.getColumnModel().getColumns();
	    while (columns.hasMoreElements()) {
	        TableColumn column = (TableColumn) columns.nextElement();
	        int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
	        int width = (int) myTable.getTableHeader().getDefaultRenderer()
	                .getTableCellRendererComponent(myTable, column.getIdentifier(), false, false, -1, col)
	                .getPreferredSize().getWidth();
	        for (int row = 0; row < rowCount; row++) {
	            int preferedWidth = (int) myTable.getCellRenderer(row, col)
	                    .getTableCellRendererComponent(myTable, myTable.getValueAt(row, col), false, false, row, col)
	                    .getPreferredSize().getWidth();
	            width = Math.max(width, preferedWidth);
	        }
	        header.setResizingColumn(column);
	        column.setWidth(width + myTable.getIntercellSpacing().width + 10);
	    }
	}
	
	public static class SelectPanel extends JPanel
	{
		public SelectPanel(MainFramePanel ower)
		{
			m_owerMainFramePanel = ower;
			
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
				scrollPane.setBounds(new Rectangle(10, 40, 400, 150));
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
		
		private MainFramePanel m_owerMainFramePanel;
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
		public RTMonitorPanel(MainFramePanel ower)
		{
			m_owerMainFramePanel = ower;
			
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

				String[] header = new String[] { 
						"stockID", "strategy", "buyTriggerPrice", "sellTriggerPrice",
						"minCommitInterval", "oneCommitAmount", "maxHoldAmount", 
						"targetProfitPrice", "targetProfitMoney", "stopLossPrice", "stopLossMoney", "maxHoldDays",
						};
				DefaultTableModel model = new DefaultTableModel(header, 0);
				m_RTMonitorTable.setModel(model);
				HelpPanel.FitTableColumns(m_RTMonitorTable);
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
                   //删除所选行;
            	dftModel.removeRow(m_RTMonitorTable.getSelectedRow());
            }
		}
		
		public void onClickCommit()
		{
			DefaultTableModel dftModel = (DefaultTableModel)m_RTMonitorTable.getModel();
			m_owerMainFramePanel.onRTMonitorPanelCommit();
		}
		
		private MainFramePanel m_owerMainFramePanel;
		private JTable m_RTMonitorTable;
	}
	
	public static class AccountInfoPanel extends JPanel
	{
		public AccountInfoPanel(MainFramePanel ower)
		{
			m_owerMainFramePanel = ower;
			
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
		
		private MainFramePanel m_owerMainFramePanel;
	}
	

	public static class MainFramePanel extends JPanel
	{
		public MainFramePanel(HelpPanel ower)
		{
			m_owerHelpPanel = ower;
			
			this.setLayout(null);
			//this.setBackground(Color.CYAN);
			this.setSize(800, 600);
			
			m_selectPane = new SelectPanel(this);
			m_selectPane.setBounds(new Rectangle(10, 20, 1175, 200));
			this.add(m_selectPane);
			
			m_RTMonitorPanel = new RTMonitorPanel(this);
			m_RTMonitorPanel.setBounds(new Rectangle(10, 240, 1175, 270));
			this.add(m_RTMonitorPanel);
			
			m_AccountInfoPanel = new AccountInfoPanel(this);
			m_AccountInfoPanel.setBounds(new Rectangle(10, 530, 1175, 300));
			this.add(m_AccountInfoPanel);
		}
		
		public void onRTMonitorPanelCommit()
		{
			m_owerHelpPanel.onRTMonitorPanelCommit();
		}
		
		private HelpPanel m_owerHelpPanel;
		
		private SelectPanel m_selectPane;
		private RTMonitorPanel m_RTMonitorPanel;
		private AccountInfoPanel m_AccountInfoPanel;
	}
	
	static class WindowListener extends WindowAdapter {  
		public void windowClosing(WindowEvent e) {  
			System.exit(0);  
		}  
	}  
	
	public CSyncObj m_sync;
	public MainFramePanel m_MainFramePanel;
}
