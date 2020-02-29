package QuantExtend2002;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import pers.di.quantplatform.QuantStrategy;

import java.net.*;  

public class RunCommand {

	/*
	 * runCmd
	 * Sample: runCmd("C:\\Windows\\System32", "ping 127.0.0.1 -n 5");
	 */
	public static String runCmd(String cmdpath, String cmd) {
		String result="";
		File dir = new File(cmdpath);
		try {
			Process ps = Runtime.getRuntime().exec(cmd, null, dir);
	
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream(), Charset.forName("GBK")));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				result+=line+"\n";
			}
 
			br.close();
			System.out.println("close ... ");
			ps.waitFor();
			System.out.println("wait over ...");
			
			return result;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("child thread donn");
		return null;
	}
	
	public static void createClassObjectFromLib() {
		File file = new File("D:\\jarload\\test.txt");
		URL url = null;
		try {
			url = file.toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URLClassLoader myClassLoader = new URLClassLoader(new URL[] { url }, Thread.currentThread()
				.getContextClassLoader());
		
		Class<? extends QuantStrategy> myClass;
		try {
			myClass = (Class<? extends QuantStrategy>) myClassLoader.loadClass("com.java.jarloader.TestAction");
			QuantStrategy userObject = (QuantStrategy) myClass.newInstance();
			System.out.println(userObject);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
