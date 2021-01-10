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

import gchisto2.gcactivity.GcActivity;
import gchisto2.gcactivity.GcActivitySet;
import gchisto2.utils.errorchecking.ArgumentChecking;
import gchisto2.utils.errorchecking.ErrorReporting;
import java.util.Date;
import java.util.LinkedList;

/**
 * A set of GC traces. Each GC trace is associated with a unique name, as well
 * as an index, which is the position of the GC trace in the GC trace set list,
 * starting from 0. This is the data structure from which all the data
 * in the loaded GC traces are reachable from.
 * <p>
 * Because it extends <tt>java.util.LinkedList</tt>, an iteration over the GC
 * activities in it can be easily done using the standard for-loop over
 * collections.
 *
 * @author Tony Printezis
 * @see    GcTrace
 * @see    GcTraceSetListener
 * @see    java.util.LinkedList
 */
public class GcTraceSet extends LinkedList<GcTrace> implements GcTraceListener {
    
    /**
     * The map that contains all the GC activity names of all the
     * GC traces added to this set.
     *
     * @see #recreateAllGcActivityNames()
     */
    private GcActivityNames allGcActivityNames = new GcActivityNames();
    
    /**
     * The GC trace set listeners.
     *
     * @see #addListener(GcTraceSetListener)
     * @see #removeListener(GcTraceSetListener)
     */
    final private GcTraceSetListenerSet listeners = new GcTraceSetListenerSet();
    
    /**
     * It creates a name for the GC trace that is associated with the
     * given file that is unique in this GC trace set. Typically, the name will
     * be the name of the file, let's call it NAME, without the associated path
     * information. If that is not unique, a suffix .NUM will be added, where
     * NUM is an integer. NUM will start from 0 and will increase until
     * NAME.NUM is unique.
     *
     * @param gcTrace The file for which a unique name will be created.
     * @return A unique GC trace name for the given file.
     */
    String createUniqueGcTraceName(GcTrace gcTrace) {
        assert gcTrace != null;
        
        String originalName = gcTrace.getSuggestedName();
        String name = originalName;
        int i = 0;
        while (findGcTrace(name) != null) {
            ++i;
            name = originalName + "." + i;
        }
        return name;
    }
    
    /**
     * It iterates over the GC traces in this set and recreates
     * the map that contains all the GC activity names.
     */
    private void recreateAllGcActivityNames() {
        allGcActivityNames = new GcActivityNames();
        for (GcTrace trace : this) {
            GcActivityNames gcActivityNames =  trace.getGcActivityNames();
            allGcActivityNames.merge(gcActivityNames);
            
        }
    }
    
    /**
     * It finds the GC trace associated with the given name.
     *
     * @param gcTraceName The name of the GC trace to be looked up.
     * @return The GC trace associated with the given name, or <tt>null</tt>
     * if the name does not appear in this GC trace set.
     */
    public GcTrace findGcTrace(String gcTraceName) {
        ArgumentChecking.notNull(gcTraceName, "gcTraceName");
        
        for (GcTrace trace : this) {
            if (trace.getName().equals(gcTraceName)) {
                return trace;
            }
        }
        return null;
    }
    
    /**
     * It finds the GC trace with the given index.
     *
     * @param index The index of the GC trace to be looked up.
     * @return The GC trace associated with the given index.
     */
    public GcTrace findGcTrace(int index) {
        ArgumentChecking.withinBounds(index, 0, size() - 1, "index");
        
        return get(index);
    }
    
    /**
     * It finds the index of the GC trace associated with the given name.
     *
     * @param gcTraceName The name of the GC trace to be looked up.
     * @return The index of the GC trace associated with the given name,
     * or <tt>-1</tt> if the name does not appear in this GC trace set.
     */
    public int findGcTraceIndex(String gcTraceName) {
        ArgumentChecking.notNull(gcTraceName, "gcTraceName");
        
        int index = 0;
        for (GcTrace trace : this) {
            if (trace.getName().equals(gcTraceName)) {
                return index;
            }
            ++index;
        }
        return -1;
    }
    
    /**
     * It adds a new GC trace to this set. Before adding it, it will set
     * its name to one that is based on the file of the GC trace and
     * that is unique in this set. After adding it, it will call the
     * <tt>gcTraceAdded()</tt> method on the listeners of this set.
     *
     * @param gcTrace The new GC trace to be added to this set.
     *
     * @see #createUniqueGcTraceName(GcTrace)
     * @see GcTraceSetListener#gcTraceAdded(GcTrace)
     */
    synchronized public void addGcTrace(GcTrace gcTrace) {
        ArgumentChecking.notNull(gcTrace, "gcTrace");
        
        String gcTraceName = createUniqueGcTraceName(gcTrace);
        gcTrace.setName(gcTraceName);
        gcTrace.setAddedDate(new Date(System.currentTimeMillis()));
        gcTrace.addListener(this);
        add(gcTrace);
        
        recreateAllGcActivityNames();
        listeners.callGCTraceAdded(gcTrace);
        gcTrace.afterAddingToGcTraceSet();
    }
    
    /**
     * It renames a GC trace. If the new name already exists, it will do
     * nothing. If the new name does not exist, it will set the name of
     * the GC trace to the new one. After renaming it, it will call the
     * <tt>gcTraceRenamed()</tt> method on the listeners of this set.
     *
     * @param gcTraceName The name of the GC trace to be renamed.
     * @param newName The new name of the GC trace.
     *
     * @see GcTraceSetListener#gcTraceRenamed(GcTrace)
     */
    synchronized public void rename(String gcTraceName, String newName) {
        ArgumentChecking.notNull(gcTraceName, "gcTraceName");
        ArgumentChecking.notNull(newName, "newName");
        
        GcTrace gcTrace = findGcTrace(gcTraceName);
        ErrorReporting.fatalError(gcTrace != null,
                gcTraceName + " does not exist in the GC trace set.");
        if (findGcTrace(newName) == null) {
            gcTrace.setName(newName);
            listeners.callGCTraceRenamed(gcTrace);
        } else {
            ErrorReporting.warning("GC trace name " + newName +
                    " already exists.");
        }
    }
    
    /**
     * It removes the GC trace associated with the given name from this set.
     * After removing it, it will call the <tt>gcTraceRemoved</tt> method
     * on the listeners of this set.
     *
     * @param gcTraceName The name of the GC trace to be removed.
     *
     * @see GcTraceSetListener#gcTraceRemoved(GcTrace)
     */
    synchronized public void remove(String gcTraceName) {
        ArgumentChecking.notNull(gcTraceName, "gcTraceName");
        
        GcTrace gcTrace = findGcTrace(gcTraceName);
        ErrorReporting.fatalError(gcTrace != null,
                gcTraceName + " does not exist in the GC trace set");
        gcTrace.beforeRemovingFromGcTraceSet();
        boolean ret = super.remove(gcTrace);
        assert ret;
        
        recreateAllGcActivityNames();
        listeners.callGCTraceRemoved(gcTrace);
    }
    
    /**
     * It moves the GC trace associated with the given name up in the order
     * in this set, so that its index is its old index minus 1. If its
     * index is 0, then it does nothing. After moving it, it will call
     * the <tt>gcTraceMovedUp</tt> method on the listeners of this set.
     *
     * @param gcTraceName The name of the GC trace to be moved.
     *
     * @see GcTraceSetListener#gcTraceMovedUp(GcTrace)
     */
    synchronized public void moveUp(String gcTraceName) {
        ArgumentChecking.notNull(gcTraceName, "gcTraceName");
        
        GcTrace gcTrace = findGcTrace(gcTraceName);
        ErrorReporting.fatalError(gcTrace != null,
                gcTraceName + " does not exist in the GC trace set");
        int index = indexOf(gcTrace);
        assert 0 <= index && index < size();
        if (index > 0) {
            super.remove(gcTrace);
            add(index - 1, gcTrace);
            listeners.callGCTraceMovedUp(gcTrace);
        } else {
            ErrorReporting.warning("GC trace " + gcTraceName +
                    " already at small index.");
        }
    }
    
    /**
     * It moves the GC trace associated with the given name down in the order
     * in this set, so that its index is its old index plus 1. If its
     * index is the highest, then it does nothing. After moving it, it will call
     * the <tt>gcTraceMovedDown</tt> method on the listeners of this set.
     *
     * @param gcTraceName The name of the GC trace to be moved.
     *
     * @see GcTraceSetListener#gcTraceMovedDown(GcTrace)
     */
    synchronized public void moveDown(String gcTraceName) {
        ArgumentChecking.notNull(gcTraceName, "gcTraceName");
        
        GcTrace gcTrace = findGcTrace(gcTraceName);
        ErrorReporting.fatalError(gcTrace != null,
                gcTraceName + " does not exist in the GC trace set");
        int index = indexOf(gcTrace);
        assert 0 <= index && index < size();
        if (index < (size() - 1)) {
            super.remove(gcTrace);
            add(index + 1, gcTrace);
            listeners.callGCTraceMovedDown(gcTrace);
        } else {
            ErrorReporting.warning("GC trace " + gcTraceName +
                    " already at highest index.");
        }
    }
    
    /**
     * It adds a listener to this set.
     *
     * @param listener The listener to be added to this set.
     */
    synchronized public void addListener(GcTraceSetListener listener) {
        ArgumentChecking.notNull(listener, "listener");
        
        listeners.add(listener);
    }
    
    /**
     * It removes a listener from this set.
     *
     * @param listener The listener to be removed from this set.
     */
    synchronized public void removeListener(GcTraceSetListener listener) {
        ArgumentChecking.notNull(listener, "listener");
        
        listeners.remove(listener);
    }
    
    public GcActivityNames getAllGcActivityNames() {
        return allGcActivityNames;
    }
    
    /**
     * It returns the ID of the given GC activity name in the map that
     * contains all the GC activity names that appear in the GC traces of
     * this set.
     *
     * @param gcActivityName The GC activity name that will be looked up.
     * @return The ID of the given GC activity name in the map that
     * contains all the GC activity names that appear in the GC traces of
     * this set.
     */
    public int getActivityId(String gcActivityName) {
        return allGcActivityNames.indexOf(gcActivityName);
    }
    
    @Override
    public void gcActivityAdded(
            GcTrace gcTrace,
            GcActivitySet gcActivitySet,
            GcActivity gcActivity) {
    }

    @Override
    public void gcActivityNameAdded(GcTrace gcTrace,
                                    int id,
                                    String gcActivityName) {
        recreateAllGcActivityNames();
    }
    
    /**
     * It creates a new GC trace set instance.
     */
    public GcTraceSet() {
    }

}
