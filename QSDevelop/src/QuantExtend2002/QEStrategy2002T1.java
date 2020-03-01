package QuantExtend2002;

import QuantExtend1711.utils.ZCZXChecker;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class QEStrategy2002T1 extends QEBase2002 {
	public static String TAG = "TEST";
	@Override
	void onStrateInit(QuantContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onStrateDayStart(QuantContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		this.transactionController().buySignalEmit(ctx, cDAStock.ID());
//		if (ctx.time().equals("13:00:00")) {
//			if (cDAStock.timePrices().size() != 0 ) {
//				CLog.output(TAG, "onStrateMinute %s %s %s %.02f (timePrices newest Price)", ctx.date(), ctx.time(), cDAStock.ID(), cDAStock.price());
//			} else {
//				CLog.output(TAG, "onStrateMinute %s %s %s %.02f (last day close price)", ctx.date(), ctx.time(), cDAStock.ID(), cDAStock.price());
//			}
//			
//			if (ctx.date().equals("2020-02-04")) {
//				this.transactionController().buySignalEmit(ctx, cDAStock.ID());
//				CLog.output(TAG, "onStrateMinute %s %s transactionController().buySignalEmit", ctx.date(), ctx.time());
//			}
//			
//		}
	}

	@Override
	void onStrateDayFinish(QuantContext ctx) {
		
		for (int i = 0; i < ctx.pool().size(); i++) {
			DAStock cStock = ctx.pool().get(i);
			//CLog.output(TAG, "onStrateDayFinish %s check:%s", ctx.date(), cStock.ID());
			//if (cStock.ID().compareTo("600033") >= 0 && cStock.ID().compareTo("600033") <= 0) {
				boolean bZCZX = ZCZXChecker.check(cStock.dayKLines(), cStock.dayKLines().size()-1);
				if(bZCZX) {
					CLog.output(TAG, "onStrateDayFinish %s ZCZX selector:%s", ctx.date(), cStock.ID());
				}
//			}
			//}
			

		}
		
		
//		if(ctx.date().equals("2020-02-03")) {
//			this.selector().setMaxCount(3);
//			for (int i = 0; i < ctx.pool().size(); i++) {
//				DAStock cStock = ctx.pool().get(i);
//				if(cStock.ID().equals("600000")) {
//					this.selector().add(cStock.ID(), 0);
//					CLog.output(TAG, "onStrateDayFinish %s %s selector: %s", ctx.date(), ctx.time(), cStock.ID());
//				}
//			}
//		}
		
	}

	public static void main(String[] args) throws Exception {
		
		CSystem.start();
		
		CLog.output(TAG, "QEStrategy2002T1 main begin");
		
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		
		Quant.instance().run("HistoryTest 2019-01-01 2020-02-20", cAccountController, new QEStrategy2002T1());
		
		CLog.output(TAG, "%s", cAccountController.account().dump());
		
		cAccountController.close();
		
		CSystem.stop();
		
		CLog.output(TAG, "QEStrategy2002T1 main end");
	}
}
