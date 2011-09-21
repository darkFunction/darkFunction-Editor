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

/**
 * Has various strategies for dealing with null or empty-string 
 * values. 
 */
public class EmptyElementXmlWriter extends DelegatingXmlWriter {

    /** 
     * Ignores empty concepts and prints out all attributes/entities. 
     * So it will print out foo="null"
     */
    public static final Object IGNORE_EMPTY_MODE = new Object();

    /**
     * Only considers null to be empty, so empty strings are outputted.
     */
    public static final Object NULL_EMPTY_MODE = new Object();

    /**
     * Considers null and an empty string to be 'empty'. 
     * If somethign is empty, it will not output them. 
     * This is the default mode.
     */
    public static final Object EMPTY_MODE = new Object();

    private StringBuffer attrs; // current attribute string
    private boolean empty;      // is the current node empty
    private boolean closed;     // is the current node closed...

    private Object emptyMode;  // the strategy to use for emptiness

    /**
     * Create an EmptyElementXmlWriter on top of an existing java.io.Writer.
     */
    public EmptyElementXmlWriter(XmlWriter xmlwriter) {
        super(xmlwriter);
        this.emptyMode = EmptyElementXmlWriter.EMPTY_MODE;
    }

    /**
     * The emptiness strategy to use. Emptiness is when it decides 
     * that an element should be ignored.
     */
    public XmlWriter setEmptyMode(Object mode) {
        if(mode != EmptyElementXmlWriter.EMPTY_MODE &&
           mode != EmptyElementXmlWriter.IGNORE_EMPTY_MODE &&
           mode != EmptyElementXmlWriter.NULL_EMPTY_MODE
          )
        {
            throw new IllegalArgumentException("Illegal mode: "+mode);
        }
        this.emptyMode = mode;
        return this;
    }

    private boolean checkEmpty(Object value) {
        // check empty-mode
        if(this.emptyMode == EmptyElementXmlWriter.EMPTY_MODE) {
            return (value == null) || "".equals(value);
        } else 
        if(this.emptyMode == EmptyElementXmlWriter.NULL_EMPTY_MODE) {
            return (value == null);
        } else {
            // same as IGNORE_EMPTY_MODE
            return false;
        }
    }

    /**
     *
     * @param name String name of tag
     */
    public XmlWriter writeEntity(String name) throws IOException {
        if(checkEmpty(name)) {
            return this;
        } else {
            return super.writeEntity(name);
        }
    }

    /**
     *
     * @param String name of attribute.
     * @param Object value of attribute.
     */
    public XmlWriter writeAttribute(String attr, Object value) throws IOException {

        if(checkEmpty(attr)) {
            return this;
        }
        
        if(checkEmpty(value)) {
            return this;
        }

        return super.writeAttribute(attr, value);
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(Object text) throws IOException {
        
        if(checkEmpty(text)) {
            return this;
        }

        return super.writeText(text);
    }

    /**
     * Write out a chunk of CDATA. This helper method surrounds the 
     * passed in data with the CDATA tag.
     *
     * @param String of CDATA text.
     */
    public XmlWriter writeCData(String cdata) throws IOException {
        
        if(checkEmpty(cdata)) {
            return this;
        }
        
        return super.writeCData(cdata);
    }

    /**
     * Write out a chunk of comment. This helper method surrounds the 
     * passed in data with the xml comment tag.
     *
     * @param String of text to comment.
     */
    public XmlWriter writeComment(String comment) throws IOException {
        
        if(checkEmpty(comment)) {
            return this;
        }

        return super.writeComment(comment);
    }

}
