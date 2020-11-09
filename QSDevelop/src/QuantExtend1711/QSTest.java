package QuantExtend1711;

import QuantExtend1711.utils.DK1LineCross3Ave;
import QuantExtend1711.utils.DKMidDropChecker;
import QuantExtend1711.utils.DayKLineLongLowerShadowChecker;
import QuantExtend1711.utils.EKGlobalRisk;
import QuantExtend1711.utils.EKRefHistoryPos;
import QuantExtend1711.utils.ETDropStable;
import QuantExtend1711.utils.ZCZXChecker;
import QuantExtend1711.utils.EKRefHistoryPos.EKRefHistoryPosParam;
import QuantExtend1711.utils.ETDropStable.ResultDropStable;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.dataengine.DATimePrices;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;

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
						CLog.debug("TEST", "PreCheck ZCZX Date:%s ID:%s !%.3f !%.3f Succ:%.3f", ctx.date(), cDAStock.ID(), 
								cEKRefHistoryPosParam.refHigh, cEKRefHistoryPosParam.refLow, succRate);
						m_selectID = cDAStock.ID();
						
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
		cAccountController.open("fast_mock001" ,  true);
		cAccountController.reset(100000);
		IAccount acc = cAccountController.account();
		
		Quant.instance().run(
				"HistoryTest 2010-01-01 2017-06-05", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccountController, 
				new QSTestStrategy());
		cAccountController.close();
		
		CLog.debug("TEST", "FastTest main end");
		CSystem.stop();
	}
}
