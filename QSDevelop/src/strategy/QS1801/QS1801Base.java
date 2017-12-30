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
	 * property global
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
	public void setGlobalStockMaxPosstion(double dMaxPossition) 
	{
		m_QUProperty.propertySetDouble("Global", "StockMaxPosstion", dMaxPossition);
	}
	public Double getGlobalStockMaxPosstion()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockMaxPosstion");
	}
	// 单只股票操作默相对仓位（相对最大满仓持股数量属性），用于首次建仓时生成个股单笔操作股票数量属性
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
		m_QUProperty.propertySetLong("Global", "StockOneCommitInterval", min);
	}
	public Long getGlobalStockOneCommitInterval()
	{
		return m_QUProperty.propertyGetLong("Global", "StockOneCommitInterval");
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
	
	/*
	 * ************************************************************************************
	 * property stock
	 * ************************************************************************************
	 */
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
	
	
	/*
	 * ************************************************************************************
	 * buy sell signal， amount
	 * ************************************************************************************
	 */
	private void setStockPropertyFullHoldAmount(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "FullHoldAmount", value);
	}
	private Long getStockPropertyFullHoldAmount(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "FullHoldAmount");
	}
	private void setStockPropertyOneCommitAmount(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "OneCommitAmount", value);
	}
	private Long getStockPropertyOneCommitAmount(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "OneCommitAmount");
	}
	
	/*
	 * buy & sell signal
	 */
	public boolean buySignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
		Long lStockOneCommitInterval = this.getGlobalStockOneCommitInterval();
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
			}
			Long lOneCommitAmount = this.getStockPropertyOneCommitAmount(stockID);
			if(null == lOneCommitAmount)
			{
				Double dGlobalStockOneCommitDefaultPossition = this.getGlobalStockOneCommitDefaultPossition();
				Long curFullPositionAmmount = this.getStockPropertyFullHoldAmount(stockID);
				long curStockOneCommitDefaultPossitionAmmount = (long)(curFullPositionAmmount*dGlobalStockOneCommitDefaultPossition);
				this.setStockPropertyOneCommitAmount(stockID, curStockOneCommitDefaultPossitionAmmount);
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
		Long lCommitAmount = (lFullHoldAmount-lAlreadyHoldAmount)>lOneCommitAmount?lOneCommitAmount:lFullHoldAmount-lAlreadyHoldAmount;
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
		CLog.output("TEST", "%s", m_QUSelector.dumpSelect());
		
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
		CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.ap().dump(), m_QUSelector.dumpSelect());
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
