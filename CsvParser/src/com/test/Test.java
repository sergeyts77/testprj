/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test;

import com.scc.common.util.io.IOUtil;
import com.scc.service.converter.Txt2XmlConverter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

/**
 *
 * @author STsimbalov
 */
public class Test {
    
     public static void main(String[] strs){ 
        // 
        try(OutputStream dest = new FileOutputStream("C:\\0_9\\1.txt.xml")){                      
            // 
            Txt2XmlConverter conv = new Txt2XmlConverter(); 
            conv.convert(new StringReader(IOUtil.readFile(new FileInputStream("C:\\0_9\\1.txt"))), dest);             
            // 
        }catch(Exception exc){
            exc.printStackTrace();        
        }
    }    
    
}
