package QuantExtend1711;

import java.util.List;

import QuantExtend1711.utils.EKRefHistoryPos;
import QuantExtend1711.utils.ZCZXChecker;
import QuantExtend1711.utils.EKRefHistoryPos.EKRefHistoryPosParam;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.localstock.common.KLine;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

/*
 * QS1711策略  + 个股历史盈利率检查
 */
public class QS1711T7 {
	public static class QS1711T7Strategy extends QS1711SCBase {
		public QS1711T7Strategy()
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
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();

			// 0-满足低于参考价直接建仓
			if(super.getXStockSelectManager().checkLowerRefCreatePrice(cDAStock))
			{
				if(super.tryBuy(ctx, cDAStock.ID()))
				{
					// 建立清仓规则
					super.getXStockClearRuleManager().setRule(cDAStock.ID(), 
							0, -0.12, 0,
							0, 0.1, 0,
							30);
				}
			}
			
			// 1-跌停不买进
			double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
			if(0 == Double.compare(fDieTing, fNowPrice))
			{
				return;
			}
				
			// 2-近期涨幅过大不买进（根据选股条件计算买入参数）
			boolean bCheckFlg = false;
			int iZCZXFindEnd = -1;
			DAKLines list = cDAStock.dayKLines();
			int iCheck = list.size()-1;
			
			int iBegin = iCheck-5;
			int iEnd = iCheck;
			
			for(int i=iEnd;i>=iBegin;i--)
			{
				if(ZCZXChecker.check(list,i))
				{
					bCheckFlg = true;
					iZCZXFindEnd = i;
					break;
				}
			}
			
			if(bCheckFlg && -1!=iZCZXFindEnd)
			{
				KLine cKLineZCZXEnd = cDAStock.dayKLines().get(iZCZXFindEnd);
				double fStdPaZCZX = (cKLineZCZXEnd.entityHigh() + cKLineZCZXEnd.entityLow())/2;
				double fZhang = (fNowPrice-fStdPaZCZX)/fStdPaZCZX;
				if(fZhang > 0.08)
				{
					return;
				}
			}
			else
			{
				return;
			}

			if(super.tryBuy(ctx, cDAStock.ID()))
			{
				// 建立清仓规则
				super.getXStockClearRuleManager().setRule(cDAStock.ID(), 
						0, -0.12, 0,
						0, 0.1, 0,
						30);
			}
		}

		@Override
		void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock) {
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();

			// 涨停不卖出
			double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
			if(0 == Double.compare(fZhangTing, fNowPrice))
			{
				return;
			}

			// 满足清仓规则，全部卖出
			boolean bClear = super.getXStockClearRuleManager().clearCheck(ctx, cDAStock, cHoldStock);
			if(bClear)
			{
				super.trySell(ctx, cHoldStock.stockID);
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
					
				// 5天内存在早晨之星
				int iBegin = cDAStock.dayKLines().size()-1-5;
				int iEnd = cDAStock.dayKLines().size()-1;
				for(int i=iEnd;i>=iBegin;i--)
				{
					if(ZCZXChecker.check(cDAStock.dayKLines(),i))
					{
						boolean bcheckVolume = ZCZXChecker.check_volume(cDAStock.dayKLines(),i);
						if(bcheckVolume)
						{
							EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
						}
					}
				}
			}
			
			
			// 筛选，盈利率
			{
				int iSelectSise = super.getXStockSelectManager().sizeSelect();
				List<String> selects = super.getXStockSelectManager().validSelectListS1(iSelectSise/2);
				super.getXStockSelectManager().clearSelect();
				for(int iStock=0; iStock<selects.size(); iStock++)
				{
					String stockID = selects.get(iStock);
					DAStock cDAStock = ctx.pool().get(stockID);
					
					double succRate = ZCZXChecker.check_history(cDAStock.dayKLines());
					if(succRate > 0.65)
					{
						super.getXStockSelectManager().addSelect(cDAStock.ID(), succRate);
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.debug("TEST", "FastTest main begin");
		
		// create testaccount
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("account_QS1711T7", true);
		cAccountController.reset(100000);
		IAccount acc = cAccountController.account();
		
		Quant.instance().run(
				"HistoryTest 2010-01-01 2017-12-15", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccountController, 
				new QS1711T7Strategy());
		cAccountController.close();
		
		CLog.debug("TEST", "FastTest main end");
		CSystem.stop();
	}
}
