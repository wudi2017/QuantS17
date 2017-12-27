package strategy.QS1711;

import java.util.ArrayList;
import java.util.List;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.account.common.TRANACT;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;
import utils.QS1711.TranReportor;
import utils.QS1711.XStockClearRuleManager;
import utils.QS1711.XStockSelectManager;

/*
 * 策略基础
 * 自带：选股功能增强，清仓规则增强
 */
public abstract class QS1711SCBase extends QuantStrategy  {

	public QS1711SCBase(int iMaxSelectCount, int iMaxHoldCount)
	{
		m_iMaxSelectCount = iMaxSelectCount;
		m_iMaxHoldCount = iMaxHoldCount;
	}
	
	public XStockSelectManager getXStockSelectManager()
	{
		return m_XStockSelectManager;
	}
	public XStockClearRuleManager getXStockClearRuleManager()
	{
		return m_XStockClearRuleManager;
	}
	
	public boolean tryBuy(QuantContext ctx, String stockID)
	{
		return tryBuy(ctx, stockID, 1);
	}
	public boolean tryBuy(QuantContext ctx, String stockID, double validRatio)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// multi commit check
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ctx.ap().getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			CommissionOrder cCommissionOrder = ctnCommissionOrderList.get(i);
			if(cCommissionOrder.stockID.equals(stockID) 
					&& TRANACT.BUY == cCommissionOrder.tranAct
					&& Math.abs((fNowPrice-cCommissionOrder.price)/cCommissionOrder.price) < 0.03)
			{
				// already commit similar today
				return false;
			}
		}

		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		if(ctnHoldStockList.size() < m_iMaxHoldCount)
		{
			CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
			ctx.ap().getTotalAssets(ctnTotalAssets);
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			ctx.ap().getMoney(ctnMoney);
			double dCreateMoney = (ctnMoney.get() > ctnTotalAssets.get()/m_iMaxHoldCount)?ctnTotalAssets.get()/m_iMaxHoldCount:ctnMoney.get();
			if(validRatio>=0 && validRatio<=1)
			{
				dCreateMoney = dCreateMoney*validRatio;
			}
			int iCreateAmount = (int) (dCreateMoney/fNowPrice)/100*100;
			if(iCreateAmount > 0)
			{
				ctx.ap().pushBuyOrder(stockID, iCreateAmount, fNowPrice);
				return true;
			}
		}
		
		return false;
	}
	public boolean trySell(QuantContext ctx, String stockID)
	{
		return trySell(ctx, stockID, 1);
	}
	public boolean trySell(QuantContext ctx, String stockID, double validRatio)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		// multi commit check
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ctx.ap().getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			CommissionOrder cCommissionOrder = ctnCommissionOrderList.get(i);
			if(cCommissionOrder.stockID.equals(stockID) 
					&& TRANACT.SELL == cCommissionOrder.tranAct
					&& Math.abs((fNowPrice-cCommissionOrder.price)/cCommissionOrder.price) < 0.03)
			{
				// already commit similar today
				return false;
			}
		}

		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			HoldStock cHoldStock = ctnHoldStockList.get(i);
			if(cHoldStock.stockID.equals(stockID))
			{
				long trySellAmount = (long)(cHoldStock.totalAmount*validRatio);
				long realSellAmuont = trySellAmount>cHoldStock.availableAmount?cHoldStock.availableAmount:trySellAmount;
				if(realSellAmuont <= 0)
				{
					return false;
				}
				
				ctx.ap().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onInit(QuantContext ctx) {
		m_XStockSelectManager = new XStockSelectManager(ctx.ap());
		m_XStockClearRuleManager = new XStockClearRuleManager(ctx.ap());
		m_TranReportor = new TranReportor(this.getClass().getSimpleName());
		this.onStrateInit(ctx);
	}
	@Override
	public void onDayStart(QuantContext ctx) {
		CLog.output("TEST", "onDayStart %s", ctx.date());
		
		// init select stock
		m_XStockSelectManager.loadFromFile();
		super.addCurrentDayInterestMinuteDataIDs(m_XStockSelectManager.validSelectListS1(m_iMaxSelectCount));
		CLog.output("TEST", "%s", m_XStockSelectManager.dumpSelect());
		
		// init clear rule
		m_XStockClearRuleManager.loadFromFile();

		this.onStrateDayStart(ctx);
	}
	
	@Override
	public void onMinuteData(QuantContext ctx) {
		
		// buy check
		List<String> validSelectList = m_XStockSelectManager.validSelectListS1(m_iMaxSelectCount);
		for(int iStock=0; iStock<validSelectList.size(); iStock++)
		{
			String selectStockID = validSelectList.get(iStock);
			DAStock cDAStock = ctx.pool().get(selectStockID);
			this.onStrateBuyCheck(ctx, cDAStock);
		}
		
		// sell check
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			HoldStock cHoldStock = ctnHoldStockList.get(i);
			DAStock cDAStock = ctx.pool().get(cHoldStock.stockID);
			this.onStrateSellCheck(ctx, cDAStock, cHoldStock);
		}
	}
	
	@Override
	public void onDayFinish(QuantContext ctx) {

		// reset clear
		m_XStockSelectManager.clearSelect();
		this.onStrateDayFinish(ctx);
		m_XStockSelectManager.saveToFile();
		
		// deleteRuleNotInHolds 
		m_XStockClearRuleManager.deleteRuleNotInHolds();
		
		// report
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		ctx.ap().getTotalAssets(ctnTotalAssets);
		double dSH = ctx.pool().get("999999").price();
		m_TranReportor.collectInfo_SHComposite(ctx.date(), dSH);
		m_TranReportor.collectInfo_TotalAssets(ctx.date(), ctnTotalAssets.get());
		m_TranReportor.generateReport();
		CLog.output("TEST", "dump account&select\n %s\n    -%s", ctx.ap().dump(), m_XStockSelectManager.dumpSelect());
	}
	
	/*
	 * 策略初始化
	 * 程序启动时只调用一次
	 */
	abstract void onStrateInit(QuantContext ctx);
	/*
	 * 策略每天启动
	 * 每个交易日开始交易前调用一次
	 */
	abstract void onStrateDayStart(QuantContext ctx);
	/*
	 * 策略买入检查
	 * 交易期间每分钟对每个选入股票(getXStockSelectManager中的)进行回调
	 * 用户调用tryBuy进行买入
	 */
	abstract void onStrateBuyCheck(QuantContext ctx, DAStock cDAStock);
	/*
	 * 策略卖出检查
	 * 交易期间每分钟对每个持有股票进行回调
	 * 用户调用trySell进行卖出
	 */
	abstract void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock);
	/*
	 * 策略选股
	 * 每天交易结束更新数据后进行回调
	 * 用户调用getXStockSelectManager进行选股
	 */
	abstract void onStrateDayFinish(QuantContext ctx);

	private int m_iMaxSelectCount;
	private int m_iMaxHoldCount;
	private XStockSelectManager m_XStockSelectManager;
	private XStockClearRuleManager m_XStockClearRuleManager;
	private TranReportor m_TranReportor;
}
