package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import pers.di.account.common.HoldStock;
import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CUtilsXML;
import pers.di.dataengine.DAStock;
import pers.di.quantplatform.AccountProxy;
import pers.di.quantplatform.QuantContext;

/*
 * XStockClearRuleManager
 * 股票持有策略控制器
 * 止盈止损与持有时间
 */
public class XStockClearRuleManager {
	
	public static class InnerHoldStockItem {
		public InnerHoldStockItem(){
			stockID = "";
			stopLossPrice = 0.0f;
			stopLossRatio = 0.0f;
			targetProfitPrice = 0.0f;
			targetProfitRatio = 0.0f;
			maxHoldDays = 0;
		}
		public String stockID;
		public double stopLossPrice; // 止损：股价
		public double stopLossRatio; // 止损：亏损百分比
		public double targetProfitPrice; // 止盈：股价
		public double targetProfitRatio; // 止盈：盈利百分比
		public int maxHoldDays; // 最大持有天数
	}
	
	public XStockClearRuleManager(AccountProxy ap)
	{
		m_ap = ap;	
		String strHoldPath = CSystem.getRWRoot() + "\\StockStrategyHelper";
		CFileSystem.createDir(strHoldPath);
		m_holdFileName = strHoldPath + "\\" + ap.ID() + "_Hold.xml";
		m_HoldItemList = new ArrayList<InnerHoldStockItem>();
	}
	
	public void setRule(String stockID, 
			double stopLossPrice, double stopLossRatio,
			double targetProfitPrice, double targetProfitRatio,
			int maxHoldDays)
	{
		InnerHoldStockItem cInnerHoldStockItem = null;
		for(int i=0; i<m_HoldItemList.size(); i++)
		{
			if(m_HoldItemList.get(i).stockID.equals(stockID))
			{
				cInnerHoldStockItem = m_HoldItemList.get(i);
				break;
			}
		}
		if(null == cInnerHoldStockItem)
		{
			cInnerHoldStockItem = new InnerHoldStockItem();
			m_HoldItemList.add(cInnerHoldStockItem);
		}
		
		if(null != cInnerHoldStockItem)
		{
			cInnerHoldStockItem.stockID = stockID;
			cInnerHoldStockItem.stopLossPrice = stopLossPrice;
			cInnerHoldStockItem.stopLossRatio = stopLossRatio;
			cInnerHoldStockItem.targetProfitPrice = targetProfitPrice;
			cInnerHoldStockItem.targetProfitRatio = targetProfitRatio;
			cInnerHoldStockItem.maxHoldDays = maxHoldDays;
		}
		
		saveToFile();
	}
	public void deleteRuleNotInHolds()
	{
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		m_ap.getHoldStockList(ctnHoldStockList);
		
		Iterator<InnerHoldStockItem> iter = m_HoldItemList.iterator();
		while (iter.hasNext()) {
			String stockIDRule = iter.next().stockID;
			
			boolean bExitInHold = false;
			for(int i=0; i<ctnHoldStockList.size(); i++)
			{
				HoldStock cHoldStock = ctnHoldStockList.get(i);
				if(stockIDRule.equals(cHoldStock.stockID))
				{
					bExitInHold = true;
					break;
				}
			}
			
			if(!bExitInHold)
			{
				iter.remove();
            }
        }
		saveToFile();
	}
	
	public boolean clearCheck(QuantContext ctx, DAStock cDAStock, HoldStock cHoldStock)
	{
		InnerHoldStockItem cInnerHoldStockItem = this.getRule(cDAStock.ID());
		
		// 持股超时卖出
		long lHoldDays = TranDaysChecker.check(ctx.pool().get("999999").dayKLines(), cHoldStock.createDate, ctx.date());
		if(lHoldDays >= cInnerHoldStockItem.maxHoldDays) 
		{
			return true;
		}
			
		// 止盈止损卖出
		if(cHoldStock.refProfitRatio() > cInnerHoldStockItem.targetProfitRatio || cHoldStock.refProfitRatio() < cInnerHoldStockItem.stopLossRatio) 
		{
			return true;
		}
		
		return false;
	}
	
	private InnerHoldStockItem getRule(String stockID)
	{
		InnerHoldStockItem cInnerHoldStockItem = null;
		for(int i=0; i<m_HoldItemList.size(); i++)
		{
			if(m_HoldItemList.get(i).stockID.equals(stockID))
			{
				cInnerHoldStockItem = m_HoldItemList.get(i);
				break;
			}
		}
		return cInnerHoldStockItem;
	}
	public int sizeHold()
	{
		return m_HoldItemList.size();
	}
	public void clearHold()
	{
		m_HoldItemList.clear();
	}
	
	public void saveToFile()
	{
		File cfile=new File(m_holdFileName);
		if(cfile.exists())
		{
			cfile.delete();
		}
		
		Document doc=null;
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder= factory.newDocumentBuilder();
			doc=builder.newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 创建元素
		Element root=doc.createElement("Hold");
		doc.appendChild(root);
		
		// 按优先级排序
		for(int i=0;i<m_HoldItemList.size();i++)
    	{
			InnerHoldStockItem cInnerHoldStockItem = m_HoldItemList.get(i);
			
			String stockID = cInnerHoldStockItem.stockID;
			String stopLossPrice = String.format("%.3f", cInnerHoldStockItem.stopLossPrice);
			String stopLossRatio = String.format("%.3f", cInnerHoldStockItem.stopLossRatio);
			String targetProfitPrice = String.format("%.3f", cInnerHoldStockItem.targetProfitPrice); 
			String targetProfitRatio = String.format("%.3f", cInnerHoldStockItem.targetProfitRatio); 
			String maxHoldDays = String.format("%d", cInnerHoldStockItem.maxHoldDays); 
			
    		Element Node_Stock = doc.createElement("Stock");
    		Node_Stock.setAttribute("stockID", stockID);
    		Node_Stock.setAttribute("stopLossPrice",stopLossPrice);
    		Node_Stock.setAttribute("stopLossRatio",stopLossRatio);
    		Node_Stock.setAttribute("targetProfitPrice",targetProfitPrice);
    		Node_Stock.setAttribute("targetProfitRatio",targetProfitRatio);
    		Node_Stock.setAttribute("maxHoldDays",maxHoldDays);
    		
    		root.appendChild(Node_Stock);
    	}
		
		TransformerFactory tfactory=TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tfactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 获得最终oriXmlStr
		String oriXmlStr ="";
		if(null != doc && null != transformer)
		{
			transformer.setOutputProperty("encoding","GBK");
			DOMSource source=new DOMSource(doc);
			
			StringWriter writer = new StringWriter();
			StreamResult result=new StreamResult(writer);
			try {
				transformer.transform(source,result);
				oriXmlStr = writer.toString();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// 格式化XmlStr
		String formatedXmlStr = "";
		try {
			formatedXmlStr = CUtilsXML.format(oriXmlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 更新到文件
		File cfile_new = new File(m_holdFileName);
		try {
			FileWriter fw = new FileWriter(cfile_new.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(formatedXmlStr);
		    bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadFromFile()
	{
		File cfile=new File(m_holdFileName);
		if(!cfile.exists())
		{
			return; // 文件不存在
		}
		
		try
		{
			String xmlStr = "";
			BufferedReader reader = new BufferedReader(new FileReader(cfile));
	        int fileLen = (int)cfile.length();
	        char[] chars = new char[fileLen];
	        reader.read(chars);
	        xmlStr = String.valueOf(chars);
			reader.close();
			//fmt.format("XML:\n" + xmlStr);
			
			if(xmlStr.length()<=0)
			{
				return; // 没有内容 
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    StringReader sr = new StringReader(xmlStr);
		    InputSource is = new InputSource(sr);
		    Document doc = builder.parse(is);
		    Element rootElement = doc.getDocumentElement();
		    
		    // 检查返回数据有效性
		    if(!rootElement.getTagName().contains("Hold")) 
			{
				return; // 没有root
			}
		    
		    m_HoldItemList.clear();

        	NodeList nodelist_Hold = rootElement.getChildNodes();
	        for (int i = 0; i < nodelist_Hold.getLength(); i++) {
	        	Node node_Select = nodelist_Hold.item(i);
	        	if(node_Select.getNodeType() == Node.ELEMENT_NODE)
	        	{
	        		String stockID = ((Element)node_Select).getAttribute("stockID");
	        		String stopLossPrice = ((Element)node_Select).getAttribute("stopLossPrice");
	        		String stopLossRatio = ((Element)node_Select).getAttribute("stopLossRatio");
	        		String targetProfitPrice = ((Element)node_Select).getAttribute("targetProfitPrice");
	        		String targetProfitRatio = ((Element)node_Select).getAttribute("targetProfitRatio");
	        		String maxHoldDays = ((Element)node_Select).getAttribute("maxHoldDays");

	        		InnerHoldStockItem cInnerHoldStockItem = new InnerHoldStockItem();
	        		cInnerHoldStockItem.stockID = stockID;
	        		cInnerHoldStockItem.stopLossPrice = Double.parseDouble(stopLossPrice);
	        		cInnerHoldStockItem.stopLossRatio = Double.parseDouble(stopLossRatio);
	        		cInnerHoldStockItem.targetProfitPrice = Double.parseDouble(targetProfitPrice);
	        		cInnerHoldStockItem.targetProfitRatio = Double.parseDouble(targetProfitRatio);
	        		cInnerHoldStockItem.maxHoldDays = Integer.parseInt(maxHoldDays);
	        		m_HoldItemList.add(cInnerHoldStockItem);
	        	}
	        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return;
		}
	}
	private AccountProxy m_ap;
	private String m_holdFileName;
	private List<InnerHoldStockItem> m_HoldItemList;
}
