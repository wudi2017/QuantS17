package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

/*
 * �糿֮��K�����
 */
public class ZCZXChecker {

	/*
	 * �糿֮��3��K�߼��
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
		
		// һ���г�����
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
				return false;
			}
		}

		
		// �м����ʮ����
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
				return false;
			}
		}
		
		// ����г���
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
				return false;
			}
		}
		
		// ��λ����
		{
			// �м���Ӱ�߲��ܳ����м�ֵ
			if(cStockDayMid.entityHigh() < cCurStockDay.entityMidle()
					&& cStockDayMid.entityHigh() < cCurStockBegin.entityMidle())
			{
				
			}
			else
			{
				return false;
			}
			
			// ���һ���ո��󲿷ֵ�һ��ʵ��
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
	 * �糿֮��3�������
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
		
		if(cCurStockDay.volume < cStockDayMid.volume
				|| cCurStockBegin.volume < cStockDayMid.volume)
		{
			return false;
		}
		
		if(cCurStockDay.volume < cCurStockBegin.volume*0.85)
		{
			return false;
		}
		
		
		return true;
	}
}
