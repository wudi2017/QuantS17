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
			GlobalDefaultMinCommitInterval = cfg.GlobalDefaultMinCommitInterval;
			GlobalDefaulMaxHoldStockCount = cfg.GlobalDefaulMaxHoldStockCount;
			GlobalDefaulStockMaxHoldPosstion = cfg.GlobalDefaulStockMaxHoldPosstion;
			GlobalDefaulStockOneCommitPossition = cfg.GlobalDefaulStockOneCommitPossition;
			GlobalDefaulStockMaxHoldDays = cfg.GlobalDefaulStockMaxHoldDays;
			GlobalDefaulStockTargetProfitRatio = cfg.GlobalDefaulStockTargetProfitRatio;
			GlobalDefaulStockStopLossRatio = cfg.GlobalDefaulStockStopLossRatio;
		}
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
		
	}
	public void setDefaultConfig(DefaultConfig defaultCfg)
	{
		m_defaultCfg.copyFrom(defaultCfg);
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
		
		// interval commit check
		Long lStockOneCommitInterval = cMonitorItem.minCommitInterval();
		if(null == lStockOneCommitInterval)
		{
			Long lMinCommitInterval =  m_defaultCfg.GlobalDefaultMinCommitInterval;
			if(null != lMinCommitInterval)
			{
				cMonitorItem.setMinCommitInterval(lMinCommitInterval);
				lStockOneCommitInterval = lMinCommitInterval;
			}
		}
		CommissionOrder cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.ap(), stockID, TRANACT.BUY);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			long seconds = CUtilsDateTime.subTime(ctx.time(), cCommissionOrder.time);
			if(seconds < lStockOneCommitInterval*60)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
				return false;
			}
		}

		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.ap().getTotalAssets(ctnTotalAssets);
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		ctx.ap().getMoney(ctnMoney);
		
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.ap(), stockID);
		
		if(null == cHoldStock) // first create
		{
			// max hold count check
			Long lMaxHoldStockCount = m_defaultCfg.GlobalDefaulMaxHoldStockCount;
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.ap().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() > lMaxHoldStockCount)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
				return false;
			}
			
			// define stock FullHoldAmount OneCommitAmount property
			Long lFullHoldAmount = cMonitorItem.maxHoldAmount();
			if(null == lFullHoldAmount)
			{
				Double dGlobalStockMaxPosstion = m_defaultCfg.GlobalDefaulStockMaxHoldPosstion;
				double curFullPositionMoney = ctnTotalAssets.get()*dGlobalStockMaxPosstion;
				long curFullPositionAmmount = (long)(curFullPositionMoney/fNowPrice);
				cMonitorItem.setMaxHoldAmount(curFullPositionAmmount);
				lFullHoldAmount = curFullPositionAmmount;
			}
			Long lOneCommitAmount = cMonitorItem.oneCommitAmount();
			if(null == lOneCommitAmount)
			{
				Double dGlobalStockOneCommitPossition = m_defaultCfg.GlobalDefaulStockOneCommitPossition;
				Long curFullPositionAmmount = cMonitorItem.maxHoldAmount();
				long curStockOneCommitPossitionAmmount = (long)(curFullPositionAmmount*dGlobalStockOneCommitPossition);
				cMonitorItem.setOneCommitAmount(curStockOneCommitPossitionAmmount);
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
					cMonitorItem.setOneCommitAmount(newlOneCommitAmount);
				}
				if(0 != lFullHoldAmount%newlOneCommitAmount)
				{
					lFullHoldAmount = (lFullHoldAmount/newlOneCommitAmount)*newlOneCommitAmount;
					cMonitorItem.setMaxHoldAmount(newlOneCommitAmount);
				}
			}
			else
			{
				cMonitorItem.setOneCommitAmount(0L);
				cMonitorItem.setMaxHoldAmount(0L);
			}
		}
		
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lFullHoldAmount = cMonitorItem.maxHoldAmount();
		Long lOneCommitAmount = cMonitorItem.oneCommitAmount();
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
		ctx.ap().pushBuyOrder(stockID, lCommitAmount.intValue(), fNowPrice);
		
		// create clear property
		if(null == cMonitorItem.maxHoldDays())
		{
			Long lStockMaxHoldDays = m_defaultCfg.GlobalDefaulStockMaxHoldDays;
			if(null != lStockMaxHoldDays)
			{
				cMonitorItem.setMaxHoldDays(lStockMaxHoldDays);
			}
		}
		if(null == cMonitorItem.targetProfitMoney())
		{
			Double dTargetProfitRatio = m_defaultCfg.GlobalDefaulStockTargetProfitRatio;
			if(null != dTargetProfitRatio)
			{
				Double dTargetProfitMoney = lFullHoldAmount*fNowPrice*dTargetProfitRatio;
				cMonitorItem.setTargetProfitMoney(dTargetProfitMoney);
			}
		}
		if(null == cMonitorItem.stopLossMoney())
		{
			Double dStockStopLossRatio = m_defaultCfg.GlobalDefaulStockStopLossRatio;
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lFullHoldAmount*fNowPrice*dStockStopLossRatio;
				cMonitorItem.setStopLossMoney(dStockStopLossMoney);
			}
		}
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
		CommissionOrder cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.ap(), stockID, TRANACT.SELL);
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
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.ap(), stockID);
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
		ctx.ap().pushSellOrder(cHoldStock.stockID, lCommitAmount.intValue(), fNowPrice);
		return true;
	}
	
	/*
	 * Auto Force Clear Process, called by user in the end of BuySellCheck
	 */
	public boolean onAutoForceClearProcess(QuantContext ctx, DAStock cDAStock)
	{
		String stockID = cDAStock.ID();
		Double fNowPrice = cDAStock.price();
		
		// check monitor table item
		MonitorItem cMonitorItem = m_QURTMonitorTable.item(stockID);
		if(null == cMonitorItem)
		{
			return false;
		}
				
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.ap(), stockID);
		if(null == cHoldStock || cHoldStock.availableAmount <= 0)
		{
			//CLog.output("TEST", "onAutoForceClearProcess %s ignore! NO availableAmount", stockID);
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
			ctx.ap().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	@Override
	public void onInit(QuantContext ctx) 
	{
		String derivedStrategyClsName = this.getClass().getSimpleName();
		String accountIDName = ctx.ap().ID();
		String strStockStrategyHelperPath = CSystem.getRWRoot() + "\\StockStrategyHelper";
		CFileSystem.createDir(strStockStrategyHelperPath);
		
		// initialize m_defaultCfg
		m_defaultCfg = new DefaultConfig();
		
		// initialize m_QUSelector
		String selectFileName = strStockStrategyHelperPath + "\\" + derivedStrategyClsName + "_QUSelector.xml";
		m_QUSelectTable = new QUSelectTable(selectFileName);
		
		// initialize monitor table
		String rtMonitorFileName = strStockStrategyHelperPath + "\\" + derivedStrategyClsName + "_QURTMonitorTable.xml";
		m_QURTMonitorTable = new QURTMonitorTable(rtMonitorFileName);
		m_QURTMonitorTable.open();
		
		// initialize report module
		m_TranReportor = new TranReportor(accountIDName);
		
		// callback onStrateInit
		this.onStrateInit(ctx);
	}
	
	@Override
	public void onUnInit(QuantContext ctx) {
	}
	
	@Override
	public void onDayStart(QuantContext ctx) 
	{
		// add current day monitor data ID according monitor table
		super.addCurrentDayInterestMinuteDataIDs(m_QURTMonitorTable.monitorStockIDs());
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
			
			this.onAutoForceClearProcess(ctx, cDAStock);
			this.onStrateMinute(ctx, cDAStock);
		}
	}
	
	@Override
	public void onDayFinish(QuantContext ctx) 
	{
		// callback onStrateDayFinish
		this.onStrateDayFinish(ctx);
		
		// commit select table
		m_QUSelectTable.commit();
		
		// commit monitor table
		m_QURTMonitorTable.commit();

		// report
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.ap().getTotalAssets(ctnTotalAssets);
		double dSH = ctx.pool().get("999999").price();
		m_TranReportor.collectInfo_SHComposite(ctx.date(), dSH);
		m_TranReportor.collectInfo_TotalAssets(ctx.date(), ctnTotalAssets.get());
		m_TranReportor.generateReport();
		CLog.output("TEST", "onDayFinish %s dump account&select\n %s\n", ctx.date(), ctx.ap().dump());
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
	private DefaultConfig m_defaultCfg;
	
	private QUSelectTable m_QUSelectTable; // 选股表
	private QURTMonitorTable m_QURTMonitorTable; // 实时监控表
	private TranReportor m_TranReportor; // 报告模块
}
