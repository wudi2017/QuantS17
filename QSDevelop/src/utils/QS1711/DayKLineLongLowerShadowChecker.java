package utils.QS1711;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

/*
 * 长下影线检查
 */
public class DayKLineLongLowerShadowChecker {
	
	public static boolean check(DAKLines kLines, int iCheck)
	{
		// 最小天数检查
		if(kLines.size() < 60)
		{
			return false;
		}

		KLine lastKLine = kLines.get(iCheck);

		// 当天最大波动检查
		double dAveWave = DayKLinePriceWaveChecker.check(kLines, iCheck);
		if(lastKLine.maxWave() < dAveWave*1.2)
		{
			return false;
		}
		
		// 下影线长度检查
		if(lastKLine.entityLow() == lastKLine.low) 
		{
			return false;
		}
		double dLowerShadow = (lastKLine.entityLow() - lastKLine.low)/lastKLine.low;
		if(dLowerShadow < dAveWave)
		{
			return false;
		}

		return true;
	}
}
