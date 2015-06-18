package me.footstone.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 
 * @author footstone
 *
 */
public class JarFileCompare {

	private static MessageDigest MD5 = null;
	
	static{
		try{
			MD5 = MessageDigest.getInstance("md5");
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String getMD5(InputStream input) throws IOException{
		byte[] buffer = new byte[2048];
		int length;
		BufferedInputStream binput = null;
		try{
			binput = new BufferedInputStream(input);
			while((length = binput.read(buffer))!=-1){
				MD5.update(buffer,0,length);
			}
		}catch(IOException e){
			throw e;
		}finally{
			if (binput != null){
				binput.close();
			}
		}
		return Hex.encode(MD5.digest(), false);
	}
	
	/**
	 * 
	 * @param srcFilepath
	 * @param dstFilepath
	 * @throws IOException
	 */
	public static List doCompare(String srcFilepath,String dstFilepath) throws IOException{
		// rule check
		
		// check file
		List list = new ArrayList();
		// md5 value of jar files
		if(checkJar(srcFilepath,dstFilepath)){
			return list;
		}
		
		JarFile sourceJar = new JarFile(srcFilepath);
		JarFile dstJar = new JarFile(dstFilepath);
		
		Enumeration<JarEntry> sourceEntry = sourceJar.entries();
		while(sourceEntry.hasMoreElements()){
			JarEntry jarEntry = sourceEntry.nextElement();
			if (!jarEntry.isDirectory()){
				String name = jarEntry.getName();
				InputStream input = sourceJar.getInputStream(jarEntry);
				String srcMd5 = getMD5(input);
				
				JarEntry dstEntry = (JarEntry) dstJar.getEntry(name);
				// srcFile中新增的文件
				if (dstEntry == null){
					list.add(name);
				}else{
					input = dstJar.getInputStream(dstEntry);
					String dstMd5 = getMD5(input);
					if (!srcMd5.equals(dstMd5)){
						list.add(name);
					}
				}
			}
		}
		// 找出dstFile中比srcFile中多的文件
		Enumeration<JarEntry> dstEntry = dstJar.entries();
		while(dstEntry.hasMoreElements()){
			JarEntry jarEntry = dstEntry.nextElement();
			if (!jarEntry.isDirectory()){
				String name = jarEntry.getName();
				JarEntry srcEntry = (JarEntry)sourceJar.getEntry(name);
				if (srcEntry == null){
					list.add(name);
				}
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param srcFilepath
	 * @param dstFilepath
	 * @return
	 * @throws IOException
	 */
	private static boolean checkJar(String srcFilepath,String dstFilepath) throws IOException{
		// check file
		boolean result = false;
		FileInputStream srcInput = null;
		FileInputStream dstInput = null;
		try{
			srcInput = new FileInputStream(srcFilepath);
			dstInput = new FileInputStream(dstFilepath);
			String srcMd5 = getMD5(srcInput);
			String dstMd5 = getMD5(dstInput);
			result = srcMd5.equals(dstMd5)? true:false;
		}catch(IOException e){
			throw e;
		}finally{
			if (srcInput!=null){
				try{
					srcInput.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			if (dstInput!=null){
				try{
					dstInput.close();
				}catch(IOException e){
					e.printStackTrace();
				}
				
			}
		}
		return result;
	}
	
	public static void main(String args[]) throws Exception{
		if (args == null || args.length != 2){
			System.out.println("PLEASE Input Two Jar File Path to Compare!");
			System.exit(-1);
		}
		List list = doCompare(args[0],args[1]);
		
		if (list.size() == 0){
			System.out.println("all match!");
		}else{
			Iterator it = list.iterator();
			while(it.hasNext()){
				String name = (String)it.next();
				System.out.println(name);
			}
		}
	}
}
