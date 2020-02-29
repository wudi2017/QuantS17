package QuantExtend1801.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pers.di.account.common.HoldStock;
import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CL2Property;
import pers.di.quantplatform.AccountProxy;

/*
 * Quant Utils Property for Stocks
 */
public class QUProperty {
	public QUProperty(String fileName)
	{
		m_CL2Property = new CL2Property(fileName);
	}
	
	// stock boolean
	public void propertySetBoolean(String main, String property, boolean value)
	{
		propertySetString(main, property, String.format("%b", value));
	}
	public Boolean propertyGetBoolean(String main, String property)
	{
		Boolean value = null;
		String sVal = propertyGetString(main, property);
		if(null != sVal)
		{
			value = Boolean.parseBoolean(sVal);
		}
		return value;
	}
	
	// stock long
	public void propertySetLong(String main, String property, long value)
	{
		propertySetString(main, property, String.format("%d", value));
	}
	public Long propertyGetLong(String main, String property)
	{
		Long value = null;
		String sVal = propertyGetString(main, property);
		if(null != sVal)
		{
			value = Long.parseLong(sVal);
		}
		return value;
	}
	
	// stock double
	public void propertySetDouble(String main, String property, double value)
	{
		propertySetString(main, property, String.format("%.3f", value));
	}
	public Double propertyGetDouble(String main, String property)
	{
		Double value = null;
		String sVal = propertyGetString(main, property);
		if(null != sVal)
		{
			value = Double.parseDouble(sVal);
		}
		return value;
	}
	
	//*********************************************************************************************
	// base
	// stock String
	public void propertySetString(String main, String property, String value) 
	{
		if(isManualStockProperty(main))
		{
			return;
		}
		m_CL2Property.setProperty(main, property, value);
	}
	public String propertyGetString(String main, String property)
	{
		return m_CL2Property.getProperty(main, property);
	}
	public boolean propertyContains(String main)
	{
		return m_CL2Property.contains(main);
	}
	public void propertyClear(String main)
	{
		if(isManualStockProperty(main))
		{
			return;
		}
		m_CL2Property.clear(main);
	}
	public List<String> propertyList()
	{
		return m_CL2Property.list();
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
	
	private boolean isManualStockProperty(String main)
	{
		if(m_CL2Property.contains(main, "Manual"))
		{
			String test = m_CL2Property.getProperty(main, "Manual");
			if(null != test && test.equals("1"))
			{
				return true;
			}
		}
		return false;
	}
	
	private CL2Property m_CL2Property;
}
