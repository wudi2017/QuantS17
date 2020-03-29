package QuantExtend2002;

import java.util.ArrayList;
import java.util.List;

import QuantExtend2002.framework.QEBase2002;
import pers.di.account.AccountController;
import pers.di.account.common.CommissionOrder;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CSystem;
import pers.di.common.CTest;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class TestQEBase2002 extends QEBase2002 {
	public static String TAG = "TEST";
	@Override
	public void onStrateInit(QuantContext ctx) {
		this.property().setGlobalStockMaxCount(2L);
		this.property().setGlobalHoldOneStockMaxMarketValue(5*10000.0);
		this.property().setGlobalBuyOneStockCommitMaxMarketValue(5*10000.0);
		this.property().setGlobalStockMinCommitInterval(60L);
		this.property().setGlobalStockMaxHoldDays(30L);
		this.property().setGlobalStockTargetProfitRatio(0.05);
		this.property().setGlobalStockStopLossRatio(-0.05);
	}

	@Override
	public void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	public void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		if (ctx.time().equals("13:00:00")) {
			if (cDAStock.timePrices().size() != 0 ) {
				CLog.output(TAG, "onStrateMinute %s %s %s %.02f (timePrices newest Price)", ctx.date(), ctx.time(), cDAStock.ID(), cDAStock.price());
			} else {
				CLog.output(TAG, "onStrateMinute %s %s %s %.02f (last day close price)", ctx.date(), ctx.time(), cDAStock.ID(), cDAStock.price());
			}
			
			if (ctx.date().equals("2020-02-04")) {
				CTest.EXPECT_DOUBLE_EQ(cDAStock.price(), 10.60, 2);
				this.transactionController().buySignalEmit(ctx, cDAStock.ID());
				CLog.output(TAG, "onStrateMinute %s %s transactionController().buySignalEmit", ctx.date(), ctx.time());
			}
			
			if (ctx.date().equals("2020-02-17")) {
				CTest.EXPECT_DOUBLE_EQ(cDAStock.price(), 11.04, 2);
				this.transactionController().sellSignalEmit(ctx, cDAStock.ID());
				CLog.output(TAG, "onStrateMinute %s %s transactionController().sellSignalEmit", ctx.date(), ctx.time());
			}
			
		}
	}

	@Override
	public void onStrateDayFinish(QuantContext ctx) {
		if(ctx.date().equals("2020-02-03")) {
			this.selector().setMaxCount(3);
			for (int i = 0; i < ctx.pool().size(); i++) {
				DAStock cStock = ctx.pool().get(i);
				if(cStock.ID().equals("600000")) {
					this.selector().add(cStock.ID(), 0);
					CLog.output(TAG, "onStrateDayFinish %s %s selector: %s", ctx.date(), ctx.time(), cStock.ID());
				}
			}
		}
		if (ctx.date().equals("2020-02-04")) {
			List<CommissionOrder> commissionList = new ArrayList<CommissionOrder>();
			ctx.accountProxy().getCommissionOrderList(commissionList);
			CTest.EXPECT_LONG_EQ(commissionList.size(), 1);
			CTest.EXPECT_LONG_EQ(commissionList.get(0).amount, 1800);
			CTest.EXPECT_DOUBLE_EQ(commissionList.get(0).price, 10.60);
		}
		
	}

	@CTest.test
	public static void test_QEBase2002() {
		CLog.output(TAG, "test_QEBase2002 main begin");
		
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("TestQEBase2002_account", true);
		cAccountController.reset(100000);
		
		Quant.instance().run("HistoryTest 2020-02-01 2020-02-20", cAccountController, new TestQEBase2002());
		
		CLog.output(TAG, "%s", cAccountController.account().dump());
		
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		cAccountController.account().getTotalAssets(ctnTotalAssets);
		CTest.EXPECT_DOUBLE_EQ(ctnTotalAssets.get(), 100761.349, 2); 
		// costBuy: 5+(1800*10.6)*0.00002=5.3816=5.382 costSell: 5+0.39744+19.872=25.26944=25.269
		// profit: (11.04-10.6)*1800=792
		// 792-5.382-25.269=761.349
		cAccountController.close();
		
		CLog.output(TAG, "test_QEBase2002 main end");
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		CTest.ADD_TEST(TestQEBase2002.class);
		CTest.RUN_ALL_TESTS("TestQEBase2002.");
		CSystem.stop();
	}
}
