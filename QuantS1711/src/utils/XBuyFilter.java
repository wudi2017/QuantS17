package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.quantplatform.AccountProxy;
import utils.XSelectFilter.SelectItem;

public class XBuyFilter {

	public static class BuyItem {
		// 优先级从大到小排序
		public static class BuyItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				BuyItem c1 = (BuyItem)object1;
				BuyItem c2 = (BuyItem)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		
		public BuyItem(){
			stockID = "";
			dPriority = 0.0f;
		}
		
		public String stockID;
		public double dPriority;
	}
	
	public XBuyFilter(AccountProxy ap)
	{
		m_ap = ap;	
		m_BuyItemList = new ArrayList<BuyItem>();
	}
	
	public void addBuy(String stockID, double priority)
	{
		// 不重复添加
		for(int i=0; i<m_BuyItemList.size(); i++)
		{
			if(m_BuyItemList.get(i).stockID.equals(stockID))
			{
				return;
			}
		}
		// 不买入已经提交委托或持有的
		if(!existCommissionOrder(stockID) && !existHoldStock(stockID))
		{
			BuyItem cBuyItem = new BuyItem();
			cBuyItem.stockID = stockID;
			cBuyItem.dPriority = priority;
			m_BuyItemList.add(cBuyItem);
			Collections.sort(m_BuyItemList, new BuyItem.BuyItemCompare());
		}
	}
	public void saveValidBuyCount(int count)
	{
		Collections.sort(m_BuyItemList, new BuyItem.BuyItemCompare());
		if(m_BuyItemList.size() > count)
		{
			Iterator<BuyItem> iter = m_BuyItemList.listIterator(count);
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
	        }
		}
	}
	public List<String> buyList()
	{
		Collections.sort(m_BuyItemList, new BuyItem.BuyItemCompare());
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

	private List<BuyItem> m_BuyItemList;
}
