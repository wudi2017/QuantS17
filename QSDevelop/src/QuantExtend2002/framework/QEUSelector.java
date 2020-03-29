package QuantExtend2002.framework;

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
import java.util.Comparator;
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

import pers.di.common.CFileSystem;
import pers.di.common.CSystem;
import pers.di.common.CUtilsXML;

/*
 * Quant Extend Utils Selector (QEUSelector)
 */
public class QEUSelector {
	public QEUSelector(String name)
	{
		String sDir = CSystem.getRWRoot() + "/QEUSelector/";
		if (!CFileSystem.isDirExist(sDir)) {
			CFileSystem.createDir(sDir);
		}
		m_selectFileName = sDir + name + ".xml";
		m_maxCount = 3;
		m_SelectItemList = new ArrayList<InnerSelectStockItem>();
	}
	
	public void setMaxCount(int maxCount) {
		m_maxCount = maxCount;
	}
	
	/*
	 * add stock to select table
	 * priority high will be saved take precedence
	 * 优先级数值越高，优先被留下，留下个数被setMaxCount限定
	 */
	public void add(String stockID, double priority)
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
		
		// 按优先级排序
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
		
		int count = 0;
		Iterator<InnerSelectStockItem> iter = m_SelectItemList.iterator();
        while (iter.hasNext()) {
        	if(count >= m_maxCount)
            {
                iter.remove();
            }
        	else
        	{
        		count++;
        	}
        	iter.next();
        }
	}
	
	public List<String> list()
	{
		// 按优先级排序
		Collections.sort(m_SelectItemList, new InnerSelectStockItem.InnerSelectStockItemCompare());
				
		List<String> selectList = new ArrayList<String>();
		for(int i=0; i<m_SelectItemList.size(); i++)
		{
			String stockID = m_SelectItemList.get(i).stockID;
			selectList.add(m_SelectItemList.get(i).stockID);
		}
		return selectList;
	}
	
	public int size()
	{
		return m_SelectItemList.size();
	}
	public void clear()
	{
		m_SelectItemList.clear();
	}

	public String dump()
	{
		List<String> validSelectList = this.list();
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

	
	private static class InnerSelectStockItem {

		public static class InnerSelectStockItemCompare implements Comparator 
		{
			public int compare(Object object1, Object object2) {
				InnerSelectStockItem c1 = (InnerSelectStockItem)object1;
				InnerSelectStockItem c2 = (InnerSelectStockItem)object2;
				int iCmp = Double.compare(c1.dPriority, c2.dPriority);
				if(iCmp > 0) 
					return 1;
				else if(iCmp < 0) 
					return -1;
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
	
	private String m_selectFileName;
	private int m_maxCount;
	private List<InnerSelectStockItem> m_SelectItemList;
}
