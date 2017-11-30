package utils;

import pers.di.common.CLog;
import pers.di.dataengine.DAKLines;

/*
 * 中期下滑走势
 */
public class DKMidDropChecker {

	public static boolean check(DAKLines kLines, int iCheck)
	{
		// 最小天数检查
		if(kLines.size() < 100)
		{
			return false;
		}
		
		double dCheck1 = DKAvePriceChecker.check(kLines, iCheck-40, iCheck-20);
		double dCheck2 = DKAvePriceChecker.check(kLines, iCheck-20, iCheck-0);
				
		CLog.output("TEST", "    DKMidDropChecker dCheck1:%f dCheck2:%f", dCheck1, dCheck2);
		
		if(dCheck1<=dCheck2)
		{
			return false;
		}
		
		return true;
	}
}
