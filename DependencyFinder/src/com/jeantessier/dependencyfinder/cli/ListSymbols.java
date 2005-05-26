/*
 *  Copyright (c) 2001-2005, Jean Tessier
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *  
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *  
 *      * Neither the name of Jean Tessier nor the names of his contributors
 *        may be used to endorse or promote products derived from this software
 *        without specific prior written permission.
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

package com.jeantessier.dependencyfinder.cli;

import java.io.*;
import java.util.*;

import com.jeantessier.classreader.*;
import com.jeantessier.commandline.*;
import com.jeantessier.dependencyfinder.*;

public class ListSymbols {
    public static final String DEFAULT_LOGFILE = "System.out";

    public static void showError(CommandLineUsage clu, String msg) {
        System.err.println(msg);
        showError(clu);
    }

    public static void showError(CommandLineUsage clu) {
        System.err.println(clu);
        System.err.println();
        System.err.println("If no files are specified, it processes the current directory.");
        System.err.println();
    }

    public static void showVersion() {
        Version version = new Version();
        
        System.err.print(version.getImplementationTitle());
        System.err.print(" ");
        System.err.print(version.getImplementationVersion());
        System.err.print(" (c) ");
        System.err.print(version.getCopyrightDate());
        System.err.print(" ");
        System.err.print(version.getCopyrightHolder());
        System.err.println();
        
        System.err.print(version.getImplementationURL());
        System.err.println();
        
        System.err.print("Compiled on ");
        System.err.print(version.getImplementationDate());
        System.err.println();
    }

    public static void main(String[] args) throws Exception {
        // Parsing the command line
        CommandLine commandLine = new CommandLine();
        commandLine.addToggleSwitch("class-names");
        commandLine.addToggleSwitch("field-names");
        commandLine.addToggleSwitch("method-names");
        commandLine.addToggleSwitch("local-names");
        commandLine.addToggleSwitch("time");
        commandLine.addSingleValueSwitch("out");
        commandLine.addToggleSwitch("help");
        commandLine.addOptionalValueSwitch("verbose", DEFAULT_LOGFILE);
        commandLine.addToggleSwitch("version");

        CommandLineUsage usage = new CommandLineUsage("ListSymbols");
        commandLine.accept(usage);

        try {
            commandLine.parse(args);
        } catch (IllegalArgumentException ex) {
            showError(usage, ex.toString());
            System.exit(1);
        } catch (CommandLineException ex) {
            showError(usage, ex.toString());
            System.exit(1);
        }

        if (commandLine.getToggleSwitch("help")) {
            showError(usage);
        }
        
        if (commandLine.getToggleSwitch("version")) {
            showVersion();
        }

        if (commandLine.getToggleSwitch("help") || commandLine.getToggleSwitch("version")) {
            System.exit(1);
        }

        VerboseListener verboseListener = new VerboseListener();
        if (commandLine.isPresent("verbose")) {
            if ("System.out".equals(commandLine.getOptionalSwitch("verbose"))) {
                verboseListener.setWriter(System.out);
            } else {
                verboseListener.setWriter(new FileWriter(commandLine.getOptionalSwitch("verbose")));
            }
        }

        /*
         *  Beginning of main processing
         */

        Date start = new Date();

        List parameters = commandLine.getParameters();
        if (parameters.size() == 0) {
            parameters.add(".");
        }

        SymbolGatherer collector = new SymbolGatherer();

        // Since SymbolGatherer lists everything by default,
        // we turn them all off if any of the switches are
        // present.  This way, if you pass nothing, you get
        // the default behavior and the tool shows everything.
        // If you pass in one or more, you only see symbols
        // of the kind(s) you specified.
        if (commandLine.isPresent("class-names") || commandLine.isPresent("field-names") || commandLine.isPresent("method-names") || commandLine.isPresent("local-names")) {
            collector.setCollectingClassNames(false);
            collector.setCollectingFieldNames(false);
            collector.setCollectingMethodNames(false);
            collector.setCollectingLocalNames(false);
        }

        if (commandLine.isPresent("class-names")) {
            collector.setCollectingClassNames(true);
        }

        if (commandLine.isPresent("field-names")) {
            collector.setCollectingFieldNames(true);
        }

        if (commandLine.isPresent("method-names")) {
            collector.setCollectingMethodNames(true);
        }

        if (commandLine.isPresent("local-names")) {
            collector.setCollectingLocalNames(true);
        }
        
        ClassfileLoader loader = new TransientClassfileLoader();
        loader.addLoadListener(new LoadListenerVisitorAdapter(collector));
        loader.addLoadListener(verboseListener);
        loader.load(parameters);

        PrintWriter out;
        if (commandLine.isPresent("out")) {
            out = new PrintWriter(new FileWriter(commandLine.getSingleSwitch("out")));
        } else {
            out = new PrintWriter(new OutputStreamWriter(System.out));
        }

        Iterator i = collector.getCollection().iterator();
        while (i.hasNext()) {
            out.println(i.next());
        }

        Date end = new Date();

        if (commandLine.getToggleSwitch("time")) {
            System.err.println(ListSymbols.class.getName() + ": " + ((end.getTime() - (double) start.getTime()) / 1000) + " secs.");
        }

        out.close();

        verboseListener.close();
    }
}
