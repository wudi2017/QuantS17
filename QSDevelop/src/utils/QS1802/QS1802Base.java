package utils.QS1802;

import java.util.*;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.account.common.TRANACT;
import pers.di.common.CFileSystem;
import pers.di.common.CL2Property;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CUtilsDateTime;
import pers.di.common.CConsole;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.AccountProxy;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;
import utils.QS1711.TranDaysChecker;
import utils.QS1711.TranReportor;
import utils.QS1801.QUCommon;
import utils.QS1801.QUProperty;
import utils.QS1802.QURTMonitorTable;
import utils.QS1802.QURTMonitorTable.MonitorItem;
import utils.QS1802.QURTMonitorTable.ICallback.CALLBACKTYPE;
import utils.QS1801.QUSelector;

public abstract class QS1802Base extends QuantStrategy {
	
	/*
	 * DefaultConfig
	 */
	public static class DefaultConfig
	{
		public DefaultConfig()
		{
			// default
			GlobalDefaultShowHelpPanel = true;
			GlobalDefaultAutoMoveSelectToMonitor = true;
			GlobalDefaultMinCommitInterval = 30L;
			GlobalDefaulMaxHoldStockCount = 5L;
			GlobalDefaulStockMaxHoldPosstion = 0.2;
			GlobalDefaulStockOneCommitPossition = 1.0;
			GlobalDefaulStockMaxHoldDays = 30L;
			GlobalDefaulStockTargetProfitRatio = 0.1;
			GlobalDefaulStockStopLossRatio = -0.12;
		}
		public void copyFrom(DefaultConfig cfg)
		{
			GlobalDefaultShowHelpPanel = cfg.GlobalDefaultShowHelpPanel;
			GlobalDefaultAutoMoveSelectToMonitor = cfg.GlobalDefaultAutoMoveSelectToMonitor;
			GlobalDefaultMinCommitInterval = cfg.GlobalDefaultMinCommitInterval;
			GlobalDefaulMaxHoldStockCount = cfg.GlobalDefaulMaxHoldStockCount;
			GlobalDefaulStockMaxHoldPosstion = cfg.GlobalDefaulStockMaxHoldPosstion;
			GlobalDefaulStockOneCommitPossition = cfg.GlobalDefaulStockOneCommitPossition;
			GlobalDefaulStockMaxHoldDays = cfg.GlobalDefaulStockMaxHoldDays;
			GlobalDefaulStockTargetProfitRatio = cfg.GlobalDefaulStockTargetProfitRatio;
			GlobalDefaulStockStopLossRatio = cfg.GlobalDefaulStockStopLossRatio;
		}
		public Boolean GlobalDefaultShowHelpPanel;
		public Boolean GlobalDefaultAutoMoveSelectToMonitor;
		public Long GlobalDefaultMinCommitInterval;
		public Long GlobalDefaulMaxHoldStockCount;
		public Double GlobalDefaulStockMaxHoldPosstion;
		public Double GlobalDefaulStockOneCommitPossition;
		public Long GlobalDefaulStockMaxHoldDays;
		public Double GlobalDefaulStockTargetProfitRatio;
		public Double GlobalDefaulStockStopLossRatio;
	}
	
	
	public QS1802Base()
	{
		// initialize m_defaultCfg
		m_defaultCfg = new DefaultConfig();
		m_helpPanel = null;
	}
	public void setDefaultConfig(DefaultConfig defaultCfg)
	{
		m_defaultCfg.copyFrom(defaultCfg);
	}
	public DefaultConfig getDefaultConfig()
	{
		return m_defaultCfg;
	}
	public QUSelectTable QUSelectTable()
	{
		return m_QUSelectTable;
	}
	
	/*
	 * buy sell signal
	 */
	public boolean buySignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// check monitor table item
		MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
		if(null == cMonitorItem)
		{
			return false;
		}
		
		Long lStockOneCommitInterval = null;
		Long lFullHoldAmount = null;
		Long lOneCommitAmount = null;
		Double dTargetProfitMoney = null;
		Double dStockStopLossMoney = null;
		Long lStockMaxHoldDays = null;
		
		// interval commit check
		lStockOneCommitInterval = cMonitorItem.minCommitInterval();
		if(null == lStockOneCommitInterval)
		{
			Long lMinCommitInterval =  m_defaultCfg.GlobalDefaultMinCommitInterval;
			if(null != lMinCommitInterval)
			{
				lStockOneCommitInterval = lMinCommitInterval;
			}
		}
		CommissionOrder cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.accountProxy(), stockID, TRANACT.BUY);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			long seconds = CUtilsDateTime.subTime(ctx.time(), cCommissionOrder.time);
			if(seconds < lStockOneCommitInterval*60)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
				return false;
			}
		}
		
		lFullHoldAmount = cMonitorItem.maxHoldAmount();
		lOneCommitAmount = cMonitorItem.oneCommitAmount();

		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.accountProxy().getTotalAssets(ctnTotalAssets);
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		ctx.accountProxy().getMoney(ctnMoney);
		
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		
		if(null == cHoldStock) // first create
		{
			// max hold count check
			Long lMaxHoldStockCount = m_defaultCfg.GlobalDefaulMaxHoldStockCount;
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.accountProxy().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() > lMaxHoldStockCount)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
				return false;
			}
			
			// define stock FullHoldAmount OneCommitAmount property
			if(null == lFullHoldAmount)
			{
				Double dGlobalStockMaxPosstion = m_defaultCfg.GlobalDefaulStockMaxHoldPosstion;
				double curFullPositionMoney = ctnTotalAssets.get()*dGlobalStockMaxPosstion;
				long curFullPositionAmmount = (long)(curFullPositionMoney/fNowPrice);
				lFullHoldAmount = curFullPositionAmmount;
				
			}
			if(null == lOneCommitAmount)
			{
				Double dGlobalStockOneCommitPossition = m_defaultCfg.GlobalDefaulStockOneCommitPossition;
				Long curFullPositionAmmount = lFullHoldAmount;
				long curStockOneCommitPossitionAmmount = (long)(curFullPositionAmmount*dGlobalStockOneCommitPossition);
				lOneCommitAmount = curStockOneCommitPossitionAmmount;
			}	
			// 标准化
			long newlOneCommitAmount = lOneCommitAmount; 
			if(0 != newlOneCommitAmount%100)
			{
				newlOneCommitAmount = newlOneCommitAmount/100*100;
			}
			if(0 != newlOneCommitAmount)
			{
				if(newlOneCommitAmount != lOneCommitAmount)
				{
					lOneCommitAmount = newlOneCommitAmount;
				}
				if(0 != lFullHoldAmount%newlOneCommitAmount)
				{
					lFullHoldAmount = (lFullHoldAmount/newlOneCommitAmount)*newlOneCommitAmount;
				}
			}
		}
		
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		if(lAlreadyHoldAmount >= lFullHoldAmount) // FullHoldAmount AlreadyHoldAmount check
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lAlreadyHoldAmount=%d lFullHoldAmount=%d",  stockID, lAlreadyHoldAmount, lFullHoldAmount);
			return false;
		}
		Long lCommitAmount = Math.min(lFullHoldAmount-lAlreadyHoldAmount, lOneCommitAmount);
		lCommitAmount = lCommitAmount/100*100;
		if(lCommitAmount < 100) // CommitAmount check
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! iCreateAmount=%d", stockID, lCommitAmount);
			return false;
		}
		double needCommitMoney = lCommitAmount*fNowPrice;
		if(needCommitMoney > ctnMoney.get()) // CommitMoney check
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! needCommitMoney=%.3f ctnMoney=%.3f", stockID, needCommitMoney,ctnMoney.get());
			return false;
		}
		
		// post request
		ctx.accountProxy().pushBuyOrder(stockID, lCommitAmount.intValue(), fNowPrice);
		
		// create clear property
		dTargetProfitMoney = cMonitorItem.targetProfitMoney();
		if(null == dTargetProfitMoney)
		{
			Double dTargetProfitRatio = m_defaultCfg.GlobalDefaulStockTargetProfitRatio;
			if(null != dTargetProfitRatio)
			{
				dTargetProfitMoney = lFullHoldAmount*fNowPrice*dTargetProfitRatio;
			}
		}
		dStockStopLossMoney = cMonitorItem.stopLossMoney();
		if(null == dStockStopLossMoney)
		{
			Double dStockStopLossRatio = m_defaultCfg.GlobalDefaulStockStopLossRatio;
			if(null != dStockStopLossRatio)
			{
				dStockStopLossMoney = lFullHoldAmount*fNowPrice*dStockStopLossRatio;
			}
		}
		lStockMaxHoldDays = cMonitorItem.maxHoldDays();
		if(null == lStockMaxHoldDays)
		{
			lStockMaxHoldDays = m_defaultCfg.GlobalDefaulStockMaxHoldDays;
		}
		
		cMonitorItem.setMinCommitInterval(lStockOneCommitInterval);
		cMonitorItem.setMaxHoldAmount(lFullHoldAmount);
		cMonitorItem.setOneCommitAmount(lOneCommitAmount);
		cMonitorItem.setTargetProfitMoney(dTargetProfitMoney);
		cMonitorItem.setStopLossMoney(dStockStopLossMoney);
		cMonitorItem.setMaxHoldDays(lStockMaxHoldDays);
		m_QURTMonitorTable.commit();
		
		return true;
	}
	public boolean sellSignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// check monitor table item
		MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
		if(null == cMonitorItem)
		{
			return false;
		}
				
		// interval commit check
		Long lStockOneCommitInterval = cMonitorItem.minCommitInterval();
		CommissionOrder cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.accountProxy(), stockID, TRANACT.SELL);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			long seconds = CUtilsDateTime.subTime(ctx.time(), cCommissionOrder.time);
			if(seconds < lStockOneCommitInterval*60)
			{
				//CLog.output("TEST", "sellSignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
				return false;
			}
		}

		// hold check
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		if(null == cHoldStock || cHoldStock.availableAmount < 0)
		{
			//CLog.output("TEST", "sellSignalEmit %s ignore! not have availableAmount", stockID);
			return false;
		}
		
		Long lAvailableAmount = null!=cHoldStock?cHoldStock.availableAmount:0L;
		Long lFullHoldAmount = cMonitorItem.maxHoldAmount();
		Long lOneCommitAmount = cMonitorItem.oneCommitAmount();
		
		Long lCommitAmount = Math.min(lAvailableAmount, lOneCommitAmount);
		if(lCommitAmount <= 0) // CommitAmount check
		{
			return false;
		}
		
		// post request
		ctx.accountProxy().pushSellOrder(cHoldStock.stockID, lCommitAmount.intValue(), fNowPrice);
		return true;
	}
	
	/*
	 * Auto Force Clear Process, called by user in the end of BuySellCheck
	 */
	public boolean onAutoForceClearProcess(QuantContext ctx, DAStock cDAStock)
	{
		String stockID = cDAStock.ID();
		Double fNowPrice = cDAStock.price();
	
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		if(null == cHoldStock || cHoldStock.availableAmount <= 0)
		{
			//CLog.output("TEST", "onAutoForceClearProcess %s ignore! NO availableAmount", stockID);
			return false;
		}
		
		// check monitor table item
		MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
		if(null == cMonitorItem)
		{
			return false;
		}
		
		Double stopLossMoney = cMonitorItem.stopLossMoney();
		Double stopLossPrice = cMonitorItem.stopLossPrice();
		Double targetProfitMoney = cMonitorItem.targetProfitMoney();
		Double targetProfitPrice = cMonitorItem.targetProfitPrice();
		Long maxHoldDays = cMonitorItem.maxHoldDays();
		
		boolean bCLearAll = false;
		// 止损额度
		if(!bCLearAll &&
				null != stopLossMoney && 0 != stopLossMoney &&
				(fNowPrice - cHoldStock.refPrimeCostPrice)*cHoldStock.totalAmount <= stopLossMoney) 
		{
			bCLearAll = true;
		}
		// 止损股价
		if(!bCLearAll &&
				null != stopLossPrice && 0 != stopLossPrice &&
				cDAStock.price() <= stopLossPrice) 
		{
			bCLearAll = true;
		}
		// 止盈额度
		if(!bCLearAll &&
				null != targetProfitMoney && 0 != targetProfitMoney &&
				(fNowPrice - cHoldStock.refPrimeCostPrice)*cHoldStock.totalAmount >= targetProfitMoney) 
		{
			bCLearAll = true;
		}
		// 止盈股价
		if(!bCLearAll &&
				null != targetProfitPrice && 0 != targetProfitPrice &&
				cDAStock.price() >= targetProfitPrice) 
		{
			bCLearAll = true;
		}
		
		// 持股超时
		if(null != maxHoldDays && 0 != maxHoldDays) 
		{
			long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
			if(lHoldDays >= maxHoldDays)
			{
				bCLearAll = true;
			}
		}

		if(bCLearAll)
		{
			ctx.accountProxy().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean onManulHandlerProcess(QuantContext ctx, DAStock cDAStock)
	{
		String stockID = cDAStock.ID();
		Double fNowPrice = cDAStock.price();
		
		MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
		if(null != cMonitorItem.buyTriggerPrice() && 
				fNowPrice <= cMonitorItem.buyTriggerPrice())
		{
			boolean bBeforeHigh = true;
			for(int i=0; i<cDAStock.timePrices().size()-1-1; i++)
			{
				double dprice = cDAStock.timePrices().get(i).price;
				if(dprice <= fNowPrice)
				{
					bBeforeHigh = false;
					break;
				}
			}
			
			if(bBeforeHigh)
			{
				this.buySignalEmit(ctx, stockID);
				return true;
			}
			
		}
		
		if(null != cMonitorItem.sellTriggerPrice() &&
				fNowPrice >= cMonitorItem.sellTriggerPrice())
		{
			boolean bBeforeLow = true;
			for(int i=0; i<cDAStock.timePrices().size()-1-1; i++)
			{
				double dprice = cDAStock.timePrices().get(i).price;
				if(dprice >= fNowPrice)
				{
					bBeforeLow = false;
					break;
				}
			}
			
			if(bBeforeLow)
			{
				this.sellSignalEmit(ctx, stockID);
				return true;
			}
		}
		
		return false;
	}
	
//	public void onQURTMonitorTableCB(CALLBACKTYPE cb)
//	{
//		if(CALLBACKTYPE.COMMITED == cb)
//		{
//			// add new
//			super.addCurrentDayInterestMinuteDataIDs(m_QURTMonitorTable.monitorStockIDs());
//			// remove not exist in m_QURTMonitorTable
//			List<String> monitorIDs = m_QURTMonitorTable.monitorStockIDs();
//			List<String> curInterestMinuteDataIDs = super.getCurrentDayInterestMinuteDataIDs();
//			List<String> rmIDs = new ArrayList<String>();
//			for(int i=0; i<curInterestMinuteDataIDs.size(); i++)
//			{
//				String stockID = curInterestMinuteDataIDs.get(i);
//				if(!monitorIDs.contains(stockID))
//				{
//					rmIDs.add(stockID);
//				}
//			}
//			for(int i=0; i<rmIDs.size(); i++)
//			{
//				String stockID = rmIDs.get(i);
//				super.removeCurrentDayInterestMinuteDataID(stockID);
//			}
//		}
//	}
	
	@Override
	public void onInit(QuantContext ctx) 
	{
		String derivedStrategyClsName = this.getClass().getSimpleName();
		String accountIDName = ctx.accountProxy().ID();
		String strStockStrategyHelperPath = CSystem.getRWRoot() + "\\StockStrategyHelper";
		CFileSystem.createDir(strStockStrategyHelperPath);
		
		// initialize m_QUSelector
		String selectFileName = strStockStrategyHelperPath + "\\" + derivedStrategyClsName + "_QUSelectTable.xml";
		m_QUSelectTable = new QUSelectTable(selectFileName);
		m_QUSelectTable.open();
		
		// initialize monitor table
		String rtMonitorFileName = strStockStrategyHelperPath + "\\" + derivedStrategyClsName + "_QURTMonitorTable.xml";
		m_QURTMonitorTable = new QURTMonitorTable(rtMonitorFileName);
		m_QURTMonitorTable.open();
		
//		m_QURTMonitorTableCB = new QURTMonitorTableCB(this);
//		m_QURTMonitorTable.registerCallback("QS1802BaseRTMCb", m_QURTMonitorTableCB);
		
		// initialize report module
		m_TranReportor = new TranReportor(accountIDName);
		
		// add current day monitor data ID according monitor table
		ctx.addCurrentDayInterestMinuteDataIDs(m_QURTMonitorTable.monitorStockIDs());
		this.onStrateDayStart(ctx);
				
		// callback onStrateInit
		this.onStrateInit(ctx);
		
		// start helpPanel
		if(m_defaultCfg.GlobalDefaultShowHelpPanel)
		{
			m_helpPanel = new HelpPanel();
			m_helpPanel.bindQUObject(m_QUSelectTable,m_QURTMonitorTable, ctx.accountProxy());
			m_helpPanel.start();
		}
	}
	
	@Override
	public void onUnInit(QuantContext ctx) {
	}
	
	@Override
	public void onDayStart(QuantContext ctx) 
	{
		// add current day monitor data ID according monitor table
		ctx.addCurrentDayInterestMinuteDataIDs(m_QURTMonitorTable.monitorStockIDs());
		this.onStrateDayStart(ctx);
	}
	
	@Override
	public void onMinuteData(QuantContext ctx) 
	{
		// callback onStrateMinute according monitor table
		List<String> monitorIDs = m_QURTMonitorTable.monitorStockIDs();
		for(int iStock=0; iStock<monitorIDs.size(); iStock++)
		{
			String stockID = monitorIDs.get(iStock);
			DAStock cDAStock = ctx.pool().get(stockID);
			
			// first: auto clear check
			this.onAutoForceClearProcess(ctx, cDAStock);
			
			MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
			if(null != cMonitorItem && cMonitorItem.strategy().equals("M"))
			{
				// call manual configure buy & sell check
				this.onManulHandlerProcess(ctx, cDAStock);
			}
			else
			{
				// call auto program buy & sell check
				this.onStrateMinute(ctx, cDAStock);
			}
		}
	}
	
	@Override
	public void onDayFinish(QuantContext ctx) 
	{
		// callback onStrateDayFinish
		this.onStrateDayFinish(ctx);
		
		// commit select table
		m_QUSelectTable.commit();
	
		// update monitor table
		this.autoRemoveInvalidMonitor(ctx);
		this.autoAddSelect2Monitor();
		m_QURTMonitorTable.commit();

		// report
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.accountProxy().getTotalAssets(ctnTotalAssets);
		double dSH = ctx.pool().get("999999").price();
		m_TranReportor.collectInfo_SHComposite(ctx.date(), dSH);
		m_TranReportor.collectInfo_TotalAssets(ctx.date(), ctnTotalAssets.get());
		m_TranReportor.generateReport();
		CLog.output("TEST", "onDayFinish %s", ctx.date());
		CLog.output("TEST", "dump account:\n %s", ctx.accountProxy().dump());
		CLog.output("TEST", "dump selecttable:\n %s", m_QUSelectTable.dump());
	}
	
	/*
	 * 策略初始化
	 * 程序启动时只调用一次
	 */
	protected abstract void onStrateInit(QuantContext ctx);
	/*
	 * 策略每天启动
	 * 每个交易日开始交易前调用一次
	 */
	protected abstract void onStrateDayStart(QuantContext ctx);
	/*
	 * 策略买卖检查
	 * 交易期间每分钟对每个选入|持有进行回调
	 * 用户调用tryBuy trySell 进行买卖操作
	 */
	protected abstract void onStrateMinute(QuantContext ctx, DAStock cDAStock);

	/*
	 * 策略选股
	 * 每天交易结束更新数据后进行回调
	 * 用户调用getXStockSelectManager进行选股
	 */
	protected abstract void onStrateDayFinish(QuantContext ctx);
	
	/**
	 * *********************************************************************************************
	 */
	
	// remove monitor ID item, which it is not exist in holdstock & strategy!=Manul
	private void autoRemoveInvalidMonitor(QuantContext ctx)
	{
		List<String> holdIDs = ctx.accountProxy().getHoldStockIDList();
		List<String> monitorIDs = m_QURTMonitorTable.monitorStockIDs();
		for(int i=0; i<monitorIDs.size(); i++)
		{
			String monitorID = monitorIDs.get(i);
			if(null == m_QURTMonitorTable.item(monitorID).strategy()
				|| !m_QURTMonitorTable.item(monitorID).strategy().equals("M"))
			{
				if(!holdIDs.contains(monitorID))
				{
					m_QURTMonitorTable.removeItem(monitorID);
				}
			}
		}
	}
	// add select ID to monitor
	private void autoAddSelect2Monitor()
	{
		if(m_defaultCfg.GlobalDefaultAutoMoveSelectToMonitor)
		{
			List<String> selectStockIDs = m_QUSelectTable.selectStockIDs();
			for(int i=0; i<selectStockIDs.size(); i++)
			{
				String selectID = selectStockIDs.get(i);
				if(null == m_QURTMonitorTable.item(selectID))
				{
					m_QURTMonitorTable.addItem(selectID);
					m_QURTMonitorTable.item(selectID).setStrategy("P");
				}
			}
		}
	}
	
//	public static class QURTMonitorTableCB implements QURTMonitorTable.ICallback
//	{
//		public QURTMonitorTableCB(QS1802Base cQS1802Base)
//		{
//			m_QS1802Base = cQS1802Base;
//		}
//		
//		@Override
//		public void onNotify(CALLBACKTYPE cb) {
//			if(null != m_QS1802Base)
//			{
//				m_QS1802Base.onQURTMonitorTableCB(cb);
//			}
//		}
//		private QS1802Base m_QS1802Base;
//	}
	
	private DefaultConfig m_defaultCfg;
	
	private QUSelectTable m_QUSelectTable; // 选股表
	private QURTMonitorTable m_QURTMonitorTable; // 实时监控表
//	private QURTMonitorTableCB m_QURTMonitorTableCB;
	private TranReportor m_TranReportor; // 报告模块
	
	private HelpPanel m_helpPanel;
}
