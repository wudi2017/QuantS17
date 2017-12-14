package strategy.QS1711;

import pers.di.account.common.HoldStock;
import pers.di.common.CUtilsMath;
import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;
import strategy.QS1711.EKRefHistoryPos.EKRefHistoryPosParam;

public class QS1711Strategy extends QS1711Base {
	public QS1711Strategy()
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
		
		// 1-跌停不买进
		double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
		double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
		if(0 == Double.compare(fDieTing, fNowPrice))
		{
			return;
		}
			
		// 2-近期涨幅过大不买进（根据选股条件计算买入参数）
		boolean bCheckFlg = false;
		int iZCZXFindEnd = -1;
		DAKLines list = cDAStock.dayKLines();
		int iCheck = list.size()-1;
		
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
				return;
			}
		}
		else
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
			
		// 涨停不卖出
		double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
		double fZhangTing = CUtilsMath.saveNDecimal(fYC*1.1f, 2);
		if(0 == Double.compare(fZhangTing, fNowPrice))
		{
			return;
		}
			
		// 持股超时卖出
		long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
		if(lHoldDays >= 30) 
		{
			super.trySell(ctx, cHoldStock.stockID);
			return;
		}
			
		// 止盈止损卖出
		if(cHoldStock.refProfitRatio() > 0.1 || cHoldStock.refProfitRatio() < -0.12) 
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
			
			// 过滤：股票ID集合，当天检查
			if(
				//cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 
				cDAStock.dayKLines().size()<60
				|| !cDAStock.dayKLines().lastDate().equals(ctx.date())
				|| cDAStock.circulatedMarketValue() > 1000.0) {	
				continue;
			}
				
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
						super.getXStockSelectManager().addSelect(cDAStock.ID(), -cEKRefHistoryPosParam.refHigh);
					}
				}
			}
		}
		
	}
}
