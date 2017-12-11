package utils.base;

import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;

/*
 * KLine���ۼ���
 */
public class EKAvePrice {
	// �����iBegin��iEnd������ƽ����
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
	
	// ���߼��㣬����date����ǰcount����߼۸�
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
