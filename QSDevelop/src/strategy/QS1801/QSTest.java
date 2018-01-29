package strategy.QS1801;

import java.util.List;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import utils.QS1711.ZCZXChecker;
import utils.QS1711.base.EKRefHistoryPos;
import utils.QS1711.base.EKRefHistoryPos.EKRefHistoryPosParam;

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
	void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
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
				//cDAStock.ID().compareTo("000060") >= 0 && cDAStock.ID().compareTo("000060") <= 0  &&
				cDAStock.dayKLines().size() >= 60
				&& cDAStock.dayKLines().lastDate().equals(ctx.date())
				&& cDAStock.circulatedMarketValue() <= 1000.0) {
				
				String stockID = cDAStock.ID();
				
				super.selectAdd(stockID, 0);
			}
		}
		
		// 过滤：早晨之星
		List<String> listSelect = super.selectList();
		super.selectClear();
		for(int iStock=0; iStock<listSelect.size(); iStock++)
		{
			String stockID = listSelect.get(iStock);
			DAStock cDAStock = ctx.pool().get(stockID);
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
						super.selectAdd(stockID, -cEKRefHistoryPosParam.refHigh);
						super.setPrivateStockPropertyLong(stockID, "ZCZX_EndIndex", i);
					}
				}
			}
		}
		
		// 保留10
		super.selectKeepMaxCount(10);
		
		super.selectDump();
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
				new QSTest());
		qSession.resetDataRoot("C:\\D\\MyProg\\QuantS17Release\\rw\\data");
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}

}
