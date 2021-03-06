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

import gchisto2.gctrace.GcTrace;
import gchisto2.gctracegenerator.GcTraceGeneratorListener;
import gchisto2.utils.MessageReporter;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author tony
 */
public class FileGcTrace extends GcTrace {

    final protected File file;
    final protected Date lastModifiedDate;
    final protected GCLogFileReader reader;
    
    private class ConcurrentFileReader extends Thread {
        private GcTraceGeneratorListener listener;
        private GcLogFileReaderThrottle throttle;
        
        @Override
        public void run() {
            MessageReporter.showMessage("Started reading file " + file.getAbsolutePath());
            listener.started();
            try {
                readFile(throttle);
                MessageReporter.showMessage("Finished reading file " + file.getAbsolutePath());
                listener.finished(FileGcTrace.this);
            } catch (IOException e) {
                MessageReporter.showError("Error reading file " + file.getAbsolutePath());
                listener.failed();
            }
        }
        
        public ConcurrentFileReader(GcTraceGeneratorListener listener,
                                    GcLogFileReaderThrottle throttle) {
            this.listener = listener;
            this.throttle = throttle;
        }
    }
    
    protected void readFileConcurrently(GcTraceGeneratorListener listener) {
        readFileConcurrently(listener, new NopGcLogFileReaderThrottle());
    }

    protected void readFileConcurrently(GcTraceGeneratorListener listener,
                                        GcLogFileReaderThrottle throttle) {
        ConcurrentFileReader reader = new ConcurrentFileReader(listener, throttle);
        reader.start();
    }

    @Override
    public String getLongName() {
        return "File : " + file.getAbsolutePath();
    }
    
    public File getFile() {
        return file;
    }
    
    private void readFile(GcLogFileReaderThrottle throttle)
            throws IOException {
        reader.readFile(file, this, throttle);
    }
    
    public void init(GcTraceGeneratorListener listener) {
        readFileConcurrently(listener);
    }
    
    @Override
    public String getSuggestedName() {
        return "File : " + file.getName();
    }
    
    @Override
    public String getInfoString() {
        return getName() + "\n" +
                "\n" +
                "Path : " + file.getAbsolutePath() + "\n" +
                "Read On : " + getAddedDate() + "\n" +
                "Last Modified On : " + lastModifiedDate;
    }
    
    public FileGcTrace(File file, GCLogFileReader reader) {
        reader.setupGcActivityNames(this);
        
        this.file = file;
        this.lastModifiedDate = new Date(file.lastModified());
        this.reader = reader;
    }

}
