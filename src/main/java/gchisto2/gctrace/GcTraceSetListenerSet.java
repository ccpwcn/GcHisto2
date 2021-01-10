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
package gchisto2.gctrace;

import gchisto2.utils.ListenerSet;
import gchisto2.utils.errorchecking.ArgumentChecking;

/**
 * A set of GC trace set listeners.
 *
 * @author Tony Printezis
 * @see    GcTraceSet
 * @see    GcTraceSetListener
 */
public class GcTraceSetListenerSet extends ListenerSet<GcTraceSetListener> {
    
    /**
     * It calls the <tt>gcTraceAdded()</tt> method on all the listeners in
     * the listener set.
     *
     * @param gcTrace The GC trace that has just been added to the GC
     * trace set.
     * 
     * @see GcTraceSetListener#gcTraceAdded(GcTrace)
     */
    public void callGCTraceAdded(GcTrace gcTrace) {
        ArgumentChecking.notNull(gcTrace, "gcTrace");
        
        for (GcTraceSetListener listener : listeners()) {
            listener.gcTraceAdded(gcTrace);
        }
    }
    
    /**
     * It calls the <tt>gcTraceRenamed()</tt> method on all the listeners in
     * the listener set.
     *
     * @param gcTrace The GC trace that has just been renamed in the GC
     * trace set.
     * 
     * @see gchisto2.gctraceset.GCTraceSetListener#gcTraceRenamed(GcTrace)
     */
    public void callGCTraceRenamed(GcTrace gcTrace) {
        ArgumentChecking.notNull(gcTrace, "gcTrace");
        
        for (GcTraceSetListener listener : listeners()) {
            listener.gcTraceRenamed(gcTrace);
        }
    }
    
    /**
     * It calls the <tt>gcTraceRemoved()</tt> method on all the listeners in
     * the listener set.
     *
     * @param gcTrace The GC trace that has just been removed from the GC
     * trace set.
     * 
     * @see gchisto2.gctraceset.GCTraceSetListener#gcTraceRemoved(GcTrace)
     */
    public void callGCTraceRemoved(GcTrace gcTrace) {
        ArgumentChecking.notNull(gcTrace, "gcTrace");
        
        for (GcTraceSetListener listener : listeners()) {
            listener.gcTraceRemoved(gcTrace);
        }
    }
    
    /**
     * It calls the <tt>gcTraceMovedUp()</tt> method on all the listeners in
     * the listener set.
     *
     * @param gcTrace The GC trace that has just been moved up in the GC
     * trace set.
     * 
     * @see gchisto2.gctraceset.GCTraceSetListener#gcTraceMovedUp(GcTrace)
     */
    public void callGCTraceMovedUp(GcTrace gcTrace) {
        ArgumentChecking.notNull(gcTrace, "gcTrace");
        
        for (GcTraceSetListener listener : listeners()) {
            listener.gcTraceMovedUp(gcTrace);
        }
    }
    
    /**
     * It calls the <tt>gcTraceMovedDown()</tt> method on all the listeners in
     * the listener set.
     *
     * @param gcTrace The GC trace that has just been moved down in the GC
     * trace set.
     * 
     * @see gchisto2.gctraceset.GCTraceSetListener#gcTraceMovedDown(GcTrace)
     */
    public void callGCTraceMovedDown(GcTrace gcTrace) {
        ArgumentChecking.notNull(gcTrace, "gcTrace");
        
        for (GcTraceSetListener listener : listeners()) {
            listener.gcTraceMovedDown(gcTrace);
        }
    }
    
    /**
     * It creates a new GC trace set listener set.
     */
    public GcTraceSetListenerSet() {
    }
    
}
