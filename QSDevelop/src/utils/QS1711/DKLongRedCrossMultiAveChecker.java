package utils.QS1711;

import pers.di.dataapi.common.*;
import pers.di.dataengine.*;
/*
 * �����ϴ��������
 */
public class DKLongRedCrossMultiAveChecker {

	public static boolean check(DAKLines kLines, int iCheck)
	{
		// ��С�������
		if(kLines.size() < 80)
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
		
		// �������߼��
		
		
		// �������
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
