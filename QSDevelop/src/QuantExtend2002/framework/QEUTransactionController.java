package QuantExtend2002.framework;

import java.util.ArrayList;
import java.util.List;

import QuantExtend1801.utils.QUCommon;
import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.account.common.TRANACT;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CUtilsDateTime;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;

public class QEUTransactionController {
	
	public QEUTransactionController(QEUProperty cQEUProperty) {
		mQEUProperty = cQEUProperty;
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
		
		// �ύƵ�ʼ�飬���������������ύ��
		Long lStockOneCommitInterval = mQEUProperty.getPrivateStockPropertyMinCommitInterval(stockID);
		if(null == lStockOneCommitInterval)
		{
			Long lMinCommitInterval =  mQEUProperty.getGlobalStockMinCommitInterval();
			if(null != lMinCommitInterval)
			{
				mQEUProperty.setPrivateStockPropertyMinCommitInterval(stockID, lMinCommitInterval);
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
		// ���������������ύ��
		cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.accountProxy(), stockID, TRANACT.SELL);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
			return false;
		}

		// ���ֹɸ�����飬��������г�������趨ֵ�Ĺ�Ʊ����
		long lMaxHoldStockCountX = mQEUProperty.getGlobalStockMaxCount();
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.accountProxy().getHoldStockList(ctnHoldStockList);
		if(ctnHoldStockList.size() >= lMaxHoldStockCountX)
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
			return false;
		}
		
		// ������״δ�����Ʊ����ʼ�����ԣ��˹�Ʊ�����������͵����ύ��������ȫ�����Գ�ʼ����
		// need init private property PrivateStockPropertyMaxHoldAmount & PrivateStockPropertyOneCommitAmount 
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		if(null == cHoldStock) 
		{
			CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
			int iRet = ctx.accountProxy().getTotalAssets(ctnTotalAssets);
			if (0 != iRet) {
				CLog.error("Quant", "QEUTransactionController.buySignalEmit getTotalAssets failed!");
				CSystem.exit(-1);
			}
			// init PrivateStockPropertyMaxHoldAmount
			Long lFullHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
			if(null == lFullHoldAmount)
			{
				/* use HoldOneStockMaxPositionRatio and HoldOneStockMaxMarketValue to decide 
				 * current stock PrivateStockPropertyMaxHoldAmount in current time.*/
				Double dGlobalFullHoldPositionRatio = mQEUProperty.getGlobalHoldOneStockMaxPositionRatio();
				Double dGlobalFullHoldPositionRatio_Value = ctnTotalAssets.get() * dGlobalFullHoldPositionRatio;
				Double dGlobalFullHoldMarketValue_Value = mQEUProperty.getGlobalHoldOneStockMaxMarketValue();
				Double dGlobalFullHoldMarketValue = Math.min(dGlobalFullHoldPositionRatio_Value, dGlobalFullHoldMarketValue_Value);
				long curFullPositionAmmount = (long)(dGlobalFullHoldMarketValue/fNowPrice)/100*100;
				mQEUProperty.setPrivateStockPropertyMaxHoldAmount(stockID, curFullPositionAmmount);
			}
			// init PrivateStockPropertyOneCommitAmount
			Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
			if(null == lOneCommitAmount)
			{
				Double dGlobalBuyCommitMaxPositionRatio = mQEUProperty.getGlobalBuyOneStockCommitMaxPositionRatio();
				Double dGlobalBuyCommitMaxPositionRatio_Value = ctnTotalAssets.get() * dGlobalBuyCommitMaxPositionRatio;
				Double dGlobalBuyCommitMaxMarketValue_Value = mQEUProperty.getGlobalBuyOneStockCommitMaxMarketValue();
				Double dGlobalBuyCommitMaxMarketValue = Math.min(dGlobalBuyCommitMaxPositionRatio_Value, dGlobalBuyCommitMaxMarketValue_Value);
				long curFullPositionAmmount = (long)(dGlobalBuyCommitMaxMarketValue/fNowPrice)/100*100;
				mQEUProperty.setPrivateStockPropertyOneCommitAmount(stockID, curFullPositionAmmount);
			}	
		}
	
		// ��ֻ��Ʊ�ֲ�����飬�����������õ���������
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lMaxHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
		Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
		if(lAlreadyHoldAmount >= lMaxHoldAmount)  // MaxHoldAmount AlreadyHoldAmount check
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lAlreadyHoldAmount=%d lFullHoldAmount=%d",  stockID, lAlreadyHoldAmount, lFullHoldAmount);
			return false;
		}
		// ��ֻ��Ʊȷ������ʱ������ȷ��������������Ϊ����  1.û���㹻�ֽ� 2.ҪԤ�������ֽ� 3.����󳬹�������� �����ۺ���������ȡ��Сֵ��
		Long lCommitAmount = Math.min(lMaxHoldAmount-lAlreadyHoldAmount, lOneCommitAmount);
		double needStockCommitMoney = lCommitAmount*fNowPrice;
		double reservedCostMoney = needStockCommitMoney*0.01 < 50.0 ? 50.0: needStockCommitMoney*0.01;
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		ctx.accountProxy().getMoney(ctnMoney);
		double stockMoney = Math.min(ctnMoney.get()-reservedCostMoney, needStockCommitMoney);
		if(stockMoney < 100.0) // money is too less, cannot transact.
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! needCommitMoney=%.3f ctnMoney=%.3f", stockID, needCommitMoney,ctnMoney.get());
			return false;
		}
		lCommitAmount = ((long)(stockMoney/fNowPrice))/100*100;
		if(lCommitAmount < 100) // CommitAmount check, less than 100, cannot commit
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! iCreateAmount=%d", stockID, lCommitAmount);
			return false;
		}
		
		// ���м����ϣ�������
		ctx.accountProxy().pushBuyOrder(stockID, lCommitAmount.intValue(), fNowPrice);
		
		// ����������ԣ����ھ�����ֻ��Ʊ���������
		if(null == mQEUProperty.getPrivateStockPropertyMaxHoldDays(stockID))
		{
			Long lStockMaxHoldDays = mQEUProperty.getGlobalStockMaxHoldDays();
			if(null != lStockMaxHoldDays)
			{
				mQEUProperty.setPrivateStockPropertyMaxHoldDays(stockID, lStockMaxHoldDays); // ����������
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyTargetProfitMoney(stockID))
		{
			Double dTargetProfitRatio = mQEUProperty.getGlobalStockTargetProfitRatio();
			if(null != dTargetProfitRatio)
			{
				Double dTargetProfitMoney = lMaxHoldAmount*fNowPrice*dTargetProfitRatio;
				mQEUProperty.setPrivateStockPropertyTargetProfitMoney(stockID, dTargetProfitMoney);// ֹӯ�Ƿ�
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyStopLossMoney(stockID))
		{
			Double dStockStopLossRatio = mQEUProperty.getGlobalStockStopLossRatio();
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lMaxHoldAmount*fNowPrice*dStockStopLossRatio;
				mQEUProperty.setPrivateStockPropertyStopLossMoney(stockID, dStockStopLossMoney);// ֹ�����
			}
		}
		return true;
	}
	
	
	public boolean sellSignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
		Long lStockOneCommitInterval = mQEUProperty.getGlobalStockMinCommitInterval();
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
		Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
		Long lCommitAmount = Math.min(lAvailableAmount, lOneCommitAmount);
		if(lCommitAmount <= 0) // CommitAmount check
		{
			return false;
		}
		
		// post request
		ctx.accountProxy().pushSellOrder(cHoldStock.stockID, lCommitAmount.intValue(), fNowPrice);
		return true;
	}
	
	private QEUProperty mQEUProperty;
}
