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
	 * 全局属性有默认值，用于自动生成单只股票的控制属性
	 * 
	 * HoldStockMaxCount 持股最大个数，表示可以持有的股票的个数，例如最多可以持有3只股票
	 * HoldOneStockMaxMarketValue 持有单只股票的最大市值，用于买入时计算单只最大持有量
	 * BuyOneStockCommitMaxMarketValue 买入单只股票一次提交的最大市值
	 * StockOneCommitInterval 提交频率控制(单位分钟)
	 * MaxHoldDays 最大持有天数
	 * TargetProfitRatio 目标止盈比（相对最大持仓位）例如：0.1 为盈利10个点
	 * StopLossRatio 停止亏损比（相对最大持仓位） 例如：-0.1 为亏损10个点
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
	
	// 单只股票最大持股数量
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
	
	// 单只股票最大持股市值
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
	
	// 单只股票每次买入提交的最大市值
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
	
	// 股票提交最小时间间隔，用于限制提交频率
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
	
	// 设置全局属性：股票最大持有天数
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
	
	// 设置全局属性：目标止盈比例（相对FullHoldAmount的）
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
	
	// 设置全局属性：目标止损比例（相对FullHoldAmount的）
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
	 * User...: 用户部分
	 * FullHoldAmount: 全仓最大持有量
	 * OneCommitAmount: 单次提交量
	 * MaxHoldDays: 最大持有天数
	 * TargetProfitMoney: 目标盈利额
	 * TargetProfitPrice: 目标盈利价
	 * StopLossMoney: 止损额
	 * StopLossPrice: 止损价
	 * 
	 * ************************************************************************************
	 */
	// 用户自定义部分
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
	// 股票全仓位时候的最大持股市值
	public void setPrivateStockPropertyMaxHoldketValue(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "MaxHoldMarketValue", value);
	}
	public Double getPrivateStockPropertyMaxHoldMarketValue(String stockID)
	{
		return this.propertyGetDouble(stockID, "MaxHoldMarketValue");
	}
	// 股票一次提交的最大市值
	public void setPrivateStockPropertyOneCommitMaxMarketValue(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "OneCommitMaxMarketValue", value);
	}
	public Double getPrivateStockPropertyOneCommitMaxMarketValue(String stockID)
	{
		return this.propertyGetDouble(stockID, "OneCommitMaxMarketValue");
	}
	
	// 股票全仓位时候的持股数量
		public void setPrivateStockPropertyMaxHoldAmount(String stockID, long value)
		{
			this.propertySetLong(stockID, "MaxHoldAmount", value);
		}
		public Long getPrivateStockPropertyMaxHoldAmount(String stockID)
		{
			return this.propertyGetLong(stockID, "MaxHoldAmount");
		}
		// 股票一次提交的数量
		public void setPrivateStockPropertyOneCommitAmount(String stockID, long value)
		{
			this.propertySetLong(stockID, "OneCommitAmount", value);
		}
		public Long getPrivateStockPropertyOneCommitAmount(String stockID)
		{
			return this.propertyGetLong(stockID, "OneCommitAmount");
		}
	// 提交频率
	public void setPrivateStockPropertyMinCommitInterval(String stockID, long value)
	{
		this.propertySetLong(stockID, "MinCommitInterval", value);
	}
	public Long getPrivateStockPropertyMinCommitInterval(String stockID)
	{
		return this.propertyGetLong(stockID, "MinCommitInterval");
	}
	// 清仓条件：最大持有时间
	public void setPrivateStockPropertyMaxHoldDays(String stockID, long value)
	{
		this.propertySetLong(stockID, "MaxHoldDays", value);
	}
	public Long getPrivateStockPropertyMaxHoldDays(String stockID)
	{
		return this.propertyGetLong(stockID, "MaxHoldDays");
	}
	// 清仓条件：目标止盈，盈利额
	public void setPrivateStockPropertyTargetProfitMoney(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "TargetProfitMoney", value);
	}
	public Double getPrivateStockPropertyTargetProfitMoney(String stockID)
	{
		return this.propertyGetDouble(stockID, "TargetProfitMoney");
	}
	// 清仓条件：目标止盈，止盈价
	public void setPrivateStockPropertyTargetProfitPrice(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "TargetProfitPrice", value);
	}
	public Double getPrivateStockPropertyTargetProfitPrice(String stockID)
	{
		return this.propertyGetDouble(stockID, "TargetProfitPrice");
	}
	// 清仓条件：目标止损，亏损额
	public void setPrivateStockPropertyStopLossMoney(String stockID, Double value)
	{
		this.propertySetDouble(stockID, "StopLossMoney", value);
	}
	public Double getPrivateStockPropertyStopLossMoney(String stockID)
	{
		return this.propertyGetDouble(stockID, "StopLossMoney");
	}
	// 清仓条件：目标止损，止损价
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
