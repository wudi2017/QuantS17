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
	 * ȫ��������Ĭ��ֵ�������Զ����ɵ�ֻ��Ʊ�Ŀ�������
	 * 
	 * HoldStockMaxCount �ֹ�����������ʾ���Գ��еĹ�Ʊ�ĸ��������������Գ���3ֻ��Ʊ
	 * HoldOneStockMaxMarketValue ���е�ֻ��Ʊ�������ֵ����������ʱ���㵥ֻ��������
	 * BuyOneStockCommitMaxMarketValue ���뵥ֻ��Ʊһ���ύ�������ֵ
	 * StockOneCommitInterval �ύƵ�ʿ���(��λ����)
	 * MaxHoldDays ����������
	 * TargetProfitRatio Ŀ��ֹӯ�ȣ�������ֲ�λ�����磺0.1 Ϊӯ��10����
	 * StopLossRatio ֹͣ����ȣ�������ֲ�λ�� ���磺-0.1 Ϊ����10����
	 * 
	 * ************************************************************************************
	 */
	public static long sDefaultStockMaxCount = 10;
	public static double sDefaultHoldOneStockMaxMarketValue = 10*10000.0;
	public static double sDefaultBuyOneStockCommitMaxMarketValue = 2*10000.0;
	public static long sDefaultStockOneCommitInterval = 60;
	public static long sDefaultMaxHoldDays = 30;
	public static double sDefaultTargetProfitRatio = 0.1; 
	public static double sDefaultStopLossRatio = -0.1;
	
	// ��ֻ��Ʊ���ֹ�����
	public void setGlobalStockMaxCount(Long count) { 
		this.propertySetLong("Global", "StockMaxCount", count); 
	}
	public Long getGlobalStockMaxCount() {
		Long ret = this.propertyGetLong("Global", "StockMaxCount");
		if(null == ret) {
			ret = sDefaultStockMaxCount;
		}
		return ret;
	}
	
	// ��ֻ��Ʊ���ֹ���ֵ
	public void setGlobalHoldOneStockMaxMarketValue(Double value) { 
		this.propertySetDouble("Global", "HoldOneStockMaxMarketValue", value); 
	}
	public Double getGlobalHoldOneStockMaxMarketValue() {
		Double ret = this.propertyGetDouble("Global", "HoldOneStockMaxMarketValue");
		if (null == ret) {
			ret = sDefaultHoldOneStockMaxMarketValue;
		}
		return ret;
	}
	
	// ��ֻ��Ʊÿ�������ύ�������ֵ
	public void setGlobalBuyOneStockCommitMaxMarketValue(Double dDefaultCommit)
	{
		this.propertySetDouble("Global", "BuyOneStockCommitMaxMarketValue", dDefaultCommit);
	}
	public Double getGlobalBuyOneStockCommitMaxMarketValue()
	{
		Double ret = this.propertyGetDouble("Global", "BuyOneStockCommitMaxMarketValue");
		if (null == ret) {
			ret = sDefaultBuyOneStockCommitMaxMarketValue;
		}
		return ret;
	}
	
	// ��Ʊ�ύ��Сʱ���������������ύƵ��
	public void setGlobalStockMinCommitInterval(Long min)
	{
		if (null != min) {
			this.propertySetLong("Global", "StockMinCommitInterval", min);
		} else {
			this.propertyClear("Global", "StockMinCommitInterval");
		}
	}
	public Long getGlobalStockMinCommitInterval()
	{
		Long ret = this.propertyGetLong("Global", "StockMinCommitInterval");
		if (null == ret) {
			ret = sDefaultStockOneCommitInterval;
		}
		return ret;
	}
	
	// ����ȫ�����ԣ���Ʊ����������
	public void setGlobalStockMaxHoldDays(Long value)
	{
		if (null != value) {
			this.propertySetLong("Global", "StockMaxHoldDays", value);
		} else {
			this.propertyClear("Global", "StockMaxHoldDays");
		}
	}
	public Long getGlobalStockMaxHoldDays()
	{
		Long ret = this.propertyGetLong("Global", "StockMaxHoldDays");
		if (null == ret) {
			ret = sDefaultMaxHoldDays;
		}
		return ret;
	}
	
	// ����ȫ�����ԣ�Ŀ��ֹӯ���������FullHoldAmount�ģ�
	public void setGlobalStockTargetProfitRatio(Double value)
	{
		if (null != value) {
			this.propertySetDouble("Global", "StockTargetProfitRatio", value);
		} else {
			this.propertyClear("Global", "StockTargetProfitRatio");
		}
	}
	public Double getGlobalStockTargetProfitRatio()
	{
		Double ret = this.propertyGetDouble("Global", "StockTargetProfitRatio");
		if (null == ret) {
			ret = sDefaultTargetProfitRatio;
		}
		return ret;
	}
	
	// ����ȫ�����ԣ�Ŀ��ֹ����������FullHoldAmount�ģ�
	public void setGlobalStockStopLossRatio(Double value)
	{
		if (null != value) {
			this.propertySetDouble("Global", "StockStopLossRatio", value);
		} else {
			this.propertyClear("Global", "StockStopLossRatio");
		}
	}
	public Double getGlobalStockStopLossRatio()
	{
		Double ret = this.propertyGetDouble("Global", "StockStopLossRatio");
		if (null == ret) {
			ret = sDefaultStopLossRatio;
		}
		return ret;
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
	// ��Ʊȫ��λʱ������ֹ���ֵ
	public void setPrivateStockPropertyMaxHoldketValue(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "MaxHoldMarketValue", value);
	}
	public Double getPrivateStockPropertyMaxHoldMarketValue(String stockID)
	{
		return this.propertyGetDouble(stockID, "MaxHoldMarketValue");
	}
	// ��Ʊһ���ύ�������ֵ
	public void setPrivateStockPropertyOneCommitMaxMarketValue(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "OneCommitMaxMarketValue", value);
	}
	public Double getPrivateStockPropertyOneCommitMaxMarketValue(String stockID)
	{
		return this.propertyGetDouble(stockID, "OneCommitMaxMarketValue");
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
		m_CL2Property.setProperty(main, property, value);
	}
	
	private String propertyGetString(String main, String property)
	{
		return m_CL2Property.getProperty(main, property);
	}
	
	public void propertyClear(String main, String property)
	{
		m_CL2Property.clear(main, property);
	}
	
	private boolean propertyContains(String main)
	{
		return m_CL2Property.contains(main);
	}
	
	public void propertyClear(String main)
	{
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
	
	private CL2Property m_CL2Property;
}
