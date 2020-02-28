package utils.QS1711;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;
import utils.QS1711.base.EKAveVolume;
import utils.QS1711.base.EKHighLowFind;

/*
 * ȫ�ַ���ָ�����жϴ��̵ķ���
 */
public class EKGlobalRisk {

	// �������k�ߣ��жϷ���
	public static boolean isHighRisk(DAKLines kLines, int iCheck)
	{
		if(kLines.size() < 40)
		{
			return false;
		}
		
		int iLow30 = EKHighLowFind.indexLow(kLines, iCheck-20, iCheck);
		int iLow10 = EKHighLowFind.indexLow(kLines, iCheck-10, iCheck);
		if(iLow30 == iLow10)  // ��10�촴�����µͣ���Ϊ�߷���
		{
			return true;
		}
		
		return false;
	}
	
	// �������k�ߣ��жϷ���
	public static boolean isLowRisk(DAKLines kLines, int iCheck)
	{
		if(kLines.size() < 40)
		{
			return false;
		}
		
		int iLow20 = EKHighLowFind.indexLow(kLines, iCheck-20, iCheck);
		int iLow10 = EKHighLowFind.indexLow(kLines, iCheck-10, iCheck);
		if(iLow20  != iLow10)  // ��10�첻�����µ�
		{
			// ��5����ڷ���������
			double mva20 = EKAveVolume.GetMVA(kLines, 20, iCheck);
			double wave20 = DayKLinePriceWaveChecker.check(kLines, iCheck);
			for(int i=0; i<5; i++)
			{
				KLine cKLine = kLines.get(iCheck-i);
				if(cKLine.volume > mva20*1.2)
				{
					if( (cKLine.close-cKLine.open)/cKLine.open > wave20*1.2)
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
