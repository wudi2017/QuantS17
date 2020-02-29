package QuantExtend2002;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import QuantExtend1711.utils.TranReportor;
import QuantExtend2002.utils.*;
import pers.di.common.CLog;
import pers.di.common.CObjectContainer;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.QuantContext;
import pers.di.quantplatform.QuantStrategy;

public abstract class QEBase2002 extends QuantStrategy {
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
	 * �����������
	 * �����ڼ�ÿ���Ӷ�ÿ��ѡ��|���н��лص�
	 */
	abstract void onStrateMinute(QuantContext ctx, DAStock cDAStock);

	/*
	 * ����ѡ��
	 * ÿ�콻�׽����������ݺ���лص�
	 * �û�����m_QEUSelector����ѡ��
	 */
	abstract void onStrateDayFinish(QuantContext ctx);
	
	/*
	 * *****************************************************************************************************
	 */
	
	public QEBase2002()
	{
	}
	@Override
	public void onInit(QuantContext context) {
		String accountIDName = context.accountProxy().ID();
		m_QEUSelector = new QEUSelector(accountIDName);
		m_QEUSelector.loadFromFile();
		m_QEUTranReportor = new QEUTranReportor(accountIDName);
		this.onStrateInit(context);
		m_QEUSelector.saveToFile();
		
	}
	@Override 
	public void onUnInit(QuantContext context){
		
	}
	@Override
	public void onDayStart(QuantContext context){
		m_QEUSelector.loadFromFile();
		context.addCurrentDayInterestMinuteDataIDs(m_QEUSelector.list());
		this.onStrateDayStart(context);
	}
	@Override
	public void onMinuteData(QuantContext context){
		List<String> selectIDs = m_QEUSelector.list();
		List<String> holdIDs = QEUCommon.getHoldStockIDList(context.accountProxy());
		
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.addAll(selectIDs);
		hashSet.addAll(holdIDs);
		List<String> uniqueIDs = new ArrayList<String>();
		uniqueIDs.addAll(hashSet);
		
		for(int iStock=0; iStock<uniqueIDs.size(); iStock++)
		{
			String stockID = uniqueIDs.get(iStock);
			DAStock cDAStock = context.pool().get(stockID);
			this.onStrateMinute(context, cDAStock);
		}
	}
	@Override
	public void onDayFinish(QuantContext context){
		// fetch user select stocks
		m_QEUSelector.clear();
		this.onStrateDayFinish(context);
		m_QEUSelector.saveToFile();
		
		// report
		CObjectContainer<Double> ctnTotalAssets = new CObjectContainer<Double>();
		context.accountProxy().getTotalAssets(ctnTotalAssets);
		double dSH = context.pool().get("999999").price();
		m_QEUTranReportor.collectInfo_SHComposite(context.date(), dSH);
		m_QEUTranReportor.collectInfo_TotalAssets(context.date(), ctnTotalAssets.get());
		m_QEUTranReportor.generateReport();
	}
	
	private QEUSelector m_QEUSelector;
	private QEUTranReportor m_QEUTranReportor;
}
