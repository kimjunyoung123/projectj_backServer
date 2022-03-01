package com.kimcompay.projectjb.payments.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class sha256 {
    public static String encrypt(String str){
		StringBuffer sb = new StringBuffer();
		try {
			  MessageDigest sh = MessageDigest.getInstance("SHA-256");

			  byte[] toDigest = sh.digest(str.getBytes());
			  for(int i =0; i<toDigest.length; i++){
				  int c = toDigest[i] & 0xff;
				  if (c <= 15)
				        sb.append("0");
				  sb.append(Integer.toHexString(c));
			  }
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
        }
		return sb.toString();
	}
}
