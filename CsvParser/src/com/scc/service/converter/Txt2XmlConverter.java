package com.scc.service.converter;

import com.scc.common.util.io.IOUtil;

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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static abstract class Header {
        private String tag;

        Header(final String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }
    private static final class DirectHeader extends Header {
        DirectHeader(final String tag) {
            super(tag);
        }
    }
    private static final class IndirectHeader extends Header {
        private String attribute;

        IndirectHeader(final String tag, final String attribute) {
            super(tag);
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }
    }

    interface Sink /*extends Closeable*/ {
        Sink append(String smth) throws IOException;
    }

    private static Sink sink(final Writer writer) {
        return new Sink() {
            @Override
            public Sink append(String smth) throws IOException {
                writer.append(smth);
                return this;
            }
        };
    }

    private static Sink sink(final OutputStream writer) {
        return new Sink() {
            @Override
            public Sink append(String smth) throws IOException {
                writer.write(smth.getBytes(IOUtil.UTF8));
                return this;
            }
        };
    }

    public void convert(final Reader source, final Writer destination) throws IOException {
        convert(source, sink(destination));
    }

    public void convert(final Reader source, final OutputStream dest) throws IOException {
        convert(source, sink(dest));
    }

    public void convert(final Path sourcePath, final Path sinkPath) throws IOException {
        final long rowCount = getRowCountInFile(sourcePath);
        try (final Writer writer = Files.newBufferedWriter(sinkPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
             final BufferedReader lines = Files.newBufferedReader(sourcePath, DEFAULT_SOURCE_ENCODING)) {
            final Sink sink = sink(writer);
            openRootDocumentTag(xmlHeader(sink), rowCount);
            convertBody(lines, sink);
            closeRootDocumentTag(sink);
        }
    }

    private void convert(Reader source, Sink sink) throws IOException {
        final BufferedReader lines = new BufferedReader(source);                //, DEFAULT_SOURCE_ENCODING/
        openRootDocumentTag(xmlHeader(sink));
        convertBody(lines, sink);
        closeRootDocumentTag(sink);
    }

    private void convertBody(BufferedReader lines, Sink destination) throws IOException {
        final String headersLine = lines.readLine();
        final List<Header> tagHeaders = parseHeaderLine(headersLine);
        long rowCount = 0;
        String line;
        while ((line = lines.readLine()) != null) {
              final String[] row = line.split(SPLIT_EXPRESSION);
            try {
                writeRow(tagHeaders, row, destination, ++rowCount);
            } catch (IndexOutOfBoundsException e) {
                throw new RowParsingException("Size of row and header mismatch", e, rowCount);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new ConvertException("Cannot parse line " + rowCount, e);
            }
        }
    }

    private static Sink xmlHeader(final Sink destination) throws IOException {
        return destination.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
    }

    private static Sink openRootDocumentTag(final Sink sink) throws IOException {
        return sink.append("<DOCUMENT>");
    }

    private static Sink openRootDocumentTag(final Sink destination, final long rowCount) throws IOException {
        return destination.append(String.format("<DOCUMENT ROWS=\"%d\">", rowCount));
    }

    private static Sink closeRootDocumentTag(final Sink destination) throws IOException {
        return destination.append("</DOCUMENT>");
    }

    private static void writeRow(final List<Header> headers, final String[] row, final Sink dest, final long rownum) throws IOException {
        //
        System.out.println("rownum=" + rownum);
        //
        dest.append(String.format("<ROW ROWNUM=\"%d\">", rownum));
        for(int i = 0; i < row.length; i++){
            final String value = row[i];
            System.out.println("value=" + value);
            final String xmlLine;
            final Header header = headers.get(i);
            if (header instanceof IndirectHeader) {
               final IndirectHeader indirectHeader = (IndirectHeader) header;
                xmlLine = String.format("<%1$s %3$s=\"%2$s\"/>", header.getTag(), value, indirectHeader.getAttribute());
            } else {
                xmlLine = String.format("<%1$s>%2$s</%1$s>", header.getTag(), escape(value));
            }
            dest.append(xmlLine);
        }
        dest.append("</ROW>");
    }

    private static String escape(String value) {
        // &, < and >
        if(value != null && Stream.of("&", "<", ">").anyMatch(value::contains)){
            final String newValue = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            return "<![CDATA[" + newValue + "]]>";
        }
        return value;
    }

    private static List<Header> parseHeaderLine(final String headersLine){
        return Stream.of(headersLine.split(SPLIT_EXPRESSION))
                .map(h -> {
                    final Matcher matcher = INDIRECT_VALUE_PATTERN.matcher(h);
                    if (matcher.matches()) {
                        final String tag = matcher.group(1);
                        final String attribute = matcher.group(2);
                        return new IndirectHeader(tag.toUpperCase(Locale.ENGLISH), attribute);
                    }
                    return new DirectHeader(h.toUpperCase(Locale.ENGLISH));
                }).collect(Collectors.toList());
    }

    private static long getRowCountInFile(final Path path) throws IOException {
        return Files.lines(path, DEFAULT_SOURCE_ENCODING).skip(1L).count();
    }
}
