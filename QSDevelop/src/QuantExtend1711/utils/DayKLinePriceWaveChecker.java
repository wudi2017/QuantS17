package QuantExtend1711.utils;

import java.util.*;

import pers.di.localstock.common.*;
import pers.di.dataengine.*;

/*
 * 波动系数
 * 
 * 算法细节：
 * 检查至少20天，最大60天的区间，计算每天的最大振幅波动百分比，去除最大的5个后，取均值。
 */
public class DayKLinePriceWaveChecker {
	
	public static double check(DAKLines kLines, int iCheck)
	{
		double dResultCheckPriceWave = 0.0f;
		
		// 检查区间确定
		int iBegin = iCheck-60;
		int iEnd = iCheck;
		if(iBegin<0)
		{
			iBegin = 0;
		}
		if(iEnd-iBegin<20)
		{
			return dResultCheckPriceWave;
		}
		
		//波动排序后去除过大的波动
		List<Double> waveList = new ArrayList<Double>();
		for(int i=iBegin; i<=iEnd; i++)
		{
			KLine cKLine = kLines.get(i);
			double wave = cKLine.maxWave();
			waveList.add(wave);
		}
		if(waveList.size()>30)
		{
		}
		else
		{
			return dResultCheckPriceWave;
		}
		Collections.sort(waveList);
		for(int i=0; i<5; i++)
		{
			waveList.remove(waveList.size()-1);
		}
		
		// 靠前20天振幅的均值
		double dSum = 0.0;
		int cnt = 0;
		for(int i=waveList.size()-1; i>waveList.size()-20; i--)
		{
			dSum = dSum + waveList.get(i);
			cnt++;
		}
		double wave = dSum/cnt;
		

		dResultCheckPriceWave = wave;
		return dResultCheckPriceWave;
	}
}
