package utils.QS1802;

import java.util.*;

public class QURTMonitorTable {
	
	public QURTMonitorTable(String fileName)
	{
		
	}
	
	public boolean open()
	{
		return false;
	}
	public boolean commit()
	{
		return false;
	}
	
	public MonitorItem item(String stockID)
	{
		return null;
	}
	
	public List<MonitorItem> items()
	{
		return null;
	}
	
	public List<String> monitorStockIDs()
	{
		return null;
	}
	
	/**
	 *********************************************************************************************
	 */
	public static class MonitorItem
	{
		public String stockID()
		{
			return m_sStockID;
		}
		public void setStockID(String stockID)
		{
			m_sStockID = stockID;
		}
		
		public String strategy()
		{
			return m_sStrategy;
		}
		public void setStrategy(String strategy)
		{
			m_sStrategy = strategy;
		}
		
		public Double buyTriggerPrice()
		{
			return m_dBuyTriggerPrice;
		}
		public void setBuyTriggerPrice(Double buyTriggerPrice)
		{
			m_dBuyTriggerPrice = buyTriggerPrice;
		}

		public Double sellTriggerPrice()
		{
			return m_dSellTriggerPrice;
		}
		public void setSellTriggerPrice(Double sellTriggerPrice)
		{
			m_dSellTriggerPrice = sellTriggerPrice;
		}
		
		public Long minCommitInterval()
		{
			return m_lMinCommitInterval;
		}
		public void setMinCommitInterval(Long minCommitInterval)
		{
			m_lMinCommitInterval = minCommitInterval;
		}
		
		public Long oneCommitAmount()
		{
			return m_lOneCommitAmount;
		}
		public void setOneCommitAmount(Long oneCommitAmount)
		{
			m_lMinCommitInterval = oneCommitAmount;
		}
		
		public Long maxHoldAmount()
		{
			return m_lMaxHoldAmount;
		}
		public void setMaxHoldAmount(Long maxHoldAmount)
		{
			m_lMinCommitInterval = maxHoldAmount;
		}
		
		public Double targetProfitMoney()
		{
			return m_dTargetProfitMoney;
		}
		public void setTargetProfitMoney(Double targetProfitMoney)
		{
			m_dTargetProfitMoney = targetProfitMoney;
		}
		
		public Double targetProfitPrice()
		{
			return m_dTargetProfitPrice;
		}
		public void setTargetProfitPrice(Double targetProfitPrice)
		{
			m_dTargetProfitPrice = targetProfitPrice;
		}
		
		public Double stopLossPrice()
		{
			return m_dStopLossPrice;
		}
		public void setStopLossPrice(Double stopLossPrice)
		{
			m_dStopLossPrice = stopLossPrice;
		}
		
		public Double stopLossMoney()
		{
			return m_dStopLossMoney;
		}
		public void setStopLossMoney(Double stopLossMoney)
		{
			m_dStopLossMoney = stopLossMoney;
		}
		
		public Long maxHoldDays()
		{
			return m_dMaxHoldDays;
		}
		public void setMaxHoldDays(Long maxHoldDays)
		{
			m_dMaxHoldDays = maxHoldDays;
		}
		
		private String m_sStockID;
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
}
