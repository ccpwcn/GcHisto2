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
package gcparser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;

public class GCDataStore extends GCStats {
    GCDataStore(EnumMap<GCMetric, Boolean> enabledMap, int cpuCount,
                boolean hasTimeZero) {
        super(enabledMap, cpuCount, hasTimeZero);

        Class<GCMetric> c = GCMetric.class;
        dataMap = new EnumMap<>(c);
        timeMap = new EnumMap<>(c);

        ArrayList<Double> tlist = null;
        for (GCMetric metric : GCMetric.values()) {
            dataMap.put(metric, new ArrayList<>());
            switch (metric.timestamp_type()) {
                case 0:
                    tlist = null;
                    break;
                case 1:
                    tlist = new ArrayList<>();
                    break;
                default:
                    break;
            }
            timeMap.put(metric, tlist);
        }
    }

    @Override
    public void add(GCMetric metric, double val) {
        super.add(metric, val);
        dataMap.get(metric).add(val);
    }

    @Override
    public void add(GCMetric metric, String s) {
        double val = Double.parseDouble(s);
        add(metric, val);
    }

    @Override
    public void addTimestamp(GCMetric metric, double beg, double end) {
        super.addTimestamp(metric, beg, end);
        ArrayList<Double> tlist = timeMap.get(metric);
        if (tlist != null) {
            tlist.add(timestamp_offset() + beg);
        }
    }

    public ArrayList<Double> data(GCMetric metric) {
        return dataMap.get(metric);
    }

    public ArrayList<Double> time(GCMetric metric) {
        return timeMap.get(metric);
    }

    @Override
    public void save(String prefix, String suffix) throws IOException {
        for (GCMetric metric : GCMetric.values()) {
            save(metric, prefix, suffix);
        }
    }

    public void save(GCMetric metric, String prefix, String suffix)
            throws IOException {
        if (disabled(metric)) {
            return;
        }

        ArrayList<Double> d = data(metric);
        if (d.size() == 0) {
            return;
        }
        Iterator<Double> diter = d.iterator();

        ArrayList<Double> t = time(metric);
        Iterator<Double> titer = t.iterator();
        // t != null ? t.iterator() : new NumberIterator(0.0, 1.0);

        String name = filename(metric, prefix, suffix);
        FileWriter fw = new FileWriter(name);
        BufferedWriter w = new BufferedWriter(fw);

        while (diter.hasNext()) {
            w.write(titer.next().toString());
            w.write(' ');
            w.write(diter.next().toString());
            w.write(eol);
        }
        w.close();
    }

    protected String filename(GCMetric metric, String prefix, String suffix) {
        StringBuilder filename = new StringBuilder();
        if (prefix != null) {
            filename.append(prefix);
        }
        filename.append(metric);
        if (suffix != null) {
            filename.append(suffix);
        }
        return filename.toString();
    }

    private final EnumMap<GCMetric, ArrayList<Double>> dataMap;
    private final EnumMap<GCMetric, ArrayList<Double>> timeMap;
}
