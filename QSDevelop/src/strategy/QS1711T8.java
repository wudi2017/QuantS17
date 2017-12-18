package strategy;

import java.util.List;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import strategy.QS1711T7.QS1711T7Strategy;
import utils.DayKLinePriceWaveChecker;
import utils.ETDropStable;
import utils.ZCZXChecker;
import utils.ETDropStable.ResultDropStable;
import utils.base.EKRefHistoryPos;
import utils.base.EKRefHistoryPos.EKRefHistoryPosParam;

public class QS1711T8 {
	public static class QS1711T8Strategy extends QS1711SCBase {
		public QS1711T8Strategy()
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
			
			double dZhang = 0.0f;
			if(bCheckFlg && -1!=iZCZXFindEnd)
			{
				KLine cKLineZCZXEnd = cDAStock.dayKLines().get(iZCZXFindEnd);
				double fStdPaZCZX = (cKLineZCZXEnd.entityHigh() + cKLineZCZXEnd.entityLow())/2;
				dZhang = (fNowPrice-fStdPaZCZX)/fStdPaZCZX;
				if(dZhang > 0.0)
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
			ResultDropStable cResultDropStable = ETDropStable.checkDropStable(cDAStock.timePrices(), cDAStock.timePrices().size()-1, dWave/3*2);
			if(!cResultDropStable.bCheck)
			{
				return;
			}
						
			if(super.tryBuy(ctx, cDAStock.ID()))
			{
				// 建立清仓规则
				super.getXStockClearRuleManager().setRule(cDAStock.ID(), 
						0, -dWave, 0,
						0, dWave, 0,
						3);
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
							EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(250, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
						}
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		cAccoutDriver.load("account_QS1711T7" ,  new MockAccountOpe(), true);
		cAccoutDriver.reset(100000);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2010-01-01 2017-12-15", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1711T7Strategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
