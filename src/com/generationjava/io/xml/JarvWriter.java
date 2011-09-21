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
import org.iso_relax.verifier.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.AttributesImpl;

/// TODO: Supply an ErrorHandler in a constructor
/**
 * Validates the written XML against a DTD or XML Schema using 
 * the iso_relax/jarv library. 
 */
public class JarvWriter extends DelegatingXmlWriter implements ErrorHandler {

    private VerifierHandler verifierHandler;
    private String tag;
    private AttributesImpl attrs = new AttributesImpl();
    private String text;
    
    public JarvWriter(XmlWriter writer, VerifierHandler verifierHandler) throws IOException {
        super(writer);
        this.verifierHandler = verifierHandler;
        try {
            verifierHandler.startDocument();
        } catch(SAXException se) {
            throw new IOException("Need to wrap the SAX exception in start document. ");
        }
//        try {
//            VerifierFactory vf = new com.sun.msv.verifier.jarv.TheFactoryImpl();
////            VerifierFactory vf = new org.kohsuke.jarv.xerces.XercesVerifierFactory();
//            Schema schema = vf.compileSchema("http://lleu.genscape.com/plantfeed.xsd");
//            Verifier v = schema.newVerifier();
//            v.setErrorHandler(this);
//            VerifierHandler vh = v.getVerifierHandler();
    }

    /// TODO: Fix this
    private String getDefaultNamespace() {
        return "";
    }

    /// TODO: Change exception type
    private void checkSchema() throws IOException {
        if(this.tag == null) {
            return;
        }
        // should use this.namespace
        try {
            verifierHandler.startElement( getDefaultNamespace(), this.tag, this.tag, attrs );
            if(this.text != null) {
                verifierHandler.characters( this.text.toCharArray(), 0, this.text.length() );
            }
            verifierHandler.endElement( getDefaultNamespace(), this.tag, this.tag);
        } catch(SAXException se) {
            throw new IOException("Need to wrap the SAX exception in start element: "+se);
        }
        this.attrs.clear();
        this.tag = null;
        this.text = null;
    }

    public XmlWriter writeEntity(String name) throws IOException {
        this.tag = name;
        return super.writeEntity(name);
    }

    public XmlWriter endEntity() throws IOException {
        checkSchema();
        return super.endEntity();
    }

    public XmlWriter writeAttribute(String attr, Object value) throws IOException {
        if(value != null) {
//            this.attrs.addAttribute( getDefaultNamespace(), attr, attr, value.getClass().getName(), toString(value) );
            this.attrs.addAttribute( getDefaultNamespace(), attr, attr, value.getClass().getName(), value.toString() );
        }
        return super.writeAttribute(attr, value);
    }

    public XmlWriter writeText(Object text) throws IOException {
        this.text = ""+text;
        return super.writeText(text);
    }

    public void error(SAXParseException spe) {
        System.err.println("Error: "+spe);
    }
    public void fatalError(SAXParseException spe) {
        System.err.println("Fatal: "+spe);
    }
    public void warning(SAXParseException spe) {
        System.err.println("Warning: "+spe);
    }
    
    public void close() throws IOException {
        try {
            verifierHandler.endDocument();
            super.close();
        } catch(SAXException se) {
            throw new IOException("Need to wrap the SAX exception in end document. ");
        }
    }

}
