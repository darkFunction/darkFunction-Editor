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
 * Handles indentation on the fly. 
 */
public class PrettyPrinterXmlWriter extends DelegatingXmlWriter {

    private boolean empty;      // is the current node empty
    private boolean closed;     // is the current node closed...

    private boolean wroteText; // was text the last thing output?
    private String indent;     // output this to indent one level when pretty printing
    private String newline;    // output this to end a line when pretty printing

    private int indentSize;

    /**
     * Create an PrettyPrinterXmlWriter on top of an existing XmlWriter.
     */
    public PrettyPrinterXmlWriter(XmlWriter xmlWriter) {
        super(xmlWriter);
        this.closed = true;
        this.wroteText = false;
        this.newline = "\n";
        this.indent = "  ";
    }

    /**
     * Specify the string to prepend to a line for each level of indent. 
     * It is 2 spaces ("  ") by default. Some may prefer a single tab ("\t")
     * or a different number of spaces. Specifying an empty string will turn
     * off indentation when pretty printing.
     *
     * @param String representing one level of indentation while pretty printing.
     */
    public XmlWriter setIndent(String indent) {
        this.indent = indent;
        return this;
    }

    /**
     * Specify the string used to terminate each line when pretty printing. 
     * It is a single newline ("\n") by default. Users who need to read
     * generated XML documents in Windows editors like Notepad may wish to
     * set this to a carriage return/newline sequence ("\r\n"). Specifying
     * an empty string will turn off generation of line breaks when pretty
     * printing.  
     *
     * @param String representing the newline sequence when pretty printing.
     */
    public XmlWriter setNewline(String newline) {
        this.newline = newline;
        return this;
    }

    public XmlWriter writeXmlVersion() throws IOException {
        super.writeXmlVersion();
        getWriter().write(this.newline);
        return this;
    }
    public XmlWriter writeXmlVersion(String version, String encoding) throws IOException {
        super.writeXmlVersion(version, encoding);
        getWriter().write(this.newline);
        return this;
    }
    public XmlWriter writeXmlVersion(String version, String encoding, String standalone) throws IOException {
        super.writeXmlVersion(version, encoding, standalone);
        getWriter().write(this.newline);
        return this;
    }

    /**
     *
     * @param name String name of tag
     */
    public XmlWriter writeEntity(String name) throws IOException {

        // writeText used instead of getWriter(). This makes the 
        // whitespace appaer in the right place
        if ( !this.closed || this.wroteText) {
            writeText(newline);
        }
        for (int i = 0; i < indentSize; i++) {
            writeText(indent);
        }

        super.writeEntity(name);
        this.closed = false;
        indentSize++;
        this.empty = true;
        this.wroteText = false;
        return this;
    }

    /**
     * End the current entity. This will throw an exception 
     * if it is called when there is not a currently open 
     * entity.
     */
    public XmlWriter endEntity() throws IOException {
        indentSize--;
        if(!this.empty) {
            if(!this.wroteText) {
                for (int i = 0; i < this.indentSize; i++) {
                    getWriter().write(indent); // Indent closing tag to proper level
                }
            }
        }
        super.endEntity();
        getWriter().write(newline); 
        this.empty = false;
        this.closed = true;
        this.wroteText = false;
        return this;
    }

    /**
     * Output body text. Any xml characters are escaped. 
     */
    public XmlWriter writeText(Object text) throws IOException {
        super.writeText(text);
        this.empty = false;
        this.wroteText = true;
        return this;
    }

    /**
     * Write out a chunk of CDATA. This helper method surrounds the 
     * passed in data with the CDATA tag.
     *
     * @param String of CDATA text.
     */
    public XmlWriter writeCData(String cdata) throws IOException {
//        indentChunk();
        super.writeCData(cdata);
//        getWriter().write(newline); 
        this.empty = false;
        this.wroteText = true;
        return this;
    }

    /**
     * Write out a chunk of comment. This helper method surrounds the 
     * passed in data with the xml comment tag.
     *
     * @param String of text to comment.
     */
    public XmlWriter writeComment(String comment) throws IOException {
        indentChunk();
        if(!comment.startsWith(" ")) {
            comment = " "+comment;
        }
        if(!comment.endsWith(" ")) {
            comment = comment+" ";
        }
        super.writeComment(comment);
        getWriter().write(newline); 
        return this;
    }

    /**
     * A helper method. It writes out an entity which contains only text.
     *
     * @param name String name of tag
     * @param text String of text to go inside the tag
     */
    public XmlWriter writeEntityWithText(String name, Object text)
                     throws IOException {
        indentChunk();
        super.writeEntityWithText(name, text);
        getWriter().write(newline);
        return this;
    }

    /**
     * A helper method. It writes out empty entities.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEmptyEntity(String name) throws IOException {
        indentChunk();
        super.writeEmptyEntity(name);
        getWriter().write(newline);
        return this;
    }

    private void indentChunk() throws IOException {
        this.empty = false;
        if(!this.wroteText) {
            for (int i = 0; i < this.indentSize; i++) {
                getWriter().write(indent); 
            }
        }
    }

}
