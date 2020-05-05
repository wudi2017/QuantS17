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
		
		// 1-跌停不买进
		double fYC = CUtilsMath.saveNDecimal(fYesterdayClosePrice, 2);
		double fDieTing = CUtilsMath.saveNDecimal(fYC*0.9f, 2);
		if(0 == Double.compare(fDieTing, fNowPrice))
		{
			return;
		}
		
		Double EntityMid = this.property().getPrivateStockPropertyDouble(cDAStock.ID(), "EntityMid");
		//如果低于对应早晨之星发生日的中间值价格，则发出买入信号
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
			
			// 股票日K线上，5天内存在早晨之星
			int iBegin = cStock.dayKLines().size()-1-5;
			int iEnd = cStock.dayKLines().size()-1;
			for(int iDayCheck=iEnd;iDayCheck>=iBegin;iDayCheck--) {
				boolean bZCZX = ExtEigenMorningCross.check(cStock.dayKLines(), iDayCheck);
				if(bZCZX) {
					double scoreCalcAveWeight = ExtEigenMorningCross.scoreCalcAveWeight(cStock.dayKLines(), iDayCheck);
					//CLog.output(TAG, "onDayFinish %s %s ZCZXScore:%.2f", ctx.date(), cStock.ID(), scoreCalcAveWeight);
					
					KLine cKlineZCZX = cStock.dayKLines().get(iDayCheck);
					KLine cKlineCross = cStock.dayKLines().get(iDayCheck-1);
					
					// 检查早晨之星发生后是否有过较大涨幅，如果有过较大涨幅则忽略，不进行选入
					for(int iCheckMax=iDayCheck; iCheckMax<cStock.dayKLines().size()-1; iCheckMax++) {
						KLine cKlineCheck = cStock.dayKLines().get(iCheckMax);
						if((cKlineCheck.high - cKlineCross.entityMidle())/cKlineCross.entityMidle() > 0.08) {
							continue;
						}
					}

					//选入股票
					this.selector().add(cStock.ID(), scoreCalcAveWeight);
					
					//TODO 买入股票数量是根据账户初始值恒定设置的，没有指数增益效果，后续需要改为根据当前账户总资产进行配置，产生指数收益效果
					
					//设置对应买入的参考价格为早晨之星发生日的中间值价格
					this.property().setPrivateStockPropertyDouble(cStock.ID(), "EntityMid", cKlineZCZX.entityMidle());
					//设置对应股票的止损价格为十字星低点
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
