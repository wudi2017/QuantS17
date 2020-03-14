package QuantExtend2002.utils;

import pers.di.dataengine.DAKLines;
import pers.di.localstock.common.KLine;

/*
 * 扩展特征 早晨之星
 */
public class ExtEigenMorningCross {
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
		
		// 获取近期平均振幅
		double dAveWave = ComEigenDayKLinePriceWave.check(kLines, iCheck);
		
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
	
	/*
	 * 成分-近期整体跌幅
	 * 30天从最高最低看，越低得分越高
	 */
	public static double scoreCalc30DayLevel(DAKLines kLines, int iCheck) {
		
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iCheck<30)
		{
			return 0.0;
		}
		
		int iHigh = ComEigenKLineHighLowFind.indexHigh(kLines, iEnd-30, iEnd);
		int iLow = ComEigenKLineHighLowFind.indexLow(kLines, iEnd-30, iEnd);
		double highPrice = kLines.get(iHigh).high; String date1 = kLines.get(iHigh).date;
		double lowPrice = kLines.get(iLow).low; String date2 = kLines.get(iLow).date;
		if(highPrice == lowPrice) return 0.0; // // 近期波动太小 为0分分
	
		// 获取近期平均振幅
		double dAveWave = ComEigenDayKLinePriceWave.check(kLines, iCheck);
		if((highPrice-lowPrice)/lowPrice < 1.5*dAveWave) {
			return 0.0; // 近期波动太小 为0分分
		}
				
				
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		double checkPrice = (cCurStockDay.entityMidle() +cStockDayMid.entityMidle() + cCurStockBegin.entityMidle())/3;
		// 获取早晨之星中值
		
		double dH = highPrice*(1-dAveWave*0.5);
		double dL = lowPrice*(1+dAveWave*0.5);
				
		if(checkPrice <= dL) return 1.0;
		if(checkPrice >= dH) return 0.0;
		
		return 1.0 - (checkPrice-dL)/(dH-dL);
	}
	
	/*
	 * 成分-十字星底部下探短期跌幅
	 * 10天从最高最低看，十字星底部的占比越低得分越高
	 */
	public static double scoreCalc10CrossDayLevel(DAKLines kLines, int iCheck) {
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iCheck<10)
		{
			return 0.0;
		}
		
		int iHigh = ComEigenKLineHighLowFind.indexHigh(kLines, iEnd-10, iEnd);
		int iLow = ComEigenKLineHighLowFind.indexLow(kLines, iEnd-10, iEnd);
		double highPrice = kLines.get(iHigh).high; String date1 = kLines.get(iHigh).date;
		double lowPrice = kLines.get(iLow).low; String date2 = kLines.get(iLow).date;
		if(highPrice == lowPrice) return 0.0; // // 近期波动太小 为0分分
		
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		double checkPrice = cStockDayMid.low;
		// 获取早晨之星最低值
		
		if(checkPrice <= lowPrice) return 1.0;
		if(checkPrice >= highPrice) return 0.0;
		return 1.0 - (checkPrice-lowPrice)/(highPrice-lowPrice);
	}
	
	/*
	 * 成分-十字星标准程度
	 * 实体得分占比0.5：看实体占最近波动比例，越小越好（有极值）
     * 上下影线得分占比0.5：看影线站最近波动比例，越小越好（有极值）
	 */
	public static double scoreCalcCrossStandard(DAKLines kLines, int iCheck) {
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iCheck<30)
		{
			return 0.0;
		}
		
		// 获取近期平均振幅
		double dAveWave = ComEigenDayKLinePriceWave.check(kLines, iCheck);
		if (dAveWave <= 0) {
			return 0.0;
		}
		
		// 获取十字星日
		KLine cStockDayMid = kLines.get(iMid);
		
		// 计算实体占波动系数比例和得分
		double entityRatioPartScole = 0.0;
		double entityRatio = (cStockDayMid.entityHigh()-cStockDayMid.low)/cStockDayMid.low;
		double entityRatioPart = entityRatio/dAveWave;
		if (entityRatioPart <= 0.2) {
			entityRatioPartScole = 1;
		} else if (entityRatioPartScole >= 0.8) {
			entityRatioPartScole = 0;
		} else {
			entityRatioPartScole = 1 - (entityRatioPart - 0.2)/0.6;
		}
		
		// 计算上下影线占波动系数比例和得分
		double shadowRatioPartScole = 0;
		double shadowRatio = ((cStockDayMid.high - cStockDayMid.entityHigh()) 
				+ (cStockDayMid.entityLow() - cStockDayMid.low)) / cStockDayMid.low;
		double shadowRatioPart = shadowRatio/dAveWave;
		if (shadowRatioPart <= 0.2) {
			shadowRatioPartScole = 1;
		} else if (shadowRatioPart >= 0.8) {
			shadowRatioPartScole = 0;
		} else {
			shadowRatioPartScole = 1 - (shadowRatioPart - 0.2)/0.6;
		}
		
		return 0.5*entityRatioPartScole + 0.5*shadowRatioPartScole;
	}
}

