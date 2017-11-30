package strategy;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import pers.di.quantplatform.QuantStrategy;
import utils.DKMidDropChecker;
import utils.ZCZXChecker;

public class QSTest {
	
	public static class QSTestStrategy extends QuantStrategy
	{
		public QSTestStrategy()
		{
		}
		
		@Override
		public void onInit(QuantContext ctx) {
		}
		@Override
		public void onDayStart(QuantContext ctx) {
		}
		@Override
		public void onMinuteData(QuantContext ctx) {
		}
		
		@Override
		public void onDayFinish(QuantContext ctx) {
			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				
				// 过滤：股票ID集合，当天检查
				boolean bCheckX = false;
				if(cDAStock.ID().compareTo("000955") >= 0 && cDAStock.ID().compareTo("000955") <= 0 
					&& cDAStock.dayKLines().size()>60
					&& cDAStock.dayKLines().lastDate().equals(ctx.date())
					&& cDAStock.circulatedMarketValue() < 1000.0) 
				{	
					bCheckX = true;
				}
				
				if(bCheckX)
				{
					int iEnd = cDAStock.dayKLines().size()-1;
					
					if(ZCZXChecker.check(cDAStock.dayKLines(),iEnd))
					{
						CLog.output("TEST", "PreCheck ZCZX Date:%s ID:%s", ctx.date(), cDAStock.ID());
						
						boolean bcheckVolume = ZCZXChecker.check_volume(cDAStock.dayKLines(),iEnd);
						boolean bDKMidDropChecker = DKMidDropChecker.check(cDAStock.dayKLines(),iEnd);
						
						CLog.output("TEST", "   %b %b", 
								bcheckVolume, bDKMidDropChecker);
						
					}
					
//					boolean bCheck = DayKLineLongLowerShadowChecker.check(cDAStock.dayKLines(), iEnd);
//					if(bCheck)
//					{
//						CLog.output("TEST", "Date:%s ID:%s", ctx.date(), cDAStock.ID());
//					}
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
				"HistoryTest 2010-01-01 2017-11-01", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QSTestStrategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
