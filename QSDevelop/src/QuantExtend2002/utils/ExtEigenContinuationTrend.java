package QuantExtend2002.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import pers.di.common.CImageCurve;
import pers.di.common.CLog;
import pers.di.common.CImageCurve.CurvePoint;
import pers.di.dataengine.DAKLines;
import pers.di.localstock.common.KLine;

/*
 * 连续趋势
 * 
 * 连续若干天涨跌判断
 * 以连续3天为小单位进行判断，避免单天的波动对趋势判断的影响
 */
public class ExtEigenContinuationTrend {
	public static String TAG = "TEST";
	
	public static void test(DAKLines kLines, int iBegin, int iEnd) {
		CLog.output(TAG, "ExtEigenContinuationTrend.check (%s->%s)", kLines.get(iBegin).date, kLines.get(iEnd).date);
		CImageCurve cCImageCurve = new CImageCurve(1600,900,"test_stock_ExtEigenContinuationTrend.jpg");
		List<CurvePoint> PoiList = new ArrayList<CurvePoint>();
		for (int i = iBegin; i <= iEnd; i++) {
			PoiList.add(new CurvePoint(i,0));
		}
		
		int iStartUpIndex = iEnd + 1;
		for (int i = iEnd; i >= iBegin; i--) {
			KLine cKLine =  kLines.get(i);
			
			CLog.output(TAG, "%s %.3f", cKLine.date, cKLine.close);
			PoiList.get(i-iBegin).m_y = cKLine.close;
			
			if (i < iStartUpIndex) {
				boolean bck_up = false;
				UpTrendInfo info = new UpTrendInfo();
				bck_up = checkUpTrend(kLines, i, info);
				if (bck_up) {
					KLine cKLineStart =  kLines.get(info.startIndex);
					CLog.output(TAG, "checkUpTrend %s->%s OK (%d %.3f %.3f)", cKLineStart.date, cKLine.date,
							info.endIndex - info.startIndex, info.incRate(), info.incRateSlope());
					iStartUpIndex = info.startIndex;
					PoiList.get(i-iBegin).m_marked = true;
				}
			}
		}
		cCImageCurve.setColor(Color.ORANGE);
		cCImageCurve.writeLogicCurveSameRatio(PoiList);
		cCImageCurve.setColor(Color.BLACK);
		cCImageCurve.writeAxis();
		cCImageCurve.GenerateImage();
	}
	
	static class UpTrendInfo {
		public int startIndex;
		public int endIndex;
		public DAKLines kLines;
		public double incRate() { // 趋势涨幅
			return (kLines.get(endIndex).close - kLines.get(startIndex).close)/kLines.get(startIndex).close;
		}
		public double incRateSlope() {// 趋势涨幅斜率
			return incRate()/(endIndex-startIndex);
		}
	}
	public static boolean checkUpTrend(DAKLines kLines, int iCheck, UpTrendInfo info) {
		
		// must has 20 days more
		if (kLines.size() < 20) {
			return false;
		}
			
		int iDayLeft = iCheck-2;
		int iDayMid = iCheck-1;
		int iDayRight = iCheck;
		
		
		// current day close >= before1 day  && current day close >= before2 day
		KLine cKLineCur = kLines.get(iDayRight);
		KLine cKLineCurBefore1 = kLines.get(iDayMid);
		KLine cKLineCurBefore2 = kLines.get(iDayLeft);
		if (cKLineCur.close < cKLineCurBefore1.close || cKLineCur.close < cKLineCurBefore2.close) {
			return false;
		}
		
		int ContinueDaysCnt = 0;
		
		while (true) {
			
			KLine cKLineUnitLeft = kLines.get(iDayLeft);
			KLine cKLineUnitMid = kLines.get(iDayMid);
			KLine cKLineUnitRight = kLines.get(iDayRight);
			double cUnitClose = cKLineUnitLeft.close > cKLineUnitMid.close? cKLineUnitLeft.close: cKLineUnitMid.close;
			cUnitClose = cUnitClose > cKLineUnitRight.close? cUnitClose: cKLineUnitRight.close;
			
			KLine cKLineBeforeUnitLeft = kLines.get(iDayLeft-1);
			KLine cKLineBeforeUnitMid = kLines.get(iDayMid-1);
			KLine cKLineBeforeUnitRight = kLines.get(iDayRight-1);
			double cBeforeUnitClose = cKLineBeforeUnitLeft.close > cKLineBeforeUnitMid.close? cKLineBeforeUnitLeft.close: cKLineBeforeUnitMid.close;
			cBeforeUnitClose = cBeforeUnitClose > cKLineBeforeUnitRight.close? cBeforeUnitClose: cKLineBeforeUnitRight.close;
			
			boolean bCheckUp = false;
			if (cUnitClose >= cBeforeUnitClose) {
				ContinueDaysCnt++;
			} else {
				break;
			}
			
			// reset dayLeft dayRitht
			iDayRight = iDayRight-1;
			iDayMid = iDayMid-1;
			iDayLeft = iDayLeft-1;
		}
		
		if (ContinueDaysCnt >= 2) {
			if (null != info) {
				info.startIndex = iCheck - ContinueDaysCnt -1;
				info.endIndex = iCheck;
				info.kLines = kLines;
			}
			return true;
		} else {
			return false;
		}
	}
}
