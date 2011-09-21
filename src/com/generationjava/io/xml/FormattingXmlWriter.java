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

import java.text.NumberFormat;
import java.text.DateFormat;
import java.util.Date;


/**
 * Formats Numbers and Dates on the fly. 
 */
public class FormattingXmlWriter extends DelegatingXmlWriter {

    private DateFormat dateFormat;
    private NumberFormat numberFormat;
    
    public FormattingXmlWriter(XmlWriter writer) {
        super(writer);
    }


    /**
     * When a Number object is written, it will use this to 
     * format it. If the argument is null then it uses .toString.
     */
    public FormattingXmlWriter setNumberFormat(NumberFormat format) {
        this.numberFormat = format;
        return this;
    }

    /**
     * When a java.util.Date object is written, it will use this to 
     * format it. If the argument is null then it uses .toString.
     */
    public FormattingXmlWriter setDateFormat(DateFormat format) {
        this.dateFormat = format;
        return this;
    }

    public XmlWriter writeEntityWithText(String name, Object text) throws IOException {
        return super.writeEntityWithText(name, format(text));
    }

    public XmlWriter writeText(Object text) throws IOException {
        return super.writeText(format(text));
    }

    public XmlWriter writeAttribute(String name, Object value) throws IOException {
        return super.writeAttribute(name, format(value));
    }

    protected String format(Object unknown) {
        /// WHICH STRATEGY TO USE FOR NULL???
        if(unknown == null) {
            return null;
        }

        if(unknown instanceof String) {
            return (String)unknown;
        }

        if(unknown instanceof Date) {
            if(dateFormat == null) {
                return unknown.toString();
            } else {
                return dateFormat.format((Date)unknown);
            }
        } else 
        if(unknown instanceof Number) {
            if(numberFormat == null) {
                return unknown.toString();
            } else {
                return numberFormat.format((Number)unknown);
            }
        }

        return unknown.toString();
    }

}
