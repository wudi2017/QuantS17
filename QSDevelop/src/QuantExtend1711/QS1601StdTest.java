package QuantExtend1711;

import java.util.*;

import QuantExtend1711.utils.TranReportor;
import QuantExtend1711.utils.XStockSelectManager;
import pers.di.account.*;
import pers.di.account.common.*;
import pers.di.common.*;
import pers.di.dataengine.*;
import pers.di.localstock.common.KLine;
import pers.di.quantplatform.*;
import pers.di.quantplatform_test.TestQuantSession_Simple.TestStrategy;
import pers.di.account.AccountController;

public class QS1601StdTest {
	
	public static class QS1601StdTestStrategy extends QuantStrategy
	{
		public QS1601StdTestStrategy()
		{
			
		}
		
		@Override
		public void onInit(QuantContext ctx) {
			m_XStockSelectManager = new XStockSelectManager(ctx.accountProxy());
			m_TranReportor = new TranReportor(this.getClass().getSimpleName());
		}
		
		@Override
		public void onUnInit(QuantContext ctx) {
		}
	
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "TestStrategy.onDayStart %s %s", ctx.date(), ctx.time());
			ctx.addCurrentDayInterestMinuteDataIDs(m_XStockSelectManager.validSelectListS2(3));
		}
		
		public void onHandleBuy(QuantContext ctx)
		{
			// find want create IDs
			List<String> validSelectList = m_XStockSelectManager.validSelectListS2(3);
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
			int iRetHoldStockList = ctx.accountProxy().getHoldStockList(cHoldStockList);
			List<CommissionOrder> cCommissionOrderList = new ArrayList<CommissionOrder>();
			int iRetBuyCommissionOrderList =  ctx.accountProxy().getCommissionOrderList(cCommissionOrderList);
			
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
				int iRetTotalAssets = ctx.accountProxy().getTotalAssets(totalAssets);
				CObjectContainer<Double> money = new CObjectContainer<Double>();
				int iRetMoney = ctx.accountProxy().getMoney(money);
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
					ctx.accountProxy().pushBuyOrder(createID, amount, curPrice); // 500 12.330769
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
			int iRetHoldStockList = ctx.accountProxy().getHoldStockList(cHoldStockList);
			
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
					ctx.accountProxy().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, curPrice); 
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
			
			CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.accountProxy().dump(), m_XStockSelectManager.dumpSelect());
			
			// report
			CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
			ctx.accountProxy().getTotalAssets(ctnTotalAssets);
			double dSH = ctx.pool().get("999999").price();
			m_TranReportor.collectInfo_SHComposite(ctx.date(), dSH);
			m_TranReportor.collectInfo_TotalAssets(ctx.date(), ctnTotalAssets.get());
			m_TranReportor.generateReport();
			CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.accountProxy().dump(), m_XStockSelectManager.dumpSelect());
		
		}
		
		private XStockSelectManager m_XStockSelectManager;
		private TranReportor m_TranReportor;
	}
	
	public void run()
	{
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		if(0 != cAccountController.open("mock001", true)
				|| 0 != cAccountController.reset(10*10000f))
		{
			CLog.error("TEST", "SampleTestStrategy AccountController ERR!");
		}
		IAccount acc = cAccountController.account();
		
		Quant.instance().run("HistoryTest 2016-03-01 2016-04-01", cAccountController, new QS1601StdTestStrategy());
		cAccountController.close();
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "QuantS1711 main");
		
		QS1601StdTest cQS1601StdTest = new QS1601StdTest();
		cQS1601StdTest.run();
		
		CSystem.stop();
	}
}