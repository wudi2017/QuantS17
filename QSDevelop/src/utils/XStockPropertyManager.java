package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pers.di.account.common.HoldStock;
import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CL2Property;
import pers.di.quantplatform.AccountProxy;
import utils.XStockClearRuleManager.InnerHoldStockItem;

public class XStockPropertyManager {

	public XStockPropertyManager(AccountProxy ap)
	{
		m_ap = ap;	
		String strPropertyPath = CSystem.getRWRoot() + "\\StockStrategyHelper";
		CFileSystem.createDir(strPropertyPath);
		m_propertyFileName = strPropertyPath + "\\" + ap.ID() + "_Property.xml";
		m_CL2Property = new CL2Property(m_propertyFileName);
	}
	
	public void loadFormFile()
	{
		m_CL2Property.sync2mem();
	}
	public void saveToFile()
	{
		m_CL2Property.sync2file();
	}
	/*
	 * 清除所有Manual属性为0并且未持仓的股票项目
	 */
	public void clearStockIDNotInHolds()
	{
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		m_ap.getHoldStockList(ctnHoldStockList);
		
		List<String> listStocks = m_CL2Property.list();

		for(int iProp=0; iProp<listStocks.size(); iProp++)
		{
			String stockIDProp = listStocks.get(iProp);
			if(this.isManualStockProperty(stockIDProp))
			{
				// save it， not changed
			}
			else
			{
				boolean bExitInHold = false;
				for(int i=0; i<ctnHoldStockList.size(); i++)
				{
					HoldStock cHoldStock = ctnHoldStockList.get(i);
					if(stockIDProp.equals(cHoldStock.stockID))
					{
						bExitInHold = true;
						break;
					}
				}
				
				if(!bExitInHold)
				{
					m_CL2Property.clear(stockIDProp);
	            }
			}
		}

		this.saveToFile();
	}
	
	public boolean hasStockID(String stockID)
	{
		return m_CL2Property.contain(stockID);
	}
	
	public void set_init(String stockID)
	{
		// 自动属性初始化
		m_CL2Property.setProperty("stockID", "Manual", "0"); // 手动配置属性，不被程序所自动操作
		// 建仓交易规则初始化
		m_CL2Property.setProperty("stockID", "createMoney", "0");
		m_CL2Property.setProperty("stockID", "maxHoldMoney", "0");
		m_CL2Property.setProperty("stockID", "tranUnitMoney", "0");
		// 最大持股天数初始化
		m_CL2Property.setProperty("stockID", "maxHoldDayCount", "0");
		// 止损规则初始化
		m_CL2Property.setProperty("stockID", "stopLossPrice", "0");
		m_CL2Property.setProperty("stockID", "stopLossRatio", "0");
		m_CL2Property.setProperty("stockID", "stopLossMoney", "0");
		// 止盈规则初始化
		m_CL2Property.setProperty("stockID", "targetProfitPrice", "0");
		m_CL2Property.setProperty("stockID", "targetProfitRatio", "0");
		m_CL2Property.setProperty("stockID", "targetProfitMoney", "0");
	}
	// 设置初始建仓股票耗资
	public void set_createMoney(String stockID, double value)
	{
		m_CL2Property.setProperty(stockID, "createMoney", String.format("%.3f",value));
	}
	// 设置最大持仓股票市值
	public void set_maxHoldMoney(String stockID, double value)
	{
		m_CL2Property.setProperty(stockID, "maxHoldMoney", String.format("%.3f",value));
	}
	// 设置交易单笔耗资
	public void set_tranUnitMoney(String stockID, double value)
	{
		m_CL2Property.setProperty(stockID, "tranUnitMoney", String.format("%.3f",value));
	}
	// 设置最大持股天数
	public void set_maxHoldDayCount(String stockID, int value)
	{
		m_CL2Property.setProperty(stockID, "maxHoldDayCount", String.format("%d",value));
	}
	// 设置止损
	public void set_stopLoss(String stockID, 
			double stopLossPrice, double stopLossRatio, double stopLossMoney)
	{
		m_CL2Property.setProperty(stockID, "stopLossPrice", String.format("%.3f",stopLossPrice));
		m_CL2Property.setProperty(stockID, "stopLossRatio", String.format("%.3f",stopLossRatio));
		m_CL2Property.setProperty(stockID, "stopLossMoney", String.format("%.3f",stopLossMoney));
	}
	// 设置止盈
	public void set_targetProfit(String stockID, 
			double targetProfitPrice, double targetProfitRatio, double targetProfitMoney)
	{
		m_CL2Property.setProperty(stockID, "targetProfitPrice", String.format("%.3f",targetProfitPrice));
		m_CL2Property.setProperty(stockID, "targetProfitRatio", String.format("%.3f",targetProfitRatio));
		m_CL2Property.setProperty(stockID, "targetProfitMoney", String.format("%.3f",targetProfitMoney));
	}

	
	/**
	 * ***********************************************************************************************************
	 */
	
	private boolean isManualStockProperty(String stockID)
	{
		if(m_CL2Property.contain(stockID, "Manual"))
		{
			String test = m_CL2Property.getProperty(stockID, "Manual");
			if(null != test && test.equals("1"))
			{
				return true;
			}
		}
		return false;
	}
	
	private AccountProxy m_ap;
	private String m_propertyFileName;
	private CL2Property m_CL2Property;
	
}
