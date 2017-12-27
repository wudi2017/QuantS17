package utils.QS1711;

import pers.di.dataengine.*;
import utils.QS1711.base.ETHighLowFind;

public class ETDropStable {

	/**
	 * 
	 * @author wudi
	 * ������ڵ�ǰλ���Ƿ����´����ȵ�
	 */
	public static class ResultDropStable
	{
		public ResultDropStable()
		{
			bCheck = false;
		}
		public boolean bCheck;
		
		public int iHigh; // �������
		public double highPrice; // ��߼�
		public int iLow; // �������
		public double lowPrice; // ��ͼ�
		
		// ƽ��ÿ���ӵ���
		public double getLevel()
		{
			int timeSpan = iLow-iHigh;
			if(timeSpan>0)
			{
				return (highPrice-lowPrice)/timeSpan;
			}
			return 0;
		}
	}
	
	public static ResultDropStable checkDropStable(DATimePrices timePrices, int iCheck, double refWave)
	{
		ResultDropStable cResultDropStable = new ResultDropStable();
		
		double param_checkDieFu = -refWave/2; // ������
		double param_checkMaxTimeSpan = 20; // �����ʱ���
		double param_checkMaxHighLowTimeSpan = 10; // �ߵ͵������ʱ���
		
		int iCheckSpan = 5;
		int iCheckBegin = iCheck;
		int iCheckEnd = iCheck;
		/*
		 * �Ե�ǰ��λ�������㣬��ǰ�Կ��iCheckSpanΪ��飬�������Ϊֹ
		 */
		while(true)
		{
			iCheckBegin = iCheckBegin - iCheckSpan;
			
			/*
			 * �յ㣬�����鵽����˳���������ʱ��δ���param_checkMaxTimeSpan���˳�
			 */
			if(iCheckBegin<0 || iCheckEnd-iCheckBegin>param_checkMaxTimeSpan) 
			{
				return cResultDropStable;
			}
			
			int indexHigh = ETHighLowFind.indexTimePriceHigh(timePrices, iCheckBegin, iCheckEnd);
			double highPrice = timePrices.get(indexHigh).price;
			int indexLow = ETHighLowFind.indexTimePriceLow(timePrices, iCheckBegin, iCheckEnd);
			double lowPrice = timePrices.get(indexLow).price;
			
			/*
			 * 1.��͵�����ߵ����
			 * 2.�Ե͵�-��ߵ� ��x������
			 */
			if(indexHigh < indexLow 
					&& indexLow - indexHigh < param_checkMaxHighLowTimeSpan)
			{
			}
			else
			{
				// �˴μ�鲻���㣬������ǰ��
				continue;
			}
			
			/*
			 * ��������x������
			 */
			double MaxDropRate = (lowPrice-highPrice)/highPrice;
			if(MaxDropRate < param_checkDieFu)
			{
			}
			else
			{
				// �˴μ�鲻���㣬������ǰ��
				continue;
			}
			
			/*
			 * ��͵������x���Ӳ����µ�
			 */
			if(iCheckEnd - indexLow >= 2 && iCheckEnd - indexLow <= 5)
			{
				cResultDropStable.bCheck = true;
				cResultDropStable.highPrice = highPrice;
				cResultDropStable.lowPrice = lowPrice;
				cResultDropStable.iHigh = indexHigh;
				cResultDropStable.iLow = indexLow;
				break;
			}
			else
			{
				// �˴μ�鲻���㣬������ǰ��
				continue;
			}
		}
		
		return cResultDropStable;
	}
}
