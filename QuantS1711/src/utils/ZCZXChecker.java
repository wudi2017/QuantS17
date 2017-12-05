package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

/*
 * 早晨之星K线组合
 */
public class ZCZXChecker {

	/*
	 * 早晨之星3日K线检查
	 */
	public static boolean check(DAKLines kLines, int iCheck)
	{
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return false;
		}
		
		double dAveWave = DayKLinePriceWaveChecker.check(kLines, iCheck);
		
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		
		//BLog.output("TEST", "%s fAveWave %.3f\n", cCurStockDay.date(), fAveWave);
		
		// 第begin天中长阴
		double dBshiti = 0.0;
		double dBMaxWave = 0.0;
		{
			double shangying = cCurStockBegin.high - cCurStockBegin.entityHigh();
			double xiaying = cCurStockBegin.entityLow() - cCurStockBegin.low;
			double shiti = cCurStockBegin.entityHigh() - cCurStockBegin.entityLow();
			double shitiRatio = shiti/cCurStockBegin.low;
			if(cCurStockBegin.open > cCurStockBegin.close // 阴线
					&& shitiRatio > dAveWave*0.5 // 实体比例相对近期比较大
					&& shangying < shiti/3*2 // 上影线长度比较小
					&& xiaying < shiti/3*2  // 下影线长度比较小
					)
			{
				
			}
			else
			{
				return false;
			}
			
			dBshiti = shiti;
			dBMaxWave = cCurStockBegin.high - cCurStockBegin.low;
		}

		// 第end天中长阳
		double dEshiti = 0.0;
		double dEMaxWave = 0.0;
		{
			double shangying = cCurStockDay.high - cCurStockDay.entityHigh();
			double xiaying = cCurStockDay.entityLow() - cCurStockDay.low;
			double shiti = cCurStockDay.entityHigh() - cCurStockDay.entityLow();
			double shitiRatio = shiti/cCurStockDay.low;
			if(cCurStockDay.open < cCurStockDay.close // 阳线
					&& shitiRatio > dAveWave*0.5 // 实体比例相对近期比较大
					&& shangying < shiti/3*2  // 上影线长度比较小
					&& xiaying < shiti/3*2 // 下影线长度比较小
					) 
			{
				
			}
			else
			{
				return false;
			}
			dEshiti = shiti;
			dEMaxWave = cCurStockDay.high - cCurStockDay.low;
		}
		
		// 中间横盘十字星
		{
			double shangying = cStockDayMid.high - cStockDayMid.entityHigh();
			double xiaying = cStockDayMid.entityLow() - cStockDayMid.low;
			double shiti = cStockDayMid.entityHigh() - cStockDayMid.entityLow();
			double shitiRatio = shiti/cStockDayMid.low;
			double dMaxWave = cStockDayMid.high - cStockDayMid.low;
			double refshiti = (dBshiti+dEshiti)/2;
			double refAveMaxwave = (dBMaxWave+dEMaxWave)/2;
			if(shitiRatio < dAveWave // 实体波动相对近期较小
					&& shiti < refshiti/2 // 实体比两边线小很多
					)
			{
				
			}
			else
			{
				return false;
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
				return false;
			}
			
			// 最后一天收复大部分第一天实体
			double fcheck = cCurStockBegin.entityLow() + (cCurStockBegin.entityHigh() - cCurStockBegin.entityLow())/3*2;
			if(cCurStockDay.entityHigh() > fcheck)
			{
				
			}
			else
			{
				return false;
			}
		}
	
		return true;
	}
	
	/*
	 * 早晨之星3日量检查
	 */
	public static boolean check_volume(DAKLines kLines, int iCheck)
	{
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return false;
		}
		
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		
		if(cCurStockDay.volume < cStockDayMid.volume // 最后天量小于中间日
				|| cCurStockDay.volume < cCurStockBegin.volume*0.5) // 最后天量小于第一天
		{
			return false;
		}

		return true;
	}
}
