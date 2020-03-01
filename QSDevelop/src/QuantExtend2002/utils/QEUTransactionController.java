package QuantExtend2002.utils;

import java.util.ArrayList;
import java.util.List;

import QuantExtend1801.utils.QUCommon;
import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.account.common.TRANACT;
import pers.di.common.CObjectContainer;
import pers.di.common.CUtilsDateTime;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;

public class QEUTransactionController {
	
	public QEUTransactionController(QEUProperty cQEUProperty) {
		mQEUProperty = cQEUProperty;
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
		
		// interval commit check -------------------------------
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

		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.accountProxy().getTotalAssets(ctnTotalAssets);
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		ctx.accountProxy().getMoney(ctnMoney);
		
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		
		// first create ------------------------------- 
		// need init private property PrivateStockPropertyMaxHoldAmount & PrivateStockPropertyOneCommitAmount 
		if(null == cHoldStock) 
		{
			// 最大持股个数检查
			long lMaxHoldStockCountX = mQEUProperty.getGlobalStockMaxCount();
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.accountProxy().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() > lMaxHoldStockCountX)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
				return false;
			}
			
			// init PrivateStockPropertyMaxHoldAmount
			Long lFullHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
			if(null == lFullHoldAmount)
			{
				Double dGlobalFullHoldMarketValue = mQEUProperty.getGlobalHoldOneStockMaxMarketValue();
				long curFullPositionAmmount = (long)(dGlobalFullHoldMarketValue/fNowPrice)/100*100;
				mQEUProperty.setPrivateStockPropertyMaxHoldAmount(stockID, curFullPositionAmmount);
			}
			// init PrivateStockPropertyOneCommitAmount
			Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
			if(null == lOneCommitAmount)
			{
				Double dGlobalBuyCommitMaxMarketValue = mQEUProperty.getGlobalBuyOneStockCommitMaxMarketValue();
				long curFullPositionAmmount = (long)(dGlobalBuyCommitMaxMarketValue/fNowPrice)/100*100;
				mQEUProperty.setPrivateStockPropertyOneCommitAmount(stockID, curFullPositionAmmount);
			}	
		}
		
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lMaxHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
		Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
		// MaxHoldAmount AlreadyHoldAmount check
		if(lAlreadyHoldAmount >= lMaxHoldAmount) 
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lAlreadyHoldAmount=%d lFullHoldAmount=%d",  stockID, lAlreadyHoldAmount, lFullHoldAmount);
			return false;
		}
		Long lCommitAmount = Math.min(lMaxHoldAmount-lAlreadyHoldAmount, lOneCommitAmount);
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
		
		// post request -------------------------------
		ctx.accountProxy().pushBuyOrder(stockID, lCommitAmount.intValue(), fNowPrice);
		
		// create clear property
		if(null == mQEUProperty.getPrivateStockPropertyMaxHoldDays(stockID))
		{
			Long lStockMaxHoldDays = mQEUProperty.getGlobalStockMaxHoldDays();
			if(null != lStockMaxHoldDays)
			{
				mQEUProperty.setPrivateStockPropertyMaxHoldDays(stockID, lStockMaxHoldDays);
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyTargetProfitMoney(stockID))
		{
			Double dTargetProfitRatio = mQEUProperty.getGlobalStockTargetProfitRatio();
			if(null != dTargetProfitRatio)
			{
				Double dTargetProfitMoney = lMaxHoldAmount*fNowPrice*dTargetProfitRatio;
				mQEUProperty.setPrivateStockPropertyTargetProfitMoney(stockID, dTargetProfitMoney);
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyStopLossMoney(stockID))
		{
			Double dStockStopLossRatio = mQEUProperty.getGlobalStockStopLossRatio();
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lMaxHoldAmount*fNowPrice*dStockStopLossRatio;
				mQEUProperty.setPrivateStockPropertyStopLossMoney(stockID, dStockStopLossMoney);
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
