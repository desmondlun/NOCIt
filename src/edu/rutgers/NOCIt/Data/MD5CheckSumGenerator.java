package edu.rutgers.NOCIt.Data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// based on https://www.mkyong.com/java/java-md5-hashing-example/
public class MD5CheckSumGenerator
{
	
	public static String convertToHexMethod1(byte[] mdbytes) {
		//convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
	}
	
	public static String convertToHexMethod2(byte[] mdbytes) {
		//convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    		String hex=Integer.toHexString(0xff & mdbytes[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
    	
    	return hexString.toString();
	}
	
	public static byte[] getMD5Bytes(String path) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(path);

	        byte[] dataBytes = new byte[1024];

	        int nread = 0;
	        while ((nread = fis.read(dataBytes)) != -1) {
	          md.update(dataBytes, 0, nread);
	        };
	        byte[] mdbytes = md.digest();
	        
	        fis.close();
	        
	        return mdbytes;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
        
	}
	
    public static void main(String[] args)
    {
    	String path = "c:\\a.zip";
    	if (path != null) {
    		byte[] mdbytes1 = getMD5Bytes(path);
        	
        	System.out.println(path);
        	System.out.println("Digest(in hex format) Method 1:: " + convertToHexMethod1(mdbytes1));
        	System.out.println("Digest(in hex format) Method 2:: " + convertToHexMethod2(mdbytes1));
    	}
    	
    }

}