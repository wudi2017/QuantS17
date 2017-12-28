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
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;
import utils.QS1711.TranReportor;
import utils.QS1801.QUCommon;
import utils.QS1801.QUProperty;
import utils.QS1801.QUSelector;

public abstract class QS1801Base extends QuantStrategy {
	
	public QS1801Base(int iMaxHoldCount)
	{
		m_iMaxHoldCount = iMaxHoldCount;
	}
	
	/*
	 * select stock utils
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
	 * property stock utils
	 */
	public void propertySetString(String stockID, String property, String value)
	{
		m_QUProperty.propertySet(stockID, property, value);
	}
	public String propertyGetString(String stockID, String property)
	{
		return m_QUProperty.propertyGet(stockID, property);
	}
	public void propertySetDouble(String stockID, String property, Double value)
	{
		m_QUProperty.propertySet(stockID, property, String.format("%.3f", value));
	}
	public Double propertyGetDouble(String stockID, String property)
	{
		Double value = null;
		String strVal = m_QUProperty.propertyGet(stockID, property);
		if(null != strVal)
		{
			value = Double.parseDouble(strVal);
		}
		return value;
	}
	public boolean propertyContains(String stockID)
	{
		return m_QUProperty.propertyContains(stockID);
	}
	public void propertyClear(String stockID)
	{
		m_QUProperty.propertyClear(stockID);
	}
	
	/*
	 * buy & sell signal
	 */
	public boolean signalBuy(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// multi commit check
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ctx.ap().getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			CommissionOrder cCommissionOrder = ctnCommissionOrderList.get(i);
			if(cCommissionOrder.stockID.equals(stockID) 
					&& TRANACT.BUY == cCommissionOrder.tranAct
					&& Math.abs((fNowPrice-cCommissionOrder.price)/cCommissionOrder.price) < 0.03)
			{
				// already commit similar today
				return false;
			}
		}

		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		if(ctnHoldStockList.size() < m_iMaxHoldCount)
		{
			CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
			ctx.ap().getTotalAssets(ctnTotalAssets);
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			ctx.ap().getMoney(ctnMoney);
			double dCreateMoney = (ctnMoney.get() > ctnTotalAssets.get()/m_iMaxHoldCount)?ctnTotalAssets.get()/m_iMaxHoldCount:ctnMoney.get();
			int iCreateAmount = (int) (dCreateMoney/fNowPrice)/100*100;
			if(iCreateAmount > 0)
			{
				ctx.ap().pushBuyOrder(stockID, iCreateAmount, fNowPrice);
				return true;
			}
		}
		
		return false;
	}

	public boolean signalSell(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// multi commit check
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ctx.ap().getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			CommissionOrder cCommissionOrder = ctnCommissionOrderList.get(i);
			if(cCommissionOrder.stockID.equals(stockID) 
					&& TRANACT.SELL == cCommissionOrder.tranAct
					&& Math.abs((fNowPrice-cCommissionOrder.price)/cCommissionOrder.price) < 0.03)
			{
				// already commit similar today
				return false;
			}
		}

		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			HoldStock cHoldStock = ctnHoldStockList.get(i);
			if(cHoldStock.stockID.equals(stockID))
			{
				long trySellAmount = (long)(cHoldStock.totalAmount);
				long realSellAmuont = trySellAmount>cHoldStock.availableAmount?cHoldStock.availableAmount:trySellAmount;
				if(realSellAmuont <= 0)
				{
					return false;
				}
				
				ctx.ap().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onInit(QuantContext ctx) {
		// String derivedClsName = this.getClass().getSimpleName();
		String accountIDName = ctx.ap().ID();
		m_QUSelector = new QUSelector(accountIDName);
		m_QUProperty = new QUProperty(accountIDName);
		m_TranReportor = new TranReportor(accountIDName);
		this.onStrateInit(ctx);
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

	private int m_iMaxHoldCount;
	private QUSelector m_QUSelector;
	private QUProperty m_QUProperty;
	private TranReportor m_TranReportor;
}
