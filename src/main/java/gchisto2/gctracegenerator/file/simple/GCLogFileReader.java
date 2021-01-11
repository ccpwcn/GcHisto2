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
package gchisto2.gctracegenerator.file.simple;

import gchisto2.gctrace.GcTrace;
import gchisto2.gctracegenerator.file.GcLogFileReaderThrottle;
import gchisto2.utils.Comparisons;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 *
 * @author tony
 */
public class GCLogFileReader
        implements gchisto2.gctracegenerator.file.GCLogFileReader {

    final private String[] SHARED_ACTIVITIES = {"YoungGC", "FullGC"};
    final private List<String> gcActivityNames = new ArrayList<String>();

    private double parseDouble(StringTokenizer st) {
        assert st.hasMoreTokens();
        String str = st.nextToken();
        return Double.parseDouble(str);
    }

    private String parseString(StringTokenizer st) {
        assert st.hasMoreTokens();
        String str = st.nextToken();
        return str;
    }

    private long parseLong(StringTokenizer st) {
        assert st.hasMoreTokens();
        String str = st.nextToken();
        return Long.parseLong(str);
    }

    private int mapGCActivityNameToID(String name)
            throws IOException {
        return gcActivityNames.indexOf(name);
    }

    private double checkBounds(double value) throws NumberFormatException {
        if (value < 0.0) {
            if (Comparisons.lt(value, 0.0)) {
                throw new NumberFormatException("value (" + value + ") less tha zero.");
            } else {
                return 0.0;
            }
        }
        return value;
    }

    private String addSpaces(String str) {
        StringBuilder builder = new StringBuilder();
        boolean prevLowercase = false;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (Character.isLowerCase(c)) {
                prevLowercase = true;
            }
            if (Character.isUpperCase(c)) {
                if (prevLowercase) {
                    builder.append(" ");
                    prevLowercase = false;
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private void ensureGCActivityAdded(GcTrace gcTrace, String name) {
        if (!gcActivityNames.contains(name)) {
            if ("Remark".equals(name)) {
                ensureGCActivityAdded(gcTrace, "InitialMark");
            }
            
            gcActivityNames.add(name);
            gcTrace.addGcActivityName(gcActivityNames.indexOf(name),
                    addSpaces(name));
        }
    }

    @Override
    public void setupGcActivityNames(GcTrace gcTrace) {
        for (String name : SHARED_ACTIVITIES) {
            ensureGCActivityAdded(gcTrace, name);
        }
    }

    @Override
    public void readFile(File file,
                         GcTrace gcTrace,
                         GcLogFileReaderThrottle throttle)
            throws IOException {
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        int lineCount = 0;
        String line = bufferedReader.readLine();

        try {
            throttle.started();
            while (throttle.shouldContinue() && line != null) {
                lineCount += 1;
                StringTokenizer st = new StringTokenizer(line);

                if (st.countTokens() > 0) {
                    String activityName = parseString(st);
                    Double startSec = parseDouble(st);
                    Double durationSec = parseDouble(st);

                    throttle.beforeAddingGcActivity(startSec);

                    ensureGCActivityAdded(gcTrace, activityName);
                    assert gcActivityNames.contains(activityName);
                    int id = mapGCActivityNameToID(activityName);

                    int remaining = st.countTokens();
                    boolean stw;
                    double overheadPerc;
                    long[] spacesBefore;
                    long[] spacesAfter;

                    assert remaining <= 3;
                    if (remaining == 0 || remaining == 2) {
                        stw = true;
                        overheadPerc = 100.0;
                    } else {
                        assert remaining == 1 || remaining == 3;
                        stw = false;
                        overheadPerc = parseDouble(st);
                    }
                    if (remaining == 0 || remaining == 1) {
                        spacesBefore = new long[0];
                        spacesAfter = new long[0];
                    } else {
                        assert remaining == 2 || remaining == 3;
                        spacesBefore = new long[1];
                        spacesBefore[0] = parseLong(st);
                        spacesAfter = new long[1];
                        spacesAfter[0] = parseLong(st);
                    }
                    assert !st.hasMoreTokens();

                    startSec = checkBounds(startSec);
                    durationSec = checkBounds(durationSec);

                    if (stw) {
                        gcTrace.addGcActivity(id, startSec, durationSec);
                    } else {
                        gcTrace.addGcActivity(id, startSec, durationSec, overheadPerc);
                    }
                    throttle.afterAddingGcActivity(startSec);
                }

                line = bufferedReader.readLine();
            }
        } catch (NoSuchElementException e) {
            throw new IOException("parsing error, line " + lineCount);
        } catch (NumberFormatException e) {
            throw new IOException("parsing error, line " + lineCount);
        } finally {
            throttle.finished();
            reader.close();
        }
    }

    public GCLogFileReader() {
    }
}
