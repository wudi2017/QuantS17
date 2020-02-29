package QuantExtend1711;

import java.util.ArrayList;
import java.util.List;

import QuantExtend1711.utils.EKRefHistoryPos;
import QuantExtend1711.utils.TranDaysChecker;
import QuantExtend1711.utils.TranReportor;
import QuantExtend1711.utils.XStockSelectManager;
import QuantExtend1711.utils.ZCZXChecker;
import QuantExtend1711.utils.EKRefHistoryPos.EKRefHistoryPosParam;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;

/*
 * 策略概要：
 * 当出现早晨之星直接尾盘冲进，第二天必须卖出
 */
public class QS1711T1 {
	public static class QS1711T1Strategy extends QS1711SBase
	{
		public QS1711T1Strategy()
		{
			super(10, 5); // maxSelect=10 maxHold=5
		}
		
		@Override
		void onStrateInit(QuantContext ctx)
		{
		}
		
		@Override
		void onStrateDayStart(QuantContext ctx)
		{
		}
		
		@Override
		void onStrateBuyCheck(QuantContext ctx, DAStock cDAStock) {
			
		}
		
		@Override
		void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock) {
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();
			
			boolean bSellFlag = false;
			do
			{
				if(cHoldStock.availableAmount <= 0)
				{
					bSellFlag = false;
					break;
				}
				
				// 涨停不卖出
				double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
				double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
				if(0 == Double.compare(fZhangTing, fNowPrice))
				{
					bSellFlag = false;
					break;
				}
				
				// 当天结束必须卖出
				if(ctx.time().compareTo("14:54:00") >= 0)
				{
					bSellFlag = true;
					break;
				}
				
				// 止盈止损卖出
				if(cHoldStock.refProfitRatio() > 0.01 || cHoldStock.refProfitRatio() < -0.02) 
				{
					bSellFlag = true;
					break;
				}
			} while(false);
				
			if(bSellFlag)
			{
				ctx.accountProxy().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
			}	
		}
		
		@Override
		void onStrateDayFinish(QuantContext ctx) {
			
			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				// 过滤：股票ID集合，当天检查
				if(
					//cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 
					cDAStock.dayKLines().size()<60
					|| !cDAStock.dayKLines().lastDate().equals(ctx.date())
					|| cDAStock.circulatedMarketValue() > 1000.0) {	
					continue;
				}
				
				// 早晨之星
				int iEnd = cDAStock.dayKLines().size()-1;
				if(ZCZXChecker.check(cDAStock.dayKLines(),iEnd))
				{
					boolean bcheckVolume = ZCZXChecker.check_volume(cDAStock.dayKLines(),iEnd);
					if(bcheckVolume)
					{
						EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
						super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
					}
				}
			}
			
			List<String> validSelectList = super.getXStockSelectManager().validSelectListS1(20);
			for(int iStock=0; iStock<validSelectList.size(); iStock++)
			{
				String stockID = validSelectList.get(iStock);
				super.tryBuy(ctx, stockID);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		
		IAccount acc = cAccountController.account();
		
		Quant.instance().run(
				"HistoryTest 2010-01-01 2017-11-25", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccountController, 
				new QS1711T1Strategy());
		cAccountController.close();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
