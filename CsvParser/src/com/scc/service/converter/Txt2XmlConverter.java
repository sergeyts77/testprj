package com.scc.service.converter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scc.common.util.io.IOUtil;

public final class Txt2XmlConverter {
    // 
    private static final String SPLIT_EXPRESSION = "\\t";
    private static final Charset DEFAULT_SOURCE_ENCODING = Charset.forName("windows-1251");
    private static final Pattern INDIRECT_VALUE_PATTERN = Pattern.compile("(\\w+)\\[(\\w+)\\]");

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.printf("Usage: %s <tsv-file>", Txt2XmlConverter.class.getName());
            System.exit(-1);
        }
        final String source = args[0];
        final Path sourcePath = Paths.get(source);
        if (!Files.exists(sourcePath)) {
            throw new FileNotFoundException(source);
        }
        final Path sinkPath = sourcePath.resolveSibling(sourcePath.getFileName() + ".xml");
        final Txt2XmlConverter txt2Xml = new Txt2XmlConverter();
        txt2Xml.convert(sourcePath, sinkPath);
//        try(final BufferedWriter destination = Files.newBufferedWriter(sourcePath.resolveSibling(sourcePath.getFileName() + ".1.xml"), StandardCharsets.UTF_8)) {
//            txt2Xml.convert(Files.newBufferedReader(sourcePath, DEFAULT_SOURCE_ENCODING), destination);
//        }
    }

    public static abstract class Header {
        private String tag;

        protected Header(final String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }
    public static final class DirectHeader extends Header {

        public  DirectHeader(final String tag) {
            super(tag);
        }
    }
    public static final class IndirectHeader extends Header {
        private String attribute;

        public IndirectHeader(final String tag, final String attribute) {
            super(tag);
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }
    }
    
    public void convert(final Reader source, final Writer destination) throws IOException{
        // 
        final BufferedReader lines = new BufferedReader(source);                //, DEFAULT_SOURCE_ENCODING/
        final String headersLine = lines.readLine();
        final List<Header> tagHeaders = parseHeaderLine(headersLine);        
        //         
        openRootDocumentTag(destination);        
        long rowCount = 0;
        String line;
        while((line = lines.readLine()) != null){
              final String[] row = line.split(SPLIT_EXPRESSION);            
              /*int j=0; 
              for(String r: row){
                ++j;
                //System.out.println("convert.j=" + j + "_value=" + r + "<<");                
              }*/
              writeRow(tagHeaders, row, destination, ++rowCount);
        }
        closeRootDocumentTag(destination);
    }
    // 
    public void convert(final Reader source, final OutputStream dest) throws IOException{        
         // 
        final BufferedReader lines = new BufferedReader(source);                //, DEFAULT_SOURCE_ENCODING/
        final String headersLine = lines.readLine();
        final List<Header> tagHeaders = parseHeaderLine(headersLine);        
        //         
        openRootTag(dest);        
        long rowCount = 0;
        String line;
        while((line = lines.readLine()) != null){
              final String[] row = line.split(SPLIT_EXPRESSION);                          
              writeRow(tagHeaders, row, dest, ++rowCount);
        }
        closeRootTag(dest);                
    }
    //         
    private static void closeRootTag(final OutputStream dest) throws IOException{
        dest.write("</DOCUMENT>".getBytes(IOUtil.UTF8));        
    }            
    private static void openRootTag(final OutputStream dest) throws IOException {
        dest.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(IOUtil.UTF8));
        dest.write("<DOCUMENT>".getBytes(IOUtil.UTF8));        
    }    
    private static void writeRow(final List<Header> headers, final String[] row, final OutputStream dest, final long rownum) throws IOException{
        // 
        System.out.println("rownum=" + rownum);
        // 
        dest.write(String.format("<ROW ROWNUM=\"%d\">", rownum).getBytes(IOUtil.UTF8));        
        for(int i = 0; i < row.length; i++){
            
            //System.out.println("i=" + i);
            // 
            final String value = row[i];
            System.out.println("value=" + value);
            if(!(headers.get(i) instanceof IndirectHeader)){
               //                
               // &, < and >
               if(value!=null && (value.contains("&") || value.contains("<") || value.contains(">"))){                 
                  //dest.write(String.format("<%1$s>%2$s</%1$s>", headers.get(i).getTag().toUpperCase(Locale.ENGLISH), "<![CDATA[" + value + "]]>").getBytes(IOUtil.UTF8));                  
                  //
                  final String newValue = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");                    
                  dest.write(String.format("<%1$s>%2$s</%1$s>", headers.get(i).getTag().toUpperCase(Locale.ENGLISH), "<![CDATA[" + newValue + "]]>").getBytes(IOUtil.UTF8));
               }else{                 
                  dest.write(String.format("<%1$s>%2$s</%1$s>", headers.get(i).getTag().toUpperCase(Locale.ENGLISH), value).getBytes(IOUtil.UTF8));
               }
            }else{
               final IndirectHeader indirectHeader = (IndirectHeader) headers.get(i);
               dest.write(String.format("<%1$s %3$s=\"%2$s\"/>", indirectHeader.getTag().toUpperCase(Locale.ENGLISH), value, indirectHeader.getAttribute()).getBytes(IOUtil.UTF8));
            }
        }
        dest.write("</ROW>".getBytes(IOUtil.UTF8));        
    }        

    private static void closeRootDocumentTag(final Writer destination) throws IOException {
        destination.append("</DOCUMENT>");
    }
    
    private static void openRootDocumentTag(final Writer destination, final long rowCount) throws IOException {
        destination.append(String.format("<DOCUMENT ROWS=\"%d\">", rowCount));
    }
    // 
    private static void openRootDocumentTag(final Writer destination) throws IOException {
        xmlHeader(destination);
        destination.append("<DOCUMENT>");
    }

    private static Writer xmlHeader(final Writer destination) throws IOException {
        return destination.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
    }

    public void convert(final Path sourcePath, final Path sinkPath) throws IOException {
        try (final Writer sink = Files.newBufferedWriter(sinkPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            final long rowCount = getRowCountInFile(sourcePath);
            xmlHeader(sink);
            openRootDocumentTag(sink, rowCount);
            long rownumStartsWithOne = 1;
            try (final BufferedReader lines = Files.newBufferedReader(sourcePath, DEFAULT_SOURCE_ENCODING)) {
                final String headersLine = lines.readLine();
                final List<Header> tagHeaders = parseHeaderLine(headersLine);
                String line;
                while ((line = lines.readLine()) != null) {
                    final String[] row = line.split(SPLIT_EXPRESSION);
                    writeRow(tagHeaders, row, sink, rownumStartsWithOne++);
                }
            }
            closeRootDocumentTag(sink);
        }
    }

    private static List<Header> parseHeaderLine(final String headersLine){
        final String[] headers = headersLine.split(SPLIT_EXPRESSION);
        final List<Header> tagHeaders = new ArrayList<>(headers.length);
        for (final String h : headers) {
            final Header header;
            final Matcher matcher = INDIRECT_VALUE_PATTERN.matcher(h);
            if (matcher.matches()) {
                final String tag = matcher.group(1);
                final String attribute = matcher.group(2);
                header = new IndirectHeader(tag, attribute);
            } else {
                header = new DirectHeader(h);
            }
            tagHeaders.add(header);
        }
        return tagHeaders;
    }

    private static void writeRow(final List<Header> headers, final String[] row, final Writer sink, final long rownum) throws IOException {
        sink.append(String.format("<ROW ROWNUM=\"%d\">", rownum));
        for(int i = 0; i < row.length; i++){
            final String value = row[i];
            if(!(headers.get(i) instanceof IndirectHeader)){
               sink.append(String.format("<%1$s>%2$s</%1$s>", headers.get(i).getTag().toUpperCase(Locale.ENGLISH), value));
            }else{
               final IndirectHeader indirectHeader = (IndirectHeader) headers.get(i);
               sink.append(String.format("<%1$s %3$s=\"%2$s\"/>", indirectHeader.getTag().toUpperCase(Locale.ENGLISH), value, indirectHeader.getAttribute()));
            }
        }
        sink.append("</ROW>");
    }

    public static long getRowCountInFile(final Path path) throws IOException {
        return Files.lines(path, DEFAULT_SOURCE_ENCODING).skip(1L).count();
    }
}
