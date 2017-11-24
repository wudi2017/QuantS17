package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

/*
 * 早晨之星
 */
public class ZCZXChecker {
	public static class ResultDYCheck
	{
		public ResultDYCheck()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public double fStarHigh;
		public double fStarLow;
	}
	
	public static ResultDYCheck check(DAKLines kLines, int iCheck)
	{
		ResultDYCheck cResultDYCheck = new ResultDYCheck();
		
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultDYCheck;
		}
		
		double dAveWave = DayKLinePriceWaveChecker.check(kLines, iCheck);
		
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		
		//BLog.output("TEST", "%s fAveWave %.3f\n", cCurStockDay.date(), fAveWave);
		
		// 一天中长阴线
		{
			double shangying = cCurStockBegin.high - cCurStockBegin.entityHigh();
			double xiaying = cCurStockBegin.entityLow() - cCurStockBegin.low;
			double shiti = cCurStockBegin.entityHigh() - cCurStockBegin.entityLow();
			double shitiRatio = shiti/cCurStockBegin.low;
			if(cCurStockBegin.open > cCurStockBegin.close
					&& shangying < shiti/3*2 
					&& xiaying < shiti/3*2 
					&& shitiRatio > dAveWave*0.5)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}

		
		// 中间横盘十字星
		{
			double shangying = cStockDayMid.high - cStockDayMid.entityHigh();
			double xiaying = cStockDayMid.entityLow() - cStockDayMid.low;
			double shiti = cStockDayMid.entityHigh() - cStockDayMid.entityLow();
			double shitiRatio = shiti/cStockDayMid.low;
			if((shangying > shiti/3 || xiaying > shiti/3)
					&& shitiRatio < dAveWave)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		// 最后中长阳
		{
			double shangying = cCurStockDay.high - cCurStockDay.entityHigh();
			double xiaying = cCurStockDay.entityLow() - cCurStockDay.low;
			double shiti = cCurStockDay.entityHigh() - cCurStockDay.entityLow();
			double shitiRatio = shiti/cCurStockDay.low;
			if(cCurStockDay.open < cCurStockDay.close
					&& shangying < shiti/3*2  
					&& xiaying < shiti/3*2 
					&& shitiRatio > dAveWave*0.5)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		// 价位控制
		{
			// 中间上影线不能超过中间值
			if(cStockDayMid.entityHigh() < cCurStockDay.entityMidle()
					&& cStockDayMid.entityHigh() < cCurStockBegin.entityMidle())
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
			
			// 最后一天收复大部分第一天实体
			double fcheck = cCurStockBegin.entityLow() + (cCurStockBegin.entityHigh() - cCurStockBegin.entityLow())/3*2;
			if(cCurStockDay.entityHigh() > fcheck)
			{
				
			}
			else
			{
				return cResultDYCheck;
			}
		}
		
		cResultDYCheck.bCheck = true;
		cResultDYCheck.fStarHigh = cCurStockDay.entityHigh();
		cResultDYCheck.fStarLow = cCurStockDay.entityLow();
		return cResultDYCheck;
	}
}
