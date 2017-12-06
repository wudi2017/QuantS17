package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

public class PricePosChecker {
	
	public static class ResultDropParam
	{
		public ResultDropParam()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public double refLow;
		public double refHigh;
	}
	public static ResultDropParam getLongDropParam(DAKLines kLines, int iCheck)
	{
		ResultDropParam cResultLongDropParam = new ResultDropParam();
		
		int iBegin = iCheck-500;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultLongDropParam;
		}
		
		KLine cCurStockDay = kLines.get(iEnd);
		
		int iIndexH = DAStockUtils.indexHigh(kLines, iBegin, iEnd);
		KLine cStockDayH = kLines.get(iIndexH);
		int iIndexL = DAStockUtils.indexLow(kLines, iBegin, iEnd);
		KLine cStockDayL = kLines.get(iIndexL);
		
		cResultLongDropParam.refHigh = (cCurStockDay.close - cStockDayH.close)/cStockDayH.close;
		cResultLongDropParam.refLow = (cCurStockDay.close - cStockDayL.close)/cStockDayL.close;
		
		return cResultLongDropParam;
	}
	
	public static ResultDropParam getDropParam(int iRefDayCnt, DAKLines kLines, int iCheck)
	{
		ResultDropParam cResultLongDropParam = new ResultDropParam();
		
		int iBegin = iCheck-iRefDayCnt;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cResultLongDropParam;
		}
		
		KLine cCurStockDay = kLines.get(iEnd);
		
		int iIndexH = DAStockUtils.indexHigh(kLines, iBegin, iEnd);
		KLine cStockDayH = kLines.get(iIndexH);
		int iIndexL = DAStockUtils.indexLow(kLines, iBegin, iEnd);
		KLine cStockDayL = kLines.get(iIndexL);
		
		cResultLongDropParam.refHigh = (cCurStockDay.close - cStockDayH.close)/cStockDayH.close;
		cResultLongDropParam.refLow = (cCurStockDay.close - cStockDayL.close)/cStockDayL.close;
		
		return cResultLongDropParam;
	}
}
