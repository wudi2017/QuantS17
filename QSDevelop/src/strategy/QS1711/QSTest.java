package strategy.QS1711;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataapi.common.TimePrice;
import pers.di.dataengine.DAStock;
import pers.di.dataengine.DATimePrices;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import pers.di.quantplatform.QuantStrategy;
import utils.QS1711.DK1LineCross3Ave;
import utils.QS1711.DKMidDropChecker;
import utils.QS1711.DayKLineLongLowerShadowChecker;
import utils.QS1711.EKGlobalRisk;
import utils.QS1711.ETDropStable;
import utils.QS1711.ETDropStable.ResultDropStable;
import utils.QS1711.base.EKRefHistoryPos;
import utils.QS1711.base.EKRefHistoryPos.EKRefHistoryPosParam;
import utils.QS1711.ZCZXChecker;

public class QSTest {
	
	public static class QSTestStrategy extends QuantStrategy
	{
		public boolean m_bEnableTran;
		public String m_selectID;
		public QSTestStrategy()
		{
			m_bEnableTran = true;
			m_selectID="";
		}
		
		@Override
		public void onInit(QuantContext ctx) {
		}
		@Override
		public void onUnInit(QuantContext ctx) {
		}
		@Override
		public void onDayStart(QuantContext ctx) {
//			if(ctx.date().equals("2016-06-13"))
//				super.addCurrentDayInterestMinuteDataID("000544");
		}
		@Override
		public void onMinuteData(QuantContext ctx) {

//			if(ctx.date().equals("2016-06-13"))
//			{
//				DAStock cDAStock = ctx.pool().get("000544");
//				DATimePrices cDATimePrices = cDAStock.timePrices();
//				//CLog.output("TEST", "%s %.3f", ctx.time(), cDAStock.price());
//				
//				ResultDropStable cResultDropStable = ETDropStable.checkDropStable(cDAStock.timePrices(), cDAStock.timePrices().size()-1, 0.05);
//				if(cResultDropStable.bCheck)
//				{
//					CLog.output("TEST", "%s %s %.3f !!!!", ctx.date(), ctx.time(), cDAStock.price());
//				}
//			}
		}
		
		@Override
		public void onDayFinish(QuantContext ctx) {
			
//			DAStock cDAStock = ctx.pool().get("999999");
//			boolean bLowRisk = EKGlobalRisk.isLowRisk(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
//			if(bLowRisk)
//			{
//				CLog.output("TEST", "Date:%s", ctx.date());
//			}
			
			m_selectID = "";
			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				
				// 过滤：股票ID集合，当天检查
				boolean bCheckX = false;
				if(cDAStock.ID().compareTo("000544") == 0
					&& cDAStock.dayKLines().size()>60
					&& cDAStock.dayKLines().lastDate().equals(ctx.date())
					&& cDAStock.circulatedMarketValue() < 1000.0) 
				{	
					bCheckX = true;
				}
				
				if(bCheckX)
				{
					
					
					int iEnd = cDAStock.dayKLines().size()-1;
					if(ZCZXChecker.check(cDAStock.dayKLines(),iEnd)
							&& ZCZXChecker.check_volume(cDAStock.dayKLines(),iEnd))
					{
						double succRate = ZCZXChecker.check_history(cDAStock.dayKLines());
						EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(20, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
						CLog.output("TEST", "PreCheck ZCZX Date:%s ID:%s !%.3f !%.3f Succ:%.3f", ctx.date(), cDAStock.ID(), 
								cEKRefHistoryPosParam.refHigh, cEKRefHistoryPosParam.refLow, succRate);
						m_selectID = cDAStock.ID();
						
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
		cAccoutDriver.load("fast_mock001" ,  new MockAccountOpe(), true);
		cAccoutDriver.reset(100000);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2010-01-01 2017-06-05", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QSTestStrategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
