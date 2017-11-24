package utils;

import java.util.*;

import pers.di.account.common.*;
import pers.di.quantplatform.*;

/*
 * 互斥买卖股票管理器
 */
public class XBSStockManager {
	
	public static class SelectResult {
		// 优先级从大到小排序
		public static class SelectResultCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				SelectResult c1 = (SelectResult)object1;
				SelectResult c2 = (SelectResult)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		
		public SelectResult(){
			stockID = "";
			dPriority = 0.0f;
		}
		public String stockID;
		public double dPriority;
	}
	
	public XBSStockManager(AccountProxy ap)
	{
		m_ap = ap;
		m_ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		m_ctnHoldStockList = new ArrayList<HoldStock>();
		
		m_SelectResultList = new ArrayList<SelectResult>();
		
	}
	
	public void addSelect(String stockID, double priority)
	{
		if(!existCommissionOrder(stockID) && !existHoldStock(stockID))
		{
			SelectResult cSelectResult = new SelectResult();
			cSelectResult.stockID = stockID;
			cSelectResult.dPriority = priority;
			m_SelectResultList.add(cSelectResult);
			Collections.sort(m_SelectResultList, new SelectResult.SelectResultCompare());
		}
	}
	public void saveValidSelectCount(int count)
	{
		Collections.sort(m_SelectResultList, new SelectResult.SelectResultCompare());
		if(m_SelectResultList.size() > count)
		{
			Iterator<SelectResult> iter = m_SelectResultList.listIterator(count);
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
	        }
		}
	}
	public List<String> selectList()
	{
		Collections.sort(m_SelectResultList, new SelectResult.SelectResultCompare());
		List<String> selectList = new ArrayList<String>();
		for(int i=0; i<m_SelectResultList.size(); i++)
		{
			selectList.add(m_SelectResultList.get(i).stockID);
		}
		return selectList;
	}
	public void clearSelect()
	{
		m_SelectResultList.clear();
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
	
	private List<SelectResult> m_SelectResultList;
}
