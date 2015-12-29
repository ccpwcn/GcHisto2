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
package gchisto.gui.panels.gcdistribution;

import gchisto.gctrace.GCTrace;
import gchisto.gctrace.GCTraceCheckpoint;
import gchisto.gui.utils.TabbedPane;

/**
 *
 * @author tony
 */
public class Panel extends TabbedPane<ChartPanelSingle> {

    protected ChartPanelSingle newPanel(GCTrace gcTrace) {
        GCTraceCheckpoint checkpoint = new GCTraceCheckpoint(gcTrace);
        checkpoint.checkpoint();
        
        Dataset dataset = new Dataset(gcTrace, checkpoint);
        
        String unitName = String.format("%1.0f ms buckets",
                Dataset.bucketDurationMS());
        String name = gcTrace.getName();
        ChartPanelSingle panel = new ChartPanelSingle(
                name, unitName, dataset, checkpoint);
        gcTrace.addListener(panel);
        
        return panel;
    }

    protected void updatePanel(ChartPanelSingle panel) {
        panel.possiblyRefresh();
    }

    public String getPanelName() {
        return "GC Pause Distribution";
    }

}
