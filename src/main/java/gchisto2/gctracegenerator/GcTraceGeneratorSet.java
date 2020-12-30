/*
 * Copyright 2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */
package gchisto2.gctracegenerator;

import gchisto2.utils.errorchecking.ErrorReporting;

import java.util.LinkedList;

/**
 * @author wengyingjian
 * @updated by lidawei 2020/12/30
 * @date 2015/10/29
 */
public class GcTraceGeneratorSet extends LinkedList<GcTraceGenerator> {

    static private final int GC_TRACE_GENERATOR_FOR_FILES_INDEX = 0;
    static private final String[] GC_TRACE_GENERATOR_CLASS_NAMES = {
            "gchisto2.gctracegenerator.file.hotspot.GcTraceGenerator",
            "gchisto2.gctracegenerator.file.hotspot.DynamicGcTraceGenerator",
            "gchisto2.gctracegenerator.file.simple.GcTraceGenerator",
            "gchisto2.gctracegenerator.file.simple.DynamicGcTraceGenerator"
    };
    private GcTraceGeneratorForFiles gcTraceGeneratorForFiles;

    public GcTraceGeneratorForFiles gcTraceGeneratorForFiles() {
        return gcTraceGeneratorForFiles;
    }

    public GcTraceGeneratorSet() {
        for (String className : GC_TRACE_GENERATOR_CLASS_NAMES) {
            try {
                Class<?> c = Class.forName(className);
                Object s = c.newInstance();
                GcTraceGenerator gcTraceGenerator = (GcTraceGenerator) s;
                add(gcTraceGenerator);
            } catch (ClassNotFoundException | InstantiationException e) {
                ErrorReporting.warning("could not instantiate " + className);
            } catch (IllegalAccessException e) {
                ErrorReporting.warning("could not access constructor of " + className);
            } catch (ClassCastException e) {
                ErrorReporting.warning("could not cast " + className + " to GcTraceGenerator");
            }
        }
        ErrorReporting.fatalError(size() > 0,
                "There must be at least one GC trace generator set up");

        try {
            gcTraceGeneratorForFiles = (GcTraceGeneratorForFiles) get(GC_TRACE_GENERATOR_FOR_FILES_INDEX);
        } catch (ClassCastException e) {
            ErrorReporting.fatalError("could not cast GC trace generator with index " +
                    GC_TRACE_GENERATOR_FOR_FILES_INDEX + " to GcTraceGeneratorForFiles");
        }
        ErrorReporting.fatalError(gcTraceGeneratorForFiles != null,
                "The GC trace generator for files should not be null");
    }
}
