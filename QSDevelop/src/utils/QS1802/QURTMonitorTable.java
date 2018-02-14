package utils.QS1802;

import java.util.*;

import pers.di.common.CSyncObj;
import pers.di.common.CXmlTable;
import pers.di.common.CXmlTable.RowCursor;
import utils.QS1802.QUSelectTable.SelectItem;

public class QURTMonitorTable {
	
	public static interface ICallback
	{
		public enum CALLBACKTYPE
		{
			INVALID,
			CHANGED,
			COMMITED,
		}
		abstract public void onNotify(CALLBACKTYPE cb);
	}
	
	public QURTMonitorTable(String fileName)
	{
		m_sync = new CSyncObj();
		m_monitorMap = new HashMap<String, MonitorItem>();
		m_CXmlTable = new CXmlTable(fileName);
	}
	
	public void registerCallback(ICallback cb)
	{
		m_sync.Lock();
		m_ICallback = cb;
		m_sync.UnLock();
	}
	
	public boolean open()
	{
		m_sync.Lock();
		m_CXmlTable.open();
		RowCursor cursor = m_CXmlTable.moveFirst();
		while(null!=cursor)
		{
			String sStockID = cursor.getColume("stockID");
			String sStrategy = cursor.getColume("strategy");
			String sBuyTriggerPrice = cursor.getColume("buyTriggerPrice");
			String sSellTriggerPrice = cursor.getColume("sellTriggerPrice");
			String sMinCommitInterval = cursor.getColume("minCommitInterval");
			String sOneCommitAmount = cursor.getColume("oneCommitAmount");
			String sMaxHoldAmount = cursor.getColume("maxHoldAmount");
			String sTargetProfitPrice = cursor.getColume("targetProfitPrice");
			String sTargetProfitMoney = cursor.getColume("targetProfitMoney");
			String sStopLossPrice = cursor.getColume("stopLossPrice");
			String sStopLossMoney = cursor.getColume("stopLossMoney");
			String sMaxHoldDays = cursor.getColume("maxHoldDays");
			
			MonitorItem cMonitorItem = new MonitorItem(this);
			if(null != sStockID)
				cMonitorItem.m_sStrategy = sStrategy;
			if(null != sBuyTriggerPrice)
				cMonitorItem.m_dBuyTriggerPrice = Double.parseDouble(sBuyTriggerPrice);
			if(null != sSellTriggerPrice)
				cMonitorItem.m_dSellTriggerPrice = Double.parseDouble(sSellTriggerPrice);
			if(null != sMinCommitInterval)
				cMonitorItem.m_lMinCommitInterval = Long.parseLong(sMinCommitInterval);
			if(null != sOneCommitAmount)
				cMonitorItem.m_lOneCommitAmount = Long.parseLong(sOneCommitAmount);
			if(null != sMaxHoldAmount)
				cMonitorItem.m_lMaxHoldAmount = Long.parseLong(sMaxHoldAmount);
			if(null != sTargetProfitPrice)
				cMonitorItem.m_dTargetProfitPrice = Double.parseDouble(sTargetProfitPrice);
			if(null != sTargetProfitMoney)
				cMonitorItem.m_dTargetProfitMoney = Double.parseDouble(sTargetProfitMoney);
			if(null != sStopLossPrice)
				cMonitorItem.m_dStopLossPrice = Double.parseDouble(sStopLossPrice);
			if(null != sStopLossMoney)
				cMonitorItem.m_dStopLossMoney = Double.parseDouble(sStopLossMoney);
			if(null != sMaxHoldDays)
				cMonitorItem.m_dMaxHoldDays = Long.parseLong(sMaxHoldDays);

			m_monitorMap.put(sStockID, cMonitorItem);
			
			cursor = m_CXmlTable.moveNext();
		}
		m_sync.UnLock();
		return true;
	}
	public boolean commit()
	{
		m_sync.Lock();
		
		m_CXmlTable.deleteAll();
		
		for (Map.Entry<String, MonitorItem> entry : m_monitorMap.entrySet()) { 
			String stockID = entry.getKey();
			MonitorItem cMonitorItem = entry.getValue();
			
			RowCursor cRowCursor = m_CXmlTable.addRow();
			cRowCursor.setColume("stockID", stockID);
			if(null != cMonitorItem.m_sStrategy)
				cRowCursor.setColume("strategy", cMonitorItem.m_sStrategy);
			if(null != cMonitorItem.m_dBuyTriggerPrice)
				cRowCursor.setColume("buyTriggerPrice", String.format("%.3f", cMonitorItem.m_dBuyTriggerPrice));
			if(null != cMonitorItem.m_dSellTriggerPrice)
				cRowCursor.setColume("sellTriggerPrice", String.format("%.3f", cMonitorItem.m_dSellTriggerPrice));
			if(null != cMonitorItem.m_lMinCommitInterval)
				cRowCursor.setColume("minCommitInterval", String.format("%d", cMonitorItem.m_lMinCommitInterval));
			if(null != cMonitorItem.m_lOneCommitAmount)
				cRowCursor.setColume("oneCommitAmount", String.format("%d", cMonitorItem.m_lOneCommitAmount));
			if(null != cMonitorItem.m_lMaxHoldAmount)
				cRowCursor.setColume("maxHoldAmount", String.format("%d", cMonitorItem.m_lMaxHoldAmount));
			if(null != cMonitorItem.m_dTargetProfitPrice)
				cRowCursor.setColume("targetProfitPrice", String.format("%.3f", cMonitorItem.m_dTargetProfitPrice));
			if(null != cMonitorItem.m_dTargetProfitMoney)
				cRowCursor.setColume("targetProfitMoney", String.format("%.3f", cMonitorItem.m_dTargetProfitMoney));
			if(null != cMonitorItem.m_dStopLossPrice)
				cRowCursor.setColume("stopLossPrice", String.format("%.3f", cMonitorItem.m_dStopLossPrice));
			if(null != cMonitorItem.m_dStopLossMoney)
				cRowCursor.setColume("stopLossMoney", String.format("%.3f", cMonitorItem.m_dStopLossMoney));
			if(null != cMonitorItem.m_dMaxHoldDays)
				cRowCursor.setColume("maxHoldDays", String.format("%d", cMonitorItem.m_dMaxHoldDays));
		}
		
		boolean bRet = m_CXmlTable.commit();
		
		if(null != m_ICallback)
		{
			m_ICallback.onNotify(ICallback.CALLBACKTYPE.COMMITED);
		}
		
		m_sync.UnLock();
		return bRet;
	}
	
	public MonitorItem item(String stockID)
	{
		m_sync.Lock();
		MonitorItem reItem = m_monitorMap.get(stockID);
		m_sync.UnLock();
		return reItem;
	}
	
	// ¿½±´Ô­Ê¼Ö»¶Á¾µÏñ
	public Map<String, MonitorItem> copyOriginROMirrorMap()
	{
		m_sync.Lock();
		Map<String, MonitorItem> retMap = new HashMap<String, MonitorItem>();
		retMap.putAll(m_monitorMap);
		m_sync.UnLock();
		return retMap;
	}
	
	public void addItem(String stockID)
	{
		m_sync.Lock();
		if(!m_monitorMap.containsKey(stockID))
		{
			MonitorItem cMonitorItem = new MonitorItem(this);
			m_monitorMap.put(stockID, cMonitorItem);
			if(null != m_ICallback)
			{
				m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
			}
		}
		m_sync.UnLock();
	}
	
	public void removeItem(String stockID)
	{
		m_sync.Lock();
		if(m_monitorMap.containsKey(stockID))
		{
			m_monitorMap.remove(stockID);
			if(null != m_ICallback)
			{
				m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
			}
		}
		m_sync.UnLock();
		return;
	}
	
	public void removeAllItem()
	{
		m_sync.Lock();
		if(m_monitorMap.size() > 0)
		{
			m_monitorMap.clear();
			if(null != m_ICallback)
			{
				m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
			}
		}
		m_sync.UnLock();
		return;
	}
	
	public List<String> monitorStockIDs()
	{
		m_sync.Lock();
		List<String> retList = new ArrayList<String>();
		for (Map.Entry<String, MonitorItem> entry : m_monitorMap.entrySet()) { 
			String stockID = entry.getKey();
			retList.add(stockID);
		}
		m_sync.UnLock();
		return retList;
	}
	
	/**
	 *********************************************************************************************
	 */
	public static class MonitorItem
	{
		public MonitorItem(QURTMonitorTable ower)
		{
			m_ower = ower;
		}

		public String strategy()
		{
			return m_sStrategy;
		}
		public void setStrategy(String strategy)
		{
			m_sStrategy = strategy;
			onItemChanged();
		}
		
		public Double buyTriggerPrice()
		{
			return m_dBuyTriggerPrice;
		}
		public void setBuyTriggerPrice(Double buyTriggerPrice)
		{
			m_dBuyTriggerPrice = buyTriggerPrice;
			onItemChanged();
		}

		public Double sellTriggerPrice()
		{
			return m_dSellTriggerPrice;
		}
		public void setSellTriggerPrice(Double sellTriggerPrice)
		{
			m_dSellTriggerPrice = sellTriggerPrice;
			onItemChanged();
		}
		
		public Long minCommitInterval()
		{
			return m_lMinCommitInterval;
		}
		public void setMinCommitInterval(Long minCommitInterval)
		{
			m_lMinCommitInterval = minCommitInterval;
			onItemChanged();
		}
		
		public Long oneCommitAmount()
		{
			return m_lOneCommitAmount;
		}
		public void setOneCommitAmount(Long oneCommitAmount)
		{
			m_lOneCommitAmount = oneCommitAmount;
			onItemChanged();
		}
		
		public Long maxHoldAmount()
		{
			return m_lMaxHoldAmount;
		}
		public void setMaxHoldAmount(Long maxHoldAmount)
		{
			m_lMaxHoldAmount = maxHoldAmount;
			onItemChanged();
		}
		
		public Double targetProfitMoney()
		{
			return m_dTargetProfitMoney;
		}
		public void setTargetProfitMoney(Double targetProfitMoney)
		{
			m_dTargetProfitMoney = targetProfitMoney;
			onItemChanged();
		}
		
		public Double targetProfitPrice()
		{
			return m_dTargetProfitPrice;
		}
		public void setTargetProfitPrice(Double targetProfitPrice)
		{
			m_dTargetProfitPrice = targetProfitPrice;
			onItemChanged();
		}
		
		public Double stopLossPrice()
		{
			return m_dStopLossPrice;
		}
		public void setStopLossPrice(Double stopLossPrice)
		{
			m_dStopLossPrice = stopLossPrice;
			onItemChanged();
		}
		
		public Double stopLossMoney()
		{
			return m_dStopLossMoney;
		}
		public void setStopLossMoney(Double stopLossMoney)
		{
			m_dStopLossMoney = stopLossMoney;
			onItemChanged();
		}
		
		public Long maxHoldDays()
		{
			return m_dMaxHoldDays;
		}
		public void setMaxHoldDays(Long maxHoldDays)
		{
			m_dMaxHoldDays = maxHoldDays;
			onItemChanged();
		}
		
		
		private void onItemChanged()
		{
			if(null != m_ower && null != m_ower.m_ICallback)
			{
				m_ower.m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
			}
		}
		
		private QURTMonitorTable m_ower;
		
		private String m_sStrategy; // P/M
		private Double m_dBuyTriggerPrice; 
		private Double m_dSellTriggerPrice;
		private Long m_lMinCommitInterval; // min
		private Long m_lOneCommitAmount;
		private Long m_lMaxHoldAmount;
		private Double m_dTargetProfitPrice;
		private Double m_dTargetProfitMoney;
		private Double m_dStopLossPrice;
		private Double m_dStopLossMoney;
		private Long m_dMaxHoldDays;
	}
	
	public CSyncObj m_sync;
	private ICallback m_ICallback;
	private Map<String, MonitorItem> m_monitorMap;
	private CXmlTable m_CXmlTable;
}
