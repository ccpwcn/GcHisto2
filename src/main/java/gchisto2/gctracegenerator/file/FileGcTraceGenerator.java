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
package gchisto2.gctracegenerator.file;

import gchisto2.gctracegenerator.GcTraceGeneratorForFiles;
import gchisto2.gctracegenerator.GcTraceGeneratorListener;
import gchisto2.utils.errorchecking.ArgumentChecking;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author tony
 */
abstract public class FileGcTraceGenerator implements GcTraceGeneratorForFiles {

    static private File currDir = new File(".");
    
    protected File getFileFromDialog(JComponent component) {
        assert component != null;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currDir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().toLowerCase().endsWith(".log");
            }

            @Override
            public String getDescription() {
                return "GC 日志文件(.log)";
            }
        });
        int ret = chooser.showOpenDialog(component);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }
    
    abstract protected FileGcTrace newFileGcTrace(File file);
    
    @Override
    public void createNewGcTrace(File file,
                                 GcTraceGeneratorListener listener) {
        FileGcTrace gcTrace = newFileGcTrace(file);
        gcTrace.init(listener);
        currDir = file;
    }
    
    @Override
    public void createNewGcTrace(JComponent component,
                                 GcTraceGeneratorListener listener) {
        ArgumentChecking.notNull(component, "component");
        
        File file = getFileFromDialog(component);
        if (file != null) {
            createNewGcTrace(file, listener);
        }
    }
    
    public FileGcTraceGenerator() {
    }
    
}
