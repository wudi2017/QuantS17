package QuantExtend2002;

import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class QEStrategy2002T1 extends QEBase2002 {

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
		// TODO Auto-generated method stub
		
	}

	@Override
	void onStrateDayFinish(QuantContext ctx) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) throws Exception {
		
		CSystem.start();
		
		CLog.output("TEST", "QEStrategy2002T1 main begin");
		
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		
		Quant.instance().run("HistoryTest 2020-01-01 2020-01-20", cAccountController, new QEStrategy2002T1());
		
		cAccountController.close();
		
		CSystem.stop();
		
		CLog.output("TEST", "QEStrategy2002T1 main end");
	}
}
