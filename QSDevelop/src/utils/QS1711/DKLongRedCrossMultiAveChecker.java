package utils.QS1711;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;
/*
 * 长阳上穿多个均线
 */
public class DKLongRedCrossMultiAveChecker {

	public static boolean check(DAKLines kLines, int iCheck)
	{
		// 最小天数检查
		if(kLines.size() < 80)
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
		
		// 穿过均线检查
		
		
		// 长阳检查
		if(lastKLine.entityHigh() == lastKLine.entityLow()) 
		{
			return false;
		}
		double yang = (lastKLine.entityHigh()-lastKLine.entityLow())/lastKLine.entityLow();
		if(yang < dAveWave) 
		{
			return false;
		}
				
		return true;
	}
}
