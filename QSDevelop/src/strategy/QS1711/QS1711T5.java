package strategy.QS1711;

import java.util.List;

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
import utils.QS1711.DayKLinePriceWaveChecker;
import utils.QS1711.EKGlobalRisk;
import utils.QS1711.ETDropStable;
import utils.QS1711.TranDaysChecker;
import utils.QS1711.ZCZXChecker;
import utils.QS1711.ETDropStable.ResultDropStable;
import utils.QS1711.base.EKRefHistoryPos;
import utils.QS1711.base.EKRefHistoryPos.EKRefHistoryPosParam;

public class QS1711T5 {
	public static class QS1711T5Strategy extends QS1711SBase
	{
		public QS1711T5Strategy()
		{
			super(20, 5); // maxSelect maxHold
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
			int iCheck = list.size()-2;
			
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
				if(fZhang > 0.05)
				{
					return;
				}
			}
			else
			{
				return;
			}
			
			// 当天分时不急跌不买进
			double dWave = DayKLinePriceWaveChecker.check(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
			ResultDropStable cResultDropStable = ETDropStable.checkDropStable(cDAStock.timePrices(), cDAStock.timePrices().size()-1, dWave*1.2);
			if(!cResultDropStable.bCheck)
			{
				return;
			}

			if(m_bGlobalLowRisk)
			{
				super.tryBuy(ctx, cDAStock.ID());
			}
			else
			{
				super.tryBuy(ctx, cDAStock.ID());
			}
		}

		@Override
		void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock) {
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();
			
			if(cHoldStock.availableAmount <= 0)
			{
				return;
			}
				
			// 涨停不卖出
			double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
			if(0 == Double.compare(fZhangTing, fNowPrice))
			{
				return;
			}
				
			// 持股超时卖出
			long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
			if(lHoldDays >= 10) 
			{
				super.trySell(ctx, cHoldStock.stockID);
				return;
			}
				
			// 止盈止损卖出
			if(cHoldStock.refProfitRatio() > 0.5 || cHoldStock.refProfitRatio() < -0.12) 
			{
				super.trySell(ctx, cHoldStock.stockID);
				return;
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
							super.getXStockSelectManager().addSelect(cDAStock.ID(), 0);
						}
					}
				}
			}
			
			// 筛选，长期跌幅在前
			{
				int iSelectSise = super.getXStockSelectManager().sizeSelect();
				List<String> selects = super.getXStockSelectManager().validSelectListS1(iSelectSise/2);
				super.getXStockSelectManager().clearSelect();
				for(int iStock=0; iStock<selects.size(); iStock++)
				{
					String stockID = selects.get(iStock);
					DAStock cDAStock = ctx.pool().get(stockID);
					EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
					if(cEKRefHistoryPosParam.bCheck)
					{
						super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
					}
				}
			}
			
			// 筛选，长期涨幅靠后
			{
				int iSelectSise = super.getXStockSelectManager().sizeSelect();
				List<String> selects = super.getXStockSelectManager().validSelectListS1(iSelectSise/2);
				super.getXStockSelectManager().clearSelect();
				for(int iStock=0; iStock<selects.size(); iStock++)
				{
					String stockID = selects.get(iStock);
					DAStock cDAStock = ctx.pool().get(stockID);
					EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
					if(cEKRefHistoryPosParam.bCheck)
					{
						super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refLow);
					}
				}
			}
			
			// 排序，近期跌幅较大
			{
				List<String> selects = super.getXStockSelectManager().validSelectListS1(10);
				super.getXStockSelectManager().clearSelect();
				for(int iStock=0; iStock<selects.size(); iStock++)
				{
					String stockID = selects.get(iStock);
					DAStock cDAStock = ctx.pool().get(stockID);
					EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(12, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
					if(cEKRefHistoryPosParam.bCheck)
					{
						super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
					}
				}
			}
			
			// 当天晚上判断大盘风险
			DAStock cDAStockSZZS = ctx.pool().get("999999");
			m_bGlobalLowRisk = EKGlobalRisk.isLowRisk(cDAStockSZZS.dayKLines(), cDAStockSZZS.dayKLines().size()-1);
		}
		
		private boolean m_bGlobalLowRisk;
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
				new QS1711T5Strategy());
		cAccountController.close();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
