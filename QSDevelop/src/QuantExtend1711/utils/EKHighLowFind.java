package QuantExtend1711.utils;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;

/*
 * KLine�ߵͲ���
 */
public class EKHighLowFind {
	// ����i��j�յ���߼۸������
	static public int indexHigh(DAKLines kLines, int i, int j)
	{
		int index = i;
		double high = -100000.0f;
		for(int k = i; k<=j; k++ )
		{
			KLine cDayKDataTmp = kLines.get(k);
			if(cDayKDataTmp.high > high) 
			{
				high = cDayKDataTmp.high;
				index = k;
			}
		}
		return index;
	}
	
	// ����i��j�յ���ͼ۸������
	static public int indexLow(DAKLines kLines, int i, int j)
	{
		int index = i;
		double low = 100000.0f;
		for(int k = i; k<=j; k++ )
		{
			KLine cDayKDataTmp = kLines.get(k);
			if(cDayKDataTmp.low < low) 
			{
				low = cDayKDataTmp.low;
				index = k;
			}
		}
		return index;
	}
}
