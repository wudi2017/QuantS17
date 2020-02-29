package QuantExtend2002.utils;

import java.util.ArrayList;
import java.util.List;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.account.common.TRANACT;
import pers.di.quantplatform.AccountProxy;

public class QEUCommon {
	public static CommissionOrder getLatestCommissionOrder(AccountProxy ap, String stockID)
	{
		CommissionOrder cCommissionOrder = null;
		
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ap.getCommissionOrderList(ctnCommissionOrderList);
		for(int i=ctnCommissionOrderList.size()-1; i>=0; i--)
		{
			if(ctnCommissionOrderList.get(i).stockID.equals(stockID))
			{
				cCommissionOrder = ctnCommissionOrderList.get(i);
				break;
			}
		}
		
		return cCommissionOrder;
	}
	
	public static CommissionOrder getLatestCommissionOrder(AccountProxy ap, String stockID, TRANACT tranAct)
	{
		CommissionOrder cCommissionOrder = null;
		
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ap.getCommissionOrderList(ctnCommissionOrderList);
		for(int i=ctnCommissionOrderList.size()-1; i>=0; i--)
		{
			if(ctnCommissionOrderList.get(i).stockID.equals(stockID)
					&& ctnCommissionOrderList.get(i).tranAct.equals(tranAct))
			{
				cCommissionOrder = ctnCommissionOrderList.get(i);
				break;
			}
		}
		
		return cCommissionOrder;
	}
	
	public static List<String> getCommissionOrderStockIDList(AccountProxy ap)
	{
		List<String> stockIDs = new ArrayList<String>();
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		ap.getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			stockIDs.add(ctnCommissionOrderList.get(i).stockID);
		}
		return stockIDs;
	}
	
	public static HoldStock getHoldStock(AccountProxy ap,String stockID)
	{
		HoldStock cHoldStock = null;
		
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ap.getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			if(ctnHoldStockList.get(i).stockID.equals(stockID))
			{
				cHoldStock = ctnHoldStockList.get(i);
				break;
			}
		}
		
		return cHoldStock;
	}
	
	public static List<String> getHoldStockIDList(AccountProxy ap)
	{
		List<String> stockIDs = new ArrayList<String>();
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		ap.getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			stockIDs.add(ctnHoldStockList.get(i).stockID);
		}
		return stockIDs;
	}
}
