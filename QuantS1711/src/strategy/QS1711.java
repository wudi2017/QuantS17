package strategy;

import java.util.ArrayList;
import java.util.List;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import pers.di.quantplatform.QuantStrategy;
import utils.PricePosChecker;
import utils.TranDaysChecker;
import utils.TranReportor;
import utils.XStockSelectManager;
import utils.ZCZXChecker;
import utils.PricePosChecker.ResultLongDropParam;
import utils.ZCZXChecker.ResultDYCheck;

public class QS1711 {

	public static class QS1711Strategy extends QuantStrategy
	{
		public QS1711Strategy()
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
				if(cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("0000200") <= 0 
					&& cDAStock.dayKLines().size()>60
					&& cDAStock.dayKLines().lastDate().equals(ctx.date())
					&& cDAStock.circulatedMarketValue() < 1000.0) 
				{	
					bCheckX = true;
				}
				
				if(bCheckX)
				{
					// 5天内存在早晨之星
					int iEnd = cDAStock.dayKLines().size()-1;
					ResultDYCheck cResultDYCheck = ZCZXChecker.check(cDAStock.dayKLines(),iEnd);
					if(cResultDYCheck.bCheck)
					{
						CLog.output("TEST", "Date:%s ID:%s", ctx.date(), cDAStock.ID());
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
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2017-01-01 2017-06-01", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1711Strategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
