/*
  * FileDetail.java
  *  
  */

package com.scc.common.model;

import java.io.UnsupportedEncodingException;

import com.scc.common.util.io.IOUtil;


/**
  *
  * @author  STsimbalov
  */
public final class FileDetail implements java.io.Serializable{
    // NEW 
    private boolean dbBacked; 
    
    private String systemId;
    //
    private String filename;
    private String type;
    //
    private byte[] content;    
    private String charContent;
    
    private int contentLen = -1;
    
    private String contentEncoding; 
    // 
    private String userId;
          
    public FileDetail(String filename, String type){  
        this.filename = filename;
        this.type = type;
    }
    //
    public FileDetail(String filename, String type, byte[] content){  
        this.filename = filename;
        this.type     = type;
        this.content      = content;         
    }
    // 
    public FileDetail setDbBacked(){
        this.dbBacked = true; 
        return this;
    }
    public boolean isDbBacked(){
        return dbBacked; 
    }
    public void setContentLen(int contentLen){
        this.contentLen=contentLen; 
    }
    public FileDetail clearContentLen(){
        contentLen=-1; 
        return this; 
    }
    public int getContentLen(){
        if(contentLen!=-1) return contentLen; 
        return content!=null ? content.length : 0;
    }
    //
    public String getName(){
        return filename;
    }
    public String getContentType(){
        return type;
    } 
    public byte[] getContent(){
        return content;
    }
    public String getCharContent(){
        return charContent;
    }
    public void setCharContent(final String charContent){
        this.charContent = charContent; 
    }
    public String getContentAsString() throws UnsupportedEncodingException{
        //return new String(content, "UTF8");                
        return new String(content, 0, getContentLen(), IOUtil.UTF8);
    }
    public String getContentAsString(String enc) throws UnsupportedEncodingException{
        //return new String(content, "UTF8");                
        return new String(content, 0, getContentLen(), enc);
    }
    public void setContent(byte[] content){
        this.content = content;
    }

    /**
     * @return the systemId
     */
    public String getSystemId(){
        return systemId;
    }  
    public void setSystemId(String systemId){
        this.systemId = systemId;
    }

    /**
     * @return the userId
     */
    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    /**
     * @return the contentEncoding
     */
    public String getContentEncoding(){
        return contentEncoding;
    }
    public void setContentEncoding(String contentEncoding){
        this.contentEncoding = contentEncoding;
    }
}
