import pers.di.common.*;

public class QuantS1711 {

	public static void main(String[] args) throws Exception {
		CSystem.start();
		
		String xmlStr = "<a><b></b></a>";
		String xmlStrFmt = CUtilsXML.format(xmlStr);
		
		CLog.output("TEST", "%s", xmlStrFmt);
		
		CSystem.stop();
	}

}
