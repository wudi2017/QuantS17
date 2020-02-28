package strategy.QS1711;

import java.util.*;

import pers.di.account.*;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.localstock.*;
import pers.di.localstock.common.*;
import pers.di.common.*;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;
import utils.QS1711.TranDaysChecker;
import utils.QS1711.TranReportor;
import utils.QS1711.XStockSelectManager;
import utils.QS1711.ZCZXChecker;
import utils.QS1711.base.EKRefHistoryPos;
import utils.QS1711.base.EKRefHistoryPos.EKRefHistoryPosParam;

/*
 * ѡ�ɲ��ԣ�
 *   @1000����ͨ������
 *   @���5̫���ڴ��ڣ�3��K������糿֮��
 *   @����2�������������򣬵�����Ŀ�ǰ
 *   
 * �������:
 *   @ѡ����ǰ10�����м��
 *   @��ͣ�����
 *   @�����Ƿ������������������糿֮���е�8�������ϣ�
 *   @������Ϊ2�ɲ�λ�����ֹɲ�����5ֻ
 *   
 * �������ԣ�
 *   @��ͣ������
 *   @�ֹɳ���30������
 *   @ֹӯ10����������ֹ��12��������
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
			m_XStockSelectManager = new XStockSelectManager(ctx.accountProxy());
			m_TranReportor = new TranReportor("R");
		}
		@Override
		public void onUnInit(QuantContext ctx) {
		}
		@Override
		public void onDayStart(QuantContext ctx) {
			CLog.output("TEST", "onDayStart %s", ctx.date());
			m_XStockSelectManager.loadFromFile();
			ctx.addCurrentDayInterestMinuteDataIDs(m_XStockSelectManager.validSelectListS1(30));
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
					// 1-��ͣ�����
					double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
					double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
					if(0 == Double.compare(fDieTing, fNowPrice))
					{
						bBuyFlag = false;
						break;
					}
					
					// 2-�����Ƿ��������������ѡ�������������������
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
					
					
					// ������
					bBuyFlag = true;
					
				} while(false);
				
				if(bBuyFlag)
				{
					List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
					ctx.accountProxy().getHoldStockList(ctnHoldStockList);
					if(ctnHoldStockList.size() < 5)
					{
						CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
						ctx.accountProxy().getTotalAssets(ctnTotalAssets);
						CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
						ctx.accountProxy().getMoney(ctnMoney);
						double dCreateMoney = (ctnMoney.get() > ctnTotalAssets.get()/5)?ctnTotalAssets.get()/5:ctnMoney.get();
						int iCreateAmount = (int) (dCreateMoney/fNowPrice)/100*100;
						if(iCreateAmount > 0)
						{
							ctx.accountProxy().pushBuyOrder(stockID, iCreateAmount, fNowPrice);
						}
					}
				}
			}
		}
		
		public void onSellCheck(QuantContext ctx)
		{
			List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
			ctx.accountProxy().getHoldStockList(ctnHoldStockList);
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
					
					// ��ͣ������
					double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
					double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
					if(0 == Double.compare(fZhangTing, fNowPrice))
					{
						bSellFlag = false;
						break;
					}
					
					// �ֹɳ�ʱ����
					long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
					if(lHoldDays >= 30) 
					{
						bSellFlag = true;
						break;
					}
					
					// ֹӯֹ������
					if(cHoldStock.refProfitRatio() > 0.1 || cHoldStock.refProfitRatio() < -0.12) 
					{
						bSellFlag = true;
						break;
					}
				} while(false);
					
				if(bSellFlag)
				{
					ctx.accountProxy().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
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
				
				// ���ˣ���ƱID���ϣ�������
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
					// 5���ڴ����糿֮��
					int iBegin = cDAStock.dayKLines().size()-1-5;
					int iEnd = cDAStock.dayKLines().size()-1;
					for(int i=iEnd;i>=iBegin;i--)
					{
						if(ZCZXChecker.check(cDAStock.dayKLines(),i))
						{
							EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							m_XStockSelectManager.addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
						}
					}
				}
			}
			m_XStockSelectManager.saveToFile();
			
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
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		IAccount acc = cAccountController.account();
		
		Quant.instance().run(
				"HistoryTest 2010-01-01 2017-11-25", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccountController, 
				new QS1605RealStrategy());
		
		cAccountController.close();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
