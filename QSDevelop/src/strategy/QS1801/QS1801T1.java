package strategy.QS1801;

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
import pers.di.dataengine.DATimePrices;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import utils.QS1711.DayKLinePriceWaveChecker;
import utils.QS1711.ETDropStable;
import utils.QS1711.ETDropStable.ResultDropStable;
import utils.QS1711.ZCZXChecker;
import utils.QS1711.base.EKRefHistoryPos;
import utils.QS1711.base.EKRefHistoryPos.EKRefHistoryPosParam;
import utils.QS1801.QUCommon;

public class QS1801T1 extends QS1801Base {

	public QS1801T1() {
	}

	@Override
	void onStrateInit(QuantContext ctx) {
		super.setGlobalMaxHoldStockCount(5); 
		super.setGlobalStockMaxHoldPosstion(0.2);
		super.setGlobalStockOneCommitPossition(1); 
		super.setGlobalStockMinCommitInterval(30);
		super.setGlobalStockMaxHoldDays(20);
		super.setGlobalStockTargetProfitRatio(0.1);
		super.setGlobalStockStopLossRatio(-0.12);
	}

	@Override
	void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		String stockID = cDAStock.ID();
		DATimePrices cDATimePrices = cDAStock.timePrices();
		double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
		double fNowPrice = cDAStock.price();
		
		if(ctx.date().equals("2016-03-04") && ctx.time().equals("13:20:00"))
		{
			CLog.output("TEST", "");
		}
		
		// buy signal
		do
		{
			// 跌停不买进
			double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
			if(Double.compare(fDieTing, fNowPrice) >= 0)
			{
				break;
			}
			
			// 涨幅相比选股时标准参考价高 不买进
			Double dStdPaZCZX = super.getPrivateStockPropertyDouble(stockID, "dStdPaZCZX");
			if(null == dStdPaZCZX || dStdPaZCZX <= 0)
			{
				break;
			}
			double fZhang = (fNowPrice-dStdPaZCZX)/dStdPaZCZX;
			if(fZhang > 0)
			{
				break;
			}
			
			// 出现分时急跌
			double dWave = DayKLinePriceWaveChecker.check(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
			ResultDropStable cResultDropStable = ETDropStable.checkDropStable(cDATimePrices, cDATimePrices.size()-1, dWave);
			if(!cResultDropStable.bCheck)
			{
				break;
			}
			
			super.buySignalEmit(ctx, stockID);
			break;
		} while(true);
		
		// default process
		super.onAutoForceClearProcess(ctx, cDAStock);
	}

	@Override
	void onStrateDayFinish(QuantContext ctx) {
	
		// 过滤：股票ID集合，基本信息
		for(int iStock=0; iStock<ctx.pool().size(); iStock++)
		{
			DAStock cDAStock = ctx.pool().get(iStock);
			if(
				//cDAStock.ID().compareTo("000921") >= 0 && cDAStock.ID().compareTo("000921") <= 0  &&
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
						
						KLine cKLineCur = cDAStock.dayKLines().get(i);
						double dStdPaZCZX = (cKLineCur.entityHigh() + cKLineCur.entityLow())/2;
						super.setPrivateStockPropertyDouble(stockID, "dStdPaZCZX", dStdPaZCZX);
					}
				}
			}
		}
		
		// 保留10
		super.selectKeepMaxCount(10);
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
				"HistoryTest 2010-01-01 2018-01-20", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1801T1());
		//qSession.resetDataRoot("C:\\D\\MyProg\\QuantS17Release\\rw\\data");
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}

}
