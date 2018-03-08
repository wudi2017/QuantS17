package utils.QS1802;

import java.util.*;

import pers.di.common.*;
import pers.di.common.CXmlTable.RowCursor;
import utils.QS1802.QURTMonitorTable.MonitorItem;

public class QUSelectTable {
	
	public static interface ICallback
	{
		public enum CALLBACKTYPE
		{
			INVALID,
			CHANGED,
		}
		abstract public void onNotify(CALLBACKTYPE cb);
	}
	
	public QUSelectTable(String fileName)
	{
		m_sync = new CSyncObj();
		m_ICallback = null;
		m_SelectItemList = new ArrayList<SelectItem>();
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
			SelectItem cSelectItem = new SelectItem();
			
			cSelectItem.m_sStockID = cursor.getColume("stockID");
			cSelectItem.m_dPriority = Double.parseDouble(cursor.getColume("priority"));
			Map<String,String> columesmap = cursor.columesMap();
			for (Map.Entry<String, String> entry : cSelectItem.m_propMap.entrySet()) { 
				String key = entry.getKey();
				String value = entry.getValue();
				
				if(!key.equals("stockID") && !key.equals("priority"))
				{
					cSelectItem.setProperty(key, value);
				}
			}
			
			m_SelectItemList.add(cSelectItem);

			cursor = m_CXmlTable.moveNext();
		}
		
		if(null != m_ICallback)
		{
			m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
		}
		m_sync.UnLock();
		return true;
	}
	
	public boolean commit()
	{
		m_sync.Lock();
		m_CXmlTable.deleteAll();
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			SelectItem cSelectItem = m_SelectItemList.get(i);
			
			RowCursor cRowCursor = m_CXmlTable.addRow();
			cRowCursor.setColume("stockID", cSelectItem.m_sStockID);
			cRowCursor.setColume("priority", String.format("%.3f", cSelectItem.m_dPriority));
			
			for (Map.Entry<String, String> entry : cSelectItem.m_propMap.entrySet()) { 
				String key = entry.getKey();
				String value = entry.getValue();
				if(!key.equals("stockID") && !key.equals("priority"))
				{
					cRowCursor.setColume(key, value);
				}
			}
		}
		
		boolean bCommit = m_CXmlTable.commit();
		
		if(null != m_ICallback)
		{
			m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
		}
		
		m_sync.UnLock();
		
		return bCommit;
	}
	
	public SelectItem item(String stockID)
	{
		m_sync.Lock();
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			SelectItem cSelectItem = m_SelectItemList.get(i);
			if(cSelectItem.m_sStockID.equals(stockID))
			{
				return cSelectItem;
			}
		}
		m_sync.UnLock();
		return null;
	}
	
	public void addItem(String stockID)
	{
		m_sync.Lock();
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			SelectItem cSelectItem = m_SelectItemList.get(i);
			if(cSelectItem.m_sStockID.equals(stockID))
			{
				return;
			}
		}
		
		SelectItem cNewSelectItem = new SelectItem();
		cNewSelectItem.m_sStockID = stockID;
		m_SelectItemList.add(cNewSelectItem);
		
		if(null != m_ICallback)
		{
			m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
		}
		
		m_sync.UnLock();
	}
	
	public void removeItem(String stockID)
	{
		m_sync.Lock();
		int index = -1;
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			SelectItem cSelectItem = m_SelectItemList.get(i);
			if(cSelectItem.m_sStockID.equals(stockID))
			{
				index = i;
				break;
			}
		}
		if(index >= 0)
		{
			m_SelectItemList.remove(index);
			
			if(null != m_ICallback)
			{
				m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
			}
		}
		
		m_sync.UnLock();
		return;
	}
	
	public void clearAllItem()
	{
		m_sync.Lock();
		if(m_SelectItemList.size() > 0)
		{
			m_SelectItemList.clear();
			
			if(null != m_ICallback)
			{
				m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
			}
		}
		m_sync.UnLock();
		return;
	}
	
	public List<SelectItem> copyOriginROItemList()
	{
//		m_sync.Lock();
//		List<SelectItem> retList = new ArrayList<SelectItem>();
//		retList.addAll(m_SelectItemList);
//		m_sync.UnLock();
		return m_SelectItemList;
	}
	
	public List<String> selectStockIDs()
	{
		m_sync.Lock();
		Collections.sort(m_SelectItemList, new SelectItem.SelectItemCompare());
		
		List<String> retList = new ArrayList<String>();
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			SelectItem cSelectItem = m_SelectItemList.get(i);
			retList.add(cSelectItem.m_sStockID);
		}
		m_sync.UnLock();
		return retList;
	}
	
	public void selectKeepMaxCount(int maxCount)
	{
		m_sync.Lock();
		Collections.sort(m_SelectItemList, new SelectItem.SelectItemCompare());
		int count = 0;
		Iterator<SelectItem> iter = m_SelectItemList.iterator();
        while (iter.hasNext()) {
        	if(count >= maxCount)
            {
                iter.remove();
            }
        	else
        	{
        		count++;
        	}
        	iter.next();
        }
        
        if(null != m_ICallback)
		{
			m_ICallback.onNotify(ICallback.CALLBACKTYPE.CHANGED);
		}
        
        m_sync.UnLock();
	}
	
	public String dump()
	{
		m_sync.UnLock();
		String dump = "";
		dump += String.format("Select(%d) ", m_SelectItemList.size());
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			SelectItem cSelectItem = m_SelectItemList.get(i);
			dump += String.format("%s ", cSelectItem.m_sStockID);
		}
		m_sync.UnLock();
		return dump;
	}
	
	/**
	 *********************************************************************************************
	 */
	public static class SelectItem
	{
		public SelectItem()
		{
			m_sStockID = "";
			m_dPriority = Double.MIN_VALUE;
			m_propMap = new HashMap<String,String>();
		}
		public String stockID()
		{
			return m_sStockID;
		}
		public Double priority()
		{
			return m_dPriority;
		}
		public void setPriority(Double dPriority)
		{
			m_dPriority = dPriority;
		}
		public String getProperty(String property)
		{
			return m_propMap.get(property);
		}
		public void setProperty(String property, String value)
		{
			m_propMap.put(property,value);
		}
		public static class SelectItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				SelectItem c1 = (SelectItem)object1;
				SelectItem c2 = (SelectItem)object2;
				int iCmp = Double.compare(c1.m_dPriority, c2.m_dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		private String m_sStockID;
		private Double m_dPriority;
		private Map<String,String> m_propMap;
	}
	
	public CSyncObj m_sync;
	private ICallback m_ICallback;
	private List<SelectItem> m_SelectItemList;
	private CXmlTable m_CXmlTable;
}
