/*
 *  Copyright (c) 2001-2006, Jean Tessier
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

package com.jeantessier.classreader;

import java.util.*;

import org.apache.oro.text.perl.*;

public class FeatureDependencyCollector extends CollectorBase {
    private static final Perl5Util perl = new Perl5Util();

    private Class_info thisClass;

    public void visitClassfile(Classfile classfile) {
        thisClass = classfile.getRawClass();

        classfile.getConstantPool().accept(this);
    }

    public void visitFieldRef_info(FieldRef_info entry) {
        if (entry.getRawClass() != thisClass) {
            add(entry.getClassName() + "." + entry.getRawNameAndType().getName());
        }
    }

    public void visitMethodRef_info(MethodRef_info entry) {
        if ((entry.getRawClass() != thisClass) && !perl.match("/<.*init>/", entry.getRawNameAndType().getName())) {
            add(entry.getClassName() + "." + entry.getRawNameAndType().getName());
        }
    }

    public void visitInterfaceMethodRef_info(InterfaceMethodRef_info entry) {
        if (entry.getRawClass() != thisClass) {
            add(entry.getClassName() + "." + entry.getRawNameAndType().getName());
        }
    }

    public void visitMethod_info(Method_info entry) {
        processSignature(entry.getDescriptor());
    
        super.visitMethod_info(entry);
    }

    public void visitCode_attribute(Code_attribute attribute) {
        byte[] code = attribute.getCode();

        Iterator ci = attribute.iterator();
        while (ci.hasNext()) {
            Instruction instr = (Instruction) ci.next();
            switch (instr.getOpcode()) {
                case 0xb2: // getstatic
                case 0xb3: // putstatic
                case 0xb4: // getfield
                case 0xb5: // putfield
                case 0xb6: // invokevirtual
                case 0xb7: // invokespecial
                case 0xb8: // invokestatic
                case 0xb9: // invokeinterface
                    int start = instr.getStart();
                    int index = (code[start+1] << 8) | code[start+2];
                    attribute.getClassfile().getConstantPool().get(index).accept(this);
                    break;
                default:
                    // Do nothing
                    break;
            }
        }

        super.visitCode_attribute(attribute);
    }

    private void processSignature(String str) {
        int currentPos = 0;
        int startPos;
        int endPos;

        while ((startPos = str.indexOf('L', currentPos)) != -1) {
            if ((endPos = str.indexOf(';', startPos)) != -1) {
                String candidate = str.substring(startPos + 1, endPos);
                if (!thisClass.getName().equals(candidate)) {
                    add(SignatureHelper.path2ClassName(candidate));
                }
                currentPos = endPos + 1;
            } else {
                currentPos = startPos + 1;
            }
        }
    }
}
