/*
 *  Copyright (c) 2001-2003, Jean Tessier
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

public abstract class ClassfileLoaderEventSource extends ClassfileLoader {
	private ClassfileLoader dir_loader = new DirectoryClassfileLoader(this);
	private ClassfileLoader jar_loader = new JarClassfileLoader(this);
	private ClassfileLoader zip_loader = new ZipClassfileLoader(this);

	private HashSet load_listeners = new HashSet();

	protected void Load(String filename) {
		if (filename.endsWith(".jar")) {
			jar_loader.Load(filename);
		} else if (filename.endsWith(".zip")) {
			zip_loader.Load(filename);
		} else {
			dir_loader.Load(filename);
		}
	}

	public void addLoadListener(LoadListener listener) {
		synchronized(load_listeners) {
			load_listeners.add(listener);
		}
	}

	public void removeLoadListener(LoadListener listener) {
		synchronized(load_listeners) {
			load_listeners.remove(listener);
		}
	}

	protected void fireBeginSession() {
		LoadEvent event = new LoadEvent(this, null, null, null);

		HashSet listeners;
		synchronized(load_listeners) {
			listeners = (HashSet) load_listeners.clone();
		}

		Iterator i = listeners.iterator();
		while(i.hasNext()) {
			((LoadListener) i.next()).BeginSession(event);
		}
	}

	protected void fireBeginGroup(String filename, int size) {
		LoadEvent event = new LoadEvent(this, filename, size);

		HashSet listeners;
		synchronized(load_listeners) {
			listeners = (HashSet) load_listeners.clone();
		}

		Iterator i = listeners.iterator();
		while(i.hasNext()) {
			((LoadListener) i.next()).BeginGroup(event);
		}
	}
	
	protected void fireBeginClassfile(String filename, String element) {
		LoadEvent event = new LoadEvent(this, filename, element, null);

		HashSet listeners;
		synchronized(load_listeners) {
			listeners = (HashSet) load_listeners.clone();
		}

		Iterator i = listeners.iterator();
		while(i.hasNext()) {
			((LoadListener) i.next()).BeginClassfile(event);
		}
	}

	protected void fireEndClassfile(String filename, String element, Classfile classfile) {
		LoadEvent event = new LoadEvent(this, filename, element, classfile);

		HashSet listeners;
		synchronized(load_listeners) {
			listeners = (HashSet) load_listeners.clone();
		}

		Iterator i = listeners.iterator();
		while(i.hasNext()) {
			((LoadListener) i.next()).EndClassfile(event);
		}
	}

	protected void fireEndGroup(String filename) {
		LoadEvent event = new LoadEvent(this, filename, null, null);

		HashSet listeners;
		synchronized(load_listeners) {
			listeners = (HashSet) load_listeners.clone();
		}

		Iterator i = listeners.iterator();
		while(i.hasNext()) {
			((LoadListener) i.next()).EndGroup(event);
		}
	}

	protected void fireEndSession() {
		LoadEvent event = new LoadEvent(this, null, null, null);

		HashSet listeners;
		synchronized(load_listeners) {
			listeners = (HashSet) load_listeners.clone();
		}

		Iterator i = listeners.iterator();
		while(i.hasNext()) {
			((LoadListener) i.next()).EndSession(event);
		}
	}
}
