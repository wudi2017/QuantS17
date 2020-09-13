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
 * ���ڲ��岨��
 */
public class ExtEigenCrestTrough {
	public static String TAG = "TEST";
	
	public static void test(DAKLines kLines, int iBegin, int iEnd) {
		CLog.output(TAG, "ExtEigenCrestTrough.check (%s->%s)", kLines.get(iBegin).date, kLines.get(iEnd).date);
		CImageCurve cCImageCurve = new CImageCurve(1600,900,"test_stock.jpg");
		List<CurvePoint> PoiList = new ArrayList<CurvePoint>();
		for (int i = iBegin; i<iEnd; i++) {
			CLog.output(TAG, "%s %.3f", kLines.get(i).date, kLines.get(i).close);
			boolean bck_top = checkCrest(kLines, i);
			PoiList.add(new CurvePoint(i,kLines.get(i).close, bck_top));
			boolean bck_bottm = checkTrough(kLines, i);
			PoiList.add(new CurvePoint(i,kLines.get(i).close, bck_bottm));
		}
		cCImageCurve.setColor(Color.ORANGE);
		cCImageCurve.writeLogicCurveSameRatio(PoiList);
		cCImageCurve.setColor(Color.BLACK);
		cCImageCurve.writeAxis();
		cCImageCurve.GenerateImage();
		
		// find top
//		for (int i = iBegin; i<iEnd; i++) {
//			check(kLines, i);
//		}
		
	}
	
	public static boolean checkCrest(DAKLines kLines, int iCheck) {
		
		//�жϣ�����ĳ��Ϊtop�죬ǰ10��ͺ�10������ߵ㣬��ǰ10���ҵ���͵���ΪlowLeft�죬���10���ҵ���͵���ΪlowRight��
		boolean bTop = false;
		int checkSpanDaysCount = 8;
		int iCheckBegin = iCheck-checkSpanDaysCount > 0? iCheck-checkSpanDaysCount: 0; 
		int iCheckEnd = iCheck+checkSpanDaysCount < kLines.size() -1? iCheck+checkSpanDaysCount:kLines.size() -1;
		if (iCheckEnd - iCheckBegin < 6) {
			CLog.output(TAG, "ExtEigenCrestTrough.check date:%s no enough data.", kLines.get(iCheck).date);
			return false;
		}
		
		// check current day is highest price for -checkSpanDaysCount ~ +checkSpanDaysCount days
		if (ComEigenKLineHighLowFind.indexHigh(kLines, iCheckBegin, iCheckEnd) != iCheck) {
			//CLog.output(TAG, "ExtEigenCrestTrough.check date:%s not top price.", kLines.get(iCheck).date);
			return false;
		}
		
		// find left low day & right low day
		int iIdxLowLeft=ComEigenKLineHighLowFind.indexLow(kLines, iCheckBegin, iCheck-1);
		int iIdxLowRight=ComEigenKLineHighLowFind.indexLow(kLines, iCheck+1, iCheckEnd);

//		CLog.output(TAG, "ExtEigenCrestTrough.check TOPdate:%s lowLeftDate:%s lowRightDate:%s", 
//				kLines.get(iCheck).date, kLines.get(iIdxLowLeft).date, kLines.get(iIdxLowRight).date);
		
		//�жϣ����Ҫ�������lowLeft�������lowRight��ȴ���6�� && �����lowLeft��top���ȴ���3 �����lowRight��top��ȴ���3
		if (iIdxLowRight - iIdxLowLeft < 6 || iCheck -iIdxLowLeft < 3 || iIdxLowRight-iCheck < 3) {
			return false;
		}
		
		//�жϣ�����Ҫ�������׶��м�3���ֵ����������β�������������Ҫʱ������3�죩 ���� ������ʼ3�����̼�ƽ��ֵ��������top�죩 �� ��������3�����̼�ƽ��ֵ֮�䣬 �½��׶�ͬ��
		{
			int addCount = 0;
			int idxLeftMid = (iIdxLowLeft + iCheck)/2;
			
			double leftHeadAvePrice = 0.0f;
			addCount = 0;
			leftHeadAvePrice += kLines.get(iIdxLowLeft).midle(); addCount+=1;
			if (iIdxLowLeft+1<iCheck) {
				leftHeadAvePrice += kLines.get(iIdxLowLeft+1).midle(); addCount+=1;
				
			}
			if (iIdxLowLeft+2<iCheck) {
				leftHeadAvePrice += kLines.get(iIdxLowLeft+2).midle(); addCount+=1;
			}
			leftHeadAvePrice = leftHeadAvePrice/addCount;
			
			double leftMidAvePrice = 0.0f;
			addCount = 0;
			leftMidAvePrice += kLines.get(idxLeftMid).midle(); addCount+=1;
			if (idxLeftMid-1>iIdxLowLeft) {
				leftMidAvePrice += kLines.get(idxLeftMid-1).midle(); addCount+=1;
			}
			if (idxLeftMid+1<iCheck) {
				leftMidAvePrice += kLines.get(idxLeftMid+1).midle(); addCount+=1;
			}
			leftMidAvePrice = leftMidAvePrice/addCount;
			
			double leftTailAvePrice = 0.0f;
			addCount = 0;
			leftTailAvePrice += kLines.get(iCheck).midle(); addCount+=1;
			if (iCheck-1>iIdxLowLeft) {
				leftTailAvePrice += kLines.get(iCheck-1).midle(); addCount+=1;
			}
			if (iCheck-2>iIdxLowLeft) {
				leftTailAvePrice += kLines.get(iCheck-2).midle(); addCount+=1;
			}
			leftTailAvePrice = leftTailAvePrice/addCount;
			
			if(leftHeadAvePrice > leftMidAvePrice || leftMidAvePrice > leftTailAvePrice) {
				return false;
			}
		}
		{
			int addCount = 0;
			int idxRightMid = (iIdxLowRight + iCheck)/2;
			
			double rightHeadAvePrice = 0.0f;
			addCount = 0;
			rightHeadAvePrice += kLines.get(iCheck).midle(); addCount+=1;
			if (iCheck+1<iIdxLowRight) {
				rightHeadAvePrice += kLines.get(iCheck+1).midle(); addCount+=1;
			}
			if (iCheck+2<iIdxLowRight) {
				rightHeadAvePrice += kLines.get(iCheck+2).midle(); addCount+=1;
			}
			rightHeadAvePrice = rightHeadAvePrice/addCount;
			
			double rightMidAvePrice = 0.0f;
			addCount = 0;
			rightMidAvePrice += kLines.get(idxRightMid).midle(); addCount+=1;
			if (idxRightMid-1>iCheck) {
				rightMidAvePrice += kLines.get(idxRightMid-1).midle(); addCount+=1;
			}
			if (idxRightMid+1<iIdxLowRight) {
				rightMidAvePrice += kLines.get(idxRightMid+1).midle(); addCount+=1;
			}
			rightMidAvePrice = rightMidAvePrice/addCount;
			
			double rightTailAvePrice = 0.0f;
			addCount = 0;
			rightTailAvePrice += kLines.get(iIdxLowRight).midle(); addCount+=1;
			if (iIdxLowRight-1>iCheck) {
				rightTailAvePrice += kLines.get(iIdxLowRight-1).midle(); addCount+=1;
			}
			if (iIdxLowRight-2>iCheck) {
				rightTailAvePrice += kLines.get(iIdxLowRight-2).midle(); addCount+=1;
			}
			rightTailAvePrice = rightTailAvePrice/addCount;
			
			if(rightHeadAvePrice < rightMidAvePrice || rightMidAvePrice < rightTailAvePrice) {
				return false;
			}
		}
		
		CLog.output(TAG, "ExtEigenCrestTrough.checkCrest TOPdate:%s OK!", kLines.get(iCheck).date);
		
		return true;
	}
	
	public static boolean checkTrough(DAKLines kLines, int iCheck) {
		
		//�жϣ�����ĳ��Ϊbottom�죬ǰ10��ͺ�10������͵㣬��ǰ10���ҵ���ߵ���ΪhighLeft�죬���10���ҵ���ߵ���ΪhighRight��
		boolean bBottom = false;
		int checkSpanDaysCount = 8;
		int iCheckBegin = iCheck-checkSpanDaysCount > 0? iCheck-checkSpanDaysCount: 0; 
		int iCheckEnd = iCheck+checkSpanDaysCount < kLines.size() -1? iCheck+checkSpanDaysCount:kLines.size() -1;
		if (iCheckEnd - iCheckBegin < 6) {
			CLog.output(TAG, "ExtEigenCrestTrough.check date:%s no enough data.", kLines.get(iCheck).date);
			return false;
		}
		
		// check current day is lowest price for -checkSpanDaysCount ~ +checkSpanDaysCount days
		if (ComEigenKLineHighLowFind.indexLow(kLines, iCheckBegin, iCheckEnd) != iCheck) {
			//CLog.output(TAG, "ExtEigenCrestTrough.check date:%s not top price.", kLines.get(iCheck).date);
			return false;
		}
		
		// find left high day & right high day
		int iIdxHighLeft=ComEigenKLineHighLowFind.indexHigh(kLines, iCheckBegin, iCheck-1);
		int iIdxHighRight=ComEigenKLineHighLowFind.indexHigh(kLines, iCheck+1, iCheckEnd);

//		CLog.output(TAG, "ExtEigenCrestTrough.check TOPdate:%s lowLeftDate:%s lowRightDate:%s", 
//				kLines.get(iCheck).date, kLines.get(iIdxLowLeft).date, kLines.get(iIdxLowRight).date);
		
		//�жϣ����Ҫ����ߵ�HighLeft�������HighRight��ȴ���6�� && �����HighLeft��top���ȴ���3 �����HighRight��top��ȴ���3
		if (iIdxHighRight - iIdxHighLeft < 6 || iCheck -iIdxHighLeft < 3 || iIdxHighRight-iCheck < 3) {
			return false;
		}
		
		//�жϣ�����Ҫ���½��׶��м�3���ֵ����������β�������������Ҫʱ������3�죩 �����½���ʼ3�����̼�ƽ��ֵ��������top�죩 �� �½�����3�����̼�ƽ��ֵ ֮�䣬 �½��׶�ͬ��
		{
			int addCount = 0;
			int idxLeftMid = (iIdxHighLeft + iCheck)/2;
			
			double leftHeadAvePrice = 0.0f;
			addCount = 0;
			leftHeadAvePrice += kLines.get(iIdxHighLeft).midle(); addCount+=1;
			if (iIdxHighLeft+1<iCheck) {
				leftHeadAvePrice += kLines.get(iIdxHighLeft+1).midle(); addCount+=1;
				
			}
			if (iIdxHighLeft+2<iCheck) {
				leftHeadAvePrice += kLines.get(iIdxHighLeft+2).midle(); addCount+=1;
			}
			leftHeadAvePrice = leftHeadAvePrice/addCount;
			
			double leftMidAvePrice = 0.0f;
			addCount = 0;
			leftMidAvePrice += kLines.get(idxLeftMid).midle(); addCount+=1;
			if (idxLeftMid-1>iIdxHighLeft) {
				leftMidAvePrice += kLines.get(idxLeftMid-1).midle(); addCount+=1;
			}
			if (idxLeftMid+1<iCheck) {
				leftMidAvePrice += kLines.get(idxLeftMid+1).midle(); addCount+=1;
			}
			leftMidAvePrice = leftMidAvePrice/addCount;
			
			double leftTailAvePrice = 0.0f;
			addCount = 0;
			leftTailAvePrice += kLines.get(iCheck).midle(); addCount+=1;
			if (iCheck-1>iIdxHighLeft) {
				leftTailAvePrice += kLines.get(iCheck-1).midle(); addCount+=1;
			}
			if (iCheck-2>iIdxHighLeft) {
				leftTailAvePrice += kLines.get(iCheck-2).midle(); addCount+=1;
			}
			leftTailAvePrice = leftTailAvePrice/addCount;
			
			if(leftHeadAvePrice < leftMidAvePrice || leftMidAvePrice < leftTailAvePrice) {
				return false;
			}
		}
		{
			int addCount = 0;
			int idxRightMid = (iIdxHighRight + iCheck)/2;
			
			double rightHeadAvePrice = 0.0f;
			addCount = 0;
			rightHeadAvePrice += kLines.get(iCheck).midle(); addCount+=1;
			if (iCheck+1<iIdxHighRight) {
				rightHeadAvePrice += kLines.get(iCheck+1).midle(); addCount+=1;
			}
			if (iCheck+2<iIdxHighRight) {
				rightHeadAvePrice += kLines.get(iCheck+2).midle(); addCount+=1;
			}
			rightHeadAvePrice = rightHeadAvePrice/addCount;
			
			double rightMidAvePrice = 0.0f;
			addCount = 0;
			rightMidAvePrice += kLines.get(idxRightMid).midle(); addCount+=1;
			if (idxRightMid-1>iCheck) {
				rightMidAvePrice += kLines.get(idxRightMid-1).midle(); addCount+=1;
			}
			if (idxRightMid+1<iIdxHighRight) {
				rightMidAvePrice += kLines.get(idxRightMid+1).midle(); addCount+=1;
			}
			rightMidAvePrice = rightMidAvePrice/addCount;
			
			double rightTailAvePrice = 0.0f;
			addCount = 0;
			rightTailAvePrice += kLines.get(iIdxHighRight).midle(); addCount+=1;
			if (iIdxHighRight-1>iCheck) {
				rightTailAvePrice += kLines.get(iIdxHighRight-1).midle(); addCount+=1;
			}
			if (iIdxHighRight-2>iCheck) {
				rightTailAvePrice += kLines.get(iIdxHighRight-2).midle(); addCount+=1;
			}
			rightTailAvePrice = rightTailAvePrice/addCount;
			
			if(rightHeadAvePrice > rightMidAvePrice || rightMidAvePrice > rightTailAvePrice) {
				return false;
			}
		}
		
		CLog.output(TAG, "ExtEigenCrestTrough.checkTrough Bottomdate:%s OK!", kLines.get(iCheck).date);
		
		return true;
	}
}
