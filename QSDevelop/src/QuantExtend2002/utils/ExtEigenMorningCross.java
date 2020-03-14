package QuantExtend2002.utils;

import pers.di.dataengine.DAKLines;
import pers.di.localstock.common.KLine;

/*
 * ��չ���� �糿֮��
 */
public class ExtEigenMorningCross {
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
		
		// ��ȡ����ƽ�����
		double dAveWave = ComEigenDayKLinePriceWave.check(kLines, iCheck);
		
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
	 * �ɷ�-�����������
	 * 30��������Ϳ���Խ�͵÷�Խ��
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
		if(highPrice == lowPrice) return 0.0; // // ���ڲ���̫С Ϊ0�ַ�
	
		// ��ȡ����ƽ�����
		double dAveWave = ComEigenDayKLinePriceWave.check(kLines, iCheck);
		if((highPrice-lowPrice)/lowPrice < 1.5*dAveWave) {
			return 0.0; // ���ڲ���̫С Ϊ0�ַ�
		}
				
				
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		double checkPrice = (cCurStockDay.entityMidle() +cStockDayMid.entityMidle() + cCurStockBegin.entityMidle())/3;
		// ��ȡ�糿֮����ֵ
		
		double dH = highPrice*(1-dAveWave*0.5);
		double dL = lowPrice*(1+dAveWave*0.5);
				
		if(checkPrice <= dL) return 1.0;
		if(checkPrice >= dH) return 0.0;
		
		return 1.0 - (checkPrice-dL)/(dH-dL);
	}
	
	/*
	 * �ɷ�-ʮ���ǵײ���̽���ڵ���
	 * 10��������Ϳ���ʮ���ǵײ���ռ��Խ�͵÷�Խ��
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
		if(highPrice == lowPrice) return 0.0; // // ���ڲ���̫С Ϊ0�ַ�
		
		KLine cCurStockDay = kLines.get(iEnd);
		KLine cStockDayMid = kLines.get(iMid);
		KLine cCurStockBegin = kLines.get(iBegin);
		double checkPrice = cStockDayMid.low;
		// ��ȡ�糿֮�����ֵ
		
		if(checkPrice <= lowPrice) return 1.0;
		if(checkPrice >= highPrice) return 0.0;
		return 1.0 - (checkPrice-lowPrice)/(highPrice-lowPrice);
	}
	
	/*
	 * �ɷ�-ʮ���Ǳ�׼�̶�
	 * ʵ��÷�ռ��0.5����ʵ��ռ�������������ԽСԽ�ã��м�ֵ��
     * ����Ӱ�ߵ÷�ռ��0.5����Ӱ��վ�������������ԽСԽ�ã��м�ֵ��
	 */
	public static double scoreCalcCrossStandard(DAKLines kLines, int iCheck) {
		int iBegin = iCheck-2;
		int iMid = iCheck-1;
		int iEnd = iCheck;
		if(iCheck<30)
		{
			return 0.0;
		}
		
		// ��ȡ����ƽ�����
		double dAveWave = ComEigenDayKLinePriceWave.check(kLines, iCheck);
		if (dAveWave <= 0) {
			return 0.0;
		}
		
		// ��ȡʮ������
		KLine cStockDayMid = kLines.get(iMid);
		
		// ����ʵ��ռ����ϵ�������͵÷�
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
		
		// ��������Ӱ��ռ����ϵ�������͵÷�
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

