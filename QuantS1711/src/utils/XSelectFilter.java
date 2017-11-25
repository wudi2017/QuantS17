package utils;

import java.util.*;

import pers.di.account.common.*;
import pers.di.quantplatform.*;

/*
 * 互斥买卖股票管理器
 */
public class XSelectFilter {
	
	public static class SelectItem {
		// 优先级从大到小排序
		public static class SelectItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				SelectItem c1 = (SelectItem)object1;
				SelectItem c2 = (SelectItem)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		
		public SelectItem(){
			stockID = "";
			dPriority = 0.0f;
		}
		public String stockID;
		public double dPriority;
	}
	
	public XSelectFilter(AccountProxy ap)
	{
		m_ap = ap;
		m_ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		m_ctnHoldStockList = new ArrayList<HoldStock>();
		
		m_SelectItemList = new ArrayList<SelectItem>();
		
	}
	
	public void addSelect(String stockID, double priority)
	{
		if(!existCommissionOrder(stockID) && !existHoldStock(stockID))
		{
			SelectItem cSelectItem = new SelectItem();
			cSelectItem.stockID = stockID;
			cSelectItem.dPriority = priority;
			m_SelectItemList.add(cSelectItem);
			Collections.sort(m_SelectItemList, new SelectItem.SelectItemCompare());
		}
	}
	public void saveValidSelectCount(int count)
	{
		Collections.sort(m_SelectItemList, new SelectItem.SelectItemCompare());
		if(m_SelectItemList.size() > count)
		{
			Iterator<SelectItem> iter = m_SelectItemList.listIterator(count);
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
	        }
		}
	}
	public List<String> selectList()
	{
		Collections.sort(m_SelectItemList, new SelectItem.SelectItemCompare());
		List<String> selectList = new ArrayList<String>();
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			selectList.add(m_SelectItemList.get(i).stockID);
		}
		return selectList;
	}
	public void clearSelect()
	{
		m_SelectItemList.clear();
	}
	public String dumpSelect()
	{
		int iAddCount = selectList().size();
		String logStr = "";
		logStr += String.format("Selected (%d) [ ", iAddCount);
		if(iAddCount == 0) logStr += "null ";
		for(int i=0; i< iAddCount; i++)
		{
			String stockId = selectList().get(i);
			logStr += String.format("%s ", stockId);
			if (i >= 7 && selectList().size()-1 > 8) {
				logStr += String.format("... ", stockId);
				break;
			}
		}
		logStr += String.format("]");
		return logStr;
	}
	
	private boolean existCommissionOrder(String stockID)
	{
		m_ctnCommissionOrderList.clear();
		m_ap.getCommissionOrderList(m_ctnCommissionOrderList);
		for(int i=0; i<m_ctnCommissionOrderList.size(); i++)
		{
			if(m_ctnCommissionOrderList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean existHoldStock(String stockID)
	{
		m_ctnHoldStockList.clear();
		m_ap.getHoldStockList(m_ctnHoldStockList);
		for(int i=0; i<m_ctnHoldStockList.size(); i++)
		{
			if(m_ctnHoldStockList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		return false;
	}
	
	
	private AccountProxy m_ap;
	List<CommissionOrder> m_ctnCommissionOrderList;
	List<HoldStock> m_ctnHoldStockList;
	
	private List<SelectItem> m_SelectItemList;
}
