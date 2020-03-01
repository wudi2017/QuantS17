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
	
	public QEUTranReportor(String name)
	{
		m_name = name;
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
		generate_abs();
		generate_ref_month();
		generate_ref_year();
	}
	
	private void generate_abs()
	{
		List<CurvePoint> cCurvePointList_TotalAssets = new ArrayList<CurvePoint>();
		List<CurvePoint> cCurvePointList_SHComposite = new ArrayList<CurvePoint>();
		List<Double> spanList = new ArrayList<Double>();
		
		// 按照key值排序遍历，生成曲线
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
				spanList.add(index/(double)iSize); // 小于30天 每天都有刻度
			 }
			 else if(iSize > 30 && iSize <= 600)  // 大于30太难 小于 20个月， 每月都有刻度
			 {
				 if(!lastDate.substring(5, 7).equals(date.substring(5, 7)))
				 {
					 spanList.add(index/(double)iSize);
				 }
			 }
			 else if(iSize > 600) // 大于20个月， 每年都有刻度
			 {
				 if(!lastDate.substring(0, 4).equals(date.substring(0, 4)))
				 {
					 spanList.add(index/(double)iSize);
				 }
			 }
			 
			 index++;
			 lastDate = date;
		}
		
		// 把曲线表示成同比图
		{
			String filename = CSystem.getRunSessionRoot() + "\\report_abs_" + m_name + ".jpg";
			CImageCurve cCImageCurve_abs = new CImageCurve(2560,1920,filename);
			cCImageCurve_abs.setColor(Color.BLUE);
			cCImageCurve_abs.writeLogicCurveSameRatio(cCurvePointList_SHComposite);
			cCImageCurve_abs.setColor(Color.GREEN);
			cCImageCurve_abs.writeLogicCurveSameRatio(cCurvePointList_TotalAssets);
			cCImageCurve_abs.writeMultiLogicCurveSameRatio();
			// 绘制坐标和刻度
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
		// 月盈亏相对图，参考大盘
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
				 // 隔月第一天
				 double curRefVal = cDailyReport.dSHComposite;
				 double curVal = cDailyReport.dTotalAssets;
				 
				 // 添加参考盈亏比
				 double refRadio = (curRefVal - lastRefVal)/lastRefVal;
				 double radio = (curVal - lastVal)/lastVal;
				 cCurvePointList_RefRadio.add(new CurvePoint(index, radio-refRadio));
				 
				 // 保存上次
				 lastRefVal = cDailyReport.dSHComposite;
				 lastVal = cDailyReport.dTotalAssets;
			 }
			 
			 index++;
			 lastDate = date;
		}
		// 月参考盈亏图保存
		{
			String filename = CSystem.getRunSessionRoot() + "\\report_ref_month_" + m_name + ".jpg";
			CImageCurve cCImageCurve_abs = new CImageCurve(2560,1920,filename);
			cCImageCurve_abs.writeLogicCurve(cCurvePointList_RefRadio);
			// 绘制坐标和刻度
			cCImageCurve_abs.setColor(Color.BLACK);
			cCImageCurve_abs.writeAxis();
			cCImageCurve_abs.GenerateImage();
		}
	}
	
	private void generate_ref_year()
	{
		// 年盈亏相对图，参考大盘
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
				 // 隔年第一天
				 double curRefVal = cDailyReport.dSHComposite;
				 double curVal = cDailyReport.dTotalAssets;
				 
				 // 添加参考盈亏比
				 double refRadio = (curRefVal - lastRefVal)/lastRefVal;
				 double radio = (curVal - lastVal)/lastVal;
				 cCurvePointList_RefRadio.add(new CurvePoint(index, radio-refRadio));
				 
				 // 保存上次
				 lastRefVal = cDailyReport.dSHComposite;
				 lastVal = cDailyReport.dTotalAssets;
			 }
			 
			 index++;
			 lastDate = date;
		}
		// 年参考盈亏图保存
		{
			String filename = CSystem.getRunSessionRoot() + "\\report_ref_year_" + m_name + ".jpg";
			CImageCurve cCImageCurve_abs = new CImageCurve(2560,1920,filename);
			cCImageCurve_abs.writeLogicCurve(cCurvePointList_RefRadio);
			// 绘制坐标和刻度
			cCImageCurve_abs.setColor(Color.BLACK);
			cCImageCurve_abs.writeAxis();
			cCImageCurve_abs.GenerateImage();
		}
	}
	
	private Map<String, DailyReport> m_cDailyReportTreeMap;
	private String m_name;
}
