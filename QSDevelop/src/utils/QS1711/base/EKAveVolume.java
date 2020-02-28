package utils.QS1711.base;

import pers.di.localstock.common.*;
import pers.di.dataengine.DAKLines;

public class EKAveVolume {

	// �����iBegin��iEnd�ľ���
	public static double aveVolume(DAKLines kLines, int iBegin, int iEnd)
	{
		double volumeSum = 0.0;
		
		for(int i=iBegin; i<=iEnd; i++)
		{
			KLine cKLine = kLines.get(i);
			volumeSum = volumeSum + cKLine.volume;
		}
		
		double volumeAve = volumeSum/(iEnd-iBegin+1);
		
		return volumeAve;
	}
	
	// ���߼��㣬����date����ǰcount�����
	static public double GetMVA(DAKLines kLines, int count, int index)
	{
		if(kLines.size() == 0) return 0.0f;
		double value = 0.0f;
		int iE = index;
		int iB = iE-count+1;
		if(iB<0) iB=0;
		double sum = 0.0f;
		int sumcnt = 0;
		for(int i = iB; i <= iE; i++)  
        {  
			KLine cDayKData = kLines.get(i);  
			sum = sum + cDayKData.volume;
			sumcnt++;
        }
		value = sum/sumcnt;
		return value;
	}
}
