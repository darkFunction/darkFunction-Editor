/*
 * Copyright (c) 2003, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of XmlWriter nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.generationjava.io.xml;

import java.io.IOException;
import java.io.Writer;

import java.util.Stack;

/**
 * Makes writing XML much much easier. 
 * Improved from 
 * <a href="http://builder.com.com/article.jhtml?id=u00220020318yan01.htm&page=1&vf=tt">article</a>
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 * @author <a href="mailto:pete@fingertipsoft.com">Peter Cassetta</a>
 * @version 1.0
 */
public class SimpleXmlWriter extends AbstractXmlWriter {

    private Writer writer;      // underlying writer
    private Stack stack;        // of xml entity names
    private StringBuffer attrs; // current attribute string
    private boolean empty;      // is the current node empty
    private boolean closed;     // is the current node closed...

    private String namespace;   // the current default namespace

    /**
     * Create an SimpleXmlWriter on top of an existing java.io.Writer.
     */
    public SimpleXmlWriter(Writer writer) {
        this.writer = writer;
        this.closed = true;
        this.stack = new Stack();
    }

    /**
     * The default namespace. Once this is turned on, any new entities 
     * will have this namespace, regardless of scope.
     *
     * @param String nname of the namespace
     */
    public void setDefaultNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDefaultNamespace() {
        if(this.namespace == null) {
            return "";
        } else {
            return this.namespace;
        }
    }

    /**
     * Output the version, encoding and standalone nature of an xml file.
     */
    public XmlWriter writeXmlVersion(String version, String encoding, String standalone) throws IOException {
        this.writer.write("<?xml version=\"");
        this.writer.write(version);
        if(encoding != null) {
            this.writer.write("\" encoding=\"");
            this.writer.write(encoding);
        }
        if(standalone != null) {
            this.writer.write("\" standalone=\"");
            this.writer.write(standalone);
        }
        this.writer.write("\"?>");
        return this;
    }

    /**
     * Begin to write out an entity. Unlike the helper tags, this tag 
     * will need to be ended with the endEntity method.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEntity(String name) throws IOException {
        
        if(this.namespace == null) {
            return openEntity(name);
        } else {
            return openEntity(this.namespace+":"+name);
        }
    }

    /**
     * Begin to output an entity. 
     *
     * @param String name of entity.
     */
    private SimpleXmlWriter openEntity(String name) throws IOException {
        boolean wasClosed = this.closed;
        closeOpeningTag();
        this.closed = false;
        this.writer.write("<");
        this.writer.write(name);
        stack.add(name);
        this.empty = true;
        return this;
    }

    // close off the opening tag
    private void closeOpeningTag() throws IOException {
        if (!this.closed) {
            writeAttributes();
            this.closed = true;
            this.writer.write(">");
        }
    }

    // write out all current attributes
    private void writeAttributes() throws IOException {
        if (this.attrs != null) {
            this.writer.write(this.attrs.toString());
            this.attrs.setLength(0);
            this.empty = false;
        }
    }

    /**
     * Write an attribute out for the current entity. 
     * Any xml characters in the value are escaped.
     * Currently it does not actually throw the exception, but 
     * the api is set that way for future changes.
     *
     * @param String name of attribute.
     * @param Object value of attribute.
     */
    public XmlWriter writeAttribute(String attr, Object value) throws IOException {

        // maintain api
        if (false) throw new IOException();

        if (this.attrs == null) {
            this.attrs = new StringBuffer();
        }
        this.attrs.append(" ");
        this.attrs.append(attr);
        this.attrs.append("=\"");
        this.attrs.append(XmlUtils.escapeXml(""+value));
        this.attrs.append("\"");
        return this;
    }

    /**
     * End the current entity. This will throw an exception 
     * if it is called when there is not a currently open 
     * entity.
     */
    public XmlWriter endEntity() throws IOException {
        if(this.stack.empty()) {
            throw new IOException("Called endEntity too many times. ");
        }
        String name = (String)this.stack.pop();
        if (name != null) {
            if (this.empty) {
                writeAttributes();
                this.writer.write("/>");
            } else {
                this.writer.write("</");
                this.writer.write(name);
                this.writer.write(">");
            }
            this.empty = false;
            this.closed = true;
        }
        return this;
    }

    /**
     * Close this writer. It does not close the underlying 
     * writer, but does flush it and throw an exception if 
     * there are as yet unclosed tags.
     */
    public void close() throws IOException {
        this.writer.flush();
        if(!this.stack.empty()) {
            throw new IOException("Tags are not all closed. "+
                "Possibly, "+this.stack.pop()+" is unclosed. ");
        }
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(Object text) throws IOException {
        
        closeOpeningTag();
        this.empty = false;
        this.writer.write(XmlUtils.escapeXml(""+text));
        return this;
    }

    /**
     * Write out a chunk of CDATA. This helper method surrounds the 
     * passed in data with the CDATA tag.
     *
     * @param String of CDATA text.
     */
    public XmlWriter writeCData(String cdata) throws IOException {
        
        writeChunk("<![CDATA[ "+cdata+" ]]>");
        return this;
    }

    /**
     * Write out a chunk of comment. This helper method surrounds the 
     * passed in data with the xml comment tag.
     *
     * @param String of text to comment.
     */
    public XmlWriter writeComment(String comment) throws IOException {
        
        writeChunk("<!--"+comment+"-->");
        return this;
    }
    private void writeChunk(String data) throws IOException {
        closeOpeningTag();
        this.empty = false;
        this.writer.write(data);
    }

    public Writer getWriter() {
        return this.writer;
    }

}
