package QuantExtend2002.framework;

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
		
		// 提交频率检查，不允许连续快速提交买单
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
		// 不允许当日卖出后还提交买单
		cCommissionOrder = QUCommon.getLatestCommissionOrder(ctx.accountProxy(), stockID, TRANACT.SELL);
		if(null != cCommissionOrder && null != lStockOneCommitInterval)
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lStockOneCommitInterval=%d", stockID, lStockOneCommitInterval);
			return false;
		}

		// 最大持股个数检查，不允许持有超过最大设定值的股票个数
		long lMaxHoldStockCountX = mQEUProperty.getGlobalStockMaxCount();
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.accountProxy().getHoldStockList(ctnHoldStockList);
		if(ctnHoldStockList.size() >= lMaxHoldStockCountX)
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! lMaxHoldStockCount=%d", stockID, lMaxHoldStockCount);
			return false;
		}
		
		// 如果是首次创建股票，初始化一些属性
		// need init private property PrivateStockPropertyMaxHoldAmount & PrivateStockPropertyOneCommitAmount 
		HoldStock cHoldStock = QUCommon.getHoldStock(ctx.accountProxy(), stockID);
		if(null == cHoldStock) 
		{
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
	
		// 本只股票持仓量检查，不允许超过设置的最大持有量
		Long lAlreadyHoldAmount = null!=cHoldStock?cHoldStock.totalAmount:0L;
		Long lMaxHoldAmount = mQEUProperty.getPrivateStockPropertyMaxHoldAmount(stockID);
		Long lOneCommitAmount = mQEUProperty.getPrivateStockPropertyOneCommitAmount(stockID);
		if(lAlreadyHoldAmount >= lMaxHoldAmount)  // MaxHoldAmount AlreadyHoldAmount check
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
		// 本只股票确定买入时，持有足够现金检查，不允许现金不充足时下单
		CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
		ctx.accountProxy().getMoney(ctnMoney);
		double needCommitMoney = lCommitAmount*fNowPrice;
		if(needCommitMoney > ctnMoney.get()) // CommitMoney check
		{
			//CLog.output("TEST", "buySignalEmit %s ignore! needCommitMoney=%.3f ctnMoney=%.3f", stockID, needCommitMoney,ctnMoney.get());
			return false;
		}
		
		// 所有检查完毕，推送买单
		ctx.accountProxy().pushBuyOrder(stockID, lCommitAmount.intValue(), fNowPrice);
		
		// 创建清仓属性，用于决定本只股票的清仓条件
		if(null == mQEUProperty.getPrivateStockPropertyMaxHoldDays(stockID))
		{
			Long lStockMaxHoldDays = mQEUProperty.getGlobalStockMaxHoldDays();
			if(null != lStockMaxHoldDays)
			{
				mQEUProperty.setPrivateStockPropertyMaxHoldDays(stockID, lStockMaxHoldDays); // 最大持有天数
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyTargetProfitMoney(stockID))
		{
			Double dTargetProfitRatio = mQEUProperty.getGlobalStockTargetProfitRatio();
			if(null != dTargetProfitRatio)
			{
				Double dTargetProfitMoney = lMaxHoldAmount*fNowPrice*dTargetProfitRatio;
				mQEUProperty.setPrivateStockPropertyTargetProfitMoney(stockID, dTargetProfitMoney);// 止盈涨幅
			}
		}
		if(null == mQEUProperty.getPrivateStockPropertyStopLossMoney(stockID))
		{
			Double dStockStopLossRatio = mQEUProperty.getGlobalStockStopLossRatio();
			if(null != dStockStopLossRatio)
			{
				Double dStockStopLossMoney = lMaxHoldAmount*fNowPrice*dStockStopLossRatio;
				mQEUProperty.setPrivateStockPropertyStopLossMoney(stockID, dStockStopLossMoney);// 止损跌幅
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
