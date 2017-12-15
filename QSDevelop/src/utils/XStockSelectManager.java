package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import pers.di.account.common.CommissionOrder;
import pers.di.account.common.HoldStock;
import pers.di.common.CFileSystem;
import pers.di.common.CLog;
import pers.di.common.CSystem;
import pers.di.common.CUtilsXML;
import pers.di.quantplatform.AccountProxy;

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

public class XStockSelectManager {

	public static class InnerSelectStockItem {

		public static class InnerSelectStockItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				InnerSelectStockItem c1 = (InnerSelectStockItem)object1;
				InnerSelectStockItem c2 = (InnerSelectStockItem)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return -1;
				else if(iCmp < 0) 
					return 1;
				else
					return 0;
			}
		}
		public InnerSelectStockItem(){
			stockID = "";
			dPriority = 0.0f;
		}
		public String stockID;
		public double dPriority;
	}
	
	public XStockSelectManager(AccountProxy ap)
	{
		m_ap = ap;	
		String strSelectPath = CSystem.getRWRoot() + "\\StockStrategyHelper";
		CFileSystem.createDir(strSelectPath);
		m_selectFileName = strSelectPath + "\\" + ap.ID() + "_Select.xml";
		m_SelectItemList = new ArrayList<InnerSelectStockItem>();
	}
	
	//-----------------------------------------------------------------------------------------
	// select op
		
	public void addSelect(String stockID, double priority)
	{
		// 不重复添加
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			if(m_SelectItemList.get(i).stockID.equals(stockID))
			{
				return;
			}
		}
		InnerSelectStockItem cInnerSelectStockItem = new InnerSelectStockItem();
		cInnerSelectStockItem.stockID = stockID;
		cInnerSelectStockItem.dPriority = priority;
		m_SelectItemList.add(cInnerSelectStockItem);
	}
	// 有效选择列表，S1-先过滤掉无效后剩余最大maxCount个
	public List<String> validSelectListS1(int maxCount)
	{
		List<String> selectList = new ArrayList<String>();
		
		// 按优先级排序
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		
		int iAddCount = 0;
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			String stockID = m_SelectItemList.get(i).stockID;
			// 排除已经提交和持有的
			if(!existCommissionOrder(stockID)
					&& !existHoldStock(stockID))
			{
				selectList.add(m_SelectItemList.get(i).stockID);
				iAddCount++;
				if(iAddCount >= maxCount)
				{
					break;
				}
			}
		}
		return selectList;
	}
	// 有效选择列表，S2-先选择maxCount数目，再过滤掉无效的
	public List<String> validSelectListS2(int maxCount)
	{
		List<String> selectList = new ArrayList<String>();
		
		// 按优先级排序
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		
		int iAddCount = 0;
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			String stockID = m_SelectItemList.get(i).stockID;
			selectList.add(stockID);
			iAddCount++;
			if(iAddCount >= maxCount )
			{
				break;
			}
		}
		
		// 排除已经提交和持有的
		Iterator<String> iter = selectList.iterator();
		while (iter.hasNext()) {
			String stockID = iter.next();
			if(existCommissionOrder(stockID)
					|| existHoldStock(stockID))
			{
				iter.remove();
            }
        }		
		return selectList;
	}
	public int sizeSelect()
	{
		return m_SelectItemList.size();
	}
	public void clearSelect()
	{
		m_SelectItemList.clear();
	}

	public String dumpSelect()
	{
		List<String> validSelectList = validSelectListS1(20);
		int iAddCount = validSelectList.size();
		String logStr = "";
		logStr += String.format("Selected (%d) [ ", iAddCount);
		if(iAddCount == 0) logStr += "null ";
		for(int i=0; i< iAddCount; i++)
		{
			String stockId = validSelectList.get(i);
			logStr += String.format("%s ", stockId);
			if (i >= 7 && validSelectList.size()-1 > 8) {
				logStr += String.format("... ", stockId);
				break;
			}
		}
		logStr += String.format("]");
		return logStr;
	}
	
	public void saveToFile()
	{
		File cfile=new File(m_selectFileName);
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
		Element root=doc.createElement("Select");
		doc.appendChild(root);
		
		// 按优先级排序
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		for(int i=0;i<m_SelectItemList.size();i++)
    	{
			InnerSelectStockItem cInnerSelectStockItem = m_SelectItemList.get(i);
    		String stockID = cInnerSelectStockItem.stockID;
    		String priority =String.format("%.3f", cInnerSelectStockItem.dPriority);
    		
    		Element Node_Stock = doc.createElement("Stock");
    		Node_Stock.setAttribute("ID", stockID);
    		Node_Stock.setAttribute("Pri", priority);
    		
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
		File cfile_new = new File(m_selectFileName);
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
		File cfile=new File(m_selectFileName);
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
		    if(!rootElement.getTagName().contains("Select")) 
			{
				return; // 没有root
			}
		    
		    m_SelectItemList.clear();

        	NodeList nodelist_Select = rootElement.getChildNodes();
	        for (int i = 0; i < nodelist_Select.getLength(); i++) {
	        	Node node_Select = nodelist_Select.item(i);
	        	if(node_Select.getNodeType() == Node.ELEMENT_NODE)
	        	{
	        		String ID = ((Element)node_Select).getAttribute("ID");
	        		String Pri = ((Element)node_Select).getAttribute("Pri");
	        		
	        		InnerSelectStockItem cInnerSelectStockItem = new InnerSelectStockItem();
	        		cInnerSelectStockItem.stockID = ID;
	        		cInnerSelectStockItem.dPriority = Double.parseDouble(Pri);
	        		m_SelectItemList.add(cInnerSelectStockItem);
	        	}
	        }
	        
	        Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
	        
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage()); 
			return;
		}
	}
	
	//-----------------------------------------------------------------------------------------
	// private help
	
	private boolean existCommissionOrder(String stockID)
	{
		List<CommissionOrder> ctnCommissionOrderList = new ArrayList<CommissionOrder>();
		m_ap.getCommissionOrderList(ctnCommissionOrderList);
		for(int i=0; i<ctnCommissionOrderList.size(); i++)
		{
			if(ctnCommissionOrderList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean existHoldStock(String stockID)
	{
		List<HoldStock> ctnHoldStockList = new ArrayList<HoldStock>();
		m_ap.getHoldStockList(ctnHoldStockList);
		for(int i=0; i<ctnHoldStockList.size(); i++)
		{
			if(ctnHoldStockList.get(i).stockID.equals(stockID))
			{
				return true;
			}
		}
		return false;
	}
	
	private AccountProxy m_ap;
	private String m_selectFileName;
	private List<InnerSelectStockItem> m_SelectItemList;
}
