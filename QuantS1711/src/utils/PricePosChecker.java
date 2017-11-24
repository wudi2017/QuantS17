package utils;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

public class PricePosChecker {
	
	public static class ResultLongDropParam
	{
		public ResultLongDropParam()
		{
			bCheck = false;
		}
		public boolean bCheck;
		public double refLow;
		public double refHigh;
	}
	public static ResultLongDropParam getLongDropParam(DAKLines kLines, int iCheck)
	{
		ResultLongDropParam cResultLongDropParam = new ResultLongDropParam();
		
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
	
}
