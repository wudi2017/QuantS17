package test;

import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import utils.XStockSelectManager;

public class TestXStockSelectManager {
	
	@CTest.test
	public static void test_TestXStockStrategyUtils()
	{
		String runsessionRoot = CSystem.getRunSessionRoot();
		CLog.output("TEST", "%s", runsessionRoot);
		
		XStockSelectManager selectMgr = new XStockSelectManager("test");
		
		selectMgr.addSelect("S1", 1);
		selectMgr.addSelect("S2", 9);
		selectMgr.addSelect("S3", 2);
		selectMgr.addSelect("S3.5", 3);
		selectMgr.addSelect("S4", 7);
		selectMgr.addSelect("S5", 4);
		selectMgr.addSelect("S6", 3);
		
		CLog.output("TEST", "%s", selectMgr.dumpSelect());
		
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestXStockSelectManager.class);
		CTest.RUN_ALL_TESTS("TestXStockSelectManager.");
	}
}
