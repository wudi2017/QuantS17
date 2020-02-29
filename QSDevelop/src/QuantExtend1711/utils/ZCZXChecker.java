package QuantExtend1711.utils;

import pers.di.common.CLog;
import pers.di.localstock.common.*;
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
		
		// ��begin���г���
		double dBshiti = 0.0;
		double dBMaxWave = 0.0;
		{
			double shangying = cCurStockBegin.high - cCurStockBegin.entityHigh();
			double xiaying = cCurStockBegin.entityLow() - cCurStockBegin.low;
			double shiti = cCurStockBegin.entityHigh() - cCurStockBegin.entityLow();
			double shitiRatio = shiti/cCurStockBegin.low;
			if(cCurStockBegin.open > cCurStockBegin.close // ����
					&& shitiRatio > dAveWave*0.5 // ʵ�������Խ��ڱȽϴ�
					&& shangying < shiti/3*2 // ��Ӱ�߳��ȱȽ�С
					&& xiaying < shiti/3*2  // ��Ӱ�߳��ȱȽ�С
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

		// ��end���г���
		double dEshiti = 0.0;
		double dEMaxWave = 0.0;
		{
			double shangying = cCurStockDay.high - cCurStockDay.entityHigh();
			double xiaying = cCurStockDay.entityLow() - cCurStockDay.low;
			double shiti = cCurStockDay.entityHigh() - cCurStockDay.entityLow();
			double shitiRatio = shiti/cCurStockDay.low;
			if(cCurStockDay.open < cCurStockDay.close // ����
					&& shitiRatio > dAveWave*0.5 // ʵ�������Խ��ڱȽϴ�
					&& shangying < shiti/3*2  // ��Ӱ�߳��ȱȽ�С
					&& xiaying < shiti/3*2 // ��Ӱ�߳��ȱȽ�С
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
		
		// �м����ʮ����
		{
			double shangying = cStockDayMid.high - cStockDayMid.entityHigh();
			double xiaying = cStockDayMid.entityLow() - cStockDayMid.low;
			double shiti = cStockDayMid.entityHigh() - cStockDayMid.entityLow();
			double shitiRatio = shiti/cStockDayMid.low;
			double dMaxWave = cStockDayMid.high - cStockDayMid.low;
			double refshiti = (dBshiti+dEshiti)/2;
			double refAveMaxwave = (dBMaxWave+dEMaxWave)/2;
			if(shitiRatio < dAveWave // ʵ�岨����Խ��ڽ�С
					&& shiti < refshiti/2 // ʵ���������С�ܶ�
					)
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
		
		if(cCurStockDay.volume < cStockDayMid.volume // �������С���м���
				|| cCurStockDay.volume < cCurStockBegin.volume*0.5) // �������С�ڵ�һ��
		{
			return false;
		}

		return true;
	}
	
	
	/*
	 * ��ʷ��飺���سɹ���
	 */
	public static double check_history(DAKLines kLines)
	{
		int iTimesSucc = 0;
		int iTimesFail = 0;
		for(int i=0; i<kLines.size(); i++)
		{
			if(ZCZXChecker.check(kLines, i) && ZCZXChecker.check_volume(kLines, i))
			{
				if(i+30 >= kLines.size()) continue;
				
				boolean bMockCheck = mocktran(kLines, i, i+30);
				if(bMockCheck)
				{
					iTimesSucc++;
				}
				else
				{
					iTimesFail++;
				}
				
				KLine cKLineIndex = kLines.get(i);
				int iHigh = EKHighLowFind.indexHigh(kLines, i, i+30);
				int iLow = EKHighLowFind.indexLow(kLines, i, i+30);
				KLine cKLineH = kLines.get(iHigh);
				KLine cKLineL = kLines.get(iLow);
//				CLog.output("TEST", "ZCZX:%s H:%s L:%s MockTran:%b", 
//						cKLineIndex.date, cKLineH.date, cKLineL.date, bMockCheck);
			}
		}
		double succRate = 0;
		if(iTimesSucc+iTimesFail>5)
		{
			succRate = iTimesSucc/(double)(iTimesSucc+iTimesFail);
		}
//		CLog.output("TEST", "succRate:%.3f", succRate);
		return succRate;
	}
	
	private static boolean mocktran(DAKLines kLines, int iB, int iE)
	{
		KLine cKLineIndex = kLines.get(iB);
		double clearHigh = cKLineIndex.entityMidle()*1.1;
		double clearLow = cKLineIndex.entityMidle()*0.9;
		for(int i=iB; i<iE; i++)
		{
			KLine cKLineCk = kLines.get(i);
			if(cKLineCk.low <= clearLow)
			{
				return false;
			}
			if(cKLineCk.high >= clearHigh)
			{
				return true;
			}
		}
		if( kLines.get(iE).entityMidle() > cKLineIndex.entityMidle())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
