package utils.QS1801;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pers.di.account.common.HoldStock;
import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CL2Property;
import pers.di.quantplatform.AccountProxy;

public class QUProperty {
	public QUProperty(String ID)
	{
		String strStockStrategyHelperPath = CSystem.getRWRoot() + "\\StockStrategyHelper";
		CFileSystem.createDir(strStockStrategyHelperPath);
		String propertyFileName = strStockStrategyHelperPath + "\\" + ID + "_QUProperty.xml";
		m_CL2Property = new CL2Property(propertyFileName);
	}
	
	public void set(String stockID, String property, String value) 
	{
		if(isManualStockProperty(stockID))
		{
			return;
		}
		m_CL2Property.setProperty(stockID, property, value);
	}
	public String get(String stockID, String property)
	{
		return m_CL2Property.getProperty(stockID, property);
	}
	public boolean contains(String stockID)
	{
		return m_CL2Property.contains(stockID);
	}
	public void clear(String stockID)
	{
		if(isManualStockProperty(stockID))
		{
			return;
		}
		m_CL2Property.clear(stockID);
	}
	public void loadFormFile()
	{
		m_CL2Property.sync2mem();
	}
	public void saveToFile()
	{
		m_CL2Property.sync2file();
	}
	
	/**
	 * ***********************************************************************************************************
	 */
	
	private boolean isManualStockProperty(String stockID)
	{
		if(m_CL2Property.contains(stockID, "Manual"))
		{
			String test = m_CL2Property.getProperty(stockID, "Manual");
			if(null != test && test.equals("1"))
			{
				return true;
			}
		}
		return false;
	}
	
	private CL2Property m_CL2Property;
}
