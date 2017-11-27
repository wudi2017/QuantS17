package utils;

import java.util.*;

import pers.di.common.CImageCurve;
import pers.di.common.CImageCurve.CurvePoint;
import pers.di.common.CLog;
import pers.di.common.CSystem;

public class TranReportor {
	
	/*
	 * �ձ���ṹ��
	 */
	public static class DailyReport
	{
		public DailyReport()
		{
		}
		public String date; // ����
		public double dTotalAssets; // ���ʲ�
		public double dSHComposite; // ������ָ֤��
	}
	
	public TranReportor(String name)
	{
		m_imgFileName = CSystem.getRunSessionRoot() + "\\report_" + name + ".jpg";
		m_cDailyReportList = new ArrayList<DailyReport>();
	}
	
	public void InfoCollector(String date, double dTotalAssets, double dSHComposite)
	{
		CLog.output("TEST", "AssetReportor %s %f %f", date, dTotalAssets, dSHComposite);
		
		DailyReport cDailyReportNew = new DailyReport();
		cDailyReportNew.date = date;
		cDailyReportNew.dTotalAssets = dTotalAssets;
		cDailyReportNew.dSHComposite = dSHComposite;
		m_cDailyReportList.add(cDailyReportNew);
		
		// create image
		CImageCurve cCImageCurve = new CImageCurve(2560,1920,m_imgFileName);
		List<CurvePoint> cCurvePointList_TotalAssets = new ArrayList<CurvePoint>();
		List<CurvePoint> cCurvePointList_SHComposite = new ArrayList<CurvePoint>();
		for(int i =0; i< m_cDailyReportList.size(); i++)
		{
			DailyReport cDailyReport = m_cDailyReportList.get(i);
			cCurvePointList_TotalAssets.add(new CurvePoint(i, cDailyReport.dTotalAssets));
			cCurvePointList_SHComposite.add(new CurvePoint(i, cDailyReport.dSHComposite));
		}
		
		cCImageCurve.addLogicCurveSameRatio(cCurvePointList_SHComposite, 1);
		cCImageCurve.addLogicCurveSameRatio(cCurvePointList_TotalAssets, 2);
		cCImageCurve.GenerateImage();
	}
	
	private List<DailyReport> m_cDailyReportList;
	private String m_imgFileName;
}
