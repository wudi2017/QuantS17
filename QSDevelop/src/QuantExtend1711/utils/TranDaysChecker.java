package QuantExtend1711.utils;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;

public class TranDaysChecker {

	public static long check(DAKLines kLines, String fromDate, String toDate)
	{
		int iCount=0;
		for(int i=0; i<kLines.size(); i++)
		{
			KLine cKLine = kLines.get(i);
			if(cKLine.date.compareTo(fromDate) >= 0 && cKLine.date.compareTo(toDate) <= 0)
			{
				iCount = iCount+1;
			}
		}
		return iCount;
	}
}
