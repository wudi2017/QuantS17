package QuantExtend2002;

import QuantExtend2002.framework.QEBase2002;
import QuantExtend2002.utils.ExtEigenMorningCross;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class RunQEStrategy2002T1 extends QEBase2002 {
	public static String TAG = "TEST";
	@Override
	public void onStrateInit(QuantContext ctx) {
		this.property().setGlobalStockMaxCount(2L);
		this.property().setGlobalHoldOneStockMaxMarketValue(8*10000.0);
		this.property().setGlobalBuyOneStockCommitMaxMarketValue(5*10000.0);
		this.property().setGlobalStockMinCommitInterval(60L);
		this.property().setGlobalStockMaxHoldDays(30L);
		this.property().setGlobalStockStopLossRatio(-0.05);
		this.property().setGlobalStockTargetProfitRatio(0.05);
	}

	@Override
	public void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	public void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		this.transactionController().buySignalEmit(ctx, cDAStock.ID());
	}

	@Override
	public void onStrateDayFinish(QuantContext ctx) {
		this.selector().setMaxCount(1);
//		// test
//		if(ctx.date().equals("2019-01-21")) {
//			DAStock cStock = ctx.pool().get("600085");
//			boolean bZCZX = ExtEigenMorningCross.check(cStock.dayKLines(), cStock.dayKLines().size()-1);
//			if(bZCZX) {
//				CLog.output(TAG, "onStrateDayFinish %s ZCZX selector:%s", ctx.date(), cStock.ID());
//				
//				double scole = ExtEigenMorningCross.scoreCalc30DayLevel(cStock.dayKLines(), cStock.dayKLines().size()-1);
//				CLog.output(TAG, "scoreCalc30DayLevel:%f", scole);
//			}
//		}
//		//test
		
		for (int i = 0; i < ctx.pool().size(); i++) {
			DAStock cStock = ctx.pool().get(i);
			
			boolean bZCZX = ExtEigenMorningCross.check(cStock.dayKLines(), cStock.dayKLines().size()-1);
			if(bZCZX) {
				double scole30DayLevel = ExtEigenMorningCross.scoreCalc30DayLevel(cStock.dayKLines(), cStock.dayKLines().size()-1);
				double scole10CrossDayLevel = ExtEigenMorningCross.scoreCalc10CrossDayLevel(cStock.dayKLines(), cStock.dayKLines().size()-1);
				double scoleCrossStandard = ExtEigenMorningCross.scoreCalcCrossStandard(cStock.dayKLines(), cStock.dayKLines().size()-1);
				double scoreBeginEndStandard = ExtEigenMorningCross.scoreBeginEndStandard(cStock.dayKLines(), cStock.dayKLines().size()-1);
				double scoreCrossDownRefBeginEnd = ExtEigenMorningCross.scoreCrossDownRefBeginEnd(cStock.dayKLines(), cStock.dayKLines().size()-1);
				CLog.output(TAG, "onStrateDayFinish %s ZCZX selector:%s 30DayL:%.2f 10CroDayL:%.2f CroStd:%.2f BEStd:%.2f CroDown:%.2f", ctx.date(), cStock.ID(), 
						scole30DayLevel, scole10CrossDayLevel, scoleCrossStandard, scoreBeginEndStandard, scoreCrossDownRefBeginEnd);
				
				//this.selector().add(cStock.ID(), 0);
			}
		}
//		CLog.output(TAG, ctx.accountProxy().dump());
	}

	public static void main(String[] args) throws Exception {
		CSystem.start();
		CLog.output(TAG, "RunQEStrategy2002T1 main begin");
		
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		// "HistoryTest 2019-01-01 2020-02-20" "Realtime"
		Quant.instance().run("HistoryTest 2019-01-21 2020-02-20", cAccountController, new RunQEStrategy2002T1()); 
		CLog.output(TAG, "%s", cAccountController.account().dump());
		cAccountController.close();
		
		CSystem.stop();
		CLog.output(TAG, "RunQEStrategy2002T1 main end");
	}
}
