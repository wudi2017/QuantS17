package utils;

import java.util.Comparator;

public class SelectResult {
	// 优先级从大到小排序
	static public class SelectResultCompare implements Comparator 
	{
		public int compare(Object object1, Object object2) {
			SelectResult c1 = (SelectResult)object1;
			SelectResult c2 = (SelectResult)object2;
			int iCmp = Double.compare(c1.fPriority, c2.fPriority);
			if(iCmp > 0) 
				return -1;
			else if(iCmp < 0) 
				return 1;
			else
				return 0;
		}
	}
	
	public SelectResult(){
		stockID = "";
		fPriority = 0.0f;
	}
	public String stockID;
	public double fPriority;
}
