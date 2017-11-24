package fasttest;

import java.util.*;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.*;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import pers.di.quantplatform.QuantStrategy;
import utils.PricePosChecker;
import utils.PricePosChecker.ResultLongDropParam;
import utils.SelectResult;
import utils.XBSStockManager;
import utils.ZCZXChecker;
import utils.ZCZXChecker.ResultDYCheck;

public class FastTest {

	public static class FastTestStrategy extends QuantStrategy
	{
		public FastTestStrategy()
		{
		}
		
		@Override
		public void onInit(QuantContext ctx) {
			m_XBSStockManager = new XBSStockManager(ctx.ap());
		}
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "onDayStart %s", ctx.date());
			super.addCurrentDayInterestMinuteDataIDs(m_XBSStockManager.selectList());
			CLog.output("TEST", "%s", m_XBSStockManager.dumpSelect());
			
			for(int i=0; i<super.getCurrentDayInterestMinuteDataIDs().size(); i++)
			{
				CLog.output("TEST", "CurrentDayInterestMinuteDataIDs %s", super.getCurrentDayInterestMinuteDataIDs().get(i));
			}
			
		}
		@Override
		public void onMinuteData(QuantContext ctx) {
			
			// buy
//			for(int iStock=0; iStock<m_XBSStockManager.selectList().size(); iStock++)
//			{
//				String stockID = m_XBSStockManager.selectList().get(iStock);
//				DAStock cDAStock = ctx.pool().get(stockID);
//				
//				{
//					double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
//					double fNowPrice = cDAStock.price();
//					// 跌停不买进
//					double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
//					double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
//					if(0 == Double.compare(fDieTing, fNowPrice))
//					{
//						out_sr.bCreate = false;
//						return;
//					}
//				}
//			}
			
		}
		@Override
		public void onDayFinish(QuantContext ctx) {

			m_XBSStockManager.clearSelect();;

			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				
				// 过滤：股票ID集合，当天检查
				boolean bCheckX = false;
				if(cDAStock.dayKLines().size()>60
						//&&cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 
						&& cDAStock.dayKLines().lastDate().equals(ctx.date())
						&& cDAStock.circulatedMarketValue() < 1000.0) {	
					bCheckX = true;
				}
				
				if(bCheckX)
				{
					// 5天内存在早晨之星
					int iBegin = cDAStock.dayKLines().size()-1-5;
					int iEnd = cDAStock.dayKLines().size()-1;
					for(int i=iEnd;i>=iBegin;i--)
					{
						ResultDYCheck cResultDYCheck = ZCZXChecker.check(cDAStock.dayKLines(),i);
						if(cResultDYCheck.bCheck)
						{
							ResultLongDropParam cResultLongDropParam = PricePosChecker.getLongDropParam(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							m_XBSStockManager.addSelect(cDAStock.ID(), -cResultLongDropParam.refHigh);
						}
					}
				}
			}
			
			m_XBSStockManager.saveValidSelectCount(5);
			
			
			CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.ap().dump(), m_XBSStockManager.dumpSelect());
		}
		
		private XBSStockManager m_XBSStockManager;
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		cAccoutDriver.load("fast_mock001" ,  new MockAccountOpe(), true);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2016-01-01 2017-01-01", 
				cAccoutDriver, 
				new FastTestStrategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
		
		
	}
}
