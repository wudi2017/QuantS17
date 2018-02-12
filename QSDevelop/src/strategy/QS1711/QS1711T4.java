package strategy.QS1711;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import utils.QS1711.DayKLinePriceWaveChecker;
import utils.QS1711.ETDropStable;
import utils.QS1711.TranDaysChecker;
import utils.QS1711.ZCZXChecker;
import utils.QS1711.base.EKAvePrice;
import utils.QS1711.base.EKRefHistoryPos;
import utils.QS1711.base.EKRefHistoryPos.EKRefHistoryPosParam;
import utils.QS1711.ETDropStable.ResultDropStable;

/*
 * �糿֮��ѡ��
 * ��ʱ�߼�������
 */
public class QS1711T4 {
	public static class QS1711T4Strategy extends QS1711SBase
	{
		public QS1711T4Strategy()
		{
			super(20, 5); // maxSelect maxHold
		}

		@Override
		void onStrateInit(QuantContext ctx)
		{
		}
		
		@Override
		void onStrateDayStart(QuantContext ctx)
		{
		}
		
		@Override
		void onStrateBuyCheck(QuantContext ctx, DAStock cDAStock) {
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();
			
//			// ʱ��������
//			if(ctx.time().compareTo("13:30:00") < 0)
//			{
//				return;
//			}
			
			// 1-��ͣ�����
			double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
			if(0 == Double.compare(fDieTing, fNowPrice))
			{
				return;
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
				if(fZhang > 0.05)
				{
					return;
				}
			}
			else
			{
				return;
			}
			
			// �����ʱ�����������
			double dWave = DayKLinePriceWaveChecker.check(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
			ResultDropStable cResultDropStable = ETDropStable.checkDropStable(cDAStock.timePrices(), cDAStock.timePrices().size()-1, dWave/3*2);
			if(!cResultDropStable.bCheck)
			{
				return;
			}

			super.tryBuy(ctx, cDAStock.ID());	
		}

		@Override
		void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock) {
			double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
			double fNowPrice = cDAStock.price();
			
			if(cHoldStock.availableAmount <= 0)
			{
				return;
			}
				
			// ��ͣ������
			double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
			double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
			if(0 == Double.compare(fZhangTing, fNowPrice))
			{
				return;
			}
				
			// �ֹɳ�ʱ����
			long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
			if(lHoldDays >= 20) 
			{
				super.trySell(ctx, cHoldStock.stockID);
				return;
			}
				
			// ֹӯֹ������
			if(cHoldStock.refProfitRatio() > 0.10 || cHoldStock.refProfitRatio() < -0.12) 
			{
				super.trySell(ctx, cHoldStock.stockID);
				return;
			}
		}

		@Override
		void onStrateDayFinish(QuantContext ctx) {
			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				// ���ˣ���ƱID���ϣ�������
				if(
					//cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 
					cDAStock.dayKLines().size()<60
					|| !cDAStock.dayKLines().lastDate().equals(ctx.date())
					|| cDAStock.circulatedMarketValue() > 1000.0) {	
					continue;
				}
				
				// �������£���������
				double ave60now = EKAvePrice.GetMA(cDAStock.dayKLines(), 60, cDAStock.dayKLines().size()-1);
				double ave60Pre = EKAvePrice.GetMA(cDAStock.dayKLines(), 60, cDAStock.dayKLines().size()-5);
				double ave20now = EKAvePrice.GetMA(cDAStock.dayKLines(), 20, cDAStock.dayKLines().size()-1);
				double ave20Pre = EKAvePrice.GetMA(cDAStock.dayKLines(), 20, cDAStock.dayKLines().size()-5);
				if(ave60Pre < ave60now || ave20Pre > ave20now )
				{
					continue;
				}
					
				// 5���ڴ����糿֮��
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
							super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
						}
					}
				}
			}
		}
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
				new QS1711T4Strategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}