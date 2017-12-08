package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

public class DAStockUtils {
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
	
	// ����i��j�յ���߼۸������
	static public int indexTimePriceHigh(DATimePrices timePrices, int i, int j)
	{
		int index = i;
		double high = -100000.0;
		for(int k = i; k<=j; k++ )
		{
			TimePrice cTimePrice = timePrices.get(k);
			if(cTimePrice.price > high) 
			{
				high = cTimePrice.price;
				index = k;
			}
		}
		return index;
	}
	
	// ����i��j�յ���ͼ۸������
	static public int indexTimePriceLow(DATimePrices timePrices, int i, int j)
	{
		int index = i;
		double low = 100000.0;
		for(int k = i; k<=j; k++ )
		{
			TimePrice cTimePrice = timePrices.get(k);
			if(cTimePrice.price < low) 
			{
				low = cTimePrice.price;
				index = k;
			}
		}
		return index;
	}
}
