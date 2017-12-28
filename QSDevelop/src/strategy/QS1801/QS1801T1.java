package strategy.QS1801;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;

public class QS1801T1 extends QS1801Base {

	public QS1801T1() {
		super(3);
	}

	@Override
	void onStrateInit(QuantContext ctx) {
	}

	@Override
	void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	void onStrateBuySellCheck(QuantContext ctx, DAStock cDAStock) {
	}

	@Override
	void onStrateDayFinish(QuantContext ctx) {
	}
	
	/*
	 * *************************************************************************************
	 */
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		cAccoutDriver.load("account_QS1801T1" ,  new MockAccountOpe(), true);
		cAccoutDriver.reset(100000);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2010-01-01 2017-12-15", // Realtime | HistoryTest 2016-01-01 2017-01-01
				cAccoutDriver, 
				new QS1801T1());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
	}

}
