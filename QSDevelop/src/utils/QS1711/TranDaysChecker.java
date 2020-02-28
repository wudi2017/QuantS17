package utils.QS1711;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;

public class TranDaysChecker {

	public static long check(DAKLines kLines, String fromDate, String toDate)
	{
		int iCount=0;
		for(int i=0; i<kLines.size(); i++)
		{
			KLine cKLine = kLines.get(i);
			if(cKLine.date.compareTo(fromDate) >= 0 && cKLine.date.compareTo(fromDate) <= 0)
			{
				iCount = iCount+1;
			}
		}
		return iCount;
	}
}
