/*
 *  Copyright (c) 2001-2002, Jean Tessier
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  
 *  	* Redistributions of source code must retain the above copyright
 *  	  notice, this list of conditions and the following disclaimer.
 *  
 *  	* Redistributions in binary form must reproduce the above copyright
 *  	  notice, this list of conditions and the following disclaimer in the
 *  	  documentation and/or other materials provided with the distribution.
 *  
 *  	* Neither the name of the Jean Tessier nor the names of his contributors
 *  	  may be used to endorse or promote products derived from this software
 *  	  without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jeantessier.classreader;

import java.io.*;
import java.util.*;

/**
 *  Base interface for parsing <code>.class</code> files.
 *  This should have been a Java interface, but I needed
 *  the protected contract for the Decorator Pattern.
 *
 *  @see ClassfileLoaderDecorator
 */
public abstract class ClassfileLoader {
	// Main methods
	public abstract Classfile  Classfile(String name);
	public abstract Collection Classfiles();
	public abstract Collection Classnames();

	public void Load(Collection filenames) throws IOException {
		Iterator i = filenames.iterator();
		while (i.hasNext()) {
			Load((String) i.next());
		}
	}

	public abstract void Load(String filename) throws IOException;

	// Event stuff
	public abstract void addLoadListener(LoadListener listener);
	public abstract void removeLoadListener(LoadListener listener);
	protected abstract void fireLoadStart(String filename);
	protected abstract void fireLoadElement(String filename, String element);
	protected abstract void fireLoadedClassfile(String filename, Classfile classfile);
	protected abstract void fireLoadStop(String filename);

	// Protected contract for Decorator Pattern
	protected abstract Classfile Load(DataInputStream in) throws IOException;
}
