package fasttest;

import java.util.*;

import pers.di.account.Account;
import pers.di.account.AccoutDriver;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.marketaccount.mock.MockAccountOpe;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantSession;
import pers.di.quantplatform.QuantStrategy;
import utils.PricePosChecker;
import utils.PricePosChecker.ResultLongDropParam;
import utils.SelectResult;
import utils.ZCZXChecker;
import utils.ZCZXChecker.ResultDYCheck;

public class FastTest {

	public static class FastTestStrategy extends QuantStrategy
	{
		public FastTestStrategy()
		{
			m_seletctID = new ArrayList<String>();
		}
		
		@Override
		public void onInit(QuantContext arg0) {
		}
		@Override
		public void onDayStart(QuantContext arg0) {
		}
		@Override
		public void onMinuteData(QuantContext arg0) {
		}
		@Override
		public void onDayFinish(QuantContext ctx) {
			CLog.output("TEST", "onDayFinish %s", ctx.date());
			
			m_seletctID.clear();
			List<SelectResult> cSelectResultList = new ArrayList<SelectResult>();
			
			for(int iStock=0; iStock<ctx.pool().size(); iStock++)
			{
				DAStock cDAStock = ctx.pool().get(iStock);
				
				// 过滤：股票ID集合，当天检查
				boolean bCheckX = false;
				if(cDAStock.ID().compareTo("000001") >= 0 && cDAStock.ID().compareTo("000200") <= 0 
						&&cDAStock.dayKLines().lastDate().equals(ctx.date())
						&&cDAStock.dayKLines().size()>60) {	
					bCheckX = true;
				}
				
				if(bCheckX)
				{
					// 5天内存在早晨之星
					int iBegin = cDAStock.dayKLines().size()-1-5;
					int iEnd = cDAStock.dayKLines().size()-1;
					for(int i=iEnd;i>=iBegin;i--)
					{
						ResultDYCheck cResultDYCheck = ZCZXChecker.check(cDAStock.dayKLines(),i);
						if(cResultDYCheck.bCheck)
						{
							
							ResultLongDropParam cResultLongDropParam = PricePosChecker.getLongDropParam(cDAStock.dayKLines(), cDAStock.dayKLines().size()-1);
							SelectResult cSelectResult = new SelectResult();
							cSelectResult.stockID = cDAStock.ID();
							cSelectResult.fPriority = -cResultLongDropParam.refHigh;
							cSelectResultList.add(cSelectResult);
							
						}
					}
				}
			}
			
			Collections.sort(cSelectResultList, new SelectResult.SelectResultCompare());
			
			int maxSelectCnt = 30;
			int iSelectCount = cSelectResultList.size();
			int iAddCount = iSelectCount>maxSelectCnt?maxSelectCnt:iSelectCount;
			for(int i=0; i<iAddCount; i++)
			{
				m_seletctID.add(cSelectResultList.get(i).stockID);
			}
		}
		
		private List<String> m_seletctID;
	}
	
	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		CLog.output("TEST", "FastTest main begin");
		
		// create testaccount
		AccoutDriver cAccoutDriver = new AccoutDriver(CSystem.getRWRoot() + "\\account");
		cAccoutDriver.load("fast_mock001" ,  new MockAccountOpe(), true);
		Account acc = cAccoutDriver.account();
		
		QuantSession qSession = new QuantSession(
				"HistoryTest 2016-01-01 2017-01-01", 
				cAccoutDriver, 
				new FastTestStrategy());
		qSession.run();
		
		CLog.output("TEST", "FastTest main end");
		CSystem.stop();
		
		
	}
}
