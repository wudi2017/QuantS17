package strategy.QS1801;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;

public class QSTest extends QS1801Base {
	public QSTest() {
	}

	@Override
	void onStrateInit(QuantContext ctx) {
		super.setGlobalMaxHoldStockCount(2);
		super.setGlobalStockMaxHoldPosstion(0.3);
		super.setGlobalStockOneCommitPossition(0.5);
		super.setGlobalStockMinCommitInterval(10);
		super.setGlobalStockMaxHoldDays(20);
		super.setGlobalStockTargetProfitRatio(0.1);
		super.setGlobalStockStopLossRatio(0.12);
	}

	@Override
	void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	void onStrateBuySellCheck(QuantContext ctx, DAStock cDAStock) {
//		String stockID = cDAStock.ID();
//		double curPrice = cDAStock.price();
//		double lastClose = super.getStockPropertyDouble(cDAStock.ID(), "lastClose");
//		if((curPrice-lastClose)/lastClose < -0.02)
//		{
//			super.buySignalEmit(ctx, stockID);
//		}
//		if((curPrice-lastClose)/lastClose > 0.02)
//		{
//			super.sellSignalEmit(ctx, stockID);
//		}
//		// default process
//		super.onAutoForceClearProcess(ctx, cDAStock);
	}

	@Override
	void onStrateDayFinish(QuantContext ctx) {
	
		// 过滤：股票ID集合，基本信息
		for(int iStock=0; iStock<ctx.pool().size(); iStock++)
		{
			DAStock cDAStock = ctx.pool().get(iStock);
			if(
				cDAStock.ID().compareTo("000060") >= 0 && cDAStock.ID().compareTo("000060") <= 0  
				&& cDAStock.dayKLines().size() >= 60
				&& cDAStock.dayKLines().lastDate().equals(ctx.date())
				&& cDAStock.circulatedMarketValue() <= 1000.0) {
				
				String stockID = cDAStock.ID();
				
				super.selectAdd(stockID, 0);
				super.setStockPropertyDouble(cDAStock.ID(), "lastClose", cDAStock.price());
				super.setStockPropertyString(cDAStock.ID(), "date", ctx.date());
			}
		}
	}
	
	/*
	 * *************************************************************************************
	 */
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		cAccoutDriver.load("account_QS1801T1" ,  new MockAccountOpe(), true);
		cAccoutDriver.reset(100000);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2016-03-01 2016-04-01", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1801T1());
		qSession.resetDataRoot("C:\\D\\MyProg\\QuantS17Release\\rw\\data");
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}

}
