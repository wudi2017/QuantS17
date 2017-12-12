package utils;

import java.awt.Color;
import java.util.*;

import pers.di.common.CImageCurve;
import pers.di.common.CImageCurve.CurvePoint;
import pers.di.common.CLog;
import pers.di.common.CSystem;

public class TranReportor {
	
	/*
	 * 日报告结构体
	 */
	public static class DailyReport
	{
		public DailyReport()
		{
		}
		public double dTotalAssets; // 总资产
		public double dSHComposite; // 当日上证指数
	}
	
	public TranReportor(String name)
	{
		m_imgFileName = CSystem.getRunSessionRoot() + "\\report_" + name + ".jpg";
		m_cDailyReportTreeMap = new TreeMap<String, DailyReport>((o1, o2) -> {
			String str1 = (String)o1;
			String str2 = (String)o2;
		    //如果有空值，直接返回0
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
		List<CurvePoint> cCurvePointList_TotalAssets = new ArrayList<CurvePoint>();
		List<CurvePoint> cCurvePointList_SHComposite = new ArrayList<CurvePoint>();
		
		int index=0;
		for (Map.Entry<String, DailyReport> entry : m_cDailyReportTreeMap.entrySet()) { 
			 String date = entry.getKey();
			 DailyReport cDailyReport = entry.getValue();
			 
			 cCurvePointList_TotalAssets.add(new CurvePoint(index, cDailyReport.dTotalAssets));
			 cCurvePointList_SHComposite.add(new CurvePoint(index, cDailyReport.dSHComposite));
				
			 index++;
		}
		
		CImageCurve cCImageCurve = new CImageCurve(2560,1920,m_imgFileName);
		cCImageCurve.setColor(Color.BLUE);
		cCImageCurve.writeLogicCurveSameRatio(cCurvePointList_SHComposite);
		cCImageCurve.setColor(Color.GREEN);
		cCImageCurve.writeLogicCurveSameRatio(cCurvePointList_TotalAssets);
		cCImageCurve.writeMultiLogicCurveSameRatio();
		cCImageCurve.setColor(Color.BLACK);
		cCImageCurve.writeAxis();
		cCImageCurve.GenerateImage();
		
	}
	
	private Map<String, DailyReport> m_cDailyReportTreeMap;
	private String m_imgFileName;
}
