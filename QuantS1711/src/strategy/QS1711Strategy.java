package strategy;

import java.util.*;

import pers.di.account.*;
import pers.di.account.common.*;
import pers.di.common.*;
import pers.di.dataapi.StockDataApi;
import pers.di.dataapi.common.*;
import pers.di.dataapi_test.TestCommonHelper;
import pers.di.dataengine.*;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.*;
import utils.XStockSelectManager;

public class QS1711Strategy {
	
	public static class TestStrategy extends QuantStrategy
	{
		public TestStrategy()
		{
			
		}
		
		@Override
		public void onInit(QuantContext ctx) {
			m_XStockSelectManager = new XStockSelectManager(ctx.ap());
		}
	
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "TestStrategy.onDayStart %s %s", ctx.date(), ctx.time());
			super.addCurrentDayInterestMinuteDataIDs(m_XStockSelectManager.validSelectList(5));
		}
		
		public void onHandleBuy(QuantContext ctx)
		{
			// find want create IDs
			List<String> validSelectList = m_XStockSelectManager.validSelectList(5);
			List<String> cIntentCreateList = new ArrayList<String>();
			for(int i=0; i<validSelectList.size(); i++)
			{
				String stockID = validSelectList.get(i);
				DAStock cDAStock = ctx.pool().get(stockID);

				double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
				double fNowPrice = cDAStock.price();
				double fRatio = (fNowPrice - fYesterdayClosePrice)/fYesterdayClosePrice;
				
//							CLog.output("TEST", "TestStrategy.onMinuteData %s %s [%s %.3f]", 
//									ctx.date(), ctx.time(), stockID, fRatio);
				
				if(fRatio < -0.02)
				{
					cIntentCreateList.add(stockID);
				}
			}
			
			List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
			int iRetHoldStockList = ctx.ap().getHoldStockList(cHoldStockList);
			List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
			int iRetBuyCommissionOrderList =  ctx.ap().getCommissionOrderList(cCommissionOrderList);
			
			// remove already hold
			Iterator<String> it = cIntentCreateList.iterator();
			while(it.hasNext()){
			    String curIntentID = it.next();
			    
			    boolean bExitInHoldOrCommit = false;
			    for(int i=0; i<cHoldStockList.size(); i++)
				{
					if(curIntentID.equals(cHoldStockList.get(i).stockID))
					{
						bExitInHoldOrCommit = true;
					}
				}
			    if(!bExitInHoldOrCommit)
			    {
			    	 for(int i=0; i<cCommissionOrderList.size(); i++)
					{
						if(curIntentID.equals(cCommissionOrderList.get(i).stockID))
						{
							bExitInHoldOrCommit = true;
						}
					}
			    }
			    
			    if(bExitInHoldOrCommit){
			        it.remove();
			    }
			}
			
			// filter
			int create_max_count = 3;
			
			int alreadyCount = 0;
			int buyStockCount = 0;
			if(0 == iRetHoldStockList 
					&& 0 == iRetBuyCommissionOrderList)
			{
				for(int i=0;i<cHoldStockList.size();i++)
				{
					HoldStock cHoldStock = cHoldStockList.get(i);
					if(cHoldStock.totalAmount > 0)
					{
						alreadyCount++;
					}
				}
				for(int i=0;i<cCommissionOrderList.size();i++)
				{
					CommissionOrder cCommissionOrder = cCommissionOrderList.get(i);
					if(cCommissionOrder.tranAct == TRANACT.SELL) 
					{
						continue;
					}
					
					boolean bExitInHold = false;
					for(int j=0;j<cHoldStockList.size();j++)
					{
						HoldStock cHoldStock = cHoldStockList.get(j);
						if(cHoldStock.stockID.equals(cCommissionOrder.stockID))
						{
							bExitInHold = true;
							break;
						}
					}
					if(!bExitInHold)
					{
						alreadyCount++;
					}
				}
				buyStockCount = create_max_count - alreadyCount;
				buyStockCount = Math.min(buyStockCount,cIntentCreateList.size());
			}
			
			// calc buy mount 
			for(int i = 0; i< buyStockCount; i++)
			{
				String createID = cIntentCreateList.get(i);

				// 买入量
				CObjectContainer<Double> totalAssets = new CObjectContainer<Double>();
				int iRetTotalAssets = ctx.ap().getTotalAssets(totalAssets);
				CObjectContainer<Double> money = new CObjectContainer<Double>();
				int iRetMoney = ctx.ap().getMoney(money);
				if(0 == iRetTotalAssets && 0 == iRetMoney)
				{
					double fMaxPositionRatio = 0.3333f;
					Double dMaxPositionMoney = totalAssets.get()*fMaxPositionRatio; // 最大买入仓位钱
					Double dMaxMoney = 10000*100.0; // 最大买入钱
					Double buyMoney = Math.min(dMaxMoney, dMaxPositionMoney);
					buyMoney = Math.min(buyMoney, money.get());
					
					double curPrice = ctx.pool().get(createID).price();
					int amount = (int)(buyMoney/curPrice);
					amount = amount/100*100; // 买入整手化
					ctx.ap().pushBuyOrder(createID, amount, curPrice); // 500 12.330769
				}
				else
				{
					CLog.output("TEST", "getTotalAssets failed\n");
				}
			}
		}
		
		public void onHandleSell(QuantContext ctx)
		{
			List<HoldStock> cHoldStockList = new ArrayList<HoldStock>();
			int iRetHoldStockList = ctx.ap().getHoldStockList(cHoldStockList);
			
			for(int i=0; i<cHoldStockList.size(); i++)
			{
				HoldStock cHoldStock = cHoldStockList.get(i);
				boolean bSell = false;
				DAStock cDAStock = ctx.pool().get(cHoldStock.stockID);
				double curPrice = cDAStock.price();
				
				// 调查天数控制
				int investigationDays = 0;
				while(true)
				{
					String sIndexDate = cDAStock.dayKLines().get(cDAStock.dayKLines().size()-1-investigationDays).date;
					if(cHoldStock.createDate.compareTo(sIndexDate) <= 0)
					{
						investigationDays++;
					}
					else
					{
						break;
					}
				}
				if(investigationDays >= 3) 
				{
					bSell = true;
				}
				
				// 止盈止损卖
				if(cHoldStock.refProfitRatio() > 0.05 || cHoldStock.refProfitRatio() < -0.05) 
				{
					bSell = true;
				}
				
				if(bSell && cHoldStock.availableAmount > 0)
				{
					ctx.ap().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, curPrice); 
				}
			}
		}

		@Override
		public void onMinuteData(QuantContext ctx) {
			//CLog.output("TEST", "TestStrategy.onMinuteData %s %s", ctx.date(), ctx.time());
			onHandleSell(ctx);
			onHandleBuy(ctx);
		}

		@Override
		public void onDayFinish(QuantContext ctx) {
			CLog.output("TEST", "TestStrategy.onDayFinish %s %s", ctx.date(), ctx.time());
			
			m_XStockSelectManager.clearSelect();
			
			// select strategy
			for(int i=0; i<ctx.pool().size(); i++)
			{
				DAStock cDAStock = ctx.pool().get(i);
				
				// stock set 
				if(cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 &&
						cDAStock.dayKLines().lastDate().equals(ctx.date())) {	
					
					DAKLines cDAKLines = cDAStock.dayKLines();
					int iSize = cDAKLines.size();
					if(iSize > 4)
					{
						KLine cStockDayCur = cDAKLines.get(iSize-1);
						KLine cStockDayBefore1 = cDAKLines.get(iSize-2);
						KLine cStockDayBefore2 = cDAKLines.get(iSize-3);

						if(cStockDayCur.close < cStockDayCur.open 
								&& cStockDayCur.close < cStockDayBefore1.close
								&& cStockDayBefore1.close < cStockDayBefore1.open
								&& cStockDayBefore1.close < cStockDayBefore2.close
								)
						{
							m_XStockSelectManager.addSelect(cDAStock.ID(), cStockDayBefore2.close - cStockDayCur.close);
						}
					}
					
				}
			}
			
			CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.ap().dump(), m_XStockSelectManager.dumpSelect());
			
		}
		
		private XStockSelectManager m_XStockSelectManager;
	}
	
	public void run()
	{
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		if(0 != cAccoutDriver.load("mock001" ,  new MockAccountOpe(), true)
				|| 0 != cAccoutDriver.reset(10*10000f))
		{
			CLog.error("TEST", "SampleTestStrategy AccoutDriver ERR!");
		}
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2016-03-01 2016-04-01", 
				cAccoutDriver, 
				new TestStrategy());
		qSession.run();
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "QuantS1711 main");
		
		QS1711Strategy cQS1711Strategy = new QS1711Strategy();
		cQS1711Strategy.run();
		
		CSystem.stop();
	}
}