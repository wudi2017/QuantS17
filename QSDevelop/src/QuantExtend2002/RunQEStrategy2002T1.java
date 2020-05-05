package QuantExtend2002;

import QuantExtend2002.framework.QEBase2002;
import QuantExtend2002.utils.ExtEigenMorningCross;
import pers.di.account.AccountController;
import pers.di.account.IAccount;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsMath;
import pers.di.dataengine.DAStock;
import pers.di.localstock.common.KLine;
import pers.di.quantplatform.Quant;
import pers.di.quantplatform.QuantContext;

public class RunQEStrategy2002T1 extends QEBase2002 {
	public static String TAG = "TEST";
	@Override
	public void onStrateInit(QuantContext ctx) {
		// clear
		this.selector().clear();
		// init
		this.property().setGlobalStockMaxCount(3L);
		this.property().setGlobalHoldOneStockMaxPositionRatio(0.3);
		this.property().setGlobalHoldOneStockMaxMarketValue(5*10000.0);
		this.property().setGlobalBuyOneStockCommitMaxPositionRatio(0.3);
		this.property().setGlobalBuyOneStockCommitMaxMarketValue(5*10000.0);
		this.property().setGlobalStockMinCommitInterval(60L);
		this.property().setGlobalStockMaxHoldDays(20L);
		this.property().setGlobalStockTargetProfitRatio(0.06);
		this.property().setGlobalStockStopLossRatio(-0.05);
	}

	@Override
	public void onStrateDayStart(QuantContext ctx) {
	}

	@Override
	public void onStrateMinute(QuantContext ctx, DAStock cDAStock) {
		double fYesterdayClosePrice = cDAStock.dayKLines().lastPrice();
		double fNowPrice = cDAStock.price();
		
		// 1-��ͣ�����
		double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
		double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
		if(0 == Double.compare(fDieTing, fNowPrice))
		{
			return;
		}
		
		Double EntityMid = this.property().getPrivateStockPropertyDouble(cDAStock.ID(), "EntityMid");
		//������ڶ�Ӧ�糿֮�Ƿ����յ��м�ֵ�۸��򷢳������ź�
		if (null != EntityMid && fNowPrice < EntityMid) {
			this.transactionController().buySignalEmit(ctx, cDAStock.ID());
		}
	}

	@Override
	public void onStrateDayFinish(QuantContext ctx) {
		this.selector().setMaxCount(1);
		
		// transfer all stock
		for (int iStock = 0; iStock < ctx.pool().size(); iStock++) {
			DAStock cStock = ctx.pool().get(iStock);
			if(!cStock.date().equals(ctx.date())) {
				/* this stock newest dayK not exist at current date, continue! 
				 * CANNOT be selected. */
				continue; 
			}
			
			// ��Ʊ��K���ϣ�5���ڴ����糿֮��
			int iBegin = cStock.dayKLines().size()-1-5;
			int iEnd = cStock.dayKLines().size()-1;
			for(int iDayCheck=iEnd;iDayCheck>=iBegin;iDayCheck--) {
				boolean bZCZX = ExtEigenMorningCross.check(cStock.dayKLines(), iDayCheck);
				if(bZCZX) {
					double scoreCalcAveWeight = ExtEigenMorningCross.scoreCalcAveWeight(cStock.dayKLines(), iDayCheck);
					//CLog.output(TAG, "onDayFinish %s %s ZCZXScore:%.2f", ctx.date(), cStock.ID(), scoreCalcAveWeight);
					
					KLine cKlineZCZX = cStock.dayKLines().get(iDayCheck);
					KLine cKlineCross = cStock.dayKLines().get(iDayCheck-1);
					
					// ����糿֮�Ƿ������Ƿ��й��ϴ��Ƿ�������й��ϴ��Ƿ�����ԣ�������ѡ��
					for(int iCheckMax=iDayCheck; iCheckMax<cStock.dayKLines().size()-1; iCheckMax++) {
						KLine cKlineCheck = cStock.dayKLines().get(iCheckMax);
						if((cKlineCheck.high - cKlineCross.entityMidle())/cKlineCross.entityMidle() > 0.08) {
							continue;
						}
					}

					//ѡ���Ʊ
					this.selector().add(cStock.ID(), scoreCalcAveWeight);
					
					//TODO �����Ʊ�����Ǹ����˻���ʼֵ�㶨���õģ�û��ָ������Ч����������Ҫ��Ϊ���ݵ�ǰ�˻����ʲ��������ã�����ָ������Ч��
					
					//���ö�Ӧ����Ĳο��۸�Ϊ�糿֮�Ƿ����յ��м�ֵ�۸�
					this.property().setPrivateStockPropertyDouble(cStock.ID(), "EntityMid", cKlineZCZX.entityMidle());
					//���ö�Ӧ��Ʊ��ֹ��۸�Ϊʮ���ǵ͵�
					//this.property().setPrivateStockPropertyStopLossPrice(cStock.ID(), cKlineCross.low);
				}
			}
		}

		CLog.output(TAG, ctx.accountProxy().dump() + "\n    -"+this.selector().dump());
		//this.selector().clear();
	}

	public static void main(String[] args) throws Exception {
		CSystem.start();
		CLog.output(TAG, "RunQEStrategy2002T1 main begin");
		
		AccountController cAccountController = new AccountController(CSystem.getRWRoot() + "\\account");
		cAccountController.open("fast_mock001", true);
		cAccountController.reset(100000);
		// "HistoryTest 2019-01-01 2020-02-20" "Realtime"
		Quant.instance().run("HistoryTest 2019-01-01 2020-03-20", cAccountController, new RunQEStrategy2002T1()); 
		CLog.output(TAG, "%s", cAccountController.account().dump());
		cAccountController.close();
		
		CSystem.stop();
		CLog.output(TAG, "RunQEStrategy2002T1 main end");
	}
}
