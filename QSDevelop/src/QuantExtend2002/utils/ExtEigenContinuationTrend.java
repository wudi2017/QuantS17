package QuantExtend2002.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
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
	
	public static void test(DAKLines kLines) {
		// test checkUpTrend
		{
			int iEnd = kLines.size()-1;
			int iBegin = iEnd-90;
			CLog.output(TAG, "ExtEigenContinuationTrend.check (%s->%s)", kLines.get(iBegin).date, kLines.get(iEnd).date);

			List<Integer> upIdxList = new ArrayList<Integer>();
			
			for (int i = iEnd; i >= iBegin; i--) {
				KLine cKLine =  kLines.get(i);
				CLog.output(TAG, "%s %.3f", cKLine.date, cKLine.close);
				
				boolean bck_up = false;
				TrendInfo info = new TrendInfo();
				bck_up = checkUpTrend(kLines, i, info);
				if (bck_up) { // find current day is up trend
					upIdxList.add(i);
					
					KLine cKLineStart =  kLines.get(info.startIndex);
					CLog.output(TAG, "checkUpTrend %s->%s OK (%d %.3f %.3f)", cKLineStart.date, cKLine.date,
							info.endIndex - info.startIndex, info.changeRateValue, info.changeRateSlope());
					i = i - (info.endIndex - info.startIndex);
				}
			}
			
			// generate show image
			CImageCurve cCImageCurve = new CImageCurve(1600,900,"test_stock_ExtEigenContinuationTrend.jpg");
			List<CurvePoint> PoiList = new ArrayList<CurvePoint>();
			for (int i = iBegin; i <= iEnd; i++) {
				KLine cKLine =  kLines.get(i);
				if (upIdxList.contains(i)) {
					PoiList.add(new CurvePoint(i,cKLine.close, true));
				} else {
					PoiList.add(new CurvePoint(i,cKLine.close));
				}
			}
			cCImageCurve.setColor(Color.ORANGE);
			cCImageCurve.writeLogicCurveSameRatio(PoiList);
			cCImageCurve.setColor(Color.BLACK);
			cCImageCurve.writeAxis();
			cCImageCurve.GenerateImage();
		}
		// test findUpTrend findDownTrend
		{
			List<TrendInfo> upTrendInfoList = new ArrayList<TrendInfo>();
			findUpTrend(kLines, kLines.size()-90, kLines.size()-1, upTrendInfoList);
			for (int i = 0; i < upTrendInfoList.size(); i++) {
				TrendInfo cUpTrendInfo = upTrendInfoList.get(i);
				KLine cKLineStart =  kLines.get(cUpTrendInfo.startIndex);
				KLine cKLineEnd =  kLines.get(cUpTrendInfo.endIndex);
				CLog.output(TAG, "Find UpTrend: %s->%s (%d %.3f %.3f)", cKLineStart.date, cKLineEnd.date, 
						cUpTrendInfo.endIndex - cUpTrendInfo.startIndex, cUpTrendInfo.changeRateValue, cUpTrendInfo.changeRateSlope());
			}
		}
		{
			List<TrendInfo> downTrendInfoList = new ArrayList<TrendInfo>();
			findDownTrend(kLines, kLines.size()-90, kLines.size()-1, downTrendInfoList);
			for (int i = 0; i < downTrendInfoList.size(); i++) {
				TrendInfo cDownTrendInfo = downTrendInfoList.get(i);
				KLine cKLineStart =  kLines.get(cDownTrendInfo.startIndex);
				KLine cKLineEnd =  kLines.get(cDownTrendInfo.endIndex);
				CLog.output(TAG, "Find DownTrend: %s->%s (%d %.3f %.3f)", cKLineStart.date, cKLineEnd.date, 
						cDownTrendInfo.endIndex - cDownTrendInfo.startIndex, cDownTrendInfo.changeRateValue, cDownTrendInfo.changeRateSlope());
			}
		}
		// test isMaxUpTrendRecently
		{
			int iEnd = kLines.size()-1;
			int iBegin = iEnd-90;
			for (int i = iBegin; i <= iEnd; i++) {
				boolean isMaxUpTrendRecently = isMaxUpTrendRecently(kLines, i, 5);
				if (isMaxUpTrendRecently) {
					KLine cKLine =  kLines.get(i);
					CLog.output(TAG, "isMaxUpTrendRecently %s", cKLine.date);
				}
			}
		}
		// test isMaxDownTrendRecently
		{
			int iEnd = kLines.size()-1;
			int iBegin = iEnd-90;
			for (int i = iBegin; i <= iEnd; i++) {
				boolean isMaxDownTrendRecently = isMaxDownTrendRecently(kLines, i, 5);
				if (isMaxDownTrendRecently) {
					KLine cKLine =  kLines.get(i);
					CLog.output(TAG, "isMaxDownTrendRecently %s", cKLine.date);
				}
			}
		}
		// test up down trend max
		{
			int iEnd = kLines.size()-1;
			int iBegin = iEnd-90;
			for (int i = iBegin; i <= iEnd; i++) {
				boolean isMaxUpTrendRecently = isMaxUpTrendRecently(kLines, i, 10);
				boolean isMaxDownTrendRecently = isMaxDownTrendRecently(kLines, i, 5);
				if (isMaxUpTrendRecently && isMaxDownTrendRecently) {
					KLine cKLine =  kLines.get(i);
					CLog.output(TAG, "isMaxUpTrendRecently isMaxDownTrendRecently %s", cKLine.date);
				}
			}
		}
	}
	
	/*
	 * hava max up -> down trend recently
	 * 1. have 3 or more uptrend and 3 more downtrend in 90 days
	 * 2. last upTread is max up changed Rate
	 * 3. last upTread is max price
	 * 4. last downTrend is max down changed Rate
	 * 5. last downTrend is after last upTrend
	 */
	public static boolean isMaxUpDownTrendRecently(DAKLines kLines, int index) {
		if (index < 100) {
			return false;
		}

		// find MaxDownTrend
		boolean isRecentMaxDownTrend = true;
		List<TrendInfo> downTrendInfoList = new ArrayList<TrendInfo>();
		findDownTrend(kLines, index-90, index, downTrendInfoList);
		if (downTrendInfoList.size() >= 5) { // have 5 or more downtread in 90 days
			TrendInfo cDownTrendInfoLast = downTrendInfoList.get(downTrendInfoList.size()-1);
			for (int i = downTrendInfoList.size()-2; i > 0 ; i--) {
				TrendInfo cDownTrendInfoTmp = downTrendInfoList.get(i);
				// last downTread have max changeRateValue
				if (Math.abs(cDownTrendInfoTmp.changeRateValue) > Math.abs(cDownTrendInfoLast.changeRateValue)) { 
					isRecentMaxDownTrend = false;
					break;
				}
			}
		} else {
			isRecentMaxDownTrend = false;
		}
		
		// find MaxUpTrend
		boolean isRecentMaxUpTrend = true;
		List<TrendInfo> upTrendInfoList = new ArrayList<TrendInfo>();
		findUpTrend(kLines, index-90, index, upTrendInfoList);
		if (upTrendInfoList.size() >= 5) { // have 5 or more uptread in 90 days
			TrendInfo cUpTrendInfoLast = upTrendInfoList.get(upTrendInfoList.size()-1);
			for (int i = upTrendInfoList.size()-2; i > 0 ; i--) {
				TrendInfo cUpTrendInfoTmp = upTrendInfoList.get(i);
				// last upTread have max changeRateValue
				// last upTread have max price
				if (cUpTrendInfoTmp.changeRateValue > cUpTrendInfoLast.changeRateValue ||
						cUpTrendInfoTmp.endPrice > cUpTrendInfoLast.endPrice) { 
					isRecentMaxUpTrend = false;
					break;
				}
			}
		} else {
			isRecentMaxUpTrend = false;
		}
		
		if (isRecentMaxDownTrend && isRecentMaxUpTrend) {
			TrendInfo lastDownTrendInfo = downTrendInfoList.get(downTrendInfoList.size()-1);
			TrendInfo lastUpTrendInfo = upTrendInfoList.get(upTrendInfoList.size()-1);
			
			// downTrend happened after upTrend
			if (lastUpTrendInfo.endIndex > lastDownTrendInfo.startIndex ||
					lastUpTrendInfo.startIndex > lastDownTrendInfo.startIndex) { 
				return false;
			}

			// downTrend happened in 5 day
			if (lastDownTrendInfo.endIndex < index - 5) {
				return false;
			}
			
			// upTrend happened in 10 days before downTrend
			if (lastUpTrendInfo.endIndex < lastDownTrendInfo.startIndex - 5) { 
				return false;
			}

			return true;
			
		} else {
			return false;
		}
	}
	
	/*
	 * hava max up trend recently
	 * 1. have 5 or more uptread in 90 days
	 * 2. last upTread is max up changed Rate
	 * 3. last upTread is max price
	 */
	public static boolean isMaxUpTrendRecently(DAKLines kLines, int index, int happenedInDays) {
		if (index < 100) {
			return false;
		}
		boolean isRecentMaxUpTrend = true;
		List<TrendInfo> upTrendInfoList = new ArrayList<TrendInfo>();
		findUpTrend(kLines, index-90, index, upTrendInfoList);
		if (upTrendInfoList.size() >= 5) { // have 5 or more uptread in 90 days
			TrendInfo cUpTrendInfoLast = upTrendInfoList.get(upTrendInfoList.size()-1);
			if (cUpTrendInfoLast.endIndex < index - happenedInDays) { // happen in days
				isRecentMaxUpTrend = false;
			} else {
				for (int i = upTrendInfoList.size()-2; i > 0 ; i--) {
					TrendInfo cUpTrendInfoTmp = upTrendInfoList.get(i);
					// last upTread have max changeRateValue
					// last upTread have max price
					if (cUpTrendInfoTmp.changeRateValue > cUpTrendInfoLast.changeRateValue ||
							cUpTrendInfoTmp.endPrice > cUpTrendInfoLast.endPrice) { 
						isRecentMaxUpTrend = false;
						break;
					}
				}
			}
		} else {
			isRecentMaxUpTrend = false;
		}

		return isRecentMaxUpTrend;
	}
	
	/*
	 * hava max down trend recently
	 * 1. have 5 or more downtread in 90 days
	 * 2. last downTread is max down changed Rate
	 */
	public static boolean isMaxDownTrendRecently(DAKLines kLines, int index, int happenedInDays) {
		if (index < 100) {
			return false;
		}
		boolean isRecentMaxDownTrend = true;
		List<TrendInfo> downTrendInfoList = new ArrayList<TrendInfo>();
		findDownTrend(kLines, index-90, index, downTrendInfoList);
		if (downTrendInfoList.size() >= 5) { // have 5 or more downtread in 90 days
			TrendInfo cDownTrendInfoLast = downTrendInfoList.get(downTrendInfoList.size()-1);
			if (cDownTrendInfoLast.endIndex < index - happenedInDays) { // happen in days
				isRecentMaxDownTrend = false;
			} else {
				for (int i = downTrendInfoList.size()-2; i > 0 ; i--) {
					TrendInfo cDownTrendInfoTmp = downTrendInfoList.get(i);
					// last downTread have max changeRateValue
					if (Math.abs(cDownTrendInfoTmp.changeRateValue) > Math.abs(cDownTrendInfoLast.changeRateValue)) { 
						isRecentMaxDownTrend = false;
						break;
					}
				}
			}
		} else {
			isRecentMaxDownTrend = false;
		}

		return isRecentMaxDownTrend;
	}

	/**********************************************************************************
	 *  BASE
	 */
	static class TrendInfo {
		public int startIndex;
		public String startDate;
		public double startPrice;
		public int endIndex;
		public String endDate;
		public double endPrice;
		public double changeRateValue; // 趋势涨幅跌幅率百分比
		public double changeRateSlope() {// 趋势涨幅跌幅斜率
			return changeRateValue/(endIndex-startIndex);
		}
	}
	/*
	 * find all UpTrend in the period [index iBegin ~ iEnd]
	 */
	public static void findUpTrend(DAKLines kLines, int iBegin, int iEnd, List<TrendInfo> result) {
		if (null == result) {
			return;
		}
		for (int i = iEnd; i >= iBegin; i--) {
			TrendInfo info = new TrendInfo();
			if (checkUpTrend(kLines, i, info)) { // find current day is up trend
				result.add(info);
				i = i - (info.endIndex - info.startIndex);
			}
		}
		Collections.reverse(result);
	}
	
	/*
	 * find all DownTrend in the period [index iBegin ~ iEnd]
	 */
	public static void findDownTrend(DAKLines kLines, int iBegin, int iEnd, List<TrendInfo> result) {
		if (null == result) {
			return;
		}
		for (int i = iEnd; i >= iBegin; i--) {
			TrendInfo info = new TrendInfo();
			if (checkDownTrend(kLines, i, info)) { // find current day is down trend
				result.add(info);
				i = i - (info.endIndex - info.startIndex);
			}
		}
		Collections.reverse(result);
	}
	
	/*
	 * find current index is UpTrend end day or not, return UpTrendInfo in last param.
	 */
	public static boolean checkUpTrend(DAKLines kLines, int iCheck, TrendInfo info) {
		int CHECK_CONTINUES_DAYS = 3;
		
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
		
		int iFindStartIndex = iCheck;
		while (true) {
			if (iDayLeft-1 < 0) { // before unit not exist
				return false;
			}
			
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
			
			if (cUnitClose < cBeforeUnitClose) {
				if (cKLineUnitLeft.close < cKLineUnitMid.close && cKLineUnitLeft.close < cKLineUnitRight.close) {
					iFindStartIndex = iDayLeft;
				} else if (cKLineUnitMid.close < cKLineUnitLeft.close && cKLineUnitMid.close < cKLineUnitRight.close) {
					iFindStartIndex = iDayMid;
				} else {
					iFindStartIndex = iDayRight;
				}
				break;
			}
			
			// reset dayLeft dayRitht
			iDayRight = iDayRight-1;
			iDayMid = iDayMid-1;
			iDayLeft = iDayLeft-1;
		}
		
		int ContinueDaysCnt = iCheck - iFindStartIndex;
		if (ContinueDaysCnt >= CHECK_CONTINUES_DAYS) {
			if (null != info) {
				info.startIndex = iFindStartIndex;
				info.startDate = kLines.get(iFindStartIndex).date;
				info.startPrice = kLines.get(iFindStartIndex).close;
				info.endIndex = iCheck;
				info.endDate = kLines.get(iCheck).date;
				info.endPrice = kLines.get(iCheck).close;
				info.changeRateValue = (kLines.get(info.endIndex).close - kLines.get(info.startIndex).close)/kLines.get(info.startIndex).close;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * find current index is DownTrend end day or not, return DownTrendInfo in last param.
	 */
	public static boolean checkDownTrend(DAKLines kLines, int iCheck, TrendInfo info) {
		int CHECK_CONTINUES_DAYS = 3;
		
		// must has 20 days more
		if (kLines.size() < 20) {
			return false;
		}
			
		int iDayLeft = iCheck-2;
		int iDayMid = iCheck-1;
		int iDayRight = iCheck;
		
		
		// current day close <= before1 day  && current day close <= before2 day
		KLine cKLineCur = kLines.get(iDayRight);
		KLine cKLineCurBefore1 = kLines.get(iDayMid);
		KLine cKLineCurBefore2 = kLines.get(iDayLeft);
		if (cKLineCur.close > cKLineCurBefore1.close || cKLineCur.close > cKLineCurBefore2.close) {
			return false;
		}
		
		int iFindStartIndex = iCheck;
		while (true) {
			if (iDayLeft-1 < 0) { // before unit not exist
				return false;
			}
			
			KLine cKLineUnitLeft = kLines.get(iDayLeft);
			KLine cKLineUnitMid = kLines.get(iDayMid);
			KLine cKLineUnitRight = kLines.get(iDayRight);
			double cUnitClose = cKLineUnitLeft.close < cKLineUnitMid.close? cKLineUnitLeft.close: cKLineUnitMid.close;
			cUnitClose = cUnitClose < cKLineUnitRight.close? cUnitClose: cKLineUnitRight.close;
			
			KLine cKLineBeforeUnitLeft = kLines.get(iDayLeft-1);
			KLine cKLineBeforeUnitMid = kLines.get(iDayMid-1);
			KLine cKLineBeforeUnitRight = kLines.get(iDayRight-1);
			double cBeforeUnitClose = cKLineBeforeUnitLeft.close < cKLineBeforeUnitMid.close? cKLineBeforeUnitLeft.close: cKLineBeforeUnitMid.close;
			cBeforeUnitClose = cBeforeUnitClose < cKLineBeforeUnitRight.close? cBeforeUnitClose: cKLineBeforeUnitRight.close;
			
			if (cUnitClose > cBeforeUnitClose) {
				if (cKLineUnitLeft.close > cKLineUnitMid.close && cKLineUnitLeft.close > cKLineUnitRight.close) {
					iFindStartIndex = iDayLeft;
				} else if (cKLineUnitMid.close > cKLineUnitLeft.close && cKLineUnitMid.close > cKLineUnitRight.close) {
					iFindStartIndex = iDayMid;
				} else {
					iFindStartIndex = iDayRight;
				}
				break;
			}
			
			// reset dayLeft dayRitht
			iDayRight = iDayRight-1;
			iDayMid = iDayMid-1;
			iDayLeft = iDayLeft-1;
		}
		
		int ContinueDaysCnt = iCheck - iFindStartIndex;
		if (ContinueDaysCnt >= CHECK_CONTINUES_DAYS) {
			if (null != info) {
				info.startIndex = iFindStartIndex;
				info.startDate = kLines.get(iFindStartIndex).date;
				info.startPrice = kLines.get(iFindStartIndex).close;
				info.endIndex = iCheck;
				info.endDate = kLines.get(iCheck).date;
				info.endPrice = kLines.get(iCheck).close;
				info.changeRateValue = (kLines.get(info.endIndex).close - kLines.get(info.startIndex).close)/kLines.get(info.startIndex).close;
			}
			return true;
		} else {
			return false;
		}
	}
}
