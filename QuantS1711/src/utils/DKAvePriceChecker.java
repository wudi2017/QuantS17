package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

/*
 * 区间均价计算
 */
public class DKAvePriceChecker {

	public static double check(DAKLines kLines, int iBegin, int iEnd)
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
}
