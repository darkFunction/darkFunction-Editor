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
 * Provides a few method implementations for XmlWriter implementations. 
 * The method implementations all depend on other methods which must 
 * be implemented. 
 */
public abstract class AbstractXmlWriter implements XmlWriter {

    /**
     * Makes it easy to output the xml version if such a thing is desired.
     * While it usually is desired, it is less surprising not to handle it.
     */
    public XmlWriter writeXmlVersion() throws IOException {
        return this.writeXmlVersion("1.0", null, null);
    }
    /** @see XmlWriter.writeXmlVersion(String, String, String) */
    public XmlWriter writeXmlVersion(String version, String encoding) throws IOException {
        return this.writeXmlVersion(version, encoding, null);
    }

    /**
     * A helper method. It writes out an entity which contains only text.
     *
     * @param name String name of tag
     * @param text String of text to go inside the tag
     */
    public XmlWriter writeEntityWithText(String name, Object text) throws IOException {
        writeEntity(name);
        writeText(text);
        return endEntity();
    }

    /**
     * A helper method. It writes out empty entities.
     *
     * @param name String name of tag
     */
    public XmlWriter writeEmptyEntity(String name) throws IOException {
        writeEntity(name);
        return endEntity();
    }

}
