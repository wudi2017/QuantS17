package QuantExtend2002.utils;

import pers.di.dataengine.DAKLines;
import pers.di.localstock.common.KLine;

public class ComEigenKLineHighLowFind {
	// 计算i到j日的最高价格的索引
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
	
	// 计算i到j日的最低价格的索引
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
