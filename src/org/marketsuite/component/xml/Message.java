package org.marketsuite.component.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Message {
    protected Document document;
    public final static String MIRROR_OF = "mirror-of";
    static private XMLOutputter outputter;
    static public XMLOutputter prettyOutputter;
    //static private final String ENCODING_FORMAT = "ISO-8859-1";
    static private final String ENCODING_FORMAT = "UTF-8";

    static {
        Format fmt = Format.getRawFormat();
        fmt.setEncoding(ENCODING_FORMAT);
        outputter = new XMLOutputter(fmt);
        fmt = Format.getPrettyFormat();
        fmt.setEncoding(ENCODING_FORMAT);
        prettyOutputter = new XMLOutputter(fmt);
    }

    public String outputString() {
        return outputter.outputString(document);
    }

    public byte[] getBytes(int length) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(length);
        outputter.output(document, stream);
        return stream.toByteArray();
    }

    public void output(OutputStream stream) throws IOException {
        outputter.output(document, stream);
    }

    public String toXml() {
        XMLOutputter xmlOutputter = new XMLOutputter();
        return xmlOutputter.outputString(document);
    }

    public String toXmlPrettyFormat() {
        return prettyOutputter.outputString(document);
    }

    static public String toXmlPrettyFormat(Element document) {
        return prettyOutputter.outputString(document);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
