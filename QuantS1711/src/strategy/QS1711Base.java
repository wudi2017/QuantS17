package strategy;

import java.util.ArrayList;
import java.util.List;

import pers.di.account.common.HoldStock;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.common.CUtilsMath;
import pers.di.dataapi.common.KLine;
import pers.di.dataengine.DAKLines;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;
import utils.PricePosChecker;
import utils.TranDaysChecker;
import utils.TranReportor;
import utils.XStockSelectManager;
import utils.ZCZXChecker;
import utils.PricePosChecker.ResultDropParam;

public abstract class QS1711Base extends QuantStrategy {
	
	public QS1711Base(int iMaxSelectCount, int iMaxHoldCount)
	{
		m_iMaxSelectCount = iMaxSelectCount;
		m_iMaxHoldCount = iMaxHoldCount;
	}
	
	public XStockSelectManager getXStockSelectManager()
	{
		return m_XStockSelectManager;
	}
	
	public void tryBuy(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		if(ctnHoldStockList.size() < m_iMaxHoldCount)
		{
			CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
			ctx.ap().getTotalAssets(ctnTotalAssets);
			CObjectContainer<Double> ctnMoney = new CObjectContainer<Double>();
			ctx.ap().getMoney(ctnMoney);
			double dCreateMoney = (ctnMoney.get() > ctnTotalAssets.get()/m_iMaxHoldCount)?ctnTotalAssets.get()/m_iMaxHoldCount:ctnMoney.get();
			int iCreateAmount = (int) (dCreateMoney/fNowPrice)/100*100;
			if(iCreateAmount > 0)
			{
				ctx.ap().pushBuyOrder(stockID, iCreateAmount, fNowPrice);
			}
		}
	}
	
	public void trySell(QuantContext ctx, String stockID)
	{
		DAStock cDAStock = ctx.pool().get(stockID);
		double fNowPrice = cDAStock.price();
		
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ctx.ap().getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			HoldStock cHoldStock = ctnHoldStockList.get(i);
			if(cHoldStock.stockID.equals(stockID))
			{
				ctx.ap().pushSellOrder(cHoldStock.stockID, cHoldStock.availableAmount, fNowPrice);
			}
		}
	}
	
	@Override
	public void onInit(QuantContext ctx) {
		m_XStockSelectManager = new XStockSelectManager(ctx.ap());
		m_TranReportor = new TranReportor(this.getClass().getSimpleName());
		this.onStrateInit(ctx);
	}
	@Override
	public void onDayStart(QuantContext ctx) {
		CLog.output("TEST", "onDayStart %s", ctx.date());
		m_XStockSelectManager.loadFromFile();
		super.addCurrentDayInterestMinuteDataIDs(m_XStockSelectManager.validSelectListS1(m_iMaxSelectCount));
		CLog.output("TEST", "%s", m_XStockSelectManager.dumpSelect());
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

		m_XStockSelectManager.clearSelect();

		for(int iStock=0; iStock<ctx.pool().size(); iStock++)
		{
			DAStock cDAStock = ctx.pool().get(iStock);
			this.onStrateDayFinish(ctx, cDAStock);
		}

		m_XStockSelectManager.saveToFile();
		
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
	 * ���Գ�ʼ��
	 * ��������ʱֻ����һ��
	 */
	abstract void onStrateInit(QuantContext ctx);
	/*
	 * ����ÿ������
	 * ÿ�������տ�ʼ����ǰ����һ��
	 */
	abstract void onStrateDayStart(QuantContext ctx);
	/*
	 * ����������
	 * �����ڼ�ÿ���Ӷ�ÿ��ѡ���Ʊ(getXStockSelectManager�е�)���лص�
	 * �û�����tryBuy��������
	 */
	abstract void onStrateBuyCheck(QuantContext ctx, DAStock cDAStock);
	/*
	 * �����������
	 * �����ڼ�ÿ���Ӷ�ÿ�����й�Ʊ���лص�
	 * �û�����trySell��������
	 */
	abstract void onStrateSellCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock);
	/*
	 * ����ѡ��
	 * ÿ�콻�׽����������ݺ���г����й�Ʊ���лص�
	 * �û�����getXStockSelectManager����ѡ��
	 */
	abstract void onStrateDayFinish(QuantContext ctx, DAStock cDAStock);

	private int m_iMaxSelectCount;
	private int m_iMaxHoldCount;
	private XStockSelectManager m_XStockSelectManager;
	private TranReportor m_TranReportor;
}
