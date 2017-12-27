package utils.QS1711;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;

/*
 * ����Ӱ�߼��
 */
public class DayKLineLongLowerShadowChecker {
	
	public static boolean check(DAKLines kLines, int iCheck)
	{
		// ��С�������
		if(kLines.size() < 60)
		{
			return false;
		}

		KLine lastKLine = kLines.get(iCheck);

		// ������󲨶����
		double dAveWave = DayKLinePriceWaveChecker.check(kLines, iCheck);
		if(lastKLine.maxWave() < dAveWave*1.2)
		{
			return false;
		}
		
		// ��Ӱ�߳��ȼ��
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
