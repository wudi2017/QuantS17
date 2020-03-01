package QuantExtend2002.framework;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pers.di.common.CImageCurve;
import pers.di.common.CSystem;
import pers.di.common.CImageCurve.CurvePoint;

/*
 * Quant Extend Utils Transaction Report (QEUTranReportor)
 */
public class QEUTranReportor {
	/*
	 * �ձ���ṹ��
	 */
	public static class DailyReport
	{
		public DailyReport()
		{
		}
		public double dTotalAssets; // ���ʲ�
		public double dSHComposite; // ������ָ֤��
	}
	
	public QEUTranReportor(String name)
	{
		m_name = name;
		m_cDailyReportTreeMap = new TreeMap<String, DailyReport>((o1, o2) -> {
			String str1 = (String)o1;
			String str2 = (String)o2;
		    //����п�ֵ��ֱ�ӷ���0
		    if (o1 == null || o2 == null)
		        return 0; 
		   return str1.compareTo(str2);
       });
	}
	
	
	public void collectInfo_TotalAssets(String date, double dTotalAssets)
	{
		DailyReport cDailyReport = m_cDailyReportTreeMap.get(date);
		if(null == cDailyReport)
		{
			DailyReport cNewDailyReport = new DailyReport();
			m_cDailyReportTreeMap.put(date, cNewDailyReport);
			cDailyReport = m_cDailyReportTreeMap.get(date);
		}
		
		cDailyReport.dTotalAssets = dTotalAssets;
	}
	
	public void collectInfo_SHComposite(String date, double dSHComposite)
	{
		DailyReport cDailyReport = m_cDailyReportTreeMap.get(date);
		if(null == cDailyReport)
		{
			DailyReport cNewDailyReport = new DailyReport();
			m_cDailyReportTreeMap.put(date, cNewDailyReport);
			cDailyReport = m_cDailyReportTreeMap.get(date);
		}
		
		cDailyReport.dSHComposite = dSHComposite;
	}
	
	public void generateReport()
	{
		generate_abs();
		generate_ref_month();
		generate_ref_year();
	}
	
	private void generate_abs()
	{
		List<CurvePoint> cCurvePointList_TotalAssets = new ArrayList<CurvePoint>();
		List<CurvePoint> cCurvePointList_SHComposite = new ArrayList<CurvePoint>();
		List<Double> spanList = new ArrayList<Double>();
		
		// ����keyֵ�����������������
		int iSize = m_cDailyReportTreeMap.size();
		int index=0;
		String lastDate = "";
		for (Map.Entry<String, DailyReport> entry : m_cDailyReportTreeMap.entrySet()) { 
			 String date = entry.getKey();
			 DailyReport cDailyReport = entry.getValue();
			 
			 if(lastDate.equals("")) lastDate = date;
			 
			 cCurvePointList_TotalAssets.add(new CurvePoint(index, cDailyReport.dTotalAssets));
			 cCurvePointList_SHComposite.add(new CurvePoint(index, cDailyReport.dSHComposite));
			
			 if(iSize <= 30)
			 {
				spanList.add(index/(double)iSize); // С��30�� ÿ�춼�п̶�
			 }
			 else if(iSize > 30 && iSize <= 600)  // ����30̫�� С�� 20���£� ÿ�¶��п̶�
			 {
				 if(!lastDate.substring(5, 7).equals(date.substring(5, 7)))
				 {
					 spanList.add(index/(double)iSize);
				 }
			 }
			 else if(iSize > 600) // ����20���£� ÿ�궼�п̶�
			 {
				 if(!lastDate.substring(0, 4).equals(date.substring(0, 4)))
				 {
					 spanList.add(index/(double)iSize);
				 }
			 }
			 
			 index++;
			 lastDate = date;
		}
		
		// �����߱�ʾ��ͬ��ͼ
		{
			String filename = CSystem.getRunSessionRoot() + "\\report_abs_" + m_name + ".jpg";
			CImageCurve cCImageCurve_abs = new CImageCurve(2560,1920,filename);
			cCImageCurve_abs.setColor(Color.BLUE);
			cCImageCurve_abs.writeLogicCurveSameRatio(cCurvePointList_SHComposite);
			cCImageCurve_abs.setColor(Color.GREEN);
			cCImageCurve_abs.writeLogicCurveSameRatio(cCurvePointList_TotalAssets);
			cCImageCurve_abs.writeMultiLogicCurveSameRatio();
			// ��������Ϳ̶�
			cCImageCurve_abs.setColor(Color.BLACK);
			cCImageCurve_abs.writeAxis();
			for(int i=0; i<spanList.size();i++)
			{
				double x=spanList.get(i);
				cCImageCurve_abs.writeUnitLine(x, -0.01, x, 0.01);
			}
			cCImageCurve_abs.GenerateImage();
		}
	}
	
	private void generate_ref_month()
	{
		// ��ӯ�����ͼ���ο�����
		List<CurvePoint> cCurvePointList_RefRadio = new ArrayList<CurvePoint>();
		double lastRefVal = 0.0;
		double lastVal = 0.0;
		int index=0;
		String lastDate = "";
		for (Map.Entry<String, DailyReport> entry : m_cDailyReportTreeMap.entrySet()) { 
			 String date = entry.getKey();
			 DailyReport cDailyReport = entry.getValue();
			 
			 if(lastDate.equals("")) 
			 {
				 lastDate = date;
				 lastRefVal = cDailyReport.dSHComposite;
				 lastVal = cDailyReport.dTotalAssets;
				 cCurvePointList_RefRadio.add(new CurvePoint(0, 0));
			 }
			 
			 if(!lastDate.substring(5, 7).equals(date.substring(5, 7)))
			 {
				 // ���µ�һ��
				 double curRefVal = cDailyReport.dSHComposite;
				 double curVal = cDailyReport.dTotalAssets;
				 
				 // ��Ӳο�ӯ����
				 double refRadio = (curRefVal - lastRefVal)/lastRefVal;
				 double radio = (curVal - lastVal)/lastVal;
				 cCurvePointList_RefRadio.add(new CurvePoint(index, radio-refRadio));
				 
				 // �����ϴ�
				 lastRefVal = cDailyReport.dSHComposite;
				 lastVal = cDailyReport.dTotalAssets;
			 }
			 
			 index++;
			 lastDate = date;
		}
		// �²ο�ӯ��ͼ����
		{
			String filename = CSystem.getRunSessionRoot() + "\\report_ref_month_" + m_name + ".jpg";
			CImageCurve cCImageCurve_abs = new CImageCurve(2560,1920,filename);
			cCImageCurve_abs.writeLogicCurve(cCurvePointList_RefRadio);
			// ��������Ϳ̶�
			cCImageCurve_abs.setColor(Color.BLACK);
			cCImageCurve_abs.writeAxis();
			cCImageCurve_abs.GenerateImage();
		}
	}
	
	private void generate_ref_year()
	{
		// ��ӯ�����ͼ���ο�����
		List<CurvePoint> cCurvePointList_RefRadio = new ArrayList<CurvePoint>();
		double lastRefVal = 0.0;
		double lastVal = 0.0;
		int index=0;
		String lastDate = "";
		for (Map.Entry<String, DailyReport> entry : m_cDailyReportTreeMap.entrySet()) { 
			 String date = entry.getKey();
			 DailyReport cDailyReport = entry.getValue();
			 
			 if(lastDate.equals("")) 
			 {
				 lastDate = date;
				 lastRefVal = cDailyReport.dSHComposite;
				 lastVal = cDailyReport.dTotalAssets;
				 cCurvePointList_RefRadio.add(new CurvePoint(0, 0));
			 }
			 
			 if(!lastDate.substring(0, 4).equals(date.substring(0, 4)))
			 {
				 // �����һ��
				 double curRefVal = cDailyReport.dSHComposite;
				 double curVal = cDailyReport.dTotalAssets;
				 
				 // ��Ӳο�ӯ����
				 double refRadio = (curRefVal - lastRefVal)/lastRefVal;
				 double radio = (curVal - lastVal)/lastVal;
				 cCurvePointList_RefRadio.add(new CurvePoint(index, radio-refRadio));
				 
				 // �����ϴ�
				 lastRefVal = cDailyReport.dSHComposite;
				 lastVal = cDailyReport.dTotalAssets;
			 }
			 
			 index++;
			 lastDate = date;
		}
		// ��ο�ӯ��ͼ����
		{
			String filename = CSystem.getRunSessionRoot() + "\\report_ref_year_" + m_name + ".jpg";
			CImageCurve cCImageCurve_abs = new CImageCurve(2560,1920,filename);
			cCImageCurve_abs.writeLogicCurve(cCurvePointList_RefRadio);
			// ��������Ϳ̶�
			cCImageCurve_abs.setColor(Color.BLACK);
			cCImageCurve_abs.writeAxis();
			cCImageCurve_abs.GenerateImage();
		}
	}
	
	private Map<String, DailyReport> m_cDailyReportTreeMap;
	private String m_name;
}
