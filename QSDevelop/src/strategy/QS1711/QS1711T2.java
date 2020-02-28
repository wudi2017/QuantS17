package strategy.QS1711;

import java.util.ArrayList;
import java.util.List;

import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.localstock.common.KLine;
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
 * ���Ը�Ҫ��
 * �������糿֮��ѡ�ɽ׶��Ż�������30�յ�����������
 */
public class QS1711T2 {
	public static class QS1712T2Strategy extends QS1711SBase
	{
		public QS1712T2Strategy()
		{
			super(10, 5); // maxSelect=10 maxHold=5
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
			
			do
			{
				// 1-��ͣ�����
				double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
				double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
				if(0 == Double.compare(fDieTing, fNowPrice))
				{
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
						break;
					}
				}
				else
				{
					break;
				}
				
				
				// ������
				super.tryBuy(ctx, cDAStock.ID());
				
			} while(false);
		}
		
		@Override
		void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock) {
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
				if(cHoldStock.refProfitRatio() > 0.10 || cHoldStock.refProfitRatio() < -0.12) 
				{
					bSellFlag = true;
					break;
				}
			} while(false);
				
			if(bSellFlag)
			{
				super.trySell(ctx, cHoldStock.stockID);
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
			
			// �ڶ������ȼ�ɸѡ�� ����30�յ�������
			List<String> select2 = super.getXStockSelectManager().validSelectListS1(20);
			super.getXStockSelectManager().clearSelect();
			for(int iStock=0; iStock<select2.size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(select2.get(iStock));
				EKRefHistoryPosParam cEKRefHistoryPosParam = EKRefHistoryPos.check(500, cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
				super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		IAccount acc = cAccountController.account();
		
		Quant.instance().run(
				"HistoryTest 2010-01-01 2017-11-25", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccountController, 
				new QS1712T2Strategy());
		cAccountController.close();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}
}
