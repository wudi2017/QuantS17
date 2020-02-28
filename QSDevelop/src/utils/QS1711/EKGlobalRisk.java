package utils.QS1711;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;
import utils.QS1711.base.EKAveVolume;
import utils.QS1711.base.EKHighLowFind;

/*
 * 全局风险指数，判断大盘的风险
 */
public class EKGlobalRisk {

	// 输入大盘k线，判断风险
	public static boolean isHighRisk(DAKLines kLines, int iCheck)
	{
		if(kLines.size() < 40)
		{
			return false;
		}
		
		int iLow30 = EKHighLowFind.indexLow(kLines, iCheck-20, iCheck);
		int iLow10 = EKHighLowFind.indexLow(kLines, iCheck-10, iCheck);
		if(iLow30 == iLow10)  // 近10天创造月新低，认为高风险
		{
			return true;
		}
		
		return false;
	}
	
	// 输入大盘k线，判断风险
	public static boolean isLowRisk(DAKLines kLines, int iCheck)
	{
		if(kLines.size() < 40)
		{
			return false;
		}
		
		int iLow20 = EKHighLowFind.indexLow(kLines, iCheck-20, iCheck);
		int iLow10 = EKHighLowFind.indexLow(kLines, iCheck-10, iCheck);
		if(iLow20  != iLow10)  // 近10天不创造新低
		{
			// 近5天存在放量大阳线
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
