package utils.QS1711.base;

import pers.di.dataapi.common.TimePrice;
import pers.di.dataengine.DATimePrices;

/*
 * TimePrice��ʱ�ߵͲ���
 */
public class ETHighLowFind {
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
