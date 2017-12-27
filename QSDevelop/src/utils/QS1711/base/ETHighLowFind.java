package utils.QS1711.base;

import pers.di.dataapi.common.TimePrice;
import pers.di.dataengine.DATimePrices;

/*
 * TimePrice分时高低查找
 */
public class ETHighLowFind {
	// 计算i到j日的最高价格的索引
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
	
	// 计算i到j日的最低价格的索引
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
