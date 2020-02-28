package utils.QS1711.base;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;

/*
 * 历史参考价格位置
 */
public class EKRefHistoryPos {
	
	public static class EKRefHistoryPosParam
	{
		public EKRefHistoryPosParam()
		{
			bCheck = false;
		}
		public boolean bCheck;
		
		public double refLow;
		public double refHigh;
	}
	
	public static EKRefHistoryPosParam check(int iRefDayCnt, DAKLines kLines, int iCheck)
	{
		EKRefHistoryPosParam cEKRefHistoryPosParam = new EKRefHistoryPosParam();
		
		int iBegin = iCheck-iRefDayCnt;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			return cEKRefHistoryPosParam;
		}
		
		KLine cCurStockDay = kLines.get(iEnd);
		
		int iIndexH = EKHighLowFind.indexHigh(kLines, iBegin, iEnd);
		KLine cStockDayH = kLines.get(iIndexH);
		int iIndexL = EKHighLowFind.indexLow(kLines, iBegin, iEnd);
		KLine cStockDayL = kLines.get(iIndexL);
		
		cEKRefHistoryPosParam.bCheck = true;
		cEKRefHistoryPosParam.refHigh = (cCurStockDay.close - cStockDayH.close)/cStockDayH.close;
		cEKRefHistoryPosParam.refLow = (cCurStockDay.close - cStockDayL.close)/cStockDayL.close;
		
		return cEKRefHistoryPosParam;
	}
	
}
