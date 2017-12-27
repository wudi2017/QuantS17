package teat;

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
		
		selector.addSelect("S1", 1);
		selector.addSelect("S2", 9);
		selector.addSelect("S3", 2);
		selector.addSelect("S3.5", 3);
		selector.addSelect("S4", 7);
		selector.addSelect("S5", 4);
		selector.addSelect("S6", 3);
		selector.addSelect("S7", 11);
		selector.addSelect("S8", 22);
		selector.addSelect("S9", 19);
		selector.addSelect("S10", 5);
		CLog.output("TEST", "1: %s", selector.dumpSelect());
		CTest.EXPECT_LONG_EQ(selector.sizeSelect(), 11);
		
		List<String> filteroutList = new ArrayList<String>();
		filteroutList.add("S2");
		filteroutList.add("S3");
		filteroutList.add("S10");
		selector.filterOut(filteroutList);
		CLog.output("TEST", "2: %s", selector.dumpSelect());
		CTest.EXPECT_LONG_EQ(selector.sizeSelect(), 8);
		
		selector.keepMaxCount(5);
		CLog.output("TEST", "2: %s", selector.dumpSelect());
		CTest.EXPECT_LONG_EQ(selector.sizeSelect(), 5);
		CTest.EXPECT_STR_EQ(selector.selectList().get(0), "S8");
		
	}
	
	public static void main(String[] args) {
		CTest.ADD_TEST(TestQUSelector.class);
		CTest.RUN_ALL_TESTS();
	}
}
