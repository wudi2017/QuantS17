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
	 * MaxHoldStockCount 单只股票最大持股数量
	 * StockMaxPosstion 单只股票最大持仓位
	 * StockOneCommitPossition 单只股票单次提交相对最大持仓位比例
	 * StockOneCommitInterval 提交频率控制
	 * MaxHoldDays 最大持有天数
	 * TargetProfitRatio 目标止盈比（相对最大持仓位）
	 * StopLossRatio 停止亏损比（相对最大持仓位）
	 * 
	 * ************************************************************************************
	 */
	// 最大持股数量
	public void setGlobalMaxHoldStockCount(long count) { 
		this.propertySetLong("Global", "MaxHoldStockCount", count); 
	}
	public Long getGlobalMaxHoldStockCount() {
		return this.propertyGetLong("Global", "MaxHoldStockCount");
	}
	// 单只股票最大仓位，用于首次建仓时生成个股最大满仓持股数量属性，此属性用户控制个股下单最大上限
	// 目标个性：FullHoldAmount
	public void setGlobalStockMaxHoldPosstion(double dMaxPossition) 
	{
		this.propertySetDouble("Global", "StockMaxHoldPosstion", dMaxPossition);
	}
	public Double getGlobalStockMaxHoldPosstion()
	{
		return this.propertyGetDouble("Global", "StockMaxHoldPosstion");
	}
	// 单只股票操作默相对仓位（相对最大满仓持股数量属性），用于首次建仓时生成个股单笔操作股票数量属性
	// 目标个性：OneCommitAmount
	public void setGlobalStockOneCommitPossition(double dDefaultCommit)
	{
		this.propertySetDouble("Global", "StockOneCommitPossition", dDefaultCommit);
	}
	public Double getGlobalStockOneCommitPossition()
	{
		return this.propertyGetDouble("Global", "StockOneCommitPossition");
	}
	// 股票提交最小时间间隔，用于限制提交频率
	public void setGlobalStockMinCommitInterval(long min)
	{
		this.propertySetLong("Global", "StockMinCommitInterval", min);
	}
	public Long getGlobalStockMinCommitInterval()
	{
		return this.propertyGetLong("Global", "StockMinCommitInterval");
	}
	// 设置全局属性：股票最大持有天数
	// 目标个性：MaxHoldDays
	public void setGlobalStockMaxHoldDays(long value)
	{
		this.propertySetLong("Global", "StockMaxHoldDays", value);
	}
	public Long getGlobalStockMaxHoldDays()
	{
		return this.propertyGetLong("Global", "StockMaxHoldDays");
	}
	// 设置全局属性：目标止盈比例（相对FullHoldAmount的）
	// 目标个性：TargetProfitMoney
	public void setGlobalStockTargetProfitRatio(Double value)
	{
		this.propertySetDouble("Global", "StockTargetProfitRatio", value);
	}
	public Double getGlobalStockTargetProfitRatio()
	{
		return this.propertyGetDouble("Global", "StockTargetProfitRatio");
	}
	// 设置全局属性：目标止损比例（相对FullHoldAmount的）
	// 目标个性：StopLossMoney
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
