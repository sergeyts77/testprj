/*
 * IOUtil.java
 *
 */
package com.scc.common.util.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.DatatypeConverter;

import com.scc.common.model.FileDetail;

/**
 *
 * @author  SCimbalov
 */
public final class IOUtil{
    //    
    private static final String DEFAULT_CHARACTER_SET = "Cp1251";
    public static final String  CP1251                = "Cp1251";    
    public static final String  UTF8                  = "UTF8";    
    // 
    //private static final String ALLOWED_CHARS         = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";
    
    /** Creates a new instance of IOUtil */
    private IOUtil() {}
    
    //
    public static String encodeToBase64(final byte[] content){
        //         
        return DatatypeConverter.printBase64Binary(content); 
    }            
    public static byte[] decodeFromBase64(final String contentStr){
        //         
        return DatatypeConverter.parseBase64Binary(contentStr);        
        //return Base64.getDecoder().decode(contentStr);
    }    
    
    public static void copy(final ByteBuffer src, final OutputStream dest) throws IOException {
        
        final int BYTE_BUF_SIZE = 100; 
        int len = src.remaining();
        int totalWritten = 0;
        byte[] buf = new byte[0];

        while(totalWritten < len) {
              int bytesToWrite = Math.min((len - totalWritten), BYTE_BUF_SIZE);

              if(buf.length < bytesToWrite){
                  buf = new byte[bytesToWrite];
              }
              src.get(buf, 0, bytesToWrite);
              dest.write(buf, 0, bytesToWrite);
              totalWritten += bytesToWrite;
        }
    }
    // 
    public static void copyStream(final InputStream in, final OutputStream out) throws IOException{
        //
        final byte[] buf = new byte[16384];         
        int len = 0;             
        while((len = in.read(buf))!=-1){ //System.out.println("len=" + len);    
              out.write(buf, 0, len);
        }                    
        out.flush();                    
    }
    // 
    public static void copyStream(final InputStream in, final OutputStream out, final int bSize) throws IOException{
        //
        final byte[] buf = new byte[bSize];         
        int len = 0;             
        while((len = in.read(buf))!=-1){ //System.out.println("len=" + len);    
              out.write(buf, 0, len);
        }                    
        out.flush();                    
    }
    // 
    public static String getFileExtension(final File file){
        final String fileName = file.getName();
        if(fileName.lastIndexOf(".")!=-1 && fileName.lastIndexOf(".")!= 0) return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    //
    public static String readFile(final InputStream input, final String charSet) throws IOException{
        // 
        String data = null;
        BufferedReader bReader = null;
        try{
            bReader = new BufferedReader(new InputStreamReader(input, charSet), 32768);
            final StringBuilder sBuf = new StringBuilder(512 * 1024);
            final char[] cbuf = new char[16384];
            int len = 0;
            while((len  = bReader.read(cbuf, 0, cbuf.length))!= -1){
                  sBuf.append(cbuf, 0, len);
            }
            data = sBuf.toString();
        }finally{
            try{
                bReader.close();
            }catch(IOException exc){}
        }
        return data;
    }
    //
    public static String readFile(InputStream input) throws IOException{
        
        String data = null;
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new InputStreamReader(input, DEFAULT_CHARACTER_SET));
            final StringBuilder sBuf = new StringBuilder();
            char[] cbuf = new char[1024];
            int len = 0;
            while((len  = bReader.read(cbuf, 0, cbuf.length))!= -1){
                  sBuf.append(cbuf, 0, len);
            }
            data = sBuf.toString();
        }finally{
            try{
                bReader.close();
            }catch(IOException exc){
                //
            }
        }
        return data;
    }
    // 
    public static byte[] readFully2(final InputStream in) throws IOException{
        //
        ByteArrayOutputStream out = null;
        final byte[] buf = new byte[16384]; 
        try {        
            out = new ByteArrayOutputStream(768 * 1024);
            int len = 0;  
            //while(in.available()>0 && (len = in.read(buf))!=-1){ //System.out.println("len=" + len);
            while((len = in.read(buf))!=-1){ //System.out.println("len=" + len);    
                   out.write(buf, 0, len);
            }            
            return out.toByteArray();
        }finally{
            try{
                if(in!=null) in.close();
                if(out!=null) out.close();
            }catch(Exception exc){
            }
        }
    }
    // 
    public static byte[] readFully(final InputStream in) throws IOException{
        
        ByteArrayOutputStream out = null;
        final byte[] buf = new byte[8192]; 
        try {        
            out = new ByteArrayOutputStream(8192);
            int len = 0;  
            //while(in.available()>0 && (len = in.read(buf))!=-1){ //System.out.println("len=" + len);
            while((len = in.read(buf))!=-1){ //System.out.println("len=" + len);    
                   out.write(buf, 0, len);
            }            
            return out.toByteArray();
        }finally{
            try{
                if(in!=null) in.close();
                if(out!=null) out.close();
            }catch(Exception exc){
            }
        }
    }
    //
    public static void writeTo(final String fileFullPath, final byte[] buf) throws IOException{
        //
        OutputStream out = null;
        try{
            out = new FileOutputStream(fileFullPath);
            out.write(buf);
        }finally{
            try{if(out!=null)out.close();}catch(Exception exc1){}
        }
    }
    // 
    public static void writeTo(final String fileFullPath, final FileDetail file) throws IOException{
        //
        OutputStream out = null;
        try{
            out = new FileOutputStream(fileFullPath);
            out.write(file.getContent(), 0, file.getContentLen());
        }finally{
            try{if(out!=null)out.close();}catch(Exception exc1){}
        }
    }
    
    // 
    public static String toXml(final String text) /*throws IOException*/{ 
        // 
        if(text==null || (text!=null && text.length()==0)){return "";}     
        //
        final StringWriter out = new StringWriter(text.length()); 
        int start = 0, last = 0;
        char[] data = text.toCharArray();         
	while(last < data.length){
	      char c = data[last];
              //
	      // escape markup delimiters only ... and do bulk
	      // writes wherever possible, for best performance
	      //
	      // note that character data can't have the CDATA
	      // termination "]]>"; escaping ">" suffices, and
	      // doing it very generally helps simple parsers
	      // that may not be quite correct.
	      //
              if(c == '<'){			// not legal in char data
		 out.write (data, start, last - start);
		 start = last + 1;
		 out.write ("&lt;");
	      }else if(c == '>'){		// see above
		out.write (data, start, last - start);
		start = last + 1;
		out.write ("&gt;");
	      }else if (c == '&'){		// not legal in char data
		out.write (data, start, last - start);
		start = last + 1;
		out.write ("&amp;");
	      }
	      last++;
	}
	out.write(data, start, last - start);       
        return out.toString();
    }    
    
    /**
     * Escape characters for text appearing in HTML markup.
     * 
     * <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
     * This method escapes all characters recommended by the Open Web App
     * Security Project - 
     * <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>link</a>.  
     * 
     * <P>The following characters are replaced with corresponding HTML 
     * character entities : 
     * <table border='1' cellpadding='3' cellspacing='0'>
     * <tr><th> Character </th><th> Encoding </th></tr>
     * <tr><td> < </td><td> &lt; </td></tr>
     * <tr><td> > </td><td> &gt; </td></tr>
     * <tr><td> & </td><td> &amp; </td></tr>
     * <tr><td> " </td><td> &quot;</td></tr>
     * <tr><td> ' </td><td> &#039;</td></tr>
     * <tr><td> ( </td><td> &#040;</td></tr> 
     * <tr><td> ) </td><td> &#041;</td></tr>
     * <tr><td> # </td><td> &#035;</td></tr>
     * <tr><td> % </td><td> &#037;</td></tr>
     * <tr><td> ; </td><td> &#059;</td></tr>
     * <tr><td> + </td><td> &#043; </td></tr>
     * <tr><td> - </td><td> &#045; </td></tr>
     * </table>
     * 
     * <P>Note that JSTL's {@code <c:out>} escapes <em>only the first 
     * five</em> of the above characters.
     * @param aText
     * @return 
     */
    public static String forHTML(final String aText){       
        // 
        if(aText==null || (aText!=null && aText.length()==0)){return "";}
        //
        final StringBuilder result = new StringBuilder(aText.length());
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character =  iterator.current();
        while(character != CharacterIterator.DONE){
              if(character == '<'){
                 result.append("&lt;");
              }else if(character == '>'){
                 result.append("&gt;");
              }else if(character == '&'){
                 result.append("&amp;");
              }else if(character == '\"'){
                 result.append("&quot;");
              }else if(character == '\''){
                 result.append("&#039;");
              }else if(character == '('){
                 result.append("&#040;");
              }else if(character == ')'){
                 result.append("&#041;");
              }else if(character == '#'){
                 result.append("&#035;");
              }else if(character == '%'){
                 result.append("&#037;");
              }else if(character == ';'){
                 result.append("&#059;");
              }else if(character == '+'){
                 result.append("&#043;");
              }else if (character == '-'){
                 result.append("&#045;");
              }else{
                //the char is not a special one
                //add it to the result as is
                result.append(character);
              }
              character = iterator.next();
        }
        return result.toString();
    }    
    // 
    public static String replaceSingleQuotes(final String str){        
        return str.replaceAll("'", "\"");        
    }      
    // 
    public static String quotesToHTML(final String str){        
        return str.replaceAll("'", "&#039;").replaceAll("\"", "&quot;");        
    }      
    
    // 
    // zip files in zip archive !!!    
    public static FileDetail zip(final String zipFileName, final Collection<FileDetail> files) throws IOException{
        //
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ZipOutputStream zout = new ZipOutputStream(out);
        try{            
            for(FileDetail file: files){
                zout.putNextEntry(new ZipEntry(file.getName()));
                zout.write(file.getContent(), 0, file.getContentLen());
                zout.closeEntry();                
            }                                    
        }finally{
            try{if(zout!=null)zout.close();}catch(IOException exc){}
        }
        final FileDetail file = new FileDetail(zipFileName, "zip");
        file.setContent(out.toByteArray());
        return file;
    }
}
