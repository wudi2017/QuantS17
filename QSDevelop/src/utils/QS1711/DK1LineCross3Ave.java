package utils.QS1711;

import pers.di.common.CLog;
import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;
import utils.QS1711.base.EKAvePrice;

/*
 * һ�����ϴ�3��
 */
public class DK1LineCross3Ave {

	public static boolean check(DAKLines kLines, int iEnd)
	{
		// ��С�������
		if(kLines.size() < 60)
		{
			return false;
		}
		
		KLine cKLine = kLines.get(iEnd);
		
		// ���һ�촩3��
		boolean bLastCross = false;
		double ma5 = EKAvePrice.GetMA(kLines, iEnd, 5);
		double ma10 = EKAvePrice.GetMA(kLines, iEnd, 10);
		double ma20 = EKAvePrice.GetMA(kLines, iEnd, 20);
		//CLog.output("TEST", "test ma5=%.3f ma10=%.3f ma20=%.3f", ma5, ma10, ma20);

		if(cKLine.close > cKLine.open
				&& cKLine.close > ma5 && cKLine.close > ma10 && cKLine.close > ma20
				&& cKLine.low < ma5 && cKLine.low < ma10 && cKLine.low < ma20)
		{
			bLastCross = true;
		}
		
		// ǰ��һ���������µ�����
		boolean bDie = false;
		double ma5x = EKAvePrice.GetMA(kLines, iEnd-3, 5);
		double ma10x = EKAvePrice.GetMA(kLines, iEnd-3, 10);
		double ma20x = EKAvePrice.GetMA(kLines, iEnd-3, 20);
		if(ma20x > ma5x)
		{
			bDie = true;
		}
		
		if(bLastCross && bDie)
		{
			return true;
		}
		
		return false;
	}
}
