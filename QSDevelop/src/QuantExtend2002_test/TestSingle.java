package QuantExtend2002_test;

import QuantExtend2002.framework.QEBase2002;
import QuantExtend2002.utils.ExtEigenContinuationTrend;
import QuantExtend2002.utils.ExtEigenCrestTrough;
import QuantExtend2002.utils.ExtEigenMorningCross;
import pers.di.account.AccountController;
import pers.di.common.CListObserver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.localstock.LocalStock;
import pers.di.localstock.common.KLine;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class TestSingle {
	public static String TAG = "TEST";
	
	public static class RunQEStrategyTESTSingle extends QEBase2002 {

		@Override
		public void onStrateInit(QuantContext ctx) {
		}
		
		@Override
		public void onStrateDayStart(QuantContext ctx) {
		}
		
		@Override
		public void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		}

		@Override
		public void onStrateDayFinish(QuantContext ctx) {
			// check one stock
			{
//				DAStock cStock = ctx.pool().get("002991"); 
//				
//				//boolean bCk = ExtEigenMorningCross.check(cStock.dayKLines(), cStock.dayKLines().size()-1);
//				//CLog.output(TAG, "%s price:%.3f %b", cStock.date(), cStock.price(), bCk);
//				
//				if (0 == ctx.date().compareTo("2020-11-06")) 
//				{
//					CLog.output(TAG, "%s price:%.3f", cStock.date(), cStock.price());
//					//ExtEigenCrestTrough.test(cStock.dayKLines(), cStock.dayKLines().size()-1-90, cStock.dayKLines().size()-1);
//					ExtEigenContinuationTrend.test(cStock.dayKLines());
//				}
			}
			// traversal all stock
			{
				for (int iStock = 0; iStock < ctx.pool().size(); iStock++) {
					DAStock cStock = ctx.pool().get(iStock);
					if(!cStock.date().equals(ctx.date())) {
						/* this stock newest dayK not exist at current date, continue! 
						 * CANNOT be selected. */
						continue; 
					}
//					if(!cStock.ID().equals("002531")) {
//						/* this stock newest dayK not exist at current date, continue! 
//						 * CANNOT be selected. */
//						continue; 
//					}
//					CLog.output(TAG, "(%d/%d) %s %s %f", iStock+1, ctx.pool().size(), 
//							cStock.ID(), cStock.date(), cStock.price());

					boolean isMaxUpTrendRecently = ExtEigenContinuationTrend.isMaxUpTrendRecently(cStock.dayKLines(), cStock.dayKLines().size()-1, 10);
					boolean isMaxDownTrendRecently = ExtEigenContinuationTrend.isMaxDownTrendRecently(cStock.dayKLines(), cStock.dayKLines().size()-1, 5);
					if (isMaxUpTrendRecently && isMaxDownTrendRecently) {
						CLog.output(TAG, "isMaxUpTrendRecently isMaxDownTrendRecently %s %s", cStock.ID(), ctx.date());
					}
					
					boolean isMaxUpDownTrendRecently = ExtEigenContinuationTrend.isMaxUpDownTrendRecently(cStock.dayKLines(), cStock.dayKLines().size()-1);
					if (isMaxUpDownTrendRecently) {
						CLog.output(TAG, "isMaxUpDownTrendRecently %s %s", cStock.ID(), ctx.date());
					}
				}
			}
		
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		CLog.output(TAG, "RunQEStrategy2002T1 main begin");
		
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		// "HistoryTest 2019-01-01 2020-02-20" "Realtime"
		Quant.instance().run("HistoryTest 2020-11-02 2020-11-06", cAccountController, new RunQEStrategyTESTSingle()); 
		CLog.output(TAG, "%s", cAccountController.account().dump());
		cAccountController.close();
		
		CSystem.stop();
		CLog.output(TAG, "RunQEStrategy2002T1 main end");
	}
}
