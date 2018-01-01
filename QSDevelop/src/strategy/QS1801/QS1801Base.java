package strategy.QS1801;

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
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.AccountProxy;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;
import utils.QS1711.TranDaysChecker;
import utils.QS1711.TranReportor;
import utils.QS1801.QUCommon;
import utils.QS1801.QUProperty;
import utils.QS1801.QUSelector;

public abstract class QS1801Base extends QuantStrategy {
	
	public QS1801Base()
	{
	}
	
	/*
	 * ************************************************************************************
	 * select stock utils
	 * ************************************************************************************
	 */
	public void selectAdd(String stockID, double priority)
	{
		m_QUSelector.selectAdd(stockID, priority);
	}
	public void selectRemove(List<String> stockIDs)
	{
		m_QUSelector.selectRemove(stockIDs);
	}
	public void selectKeepMaxCount(int maxCount)
	{
		m_QUSelector.selectKeepMaxCount(maxCount);
	}
	public List<String> selectList()
	{
		return m_QUSelector.selectList();
	}
	public int selectSize()
	{
		return m_QUSelector.selectSize();
	}
	public void selectClear()
	{
		m_QUSelector.selectClear();
	}
	public String selectDump()
	{
		return m_QUSelector.dumpSelect();
	}
	
	/*
	 * ************************************************************************************
	 * global property 
	 * 
	 * MaxHoldStockCount 最大持股数量
	 * StockMaxPosstion 单只股票最大持仓位
	 * StockOneCommitDefaultPossition 单只股票单次提交相对最大持仓位比例
	 * StockOneCommitInterval 提交频率控制
	 * MaxHoldDays 最大持有天数
	 * TargetProfitRatio 目标止盈比（相对最大持仓位）
	 * StopLossRatio 停止亏损比（相对最大持仓位）
	 * 
	 * ************************************************************************************
	 */
	// 最大持股数量
	public void setGlobalMaxHoldStockCount(long count) 
	{
		m_QUProperty.propertySetLong("Global", "MaxHoldStockCount", count);
	}
	public Long getGlobalMaxHoldStockCount()
	{
		return m_QUProperty.propertyGetLong("Global", "MaxHoldStockCount");
	}
	// 单只股票最大仓位，用于首次建仓时生成个股最大满仓持股数量属性，此属性用户控制个股下单最大上限
	// 目标个性：FullHoldAmount
	public void setGlobalStockMaxPosstion(double dMaxPossition) 
	{
		m_QUProperty.propertySetDouble("Global", "StockMaxPosstion", dMaxPossition);
	}
	public Double getGlobalStockMaxPosstion()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockMaxPosstion");
	}
	// 单只股票操作默相对仓位（相对最大满仓持股数量属性），用于首次建仓时生成个股单笔操作股票数量属性
	// 目标个性：OneCommitAmount
	public void setGlobalStockOneCommitDefaultPossition(double dDefaultCommit)
	{
		m_QUProperty.propertySetDouble("Global", "StockOneCommitDefaultPossition", dDefaultCommit);
	}
	public Double getGlobalStockOneCommitDefaultPossition()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockOneCommitDefaultPossition");
	}
	// 股票提交最小时间间隔，用于限制提交频率
	public void setGlobalStockOneCommitInterval(long min)
	{
		m_QUProperty.propertySetLong("Global", "CommitInterval", min);
	}
	public Long getGlobalStockOneCommitInterval()
	{
		return m_QUProperty.propertyGetLong("Global", "CommitInterval");
	}
	// 设置全局属性：股票最大持有天数
	// 目标个性：MaxHoldDays
	public void setGlobalStockMaxHoldDays(long value)
	{
		m_QUProperty.propertySetLong("Global", "MaxHoldDays", value);
	}
	public Long getGlobalStockMaxHoldDays()
	{
		return m_QUProperty.propertyGetLong("Global", "MaxHoldDays");
	}
	// 设置全局属性：目标止盈比例（相对FullHoldAmount的）
	// 目标个性：TargetProfitMoney
	public void setGlobalStockTargetProfitRatio(Double value)
	{
		m_QUProperty.propertySetDouble("Global", "TargetProfitRatio", value);
	}
	public Double getGlobalStockTargetProfitRatio()
	{
		return m_QUProperty.propertyGetDouble("Global", "TargetProfitRatio");
	}
	// 设置全局属性：目标止损比例（相对FullHoldAmount的）
	// 目标个性：StopLossMoney
	public void setGlobalStockStopLossRatio(Double value)
	{
		m_QUProperty.propertySetDouble("Global", "StopLossRatio", value);
	}
	public Double getGlobalStockStopLossRatio()
	{
		return m_QUProperty.propertyGetDouble("Global", "StopLossRatio");
	}
		

	
	/*
	 * ************************************************************************************
	 * stock property
	 * 
	 * User...: 用户部分
	 * FullHoldAmount: 全仓最大持有量
	 * OneCommitAmount: 单次提交量
	 * MaxHoldDays: 最大持有天数
	 * TargetProfitMoney: 目标盈利额
	 * TargetProfitPrice: 目标盈利价
	 * StopLossMoney: 止损额
	 * StopLossPrice: 止损价
	 * 
	 * ************************************************************************************
	 */
	// 用户自定义部分
	public void setStockPropertyString(String stockID, String property, String value)
	{
		m_QUProperty.propertySetString(stockID, "USER_"+property, value);
	}
	public String getStockPropertyString(String stockID, String property)
	{
		return m_QUProperty.propertyGetString(stockID, "USER_"+property);
	}
	public void setStockPropertyDouble(String stockID, String property, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "USER_"+property, value);
	}
	public Double getStockPropertyDouble(String stockID, String property)
	{
		return m_QUProperty.propertyGetDouble(stockID, "USER_"+property);
	}
	public boolean stockPropertContains(String stockID)
	{
		return m_QUProperty.propertyContains(stockID);
	}
	public void stockPropertClear(String stockID)
	{
		m_QUProperty.propertyClear(stockID);
	}
	// 股票全仓位时候的持股数量
	public void setStockPropertyFullHoldAmount(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "FullHoldAmount", value);
	}
	public Long getStockPropertyFullHoldAmount(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "FullHoldAmount");
	}
	// 股票一次提交的数量
	public void setStockPropertyOneCommitAmount(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "OneCommitAmount", value);
	}
	public Long getStockPropertyOneCommitAmount(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "OneCommitAmount");
	}
	// 提交频率
	public void setStockPropertyMinCommitInterval(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "MinCommitInterval", value);
	}
	public Long getStockPropertyMinCommitInterval(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "MinCommitInterval");
	}
	// 清仓条件：最大持有时间
	public void setStockPropertyMaxHoldDays(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "MaxHoldDays", value);
	}
	public Long getStockPropertyMaxHoldDays(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "MaxHoldDays");
	}
	// 清仓条件：目标止盈，盈利额
	public void setStockPropertyTargetProfitMoney(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "TargetProfitMoney", value);
	}
	public Double getStockPropertyTargetProfitMoney(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "TargetProfitMoney");
	}
	// 清仓条件：目标止盈，止盈价
	public void setStockPropertyTargetProfitPrice(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "TargetProfitPrice", value);
	}
	public Double getStockPropertyTargetProfitPrice(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "TargetProfitPrice");
	}
	// 清仓条件：目标止损，亏损额
	public void setStockPropertyStopLossMoney(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "StopLossMoney", value);
	}
	public Double getStockPropertyStopLossMoney(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "StopLossMoney");
	}
	// 清仓条件：目标止损，止损价
	public void setStockPropertyStopLossPrice(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "StopLossPrice", value);
	}
	public Double getStockPropertyStopLossPrice(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "StopLossPrice");
	}
	
	/*
	 * ************************************************************************************
	 * buy sell signal， amount
	 * ************************************************************************************
	 */
	public boolean buySignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
		Long lStockOneCommitInterval = this.getStockPropertyMinCommitInterval(stockID);
		if(null == lStockOneCommitInterval)
		{
			Long lMinCommitInterval =  this.getGlobalStockOneCommitInterval();
			if(null != lMinCommitInterval)
			{
				this.setStockPropertyMinCommitInterval(stockID, lMinCommitInterval);
				lStockOneCommitInterval = lMinCommitInterval;
			}
		}
		CommissionOrder cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.ap(), stockID, TRANACT.BUY);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			long seconds = CUtilsDateTime.subTime(ctx.time(), cCommissionOrder.time);
			if(seconds < lStockOneCommitInterval*60)
			{
				CLog.output("TEST", "buySignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
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
			Long lMaxHoldStockCount = this.getGlobalMaxHoldStockCount();
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.ap().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() > lMaxHoldStockCount)
			{
				CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
				return false;
			}
			
			// define stock FullHoldAmount OneCommitAmount property
			Long lFullHoldAmount = this.getStockPropertyFullHoldAmount(stockID);
			if(null == lFullHoldAmount)
			{
				Double dGlobalStockMaxPosstion = this.getGlobalStockMaxPosstion();
				double curFullPositionMoney = ctnTotalAssets.get()*dGlobalStockMaxPosstion;
				long curFullPositionAmmount = (long)(curFullPositionMoney/fNowPrice);
				this.setStockPropertyFullHoldAmount(stockID, curFullPositionAmmount);
				lFullHoldAmount = curFullPositionAmmount;
			}
			Long lOneCommitAmount = this.getStockPropertyOneCommitAmount(stockID);
			if(null == lOneCommitAmount)
			{
				Double dGlobalStockOneCommitDefaultPossition = this.getGlobalStockOneCommitDefaultPossition();
				Long curFullPositionAmmount = this.getStockPropertyFullHoldAmount(stockID);
				long curStockOneCommitDefaultPossitionAmmount = (long)(curFullPositionAmmount*dGlobalStockOneCommitDefaultPossition);
				this.setStockPropertyOneCommitAmount(stockID, curStockOneCommitDefaultPossitionAmmount);
				lOneCommitAmount = curStockOneCommitDefaultPossitionAmmount;
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
					this.setStockPropertyOneCommitAmount(stockID, newlOneCommitAmount);
				}
				if(0 != lFullHoldAmount%newlOneCommitAmount)
				{
					lFullHoldAmount = (lFullHoldAmount/newlOneCommitAmount)*newlOneCommitAmount;
					this.setStockPropertyFullHoldAmount(stockID, lFullHoldAmount);
				}
			}
			else
			{
				this.setStockPropertyOneCommitAmount(stockID, 0);
				this.setStockPropertyFullHoldAmount(stockID, 0);
			}
		}
		
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lFullHoldAmount = this.getStockPropertyFullHoldAmount(stockID);
		Long lOneCommitAmount = this.getStockPropertyOneCommitAmount(stockID);
		if(lAlreadyHoldAmount >= lFullHoldAmount) // FullHoldAmount AlreadyHoldAmount check
		{
			CLog.output("TEST", "buySignalEmit %s ignore! lAlreadyHoldAmount=%d lFullHoldAmount=%d", 
					stockID, lAlreadyHoldAmount, lFullHoldAmount);
			return false;
		}
		Long lCommitAmount = Math.min(lFullHoldAmount-lAlreadyHoldAmount, lOneCommitAmount);
		lCommitAmount = lCommitAmount/100*100;
		if(lCommitAmount < 100) // CommitAmount check
		{
			CLog.output("TEST", "buySignalEmit %s ignore! iCreateAmount=%d", stockID, lCommitAmount);
			return false;
		}
		double needCommitMoney = lCommitAmount*fNowPrice;
		if(needCommitMoney > ctnMoney.get()) // CommitMoney check
		{
			CLog.output("TEST", "buySignalEmit %s ignore! needCommitMoney=%.3f ctnMoney=%.3f", stockID, needCommitMoney,ctnMoney.get());
			return false;
		}
		
		// post request
		ctx.ap().pushBuyOrder(stockID, lCommitAmount.intValue(), fNowPrice);
		
		// create clear property
		if(null == this.getStockPropertyMaxHoldDays(stockID))
		{
			Long lStockMaxHoldDays = this.getGlobalStockMaxHoldDays();
			if(null != lStockMaxHoldDays)
			{
				this.setStockPropertyMaxHoldDays(stockID, lStockMaxHoldDays);
			}
		}
		if(null == this.getStockPropertyTargetProfitMoney(stockID))
		{
			Double dTargetProfitRatio = this.getGlobalStockTargetProfitRatio();
			if(null != dTargetProfitRatio)
			{
				Double dTargetProfitMoney = lFullHoldAmount*fNowPrice*dTargetProfitRatio;
				this.setStockPropertyTargetProfitMoney(stockID, dTargetProfitMoney);
			}
		}
		if(null == this.getStockPropertyStopLossMoney(stockID))
		{
			Double dStockStopLossRatio = this.getGlobalStockStopLossRatio();
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lFullHoldAmount*fNowPrice*dStockStopLossRatio;
				this.setStockPropertyStopLossMoney(stockID, dStockStopLossMoney);
			}
		}
		return true;
	}
	public boolean sellSignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
		Long lStockOneCommitInterval = this.getGlobalStockOneCommitInterval();
		CommissionOrder cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.ap(), stockID, TRANACT.SELL);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			long seconds = CUtilsDateTime.subTime(ctx.time(), cCommissionOrder.time);
			if(seconds < lStockOneCommitInterval*60)
			{
				CLog.output("TEST", "sellSignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
				return false;
			}
		}

		// hold check
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.ap(), stockID);
		if(null == cHoldStock || cHoldStock.availableAmount < 0)
		{
			CLog.output("TEST", "sellSignalEmit %s ignore! not have availableAmount", stockID);
			return false;
		}
		
		Long lAvailableAmount = null!=cHoldStock?cHoldStock.availableAmount:0L;
		Long lFullHoldAmount = this.getStockPropertyFullHoldAmount(stockID);
		Long lOneCommitAmount = this.getStockPropertyOneCommitAmount(stockID);
		
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
	 * ************************************************************************************
	 * Auto Force Clear Process, called by user in the end of BuySellCheck
	 * ************************************************************************************
	 */
	public void onAutoForceClearProcess(QuantContext ctx, DAStock cDAStock)
	{
		String stockID = cDAStock.ID();
		Double fNowPrice = cDAStock.price();
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.ap(), stockID);
		if(null == cHoldStock || cHoldStock.availableAmount <= 0)
		{
			CLog.output("TEST", "onAutoForceClearProcess %s ignore! availableAmount=%d", stockID, cHoldStock.availableAmount);
			return;
		}
		
		Double stopLossMoney = this.getStockPropertyStopLossMoney(stockID);
		Double stopLossPrice = this.getStockPropertyStopLossPrice(stockID);
		Double targetProfitMoney = this.getStockPropertyTargetProfitMoney(stockID);
		Double targetProfitPrice = this.getStockPropertyTargetProfitPrice(stockID);
		Long maxHoldDays = this.getStockPropertyMaxHoldDays(stockID);
		
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
		}
	}
	
	
	@Override
	public void onInit(QuantContext ctx) {
		
		// String derivedClsName = this.getClass().getSimpleName();
		String accountIDName = ctx.ap().ID();
		
		m_QUSelector = new QUSelector(accountIDName);
		m_QUSelector.loadFromFile();
		
		m_QUProperty = new QUProperty(accountIDName);
		m_QUProperty.loadFormFile();
		
		m_TranReportor = new TranReportor(accountIDName);
		
		this.onStrateInit(ctx);
		
		m_QUSelector.saveToFile();
		m_QUProperty.saveToFile();
	}
	@Override
	public void onDayStart(QuantContext ctx) {
		CLog.output("TEST", "onDayStart %s", ctx.date());
		// init select stock
		m_QUSelector.loadFromFile();
		// init property
		m_QUProperty.loadFormFile();

		super.addCurrentDayInterestMinuteDataIDs(m_QUSelector.selectList());
		//CLog.output("TEST", "onDayStart %s", m_QUSelector.dumpSelect());
		
		this.onStrateDayStart(ctx);
	}
	
	@Override
	public void onMinuteData(QuantContext ctx) {
		
		// callback to user with select&hold
		// user will raise buy|sell signal
		
		List<String> selectIDs = m_QUSelector.selectList();
		List<String> holdIDs = QUCommon.getHoldStockIDList(ctx.ap());
		
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.addAll(selectIDs);
		hashSet.addAll(holdIDs);
		List<String> uniqueIDs = new ArrayList<String>();
		uniqueIDs.addAll(hashSet);
		
		for(int iStock=0; iStock<uniqueIDs.size(); iStock++)
		{
			String stockID = uniqueIDs.get(iStock);
			DAStock cDAStock = ctx.pool().get(stockID);
			this.onStrateBuySellCheck(ctx, cDAStock);
		}
	}
	
	@Override
	public void onDayFinish(QuantContext ctx) {
		
		// fetch user select stocks
		m_QUSelector.selectClear();
		this.onStrateDayFinish(ctx);
		m_QUSelector.saveToFile();
		
		// property reset， remove it which not in select|hold
		List<String> selectIDs = m_QUSelector.selectList();
		List<String> holdIDs = QUCommon.getHoldStockIDList(ctx.ap());
		List<String> propStockIDs = m_QUProperty.propertyList();
		for(int i=0; i<propStockIDs.size(); i++)
		{
			String propStockID = propStockIDs.get(i);
			if(propStockID.equals("Global")) continue;
			if(!selectIDs.contains(propStockID)
					&& !holdIDs.contains(propStockID))
			{
				m_QUProperty.propertyClear(propStockID);
			}
		}
		m_QUProperty.saveToFile();
		
		// report
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.ap().getTotalAssets(ctnTotalAssets);
		double dSH = ctx.pool().get("999999").price();
		m_TranReportor.collectInfo_SHComposite(ctx.date(), dSH);
		m_TranReportor.collectInfo_TotalAssets(ctx.date(), ctnTotalAssets.get());
		m_TranReportor.generateReport();
		CLog.output("TEST", "onDayFinish %s dump account&select\n %s\n    -%s", ctx.date(), ctx.ap().dump(), m_QUSelector.dumpSelect());
	}
	
	/*
	 * 策略初始化
	 * 程序启动时只调用一次
	 */
	abstract void onStrateInit(QuantContext ctx);
	/*
	 * 策略每天启动
	 * 每个交易日开始交易前调用一次
	 */
	abstract void onStrateDayStart(QuantContext ctx);
	/*
	 * 策略买卖检查
	 * 交易期间每分钟对每个选入|持有进行回调
	 * 用户调用tryBuy trySell 进行买卖操作
	 */
	abstract void onStrateBuySellCheck(QuantContext ctx, DAStock cDAStock);

	/*
	 * 策略选股
	 * 每天交易结束更新数据后进行回调
	 * 用户调用getXStockSelectManager进行选股
	 */
	abstract void onStrateDayFinish(QuantContext ctx);

	private Long m_lMaxHoldCount;
	private QUSelector m_QUSelector;
	private QUProperty m_QUProperty;
	private TranReportor m_TranReportor;
}
