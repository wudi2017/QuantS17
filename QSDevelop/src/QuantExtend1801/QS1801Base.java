package QuantExtend1801;

import java.util.*;

import QuantExtend1711.utils.TranDaysChecker;
import QuantExtend1711.utils.TranReportor;
import QuantExtend1801.utils.QUCommon;
import QuantExtend1801.utils.QUProperty;
import QuantExtend1801.utils.QUSelector;
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

public abstract class QS1801Base extends QuantStrategy implements CConsole.IHandler {
	
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
	 * MaxHoldStockCount ���ֹ�����
	 * StockMaxPosstion ��ֻ��Ʊ���ֲ�λ
	 * StockOneCommitPossition ��ֻ��Ʊ�����ύ������ֲ�λ����
	 * StockOneCommitInterval �ύƵ�ʿ���
	 * MaxHoldDays ����������
	 * TargetProfitRatio Ŀ��ֹӯ�ȣ�������ֲ�λ��
	 * StopLossRatio ֹͣ����ȣ�������ֲ�λ��
	 * 
	 * ************************************************************************************
	 */
	// ���ֹ�����
	public void setGlobalMaxHoldStockCount(long count) 
	{
		m_QUProperty.propertySetLong("Global", "MaxHoldStockCount", count);
	}
	public Long getGlobalMaxHoldStockCount()
	{
		return m_QUProperty.propertyGetLong("Global", "MaxHoldStockCount");
	}
	// ��ֻ��Ʊ����λ�������״ν���ʱ���ɸ���������ֳֹ��������ԣ��������û����Ƹ����µ��������
	// Ŀ����ԣ�FullHoldAmount
	public void setGlobalStockMaxHoldPosstion(double dMaxPossition) 
	{
		m_QUProperty.propertySetDouble("Global", "StockMaxHoldPosstion", dMaxPossition);
	}
	public Double getGlobalStockMaxHoldPosstion()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockMaxHoldPosstion");
	}
	// ��ֻ��Ʊ����Ĭ��Բ�λ�����������ֳֹ��������ԣ��������״ν���ʱ���ɸ��ɵ��ʲ�����Ʊ��������
	// Ŀ����ԣ�OneCommitAmount
	public void setGlobalStockOneCommitPossition(double dDefaultCommit)
	{
		m_QUProperty.propertySetDouble("Global", "StockOneCommitPossition", dDefaultCommit);
	}
	public Double getGlobalStockOneCommitPossition()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockOneCommitPossition");
	}
	// ��Ʊ�ύ��Сʱ���������������ύƵ��
	public void setGlobalStockMinCommitInterval(long min)
	{
		m_QUProperty.propertySetLong("Global", "StockMinCommitInterval", min);
	}
	public Long getGlobalStockMinCommitInterval()
	{
		return m_QUProperty.propertyGetLong("Global", "StockMinCommitInterval");
	}
	// ����ȫ�����ԣ���Ʊ����������
	// Ŀ����ԣ�MaxHoldDays
	public void setGlobalStockMaxHoldDays(long value)
	{
		m_QUProperty.propertySetLong("Global", "StockMaxHoldDays", value);
	}
	public Long getGlobalStockMaxHoldDays()
	{
		return m_QUProperty.propertyGetLong("Global", "StockMaxHoldDays");
	}
	// ����ȫ�����ԣ�Ŀ��ֹӯ���������FullHoldAmount�ģ�
	// Ŀ����ԣ�TargetProfitMoney
	public void setGlobalStockTargetProfitRatio(Double value)
	{
		m_QUProperty.propertySetDouble("Global", "StockTargetProfitRatio", value);
	}
	public Double getGlobalStockTargetProfitRatio()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockTargetProfitRatio");
	}
	// ����ȫ�����ԣ�Ŀ��ֹ����������FullHoldAmount�ģ�
	// Ŀ����ԣ�StopLossMoney
	public void setGlobalStockStopLossRatio(Double value)
	{
		m_QUProperty.propertySetDouble("Global", "StockStopLossRatio", value);
	}
	public Double getGlobalStockStopLossRatio()
	{
		return m_QUProperty.propertyGetDouble("Global", "StockStopLossRatio");
	}
		

	
	/*
	 * ************************************************************************************
	 * stock property
	 * 
	 * User...: �û�����
	 * FullHoldAmount: ȫ����������
	 * OneCommitAmount: �����ύ��
	 * MaxHoldDays: ����������
	 * TargetProfitMoney: Ŀ��ӯ����
	 * TargetProfitPrice: Ŀ��ӯ����
	 * StopLossMoney: ֹ���
	 * StopLossPrice: ֹ���
	 * 
	 * ************************************************************************************
	 */
	// �û��Զ��岿��
	public void setPrivateStockPropertyString(String stockID, String property, String value)
	{
		m_QUProperty.propertySetString(stockID, "USER_"+property, value);
	}
	public String getPrivateStockPropertyString(String stockID, String property)
	{
		return m_QUProperty.propertyGetString(stockID, "USER_"+property);
	}
	public void setPrivateStockPropertyDouble(String stockID, String property, double value)
	{
		m_QUProperty.propertySetDouble(stockID, "USER_"+property, value);
	}
	public Double getPrivateStockPropertyDouble(String stockID, String property)
	{
		return m_QUProperty.propertyGetDouble(stockID, "USER_"+property);
	}
	public void setPrivateStockPropertyLong(String stockID, String property, long value)
	{
		m_QUProperty.propertySetLong(stockID, "USER_"+property, value);
	}
	public Long getPrivateStockPropertyLong(String stockID, String property)
	{
		return m_QUProperty.propertyGetLong(stockID, "USER_"+property);
	}
	public boolean stockPrivatePropertContains(String stockID)
	{
		return m_QUProperty.propertyContains(stockID);
	}
	public void stockPrivatePropertClear(String stockID)
	{
		m_QUProperty.propertyClear(stockID);
	}
	// ��Ʊȫ��λʱ��ĳֹ�����
	public void setPrivateStockPropertyMaxHoldAmount(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "MaxHoldAmount", value);
	}
	public Long getPrivateStockPropertyMaxHoldAmount(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "MaxHoldAmount");
	}
	// ��Ʊһ���ύ������
	public void setPrivateStockPropertyOneCommitAmount(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "OneCommitAmount", value);
	}
	public Long getPrivateStockPropertyOneCommitAmount(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "OneCommitAmount");
	}
	// �ύƵ��
	public void setPrivateStockPropertyMinCommitInterval(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "MinCommitInterval", value);
	}
	public Long getPrivateStockPropertyMinCommitInterval(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "MinCommitInterval");
	}
	// ���������������ʱ��
	public void setPrivateStockPropertyMaxHoldDays(String stockID, long value)
	{
		m_QUProperty.propertySetLong(stockID, "MaxHoldDays", value);
	}
	public Long getPrivateStockPropertyMaxHoldDays(String stockID)
	{
		return m_QUProperty.propertyGetLong(stockID, "MaxHoldDays");
	}
	// ���������Ŀ��ֹӯ��ӯ����
	public void setPrivateStockPropertyTargetProfitMoney(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "TargetProfitMoney", value);
	}
	public Double getPrivateStockPropertyTargetProfitMoney(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "TargetProfitMoney");
	}
	// ���������Ŀ��ֹӯ��ֹӯ��
	public void setPrivateStockPropertyTargetProfitPrice(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "TargetProfitPrice", value);
	}
	public Double getPrivateStockPropertyTargetProfitPrice(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "TargetProfitPrice");
	}
	// ���������Ŀ��ֹ�𣬿����
	public void setPrivateStockPropertyStopLossMoney(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "StopLossMoney", value);
	}
	public Double getPrivateStockPropertyStopLossMoney(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "StopLossMoney");
	}
	// ���������Ŀ��ֹ��ֹ���
	public void setPrivateStockPropertyStopLossPrice(String stockID, Double value)
	{
		m_QUProperty.propertySetDouble(stockID, "StopLossPrice", value);
	}
	public Double getPrivateStockPropertyStopLossPrice(String stockID)
	{
		return m_QUProperty.propertyGetDouble(stockID, "StopLossPrice");
	}
	
	/*
	 * ************************************************************************************
	 * buy sell signal�� amount
	 * ************************************************************************************
	 */
	public boolean buySignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
		Long lStockOneCommitInterval = this.getPrivateStockPropertyMinCommitInterval(stockID);
		if(null == lStockOneCommitInterval)
		{
			Long lMinCommitInterval =  this.getGlobalStockMinCommitInterval();
			if(null != lMinCommitInterval)
			{
				this.setPrivateStockPropertyMinCommitInterval(stockID, lMinCommitInterval);
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

		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.accountProxy().getTotalAssets(ctnTotalAssets);
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		ctx.accountProxy().getMoney(ctnMoney);
		
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		
		if(null == cHoldStock) // first create
		{
			// max hold count check
			Long lMaxHoldStockCount = this.getGlobalMaxHoldStockCount();
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.accountProxy().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() > lMaxHoldStockCount)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
				return false;
			}
			
			// define stock FullHoldAmount OneCommitAmount property
			Long lFullHoldAmount = this.getPrivateStockPropertyMaxHoldAmount(stockID);
			if(null == lFullHoldAmount)
			{
				Double dGlobalStockMaxPosstion = this.getGlobalStockMaxHoldPosstion();
				double curFullPositionMoney = ctnTotalAssets.get()*dGlobalStockMaxPosstion;
				long curFullPositionAmmount = (long)(curFullPositionMoney/fNowPrice);
				this.setPrivateStockPropertyMaxHoldAmount(stockID, curFullPositionAmmount);
				lFullHoldAmount = curFullPositionAmmount;
			}
			Long lOneCommitAmount = this.getPrivateStockPropertyOneCommitAmount(stockID);
			if(null == lOneCommitAmount)
			{
				Double dGlobalStockOneCommitPossition = this.getGlobalStockOneCommitPossition();
				Long curFullPositionAmmount = this.getPrivateStockPropertyMaxHoldAmount(stockID);
				long curStockOneCommitPossitionAmmount = (long)(curFullPositionAmmount*dGlobalStockOneCommitPossition);
				this.setPrivateStockPropertyOneCommitAmount(stockID, curStockOneCommitPossitionAmmount);
				lOneCommitAmount = curStockOneCommitPossitionAmmount;
			}	
			// ��׼��
			long newlOneCommitAmount = lOneCommitAmount; 
			if(0 != newlOneCommitAmount%100)
			{
				newlOneCommitAmount = newlOneCommitAmount/100*100;
			}
			if(0 != newlOneCommitAmount)
			{
				if(newlOneCommitAmount != lOneCommitAmount)
				{
					this.setPrivateStockPropertyOneCommitAmount(stockID, newlOneCommitAmount);
				}
				if(0 != lFullHoldAmount%newlOneCommitAmount)
				{
					lFullHoldAmount = (lFullHoldAmount/newlOneCommitAmount)*newlOneCommitAmount;
					this.setPrivateStockPropertyMaxHoldAmount(stockID, lFullHoldAmount);
				}
			}
			else
			{
				this.setPrivateStockPropertyOneCommitAmount(stockID, 0);
				this.setPrivateStockPropertyMaxHoldAmount(stockID, 0);
			}
		}
		
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lFullHoldAmount = this.getPrivateStockPropertyMaxHoldAmount(stockID);
		Long lOneCommitAmount = this.getPrivateStockPropertyOneCommitAmount(stockID);
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
		if(null == this.getPrivateStockPropertyMaxHoldDays(stockID))
		{
			Long lStockMaxHoldDays = this.getGlobalStockMaxHoldDays();
			if(null != lStockMaxHoldDays)
			{
				this.setPrivateStockPropertyMaxHoldDays(stockID, lStockMaxHoldDays);
			}
		}
		if(null == this.getPrivateStockPropertyTargetProfitMoney(stockID))
		{
			Double dTargetProfitRatio = this.getGlobalStockTargetProfitRatio();
			if(null != dTargetProfitRatio)
			{
				Double dTargetProfitMoney = lFullHoldAmount*fNowPrice*dTargetProfitRatio;
				this.setPrivateStockPropertyTargetProfitMoney(stockID, dTargetProfitMoney);
			}
		}
		if(null == this.getPrivateStockPropertyStopLossMoney(stockID))
		{
			Double dStockStopLossRatio = this.getGlobalStockStopLossRatio();
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lFullHoldAmount*fNowPrice*dStockStopLossRatio;
				this.setPrivateStockPropertyStopLossMoney(stockID, dStockStopLossMoney);
			}
		}
		return true;
	}
	public boolean sellSignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
		Long lStockOneCommitInterval = this.getGlobalStockMinCommitInterval();
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
		Long lFullHoldAmount = this.getPrivateStockPropertyMaxHoldAmount(stockID);
		Long lOneCommitAmount = this.getPrivateStockPropertyOneCommitAmount(stockID);
		
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
	 * ************************************************************************************
	 * Auto Force Clear Process, called by user in the end of BuySellCheck
	 * ************************************************************************************
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
		
		Double stopLossMoney = this.getPrivateStockPropertyStopLossMoney(stockID);
		Double stopLossPrice = this.getPrivateStockPropertyStopLossPrice(stockID);
		Double targetProfitMoney = this.getPrivateStockPropertyTargetProfitMoney(stockID);
		Double targetProfitPrice = this.getPrivateStockPropertyTargetProfitPrice(stockID);
		Long maxHoldDays = this.getPrivateStockPropertyMaxHoldDays(stockID);
		
		boolean bCLearAll = false;
		// ֹ����
		if(!bCLearAll &&
				null != stopLossMoney && 0 != stopLossMoney &&
				(fNowPrice - cHoldStock.refPrimeCostPrice)*cHoldStock.totalAmount <= stopLossMoney) 
		{
			bCLearAll = true;
		}
		// ֹ��ɼ�
		if(!bCLearAll &&
				null != stopLossPrice && 0 != stopLossPrice &&
				cDAStock.price() <= stopLossPrice) 
		{
			bCLearAll = true;
		}
		// ֹӯ���
		if(!bCLearAll &&
				null != targetProfitMoney && 0 != targetProfitMoney &&
				(fNowPrice - cHoldStock.refPrimeCostPrice)*cHoldStock.totalAmount >= targetProfitMoney) 
		{
			bCLearAll = true;
		}
		// ֹӯ�ɼ�
		if(!bCLearAll &&
				null != targetProfitPrice && 0 != targetProfitPrice &&
				cDAStock.price() >= targetProfitPrice) 
		{
			bCLearAll = true;
		}
		
		// �ֹɳ�ʱ
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
	
	
	@Override
	public void onInit(QuantContext ctx) {
		
		// String derivedClsName = this.getClass().getSimpleName();
		String accountIDName = ctx.accountProxy().ID();
		
		m_QUSelector = new QUSelector(accountIDName);
		m_QUSelector.loadFromFile();
		
		m_QUProperty = new QUProperty(accountIDName);
		m_QUProperty.loadFormFile();
		
		m_TranReportor = new TranReportor(accountIDName);
		
		this.onStrateInit(ctx);
		
		m_QUSelector.saveToFile();
		m_QUProperty.saveToFile();
		
		m_Console = new CConsole();
		m_Console.Start(this);
	}
	
	@Override
	public void onUnInit(QuantContext ctx) {
		m_Console.Stop();
	}
	
	@Override
	public void onDayStart(QuantContext ctx) {
		CLog.output("TEST", "onDayStart %s", ctx.date());

		// init select stock
		m_QUSelector.loadFromFile();
		// init property
		m_QUProperty.loadFormFile();

		ctx.addCurrentDayInterestMinuteDataIDs(m_QUSelector.selectList());
		//CLog.output("TEST", "onDayStart %s", m_QUSelector.dumpSelect());
		
		this.onStrateDayStart(ctx);
	}
	
	@Override
	public void onMinuteData(QuantContext ctx) {
		
		// callback to user with select&hold
		// user will raise buy|sell signal
		
		List<String> selectIDs = m_QUSelector.selectList();
		List<String> holdIDs = QUCommon.getHoldStockIDList(ctx.accountProxy());
		
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.addAll(selectIDs);
		hashSet.addAll(holdIDs);
		List<String> uniqueIDs = new ArrayList<String>();
		uniqueIDs.addAll(hashSet);
		
		for(int iStock=0; iStock<uniqueIDs.size(); iStock++)
		{
			String stockID = uniqueIDs.get(iStock);
			DAStock cDAStock = ctx.pool().get(stockID);
			this.onStrateMinute(ctx, cDAStock);
		}
	}
	
	@Override
	public void onDayFinish(QuantContext ctx) {
		
		// fetch user select stocks
		m_QUSelector.selectClear();
		this.onStrateDayFinish(ctx);
		m_QUSelector.saveToFile();
		
		// property reset�� remove it which not in select|hold
		List<String> selectIDs = m_QUSelector.selectList();
		List<String> holdIDs = QUCommon.getHoldStockIDList(ctx.accountProxy());
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
		ctx.accountProxy().getTotalAssets(ctnTotalAssets);
		double dSH = ctx.pool().get("999999").price();
		m_TranReportor.collectInfo_SHComposite(ctx.date(), dSH);
		m_TranReportor.collectInfo_TotalAssets(ctx.date(), ctnTotalAssets.get());
		m_TranReportor.generateReport();
		CLog.output("TEST", "onDayFinish %s dump account&select\n %s\n    -%s", ctx.date(), ctx.accountProxy().dump(), m_QUSelector.dumpSelect());
	}
	
	@Override
	public void command(String cmd)
	{
		CLog.output("TEST", "command %s", cmd);
		if(cmd.equals("pa"))
		{
		}
	}
	
	/*
	 * ���Գ�ʼ��
	 * ��������ʱֻ����һ��
	 */
	abstract void onStrateInit(QuantContext ctx);
	/*
	 * ����ÿ������
	 * ÿ�������տ�ʼ����ǰ����һ��
	 */
	abstract void onStrateDayStart(QuantContext ctx);
	/*
	 * �����������
	 * �����ڼ�ÿ���Ӷ�ÿ��ѡ��|���н��лص�
	 * �û�����tryBuy trySell ������������
	 */
	abstract void onStrateMinute(QuantContext ctx, DAStock cDAStock);

	/*
	 * ����ѡ��
	 * ÿ�콻�׽����������ݺ���лص�
	 * �û�����getXStockSelectManager����ѡ��
	 */
	abstract void onStrateDayFinish(QuantContext ctx);

	private CConsole m_Console;
	private QUSelector m_QUSelector;
	private QUProperty m_QUProperty;
	private TranReportor m_TranReportor;
}
