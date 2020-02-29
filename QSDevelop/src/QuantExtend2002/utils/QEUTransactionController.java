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
	 * buy sell signal£¬ amount
	 * ************************************************************************************
	 */
	public boolean buySignalEmit(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// interval commit check
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
		
		if(null == cHoldStock) // first create
		{
			// max hold count check, default is 1000;
			Long lMaxHoldStockCount = mQEUProperty.getGlobalMaxHoldStockCount();
			if(null == lMaxHoldStockCount)
			{
				lMaxHoldStockCount = 1000L;
			}
			
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.accountProxy().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() > lMaxHoldStockCount)
			{
				//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
				return false;
			}
			
			// define stock FullHoldAmount OneCommitAmount property
			Long lFullHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
			if(null == lFullHoldAmount)
			{
				Double dGlobalStockMaxPosstion = mQEUProperty.getGlobalStockMaxHoldPosstion();
				if (null == dGlobalStockMaxPosstion) {
					dGlobalStockMaxPosstion = 0.1;
				}
				double curFullPositionMoney = ctnTotalAssets.get()*dGlobalStockMaxPosstion;
				long curFullPositionAmmount = (long)(curFullPositionMoney/fNowPrice);
				mQEUProperty.setPrivateStockPropertyMaxHoldAmount(stockID, curFullPositionAmmount);
				lFullHoldAmount = curFullPositionAmmount;
			}
			Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
			if(null == lOneCommitAmount)
			{
				Double dGlobalStockOneCommitPossition = mQEUProperty.getGlobalStockOneCommitPossition();
				if (null == dGlobalStockOneCommitPossition) {
					dGlobalStockOneCommitPossition = 0.1;
				}
				Long curFullPositionAmmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
				long curStockOneCommitPossitionAmmount = (long)(curFullPositionAmmount*dGlobalStockOneCommitPossition);
				mQEUProperty.setPrivateStockPropertyOneCommitAmount(stockID, curStockOneCommitPossitionAmmount);
				lOneCommitAmount = curStockOneCommitPossitionAmmount;
			}	
			// ±ê×¼»¯
			long newlOneCommitAmount = lOneCommitAmount; 
			if(0 != newlOneCommitAmount%100)
			{
				newlOneCommitAmount = newlOneCommitAmount/100*100;
				if(0 == newlOneCommitAmount) {
					newlOneCommitAmount = 100;
				}
			}
			if(0 != newlOneCommitAmount)
			{
				if(newlOneCommitAmount != lOneCommitAmount)
				{
					mQEUProperty.setPrivateStockPropertyOneCommitAmount(stockID, newlOneCommitAmount);
				}
				if(0 != lFullHoldAmount%newlOneCommitAmount)
				{
					lFullHoldAmount = (lFullHoldAmount/newlOneCommitAmount)*newlOneCommitAmount;
					mQEUProperty.setPrivateStockPropertyMaxHoldAmount(stockID, lFullHoldAmount);
				}
			}
			else
			{
				mQEUProperty.setPrivateStockPropertyOneCommitAmount(stockID, 0);
				mQEUProperty.setPrivateStockPropertyMaxHoldAmount(stockID, 0);
			}
		}
		
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lFullHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
		Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
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
				Double dTargetProfitMoney = lFullHoldAmount*fNowPrice*dTargetProfitRatio;
				mQEUProperty.setPrivateStockPropertyTargetProfitMoney(stockID, dTargetProfitMoney);
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyStopLossMoney(stockID))
		{
			Double dStockStopLossRatio = mQEUProperty.getGlobalStockStopLossRatio();
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lFullHoldAmount*fNowPrice*dStockStopLossRatio;
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
		Long lFullHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
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
