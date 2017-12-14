package utils.base;

import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;

/*
 * KLine均价计算
 */
public class EKAvePrice {
	// 计算从iBegin到iEnd的收盘平均价
	public static double avePrice(DAKLines kLines, int iBegin, int iEnd)
	{
		double priceSum = 0.0;
		
		for(int i=iBegin; i<=iEnd; i++)
		{
			KLine cKLine = kLines.get(i);
			priceSum = priceSum + cKLine.close;
		}
		
		double priceAve = priceSum/(iEnd-iBegin+1);
		
		return priceAve;
	}
	
	// 均线计算，计算date日期前count天均线价格
	static public double GetMA(DAKLines kLines, int count, int index)
	{
		if(kLines.size() == 0) return 0.0f;
		double value = 0.0f;
		int iE = index;
		int iB = iE-count+1;
		if(iB<0) iB=0;
		double sum = 0.0f;
		int sumcnt = 0;
		for(int i = iB; i <= iE; i++)  
        {  
			KLine cDayKData = kLines.get(i);  
			sum = sum + cDayKData.close;
			sumcnt++;
        }
		value = sum/sumcnt;
		return value;
	}
}
