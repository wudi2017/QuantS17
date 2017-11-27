package utils;

import java.util.*;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.quantplatform.AccountProxy;

public class XStockSelectManager {

	public static class InnerSelectStockItem {

		public static class InnerSelectStockItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				InnerSelectStockItem c1 = (InnerSelectStockItem)object1;
				InnerSelectStockItem c2 = (InnerSelectStockItem)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		public InnerSelectStockItem(){
			stockID = "";
			dPriority = 0.0f;
		}
		public String stockID;
		public double dPriority;
	}
	
	public XStockSelectManager(AccountProxy ap)
	{
		m_ap = ap;	
		m_SelectItemList = new ArrayList<InnerSelectStockItem>();
	}
	
	//-----------------------------------------------------------------------------------------
	// select op
		
	public void addSelect(String stockID, double priority)
	{
		// 不重复添加
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			if(m_SelectItemList.get(i).stockID.equals(stockID))
			{
				return;
			}
		}
		// 不添加已经提交委托或持有的
		if(!existCommissionOrder(stockID) && !existHoldStock(stockID))
		{
			InnerSelectStockItem cInnerSelectStockItem = new InnerSelectStockItem();
			cInnerSelectStockItem.stockID = stockID;
			cInnerSelectStockItem.dPriority = priority;
			m_SelectItemList.add(cInnerSelectStockItem);
			Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		}
	}
	public List<String> validSelectList(int maxCount)
	{
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		List<String> selectList = new ArrayList<String>();
		int iAddCount = 0;
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			String stockID = m_SelectItemList.get(i).stockID;
			if(!existCommissionOrder(stockID)
					&& !existHoldStock(stockID))
			{
				selectList.add(m_SelectItemList.get(i).stockID);
				iAddCount++;
				if(iAddCount >= maxCount)
				{
					break;
				}
			}
		}
		return selectList;
	}
	public void clearSelect()
	{
		m_SelectItemList.clear();
	}
	public String dumpSelect()
	{
		List<String> validSelectList = validSelectList(20);
		int iAddCount = validSelectList.size();
		String logStr = "";
		logStr += String.format("Selected (%d) [ ", iAddCount);
		if(iAddCount == 0) logStr += "null ";
		for(int i=0; i< iAddCount; i++)
		{
			String stockId = validSelectList.get(i);
			logStr += String.format("%s ", stockId);
			if (i >= 7 && validSelectList.size()-1 > 8) {
				logStr += String.format("... ", stockId);
				break;
			}
		}
		logStr += String.format("]");
		return logStr;
	}
	
	//-----------------------------------------------------------------------------------------
	// private help
	
	private boolean existCommissionOrder(String stockID)
	{
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		m_ap.getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			if(ctnCommissionOrderList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean existHoldStock(String stockID)
	{
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		m_ap.getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			if(ctnHoldStockList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		return false;
	}
	
	private AccountProxy m_ap;
	private List<InnerSelectStockItem> m_SelectItemList;
}
