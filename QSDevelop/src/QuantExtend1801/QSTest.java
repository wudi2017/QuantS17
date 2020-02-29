package QuantExtend1801;

import java.awt.*;  
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;

import QuantExtend1711.utils.EKRefHistoryPos;
import QuantExtend1711.utils.ZCZXChecker;
import QuantExtend1711.utils.EKRefHistoryPos.EKRefHistoryPosParam;

import java.util.List;

import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;

public class QSTest extends QS1801Base {
	public QSTest() {
	}

	@Override
	void onStrateInit(QuantContext ctx) {
		super.setGlobalMaxHoldStockCount(2);
		super.setGlobalStockMaxHoldPosstion(0.3);
		super.setGlobalStockOneCommitPossition(0.5);
		super.setGlobalStockMinCommitInterval(10);
		super.setGlobalStockMaxHoldDays(20);
		super.setGlobalStockTargetProfitRatio(0.1);
		super.setGlobalStockStopLossRatio(0.12);
	}

	@Override
	void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
//		String stockID = cDAStock.ID();
//		double curPrice = cDAStock.price();
//		double lastClose = super.getStockPropertyDouble(cDAStock.ID(), "lastClose");
//		if((curPrice-lastClose)/lastClose < -0.02)
//		{
//			super.buySignalEmit(ctx, stockID);
//		}
//		if((curPrice-lastClose)/lastClose > 0.02)
//		{
//			super.sellSignalEmit(ctx, stockID);
//		}
//		// default process
//		super.onAutoForceClearProcess(ctx, cDAStock);
	}

	@Override
	void onStrateDayFinish(QuantContext ctx) {
	
		// 过滤：股票ID集合，基本信息
		for(int iStock=0; iStock<ctx.pool().size(); iStock++)
		{
			DAStock cDAStock = ctx.pool().get(iStock);
			if(
				//cDAStock.ID().compareTo("000060") >= 0 && cDAStock.ID().compareTo("000060") <= 0  &&
				cDAStock.dayKLines().size() >= 60
				&& cDAStock.dayKLines().lastDate().equals(ctx.date())
				&& cDAStock.circulatedMarketValue() <= 1000.0) {
				
				String stockID = cDAStock.ID();
				
				super.selectAdd(stockID, 0);
			}
		}
		
		// 过滤：早晨之星
		List<String> listSelect = super.selectList();
		super.selectClear();
		for(int iStock=0; iStock<listSelect.size(); iStock++)
		{
			String stockID = listSelect.get(iStock);
			DAStock cDAStock = ctx.pool().get(stockID);
			// 5天内存在早晨之星
			int iBegin = cDAStock.dayKLines().size()-1-5;
			int iEnd = cDAStock.dayKLines().size()-1;
			for(int i=iEnd;i>=iBegin;i--)
			{
				if(ZCZXChecker.check(cDAStock.dayKLines(),i))
				{
					boolean bcheckVolume = ZCZXChecker.check_volume(cDAStock.dayKLines(),i);
					if(bcheckVolume)
					{
						EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
						super.selectAdd(stockID, -cEKRefHistoryPosParam.refHigh);
						super.setPrivateStockPropertyLong(stockID, "ZCZX_EndIndex", i);
					}
				}
			}
		}
		
		// 保留10
		super.selectKeepMaxCount(10);
		
		super.selectDump();
	}
	
	/*
	 * *************************************************************************************
	 */
	
	public static class JTb1 extends JFrame  
	{  
	    JTable table;  
	    public JTb1()  
	    {  
	        super("test title"); 
	        super.setSize(800,600); 
	        
	        try{ 
	        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  
	        }catch(Exception e)
	        {
	        	
	        }  
	        Container c=getContentPane();  
	        //创建表值  
	        Object[][]data={  
	            {"082520","name1","03A01",80,90,95,(80+90+95)},  
	            {"082521","name2","03A02",88,90,90,(88+90+90)}  
	        };  
	        String[] rowName={"学号","姓名","班级","数学","体育","英语","总分"};  
	        table=new JTable(data,rowName);//创建表格对象  
	        c.add(new JScrollPane(table));  
	        setVisible(true);  
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	    }  
	}
	
	public static class JTabbedPaneDemo  extends JPanel {  
		  
	    public JTabbedPaneDemo() {  
	    	JTabbedPane jTabbedpane = new JTabbedPane();
	    	
	        // 第一个标签下的JPanel  
	        JPanel jpanelFirst = new JPanel();  
	        // jTabbedpane.addTab(tabNames[i++],icon,creatComponent(),"first");//加入第一个页面  
	        jTabbedpane.addTab("tabName1", null, jpanelFirst, "first");// 加入第一个页面
	        jpanelFirst.setLayout(new GridLayout(1, 1));

//	        // 第二个标签下的JPanel  
	        JPanel jpanelSecond = new JPanel();  
	        jTabbedpane.addTab("tabName2", null, jpanelSecond, "second");// 加入第一个页面 
     
	        setLayout(new GridLayout(1, 1));  
	        add(jTabbedpane);  
	        
	        
	        String[] rowName={"学号","姓名","班级","数学","体育","英语","总分"}; 
	        Object[][]data={  
		            {"082520","name1","03A01",80,90,95,(80+90+95)},  
		            {"082521","name2","03A02",88,90,90,(88+90+90)}  
		        };  
	        JTable table = new JTable(data,rowName);//创建表格对象
	        
	        JScrollPane jspan = new JScrollPane(table);
	        jpanelFirst.add(jspan);
	    }  
	}
	public static void main(String[] args) throws Exception {
		
		JFrame frame = new JFrame();  
        frame.setLayout(null);  
        frame.setContentPane(new JTabbedPaneDemo());  
        frame.setSize(800, 600);  
        frame.setVisible(true);  
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
//		CSystem.start();
//		
//		CLog.output("TEST", "FastTest main begin");
//		
//		// create testaccount
//		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
//		cAccoutDriver.load("account_QS1801T1" ,  new MockAccountOpe(), true);
//		cAccoutDriver.reset(100000);
//		Account acc = cAccoutDriver.account();
//		
//		Quant.instance().run(
//				"HistoryTest 2016-03-01 2016-04-01", // Realtime | HistoryTest 2016-01-01 2017-01-01
//				cAccoutDriver, 
//				new QSTest());
//		qSession.resetDataRoot("C:\\D\\MyProg\\QuantS17Release\\rw\\data");
//		qSession.run();
//		
//		CLog.output("TEST", "FastTest main end");
//		CSystem.stop();
	}

}
