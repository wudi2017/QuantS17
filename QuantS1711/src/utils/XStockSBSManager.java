package utils;

import java.util.*;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.quantplatform.AccountProxy;

public class XStockSBSManager {

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
	
	public static class InnerBuyStockItem {

		public static class InnerBuyStockItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				InnerBuyStockItem c1 = (InnerBuyStockItem)object1;
				InnerBuyStockItem c2 = (InnerBuyStockItem)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		public InnerBuyStockItem(){
			stockID = "";
			dPriority = 0.0f;
		}
		public String stockID;
		public double dPriority;
	}
	
	
	public XStockSBSManager(AccountProxy ap)
	{
		m_ap = ap;	
		m_SelectItemList = new ArrayList<InnerSelectStockItem>();
		m_BuyItemList = new ArrayList<InnerBuyStockItem>();
	}
	
	//-----------------------------------------------------------------------------------------
	// select op
		
	public void addSelect(String stockID, double priority)
	{
		// ���ظ����
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			if(m_SelectItemList.get(i).stockID.equals(stockID))
			{
				return;
			}
		}
		// ������Ѿ��ύί�л���е�
		if(!existCommissionOrder(stockID) && !existHoldStock(stockID))
		{
			InnerSelectStockItem cInnerSelectStockItem = new InnerSelectStockItem();
			cInnerSelectStockItem.stockID = stockID;
			cInnerSelectStockItem.dPriority = priority;
			m_SelectItemList.add(cInnerSelectStockItem);
			Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		}
	}
	public void saveValidSelectCount(int count)
	{
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		if(m_SelectItemList.size() > count)
		{
			Iterator<InnerSelectStockItem> iter = m_SelectItemList.listIterator(count);
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
	        }
		}
	}
	public List<String> selectList()
	{
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
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
	
	//-----------------------------------------------------------------------------------------
	// buy op
	
	public void addBuy(String stockID, double priority)
	{
		// ���ظ����
		for(int i=0; i<m_BuyItemList.size(); i++)
		{
			if(m_BuyItemList.get(i).stockID.equals(stockID))
			{
				return;
			}
		}
		// �������Ѿ��ύί�л���е�
		if(!existCommissionOrder(stockID) && !existHoldStock(stockID))
		{
			InnerBuyStockItem cInnerBuyStockItem = new InnerBuyStockItem();
			cInnerBuyStockItem.stockID = stockID;
			cInnerBuyStockItem.dPriority = priority;
			m_BuyItemList.add(cInnerBuyStockItem);
			Collections.sort(m_BuyItemList, new InnerBuyStockItem.InnerBuyStockItemCompare());
		}
	}
	public void setBuyValidCount(int count)
	{
		Collections.sort(m_BuyItemList, new InnerBuyStockItem.InnerBuyStockItemCompare());
		if(m_BuyItemList.size() > count)
		{
			Iterator<InnerBuyStockItem> iter = m_BuyItemList.listIterator(count);
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
	        }
		}
	}
	public List<String> buyList()
	{
		Collections.sort(m_BuyItemList, new InnerBuyStockItem.InnerBuyStockItemCompare());
		List<String> buyList = new ArrayList<String>();
		for(int i=0; i<m_BuyItemList.size(); i++)
		{
			buyList.add(m_BuyItemList.get(i).stockID);
		}
		return buyList;
	}
	public void clearBuy()
	{
		m_BuyItemList.clear();
	}
	public String dumpBuy()
	{
		int iAddCount = buyList().size();
		String logStr = "";
		logStr += String.format("Buy (%d) [ ", iAddCount);
		if(iAddCount == 0) logStr += "null ";
		for(int i=0; i< iAddCount; i++)
		{
			String stockId = buyList().get(i);
			logStr += String.format("%s ", stockId);
			if (i >= 7 && buyList().size()-1 > 8) {
				logStr += String.format("... ", stockId);
				break;
			}
		}
		logStr += String.format("]");
		return logStr;
	}
	
	//-----------------------------------------------------------------------------------------
	// private help
	
	public boolean existCommissionOrder(String stockID)
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
	
	public boolean existHoldStock(String stockID)
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
	private List<InnerBuyStockItem> m_BuyItemList;
}
