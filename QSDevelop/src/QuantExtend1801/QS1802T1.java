package QuantExtend1801;

import java.util.List;

import QuantExtend1711.utils.DayKLinePriceWaveChecker;
import QuantExtend1711.utils.EKRefHistoryPos;
import QuantExtend1711.utils.ETDropStable;
import QuantExtend1711.utils.ZCZXChecker;
import QuantExtend1711.utils.EKRefHistoryPos.EKRefHistoryPosParam;
import QuantExtend1711.utils.ETDropStable.ResultDropStable;
import QuantExtend1801.utils.QS1802Base;
import QuantExtend1801.utils.QUCommon;
import QuantExtend1801.utils.QUSelectTable.SelectItem;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.dataengine.DATimePrices;
import pers.di.localstock.common.KLine;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class QS1802T1 extends QS1802Base {
	
	public QS1802T1(boolean bAutoSelect2Monitor, boolean bHelpPane) {
		super();
		DefaultConfig cfg = super.getDefaultConfig();
		cfg.GlobalDefaultAutoMoveSelectToMonitor = bAutoSelect2Monitor;
		cfg.GlobalDefaultShowHelpPanel = bHelpPane;
		super.setDefaultConfig(cfg);
	}

	@Override
	protected void onStrateInit(QuantContext ctx) {
	}

	@Override
	protected void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	protected void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		String stockID = cDAStock.ID();
		DATimePrices cDATimePrices = cDAStock.timePrices();
		double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
		double fNowPrice = cDAStock.price();
		
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
			SelectItem cSelectItem = super.QUSelectTable().item(stockID);
			if(null == cSelectItem)
			{
				break;
			}
			String sStdPaZCZX = cSelectItem.getProperty("dStdPaZCZX");
			Double dStdPaZCZX = Double.parseDouble(sStdPaZCZX);
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
	}

	@Override
	protected void onStrateDayFinish(QuantContext ctx) {
		// 清除选股表
		super.QUSelectTable().clearAllItem();
		
		// 添加选入项目
		for(int iStock=0; iStock<ctx.pool().size(); iStock++)
		{
			DAStock cDAStock = ctx.pool().get(iStock);
			if(
				cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0  &&
				cDAStock.dayKLines().size() >= 60
				&& cDAStock.dayKLines().lastDate().equals(ctx.date())
				&& cDAStock.circulatedMarketValue() <= 1000.0) {
				
				String stockID = cDAStock.ID();
				super.QUSelectTable().addItem(stockID);
			}
		}
		
		// 过滤：早晨之星
		List<String> listSelect = super.QUSelectTable().selectStockIDs();
		for(int iStock=0; iStock<listSelect.size(); iStock++)
		{
			String stockID = listSelect.get(iStock);
			DAStock cDAStock = ctx.pool().get(stockID);
			
			SelectItem cSelectItem = super.QUSelectTable().item(stockID);
			boolean bSelectFlag = false;
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
						cSelectItem.setPriority(-cEKRefHistoryPosParam.refHigh);
						KLine cKLineCur = cDAStock.dayKLines().get(i);
						double dStdPaZCZX = (cKLineCur.entityHigh() + cKLineCur.entityLow())/2;
						String sStdPaZCZX = String.format("%.3f", dStdPaZCZX);
						cSelectItem.setProperty("dStdPaZCZX", sStdPaZCZX);
						bSelectFlag = true;
					}
				}
			}
			
			if(!bSelectFlag)
			{
				super.QUSelectTable().removeItem(stockID);
			}
		}
		
		// 保留选入10
		super.QUSelectTable().selectKeepMaxCount(10);
	}
	
	/*
	 * *************************************************************************************
	 */
	public static void main(String[] args) throws Exception {
		
		CSystem.start();
		
		CLog.debug("TEST", "FastTest main begin");
		
		// create testaccount
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("account_QS1801T1", true);
		//cAccountController.reset(100000);
		IAccount acc = cAccountController.account();
		
		Quant.instance().run(
				"Realtime", // Realtime | HistoryTest 2016-01-01 2016-03-01
				cAccountController, 
				new QS1802T1(true, true)); // bAutoSelect2Monitor, bHelpPane
		//qSession.resetDataRoot("C:\\D\\MyProg\\QuantS17Release\\rw\\data");
		cAccountController.close();
		
		CLog.debug("TEST", "FastTest main end");
		CSystem.stop();
	}
}
