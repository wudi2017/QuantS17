package fasttest;

import java.util.*;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataapi.common.*;
import pers.di.common.*;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import pers.di.quantplatform.QuantStrategy;
import utils.PricePosChecker;
import utils.PricePosChecker.ResultLongDropParam;
import utils.XBuyFilter;
import utils.XSelectFilter;
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
			m_XSelectFilter = new XSelectFilter(ctx.ap());
			m_XBuyFilter = new XBuyFilter(ctx.ap());
		}
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "onDayStart %s", ctx.date());
			super.addCurrentDayInterestMinuteDataIDs(m_XSelectFilter.selectList());
			CLog.output("TEST", "%s", m_XSelectFilter.dumpSelect());
		}
		@Override
		public void onMinuteData(QuantContext ctx) {
			
			// buy
			m_XBuyFilter.clearBuy();
			for(int iStock=0; iStock<m_XSelectFilter.selectList().size(); iStock++)
			{
				String stockID = m_XSelectFilter.selectList().get(iStock);
				DAStock cDAStock = ctx.pool().get(stockID);
				
				{
					double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
					double fNowPrice = cDAStock.price();
					
					// 跌停不买进
					double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
					double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
					if(0 == Double.compare(fDieTing, fNowPrice))
					{
						continue;
					}
					
					// 计算买入参数
					boolean bCheckFlg = false;
					double fStarHigh = 0.0;
					double fStarLow = 0.0;
					DAKLines list = cDAStock.dayKLines();
					int iCheck = list.size()-2;
					
					int iBegin = iCheck-5;
					int iEnd = iCheck;
					
					for(int i=iEnd;i>=iBegin;i--)
					{
						ResultDYCheck cResultDYCheck = ZCZXChecker.check(list,i);
						if(cResultDYCheck.bCheck)
						{
							bCheckFlg = true;
							fStarHigh = cResultDYCheck.fStarHigh;
							fStarLow = cResultDYCheck.fStarLow;
							break;
						}
					}
					
					// 近期涨幅过大不买进
					if(bCheckFlg)
					{
						double fStdPaZCZX = (fStarHigh + fStarLow)/2;
						double fZhang = (fNowPrice-fStdPaZCZX)/fStdPaZCZX;
						if(fZhang > 0.08)
						{
							continue;
						}
					}
					
					// 添加建仓项
					m_XBuyFilter.addBuy(stockID, 0);
				}
			}
			
			if(ctx.time().equals("15:00:00") && m_XBuyFilter.buyList().size() > 0)
			{
				CLog.output("TEST", "dump buy\n %s\n", m_XBuyFilter.dumpBuy());
			}
		}
		@Override
		public void onDayFinish(QuantContext ctx) {

			m_XSelectFilter.clearSelect();;

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
							m_XSelectFilter.addSelect(cDAStock.ID(), -cResultLongDropParam.refHigh);
						}
					}
				}
			}
			
			m_XSelectFilter.saveValidSelectCount(5);
			
			
			CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.ap().dump(), m_XSelectFilter.dumpSelect());
		}
		
		private XSelectFilter m_XSelectFilter;
		private XBuyFilter m_XBuyFilter;
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
