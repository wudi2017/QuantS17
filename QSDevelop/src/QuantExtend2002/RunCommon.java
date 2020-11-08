package QuantExtend2002;

import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsDateTime;
import pers.di.localstock.LocalStock;

public class RunCommon {
	public static String TAG = "TEST";
	public static void help() {
		CLog.output(TAG, "RunCommon main. param error.");
		CLog.output(TAG, "Usage:");
		CLog.output(TAG, "    RunCommon operation param");
		CLog.output(TAG, "operation:");
		CLog.output(TAG, "    update");
		CLog.output(TAG, "param:");
		CLog.output(TAG, "    [update] all|000001");
	}
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		if (args.length != 2) {
			help();
		} else {
			if(RunCommon(args[0], args[1]) < 0) {
				help();
			}
		}
		
		CSystem.stop();
	}
	
	public static int RunCommon(String operation, String param) {
		if (operation.equals("update")) {
			if (param.equals("all")) {
				// update all stock
				String dateStr = CUtilsDateTime.GetCurDateStr();
				LocalStock.instance().updateAllLocalStocks(dateStr);
			} else {
				// update 1 stock
				String dateStr = CUtilsDateTime.GetCurDateStr();
				LocalStock.instance().updateLocalStocks(param, dateStr);
			}
			return 0;
		} else {
			return -1;
		}
	}
}
