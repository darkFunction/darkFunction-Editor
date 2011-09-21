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

/**
 * Superclass for any XmlWriter which will wrap another XmlWriter. 
 * It passes all calls on to the underlying XmlWriter, and is 
 * expected to be used by all filtering XmlWriters. 
 *
 * Possibly this class should be abstract.
 */
public class DelegatingXmlWriter implements XmlWriter {

    private XmlWriter xmlWriter;

    public DelegatingXmlWriter(XmlWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    /**
     * Makes it easy to output the xml version if such a thing is desired.
     * While it usually is desired, it is less surprising not to handle it.
     */
    public XmlWriter writeXmlVersion() throws IOException {
        this.xmlWriter.writeXmlVersion();
        return this;
    }

    /** @see DelegatingXmlWriter.writeXmlVersion(String, String, String) */
    public XmlWriter writeXmlVersion(String version, String encoding) throws IOException {
        this.xmlWriter.writeXmlVersion(version, encoding);
        return this;
    }

    /**
     * Output the version, encoding and standalone nature of an xml file.
     */
    public XmlWriter writeXmlVersion(String version, String encoding, String standalone) throws IOException {
        this.xmlWriter.writeXmlVersion(version, encoding, standalone);
        return this;
    }

    /**
     * A helper method. It writes out an entity which contains only text.
     *
     * @param name String name of tag
     * @param text String of text to go inside the tag
     */
    public XmlWriter writeEntityWithText(String name, Object text) throws
    IOException {
        this.xmlWriter.writeEntityWithText(name, text);
        return this;
    }

    /**
     * A helper method. It writes out empty entities.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEmptyEntity(String name) throws IOException {
        this.xmlWriter.writeEmptyEntity(name);
        return this;
    }

    /**
     * Begin to write out an entity. Unlike the helper tags, this tag 
     * will need to be ended with the endEntity method.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEntity(String name) throws IOException {
        this.xmlWriter.writeEntity(name);
        return this;
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
        this.xmlWriter.writeAttribute(attr, value);
        return this;
    }

    /**
     * End the current entity. This will throw an exception 
     * if it is called when there is not a currently open 
     * entity.
     */
    public XmlWriter endEntity() throws IOException {
        this.xmlWriter.endEntity();
        return this;
    }

    /**
     * Close this writer. It does not close the underlying 
     * writer, but does throw an exception if there are 
     * as yet unclosed tags.
     */
    public void close() throws IOException {
        this.xmlWriter.close();
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(Object text) throws IOException {
        this.xmlWriter.writeText(text);
        return this;
    }

    /**
     * Write out a chunk of CDATA. This helper method surrounds the 
     * passed in data with the CDATA tag.
     *
     * @param String of CDATA text.
     */
    public XmlWriter writeCData(String cdata) throws IOException {
        this.xmlWriter.writeCData(cdata);
        return this;
    }

    /**
     * Write out a chunk of comment. This helper method surrounds the 
     * passed in data with the xml comment tag.
     *
     * @param String of text to comment.
     */
    public XmlWriter writeComment(String comment) throws IOException {
        this.xmlWriter.writeComment(comment);
        return this;
    }

    public Writer getWriter() {
        return this.xmlWriter.getWriter();
    }

}
