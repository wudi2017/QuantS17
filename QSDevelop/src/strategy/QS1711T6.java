package strategy;

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
import utils.TranDaysChecker;
import utils.XStockHoldManager;
import utils.XStockHoldManager.InnerHoldStockItem;
import utils.ZCZXChecker;
import utils.base.EKRefHistoryPos;
import utils.base.EKRefHistoryPos.EKRefHistoryPosParam;

public class QS1711T6 {
	public static class QS1711T6Strategy extends QS1711Base {
		public QS1711T6Strategy()
		{
			super(10, 5); // maxSelect=10 maxHold=5
		}

		@Override
		void onStrateInit(QuantContext ctx)
		{
			m_XStockHoldManager = new XStockHoldManager(ctx.ap());
		}
		
		@Override
		void onStrateDayStart(QuantContext ctx)
		{
			m_XStockHoldManager.loadFromFile();
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

			m_XStockHoldManager.setHold(cDAStock.ID(), 0, -0.12, 10000, 0.1, 30);
			m_XStockHoldManager.saveToFile();
			super.tryBuy(ctx, cDAStock.ID());	
		}

		@Override
		void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock) {
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();
			
			InnerHoldStockItem cInnerHoldStockItem = m_XStockHoldManager.getHold(cDAStock.ID());
			
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
			if(lHoldDays >= cInnerHoldStockItem.maxHoldDays) 
			{
				super.trySell(ctx, cHoldStock.stockID);
				return;
			}
				
			// 止盈止损卖出
			if(cHoldStock.refProfitRatio() > cInnerHoldStockItem.targetProfitRatio || cHoldStock.refProfitRatio() < cInnerHoldStockItem.stopLossRatio) 
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
							EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
						}
					}
				}
			}
			
		}
		
		private XStockHoldManager m_XStockHoldManager;
	}
	
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		cAccoutDriver.load("account_QS1711" ,  new MockAccountOpe(), true);
		cAccoutDriver.reset(100000);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2010-01-01 2010-03-01", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1711T6Strategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
