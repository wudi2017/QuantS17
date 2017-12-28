package test;

import java.util.*;

import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import utils.QS1801.QUSelector;

public class TestQUSelector {

	@CTest.test
	public static void test_QUSelector()
	{
		String runsessionRoot = CSystem.getRunSessionRoot();
		CLog.output("TEST", "%s", runsessionRoot);
		
		QUSelector selector = new QUSelector("test");
		
		selector.selectAdd("S1", 1);
		selector.selectAdd("S2", 9);
		selector.selectAdd("S3", 2);
		selector.selectAdd("S3.5", 3);
		selector.selectAdd("S4", 7);
		selector.selectAdd("S5", 4);
		selector.selectAdd("S6", 3);
		selector.selectAdd("S7", 11);
		selector.selectAdd("S8", 22);
		selector.selectAdd("S9", 19);
		selector.selectAdd("S10", 5);
		CLog.output("TEST", "1: %s", selector.dumpSelect());
		CTest.EXPECT_LONG_EQ(selector.selectSize(), 11);
		
		List<String> rmList = new ArrayList<String>();
		rmList.add("S2");
		rmList.add("S3");
		rmList.add("S10");
		selector.selectRemove(rmList);
		CLog.output("TEST", "2: %s", selector.dumpSelect());
		CTest.EXPECT_LONG_EQ(selector.selectSize(), 8);
		
		selector.selectKeepMaxCount(5);
		CLog.output("TEST", "2: %s", selector.dumpSelect());
		CTest.EXPECT_LONG_EQ(selector.selectSize(), 5);
		CTest.EXPECT_STR_EQ(selector.selectList().get(0), "S8");
		
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestQUSelector.class);
		CTest.RUN_ALL_TESTS();
	}
}
