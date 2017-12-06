package strategy;

import java.util.*;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.account.common.HoldStock;
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
import utils.PricePosChecker.ResultDropParam;
import utils.TranDaysChecker;
import utils.TranReportor;
import utils.XStockSelectManager;
import utils.ZCZXChecker;

/*
 * 选股策略：
 *   @1000亿流通盘以内
 *   @最近5太难内存在，3日K线组合早晨之星
 *   @按照2年内最大跌幅排序，跌幅大的靠前
 *   
 * 买入策略:
 *   @选出的前10个进行检查
 *   @跌停不买进
 *   @近期涨幅过大不买进（高于最近早晨之星中点8个点以上）
 *   @买入量为2成仓位，最大持股不超过5只
 *   
 * 卖出策略：
 *   @涨停不卖出
 *   @持股超过30天卖出
 *   @止盈10个点卖出，止损12个点卖出
 * 
 */
public class QS1605Real {

	public static class QS1605RealStrategy extends QuantStrategy
	{
		public QS1605RealStrategy()
		{
		}
		
		@Override
		public void onInit(QuantContext ctx) {
			m_XStockSelectManager = new XStockSelectManager(ctx.ap());
			m_TranReportor = new TranReportor("R");
		}
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "onDayStart %s", ctx.date());
			m_XStockSelectManager.loadFromFile();
			super.addCurrentDayInterestMinuteDataIDs(m_XStockSelectManager.validSelectListS1(30));
			CLog.output("TEST", "%s", m_XStockSelectManager.dumpSelect());
		}
		
		public void onBuyCheck(QuantContext ctx)
		{
			List<String> validSelectList = m_XStockSelectManager.validSelectListS1(10);
			for(int iStock=0; iStock<validSelectList.size(); iStock++)
			{
				String stockID = validSelectList.get(iStock);
				
				DAStock cDAStock = ctx.pool().get(stockID);
				double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
				double fNowPrice = cDAStock.price();
				
				boolean bBuyFlag = false;
				do
				{
					// 1-跌停不买进
					double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
					double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
					if(0 == Double.compare(fDieTing, fNowPrice))
					{
						bBuyFlag = false;
						break;
					}
					
					// 2-近期涨幅过大不买进（根据选股条件计算买入参数）
					boolean bCheckFlg = false;
					int iZCZXFindEnd = -1;
					DAKLines list = cDAStock.dayKLines();
					int iCheck = list.size()-2;
					
					int iBegin = iCheck-5;
					int iEnd = iCheck;
					
					for(int i=iEnd;i>=iBegin;i--)
					{
						if(ZCZXChecker.check(list,i))
						{
							bCheckFlg = true;
							iZCZXFindEnd = i;
							break;
						}
					}
					
					if(bCheckFlg && -1!=iZCZXFindEnd)
					{
						KLine cKLineZCZXEnd = cDAStock.dayKLines().get(iZCZXFindEnd);
						double fStdPaZCZX = (cKLineZCZXEnd.entityHigh() + cKLineZCZXEnd.entityLow())/2;
						double fZhang = (fNowPrice-fStdPaZCZX)/fStdPaZCZX;
						if(fZhang > 0.08)
						{
							bBuyFlag = false;
							break;
						}
					}
					else
					{
						break;
					}
					
					
					// 买入标记
					bBuyFlag = true;
					
				} while(false);
				
				if(bBuyFlag)
				{
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
			}
		}
		
		public void onSellCheck(QuantContext ctx)
		{
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
					
					// 持股超时卖出
					long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
					if(lHoldDays >= 30) 
					{
						bSellFlag = true;
						break;
					}
					
					// 止盈止损卖出
					if(cHoldStock.refProfitRatio() > 0.1 || cHoldStock.refProfitRatio() < -0.12) 
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
		public void onMinuteData(QuantContext ctx) {
			onBuyCheck(ctx);
			onSellCheck(ctx);
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
					//cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 
					cDAStock.dayKLines().size()>60
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
						if(ZCZXChecker.check(cDAStock.dayKLines(),i))
						{
							ResultDropParam cResultLongDropParam = PricePosChecker.getLongDropParam(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							m_XStockSelectManager.addSelect(cDAStock.ID(), -cResultLongDropParam.refHigh);
						}
					}
				}
			}
			m_XStockSelectManager.saveToFile();
			
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
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2010-01-01 2017-11-25", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1605RealStrategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
