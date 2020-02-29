package QuantExtend2002.utils;

import java.util.List;

import pers.di.common.CFileSystem;
import pers.di.common.CL2Property;
import pers.di.common.CSystem;

/*
 * Quant Extend Utils Auto Trigger (QEUAutoTrigger)
 * you could set the trigger property, for control auto buy or sell condition
 */
public class QEUProperty {
	
	public QEUProperty(String name)
	{
		String sDir = CSystem.getRWRoot() + "/QEUAutoTrigger/";
		if (!CFileSystem.isDirExist(sDir)) {
			CFileSystem.createDir(sDir);
		}
		String propFileName = sDir + name + ".xml";
		m_CL2Property = new CL2Property(propFileName);
	}
	
	/*
	 * ************************************************************************************
	 * global property 
	 * 
	 * MaxHoldStockCount ��ֻ��Ʊ���ֹ�����
	 * StockMaxPosstion ��ֻ��Ʊ���ֲ�λ
	 * StockOneCommitPossition ��ֻ��Ʊ�����ύ������ֲ�λ����
	 * StockOneCommitInterval �ύƵ�ʿ���
	 * MaxHoldDays ����������
	 * TargetProfitRatio Ŀ��ֹӯ�ȣ�������ֲ�λ��
	 * StopLossRatio ֹͣ����ȣ�������ֲ�λ��
	 * 
	 * ************************************************************************************
	 */
	// ���ֹ�����
	public void setGlobalMaxHoldStockCount(long count) { 
		this.propertySetLong("Global", "MaxHoldStockCount", count); 
	}
	public Long getGlobalMaxHoldStockCount() {
		return this.propertyGetLong("Global", "MaxHoldStockCount");
	}
	// ��ֻ��Ʊ����λ�������״ν���ʱ���ɸ���������ֳֹ��������ԣ��������û����Ƹ����µ��������
	// Ŀ����ԣ�FullHoldAmount
	public void setGlobalStockMaxHoldPosstion(double dMaxPossition) 
	{
		this.propertySetDouble("Global", "StockMaxHoldPosstion", dMaxPossition);
	}
	public Double getGlobalStockMaxHoldPosstion()
	{
		return this.propertyGetDouble("Global", "StockMaxHoldPosstion");
	}
	// ��ֻ��Ʊ����Ĭ��Բ�λ�����������ֳֹ��������ԣ��������״ν���ʱ���ɸ��ɵ��ʲ�����Ʊ��������
	// Ŀ����ԣ�OneCommitAmount
	public void setGlobalStockOneCommitPossition(double dDefaultCommit)
	{
		this.propertySetDouble("Global", "StockOneCommitPossition", dDefaultCommit);
	}
	public Double getGlobalStockOneCommitPossition()
	{
		return this.propertyGetDouble("Global", "StockOneCommitPossition");
	}
	// ��Ʊ�ύ��Сʱ���������������ύƵ��
	public void setGlobalStockMinCommitInterval(long min)
	{
		this.propertySetLong("Global", "StockMinCommitInterval", min);
	}
	public Long getGlobalStockMinCommitInterval()
	{
		return this.propertyGetLong("Global", "StockMinCommitInterval");
	}
	// ����ȫ�����ԣ���Ʊ����������
	// Ŀ����ԣ�MaxHoldDays
	public void setGlobalStockMaxHoldDays(long value)
	{
		this.propertySetLong("Global", "StockMaxHoldDays", value);
	}
	public Long getGlobalStockMaxHoldDays()
	{
		return this.propertyGetLong("Global", "StockMaxHoldDays");
	}
	// ����ȫ�����ԣ�Ŀ��ֹӯ���������FullHoldAmount�ģ�
	// Ŀ����ԣ�TargetProfitMoney
	public void setGlobalStockTargetProfitRatio(Double value)
	{
		this.propertySetDouble("Global", "StockTargetProfitRatio", value);
	}
	public Double getGlobalStockTargetProfitRatio()
	{
		return this.propertyGetDouble("Global", "StockTargetProfitRatio");
	}
	// ����ȫ�����ԣ�Ŀ��ֹ����������FullHoldAmount�ģ�
	// Ŀ����ԣ�StopLossMoney
	public void setGlobalStockStopLossRatio(Double value)
	{
		this.propertySetDouble("Global", "StockStopLossRatio", value);
	}
	public Double getGlobalStockStopLossRatio()
	{
		return this.propertyGetDouble("Global", "StockStopLossRatio");
	}
		

	
	/*
	 * ************************************************************************************
	 * stock property
	 * 
	 * User...: �û�����
	 * FullHoldAmount: ȫ����������
	 * OneCommitAmount: �����ύ��
	 * MaxHoldDays: ����������
	 * TargetProfitMoney: Ŀ��ӯ����
	 * TargetProfitPrice: Ŀ��ӯ����
	 * StopLossMoney: ֹ���
	 * StopLossPrice: ֹ���
	 * 
	 * ************************************************************************************
	 */
	// �û��Զ��岿��
	public void setPrivateStockPropertyString(String stockID, String property, String value)
	{
		this.propertySetString(stockID, "USER_"+property, value);
	}
	public String getPrivateStockPropertyString(String stockID, String property)
	{
		return this.propertyGetString(stockID, "USER_"+property);
	}
	public void setPrivateStockPropertyDouble(String stockID, String property, double value)
	{
		this.propertySetDouble(stockID, "USER_"+property, value);
	}
	public Double getPrivateStockPropertyDouble(String stockID, String property)
	{
		return this.propertyGetDouble(stockID, "USER_"+property);
	}
	public void setPrivateStockPropertyLong(String stockID, String property, long value)
	{
		this.propertySetLong(stockID, "USER_"+property, value);
	}
	public Long getPrivateStockPropertyLong(String stockID, String property)
	{
		return this.propertyGetLong(stockID, "USER_"+property);
	}
	public boolean stockPrivatePropertContains(String stockID)
	{
		return this.propertyContains(stockID);
	}
	public void stockPrivatePropertClear(String stockID)
	{
		this.propertyClear(stockID);
	}
	// ��Ʊȫ��λʱ��ĳֹ�����
	public void setPrivateStockPropertyMaxHoldAmount(String stockID, long value)
	{
		this.propertySetLong(stockID, "MaxHoldAmount", value);
	}
	public Long getPrivateStockPropertyMaxHoldAmount(String stockID)
	{
		return this.propertyGetLong(stockID, "MaxHoldAmount");
	}
	// ��Ʊһ���ύ������
	public void setPrivateStockPropertyOneCommitAmount(String stockID, long value)
	{
		this.propertySetLong(stockID, "OneCommitAmount", value);
	}
	public Long getPrivateStockPropertyOneCommitAmount(String stockID)
	{
		return this.propertyGetLong(stockID, "OneCommitAmount");
	}
	// �ύƵ��
	public void setPrivateStockPropertyMinCommitInterval(String stockID, long value)
	{
		this.propertySetLong(stockID, "MinCommitInterval", value);
	}
	public Long getPrivateStockPropertyMinCommitInterval(String stockID)
	{
		return this.propertyGetLong(stockID, "MinCommitInterval");
	}
	// ���������������ʱ��
	public void setPrivateStockPropertyMaxHoldDays(String stockID, long value)
	{
		this.propertySetLong(stockID, "MaxHoldDays", value);
	}
	public Long getPrivateStockPropertyMaxHoldDays(String stockID)
	{
		return this.propertyGetLong(stockID, "MaxHoldDays");
	}
	// ���������Ŀ��ֹӯ��ӯ����
	public void setPrivateStockPropertyTargetProfitMoney(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "TargetProfitMoney", value);
	}
	public Double getPrivateStockPropertyTargetProfitMoney(String stockID)
	{
		return this.propertyGetDouble(stockID, "TargetProfitMoney");
	}
	// ���������Ŀ��ֹӯ��ֹӯ��
	public void setPrivateStockPropertyTargetProfitPrice(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "TargetProfitPrice", value);
	}
	public Double getPrivateStockPropertyTargetProfitPrice(String stockID)
	{
		return this.propertyGetDouble(stockID, "TargetProfitPrice");
	}
	// ���������Ŀ��ֹ�𣬿����
	public void setPrivateStockPropertyStopLossMoney(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "StopLossMoney", value);
	}
	public Double getPrivateStockPropertyStopLossMoney(String stockID)
	{
		return this.propertyGetDouble(stockID, "StopLossMoney");
	}
	// ���������Ŀ��ֹ��ֹ���
	public void setPrivateStockPropertyStopLossPrice(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "StopLossPrice", value);
	}
	public Double getPrivateStockPropertyStopLossPrice(String stockID)
	{
		return this.propertyGetDouble(stockID, "StopLossPrice");
	}
	
	/**
	 * ***********************************************************************************************************
	 */
	
	private void propertySetBoolean(String main, String property, boolean value)
	{
		propertySetString(main, property, String.format("%b", value));
	}
	private Boolean propertyGetBoolean(String main, String property)
	{
		Boolean value = null;
		String sVal = propertyGetString(main, property);
		if(null != sVal)
		{
			value = Boolean.parseBoolean(sVal);
		}
		return value;
	}
	
	private void propertySetLong(String main, String property, long value)
	{
		propertySetString(main, property, String.format("%d", value));
	}
	private Long propertyGetLong(String main, String property)
	{
		Long value = null;
		String sVal = propertyGetString(main, property);
		if(null != sVal)
		{
			value = Long.parseLong(sVal);
		}
		return value;
	}
	
	private void propertySetDouble(String main, String property, double value)
	{
		propertySetString(main, property, String.format("%.3f", value));
	}
	private Double propertyGetDouble(String main, String property)
	{
		Double value = null;
		String sVal = propertyGetString(main, property);
		if(null != sVal)
		{
			value = Double.parseDouble(sVal);
		}
		return value;
	}
	
	private void propertySetString(String main, String property, String value) 
	{
		if(isManualStockProperty(main))
		{
			return;
		}
		m_CL2Property.setProperty(main, property, value);
	}
	private String propertyGetString(String main, String property)
	{
		return m_CL2Property.getProperty(main, property);
	}
	private boolean propertyContains(String main)
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
	
	public void loadFormFile()
	{
		m_CL2Property.sync2mem();
	}
	public void saveToFile()
	{
		m_CL2Property.sync2file();
	}
	
	private CL2Property m_CL2Property;
}
