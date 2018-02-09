package utils.QS1801;

import java.util.*;

public class QURTMonitorTable {
	
	public static class MonitorItem
	{
		public String stockID()
		{
			return m_sStockID;
		}
		public String strategy()
		{
			return m_sStrategy;
		}
		public Double buyTriggerPrice()
		{
			return m_dBuyTriggerPrice;
		}
		public Double sellTriggerPrice()
		{
			return m_dSellTriggerPrice;
		}
		public Long oneCommitAmount()
		{
			return m_lOneCommitAmount;
		}
		public Long maxHoldAmount()
		{
			return m_lMaxHoldAmount;
		}
		public Double targetProfitMoney()
		{
			return m_dTargetProfitMoney;
		}
		public Double targetProfitPrice()
		{
			return m_dTargetProfitPrice;
		}
		public Double stopLossPrice()
		{
			return m_dStopLossPrice;
		}
		public Double stopLossMoney()
		{
			return m_dStopLossMoney;
		}
		public Long maxHoldDays()
		{
			return m_dMaxHoldDays;
		}
		
		private String m_sStockID;
		private String m_sStrategy; // P/M
		private Double m_dBuyTriggerPrice; 
		private Double m_dSellTriggerPrice;
		private Long m_lOneCommitAmount;
		private Long m_lMaxHoldAmount;
		private Double m_dTargetProfitPrice;
		private Double m_dTargetProfitMoney;
		private Double m_dStopLossPrice;
		private Double m_dStopLossMoney;
		private Long m_dMaxHoldDays;
	}
	
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
	
	public MonitorItem MonitorItem(String stockID)
	{
		return null;
	}
	
	public List<MonitorItem> MonitorItems()
	{
		return null;
	}
}
