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
import pers.di.dataapi.common.KLine;
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
import utils.PricePosChecker.ResultDropParam;

/*
 * 当出现早晨之星直接尾盘冲进，第二天必须卖出
 */
public class QS1711T1 {
	public static class QS1711Strategy extends QuantStrategy
	{
		public QS1711Strategy()
		{
		}
		
		
		public void doBuy(QuantContext ctx, String stockID)
		{
			DAStock cDAStock = ctx.pool().get(stockID);
			double fNowPrice = cDAStock.price();
			
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.ap().getHoldStockList(ctnHoldStockList);
			if(ctnHoldStockList.size() < 5)
			{
				CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
				ctx.ap().getTotalAssets(ctnTotalAssets);
				CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
				ctx.ap().getMoney(ctnMoney);
				double dCreateMoney = (ctnMoney.get() > ctnTotalAssets.get()/5)?ctnTotalAssets.get()/5:ctnMoney.get();
				int iCreateAmount = (int) (dCreateMoney/fNowPrice)/100*100;
				if(iCreateAmount > 0)
				{
					ctx.ap().pushBuyOrder(stockID, iCreateAmount, fNowPrice);
				}
			}
		}
		
		@Override
		public void onInit(QuantContext ctx) {
			m_XStockSelectManager = new XStockSelectManager(ctx.ap());
			m_TranReportor = new TranReportor("R");
		}
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "onDayStart %s", ctx.date());
		}
		
		@Override
		public void onMinuteData(QuantContext ctx) {
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.ap().getHoldStockList(ctnHoldStockList);
			for(int i=0; i<ctnHoldStockList.size(); i++)
			{
				HoldStock cHoldStock = ctnHoldStockList.get(i);

				DAStock cDAStock = ctx.pool().get(cHoldStock.stockID);
				double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
				double fNowPrice = cDAStock.price();
				
				boolean bSellFlag = false;
				do
				{
					if(cHoldStock.availableAmount <= 0)
					{
						bSellFlag = false;
						break;
					}
					
					// 涨停不卖出
					double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
					double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
					if(0 == Double.compare(fZhangTing, fNowPrice))
					{
						bSellFlag = false;
						break;
					}
					
					// 当天结束必须卖出
					if(ctx.time().compareTo("14:54:00") >= 0)
					{
						bSellFlag = true;
						break;
					}
					
					// 止盈止损卖出
					if(cHoldStock.refProfitRatio() > 0.01 || cHoldStock.refProfitRatio() < -0.02) 
					{
						bSellFlag = true;
						break;
					}
				} while(false);
					
				if(bSellFlag)
				{
					ctx.ap().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
				}	
			}
		}
		
		@Override
		public void onDayFinish(QuantContext ctx) {

			m_XStockSelectManager.clearSelect();

			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				
				// 过滤：股票ID集合，当天检查
				boolean bCheckX = false;
				if(
					//cDAStock.ID().compareTo("000040") == 0 &&
					cDAStock.dayKLines().size()>60
					&& cDAStock.dayKLines().lastDate().equals(ctx.date())
					&& cDAStock.circulatedMarketValue() < 1000.0) {	
					bCheckX = true;
				}
				
				if(bCheckX)
				{
					// 早晨之星
					int iEnd = cDAStock.dayKLines().size()-1;
					if(ZCZXChecker.check(cDAStock.dayKLines(),iEnd))
					{
						boolean bcheckVolume = ZCZXChecker.check_volume(cDAStock.dayKLines(),iEnd);
						if(bcheckVolume)
						{
							ResultDropParam cResultLongDropParam = PricePosChecker.getLongDropParam(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							m_XStockSelectManager.addSelect(cDAStock.ID(), -cResultLongDropParam.refHigh);
						}
					}
					
				}
			}
	
			List<String> validSelectList = m_XStockSelectManager.validSelectListS1(10);
			for(int iStock=0; iStock<validSelectList.size(); iStock++)
			{
				String stockID = validSelectList.get(iStock);
				
				DAStock cDAStock = ctx.pool().get(stockID);
				double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
				double fNowPrice = cDAStock.price();
				
				doBuy(ctx, stockID);
			}
			
			// report
			CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
			ctx.ap().getTotalAssets(ctnTotalAssets);
			double dSH = ctx.pool().get("999999").price();
			m_TranReportor.InfoCollector(ctx.date(), ctnTotalAssets.get(), dSH);
			CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.ap().dump(), m_XStockSelectManager.dumpSelect());
		}
		
		private XStockSelectManager m_XStockSelectManager;
		private TranReportor m_TranReportor;
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
				"HistoryTest 2010-01-01 2017-11-25", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1711Strategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
