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
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.znerd.xmlenc.XMLOutputter;

/**
 * An XmlWriter implementation, meaning it actually does output,  
 * that sits on top of the znerd.ord XmlEnc library. 
 *
 * This should allow for faster speed with some additional features, 
 * though the design of this API blocks these features from the user.
 */
public class XmlEncXmlWriter extends AbstractXmlWriter {

    private XMLOutputter xmlenc;      // underlying writer
    private boolean empty;      // is the current node empty
    private boolean closed;     // is the current node closed...

    private String namespace;   // the current default namespace

    // TODO: Remove this if XMLOutputter lets me return the writer
    private Writer writer; // so I can return the writer


    public XmlEncXmlWriter(Writer writer) {
        this(writer, "UTF-8");
    }
    public XmlEncXmlWriter(Writer writer, String encoding) {
        this.writer = writer;
        try {
            this.xmlenc = new XMLOutputter(writer, encoding);
        } catch(UnsupportedEncodingException uee) {
            throw new RuntimeException("UnsupportedEncodingException occurred in XmlEnc: "+uee.getMessage());
        }
        this.closed = true;
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

    /**
     * Output the version, encoding and standalone nature of an xml file.
     */
    public XmlWriter writeXmlVersion(String version, String encoding, String standalone) throws IOException {
        // how do we pass things in???
        this.xmlenc.declaration();
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
    private XmlWriter openEntity(String name) throws IOException {
        boolean wasClosed = this.closed;
        closeOpeningTag();
        this.closed = false;
        this.xmlenc.startTag(name);
        this.empty = true;
        return this;
    }

    // close off the opening tag
    private void closeOpeningTag() throws IOException {
        if (!this.closed) {
            this.empty = false;
            this.closed = true;
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

        this.xmlenc.attribute(attr, ""+value);
        return this;
    }

    /**
     * End the current entity. This will throw an exception 
     * if it is called when there is not a currently open 
     * entity.
     */
    public XmlWriter endEntity() throws IOException {
        if (this.empty) {
            this.xmlenc.endTag();
        } else {
            this.xmlenc.endTag();
        }
        this.empty = false;
        this.closed = true;
        return this;
    }

    /**
     * Close this.xmlenc. It does not close the underlying 
     * writer, but does flush the underlying writer and 
     * throw an exception if there are 
     * as yet unclosed tags.
     */
    public void close() throws IOException {
        this.writer.flush();
        this.xmlenc.close();
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(Object text) throws IOException {
        
        closeOpeningTag();
        this.empty = false;
        this.xmlenc.pcdata(""+text);
        return this;
    }

    /**
     * Write out a chunk of CDATA. This helper method surrounds the 
     * passed in data with the CDATA tag.
     *
     * @param String of CDATA text.
     */
    public XmlWriter writeCData(String cdata) throws IOException {
        
        this.xmlenc.cdata(cdata);
        return this;
    }

    /**
     * Write out a chunk of comment. This helper method surrounds the 
     * passed in data with the xml comment tag.
     *
     * @param String of text to comment.
     */
    public XmlWriter writeComment(String comment) throws IOException {
        
//        writeChunk("<!-- "+comment+" -->");
        this.xmlenc.comment(comment);
        return this;
    }

    public Writer getWriter() {
        return this.writer;
    }

}

