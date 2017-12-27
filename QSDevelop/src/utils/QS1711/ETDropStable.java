package utils.QS1711;

import pers.di.dataengine.*;
import utils.QS1711.base.ETHighLowFind;

public class ETDropStable {

	/**
	 * 
	 * @author wudi
	 * 检查日内当前位置是否是下挫企稳点
	 */
	public static class ResultDropStable
	{
		public ResultDropStable()
		{
			bCheck = false;
		}
		public boolean bCheck;
		
		public int iHigh; // 最高索引
		public double highPrice; // 最高价
		public int iLow; // 最低索引
		public double lowPrice; // 最低价
		
		// 平均每分钟跌幅
		public double getLevel()
		{
			int timeSpan = iLow-iHigh;
			if(timeSpan>0)
			{
				return (highPrice-lowPrice)/timeSpan;
			}
			return 0;
		}
	}
	
	public static ResultDropStable checkDropStable(DATimePrices timePrices, int iCheck, double refWave)
	{
		ResultDropStable cResultDropStable = new ResultDropStable();
		
		double param_checkDieFu = -refWave/2; // 检查跌幅
		double param_checkMaxTimeSpan = 20; // 最大检查时间段
		double param_checkMaxHighLowTimeSpan = 10; // 高低点最大差距时间段
		
		int iCheckSpan = 5;
		int iCheckBegin = iCheck;
		int iCheckEnd = iCheck;
		/*
		 * 以当前点位结束检查点，向前以跨度iCheckSpan为检查，到最大跨度为止
		 */
		while(true)
		{
			iCheckBegin = iCheckBegin - iCheckSpan;
			
			/*
			 * 终点，如果检查到完毕退出，如果检查时间段大于param_checkMaxTimeSpan分退出
			 */
			if(iCheckBegin<0 || iCheckEnd-iCheckBegin>param_checkMaxTimeSpan) 
			{
				return cResultDropStable;
			}
			
			int indexHigh = ETHighLowFind.indexTimePriceHigh(timePrices, iCheckBegin, iCheckEnd);
			double highPrice = timePrices.get(indexHigh).price;
			int indexLow = ETHighLowFind.indexTimePriceLow(timePrices, iCheckBegin, iCheckEnd);
			double lowPrice = timePrices.get(indexLow).price;
			
			/*
			 * 1.最低点在最高点后面
			 * 2.对低点-最高点 在x分钟内
			 */
			if(indexHigh < indexLow 
					&& indexLow - indexHigh < param_checkMaxHighLowTimeSpan)
			{
			}
			else
			{
				// 此次检查不满足，继续向前找
				continue;
			}
			
			/*
			 * 最大跌幅在x点以上
			 */
			double MaxDropRate = (lowPrice-highPrice)/highPrice;
			if(MaxDropRate < param_checkDieFu)
			{
			}
			else
			{
				// 此次检查不满足，继续向前找
				continue;
			}
			
			/*
			 * 最低点产生后x分钟不创新低
			 */
			if(iCheckEnd - indexLow >= 2 && iCheckEnd - indexLow <= 5)
			{
				cResultDropStable.bCheck = true;
				cResultDropStable.highPrice = highPrice;
				cResultDropStable.lowPrice = lowPrice;
				cResultDropStable.iHigh = indexHigh;
				cResultDropStable.iLow = indexLow;
				break;
			}
			else
			{
				// 此次检查不满足，继续向前找
				continue;
			}
		}
		
		return cResultDropStable;
	}
}
