package QuantExtend2002;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import QuantExtend1711.utils.TranDaysChecker;
import QuantExtend1801.utils.QUCommon;
import QuantExtend1801.utils.QUProperty;
import QuantExtend2002.utils.*;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;

public abstract class QEBase2002 extends QuantStrategy {
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
	 */
	abstract void onStrateMinute(QuantContext ctx, DAStock cDAStock);

	/*
	 * 策略选股
	 * 每天交易结束更新数据后进行回调
	 * 用户调用m_QEUSelector进行选股
	 */
	abstract void onStrateDayFinish(QuantContext ctx);
	
	/*
	 * *****************************************************************************************************
	 */
	
	public QEBase2002()
	{
	}
	@Override
	public void onInit(QuantContext context) {
		String accountIDName = context.accountProxy().ID();
		m_QEUSelector = new QEUSelector(accountIDName);
		m_QEUSelector.loadFromFile();
		m_QEUProperty = new QEUProperty(accountIDName);
		m_QEUProperty.loadFormFile();
		m_QEUTransactionController = new QEUTransactionController(m_QEUProperty);
		m_QEUTranReportor = new QEUTranReportor(accountIDName);
		this.onStrateInit(context);
		m_QEUSelector.saveToFile();
		m_QEUProperty.saveToFile();
		
	}
	@Override 
	public void onUnInit(QuantContext context){
		
	}
	@Override
	public void onDayStart(QuantContext context){
		m_QEUSelector.loadFromFile();
		m_QEUProperty.loadFormFile();
		// add select to interest 
		context.addCurrentDayInterestMinuteDataIDs(m_QEUSelector.list());
		// add hold stock to interest 
		context.addCurrentDayInterestMinuteDataIDs(QEUCommon.getHoldStockIDList(context.accountProxy()));
		this.onStrateDayStart(context);
	}
	@Override
	public void onMinuteData(QuantContext context){
		
		// callback to user with select&hold
		// user will raise buy|sell signal
		List<String> selectIDs = m_QEUSelector.list();
		List<String> holdIDs = QEUCommon.getHoldStockIDList(context.accountProxy());
		
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.addAll(selectIDs);
		hashSet.addAll(holdIDs);
		List<String> uniqueIDs = new ArrayList<String>();
		uniqueIDs.addAll(hashSet);
		
		for(int iStock=0; iStock<uniqueIDs.size(); iStock++)
		{
			String stockID = uniqueIDs.get(iStock);
			DAStock cDAStock = context.pool().get(stockID);
			this.onAutoForceClearProcess(context, cDAStock);
			this.onStrateMinute(context, cDAStock);
		}
	}
	@Override
	public void onDayFinish(QuantContext context){
		// select save, fetch user select stocks and save to file
		m_QEUSelector.clear();
		this.onStrateDayFinish(context);
		m_QEUSelector.saveToFile();
		
		// property save， remove it which not in select|hold and save to file
		List<String> selectIDs = m_QEUSelector.list();
		List<String> holdIDs = QUCommon.getHoldStockIDList(context.accountProxy());
		List<String> propStockIDs = m_QEUProperty.propertyList();
		for(int i=0; i<propStockIDs.size(); i++)
		{
			String propStockID = propStockIDs.get(i);
			if(propStockID.equals("Global")) continue;
			if(!selectIDs.contains(propStockID)
					&& !holdIDs.contains(propStockID))
			{
				m_QEUProperty.propertyClear(propStockID);
			}
		}
		m_QEUProperty.saveToFile();
				
		// report save to file
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		context.accountProxy().getTotalAssets(ctnTotalAssets);
		double dSH = context.pool().get("999999").price();
		m_QEUTranReportor.collectInfo_SHComposite(context.date(), dSH);
		m_QEUTranReportor.collectInfo_TotalAssets(context.date(), ctnTotalAssets.get());
		m_QEUTranReportor.generateReport();
	}
	
	/*
	 * Auto Clear Process, called by onMinuteData before user callback
	 */
	public void onAutoForceClearProcess(QuantContext ctx, DAStock cDAStock)
	{
		String stockID = cDAStock.ID();
		Double fNowPrice = cDAStock.price();
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		if(null == cHoldStock || cHoldStock.availableAmount <= 0)
		{
			//CLog.output("TEST", "onAutoForceClearProcess %s ignore! NO availableAmount", stockID);
			return;
		}
		
		Double stopLossMoney = this.property().getPrivateStockPropertyStopLossMoney(stockID);
		Double stopLossPrice = this.property().getPrivateStockPropertyStopLossPrice(stockID);
		Double targetProfitMoney = this.property().getPrivateStockPropertyTargetProfitMoney(stockID);
		Double targetProfitPrice = this.property().getPrivateStockPropertyTargetProfitPrice(stockID);
		Long maxHoldDays = this.property().getPrivateStockPropertyMaxHoldDays(stockID);
		
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
		}
	}
	
	protected QEUSelector selector() {
		return m_QEUSelector;
	}
	
	protected QEUProperty property() {
		return m_QEUProperty;
	}
	
	protected QEUTransactionController transactionController() {
		return m_QEUTransactionController;
	}
	
	private QEUSelector m_QEUSelector;
	private QEUProperty m_QEUProperty;
	private QEUTransactionController m_QEUTransactionController;
	private QEUTranReportor m_QEUTranReportor;
};
