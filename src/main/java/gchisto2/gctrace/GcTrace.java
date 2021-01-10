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
import java.util.ArrayList;
import java.util.Date;

/**
 * It represents a GC trace. It contains a set of GC activity sets,
 * one for each of the different GC activities that appear in the GC trace file.
 * <p>
 * Because it extends <tt>java.util.ArrayList</tt>, an iteration over the GC
 * activitiy sets in it can be easily done using the standard for-loop over
 * collections.
 *
 * @author Tony Printezis
 * @see    GcActivitySet
 * @see    GcActivityNames
 * @see    GcTraceSet
 */
public abstract class GcTrace extends ArrayList<GcActivitySet> {

    /**
     * The name that is associated with this GC trace. This is unique across
     * all the GC traces that are included in a single GC trace set.
     *
     * @see #getName()
     * @see #setName(String)
     * @see GcTraceSet#createUniqueGcTraceName(GcTrace)
     */
    private String name;
    /**
     * The date/time when this GC trace was populated.
     *
     * @see #getAddedDate()
     * @see #setAddedDate(Date)
     */
    private Date addedDate;
    /**
     * A map of the GC activity names that appear in this GC trace.
     */
    final private GcActivityNames gcActivityNames = new GcActivityNames();
    final private GcTraceListenerSet listeners = new GcTraceListenerSet();
    final private Object hashCodeObject = new Object();
    final private GcActivitySet allGcActivities = new GcActivitySet("All");
    private double lastTimeStampSec;

    /**
     * 建议的名称
     * @return 字符串
     */
    abstract public String getSuggestedName();

    /**
     * GC信息
     * @return 字符串
     */
    abstract public String getInfoString();

    @Override
    public boolean equals(Object gcTrace) {
        assert gcTrace instanceof GcTrace;
        return ((GcTrace) gcTrace).hashCodeObject == hashCodeObject;
    }

    @Override
    public int hashCode() {
        return hashCodeObject.hashCode();
    }

    /**
     * It returns the name that is associated with this GC trace. This is
     * unique across all the GC traces that are included in a single GC trace
     * set.
     *
     * @return The name that is associated with this GC trace. This is
     * unique across all the GC traces that are included in a single GC trace
     * set.
     *
     * @see #setName(String)
     * @see GcTraceSet#createUniqueGcTraceName(GcTrace)
     */
    public String getName() {
        return name;
    }
    
    public String getLongName() {
        return getName();
    }

    /**
     * It returns the date/time when this GC trace was populated.
     *
     * @return The date/time when this GC trace was populated.
     * @see #setAddedDate(Date)
     */
    public Date getAddedDate() {
        return addedDate;
    }

    /**
     * It returns a map of the GC activity names that appear in this GC trace.
     *
     * @return A map of the GC activity names that appear in this GC trace.
     */
    public GcActivityNames getGcActivityNames() {
        return gcActivityNames;
    }

    public GcActivitySet getAllGcActivities() {
        return allGcActivities;
    }

    public double getLastTimeStampSec() {
        return lastTimeStampSec;
    }
    
    /**
     * It returns an array containing the GC activity names that appear in
     * this GC trace.
     *
     * @return An array containing the GC activity names that appear in this
     * GC trace.
     */
    public String[] getGcActivityNamesArray() {
        return gcActivityNames.getNames();
    }

    /**
     * It sets the name of this GC trace.
     *
     * @param name The new name of this GC trace.
     *
     * @see #getName()
     * @see GcTraceSet#createUniqueGcTraceName(GcTrace)
     */
    public void setName(String name) {
        ArgumentChecking.notNull(name, "name");

        this.name = name;
    }

    /**
     * It sets the read date/time of this GC trace.
     *
     *
     * @param addedDate The new read date/time of this GC trace.
     * @see #getAddedDate()
     */
    public void setAddedDate(Date addedDate) {
        ArgumentChecking.notNull(addedDate, "addedDate");

        this.addedDate = addedDate;
    }

    /**
     * It adds a new GC activity to this GC trace. The GC activity will be added
     * to the GC activity set that corresponds to the given GC activity name.
     * If a GC activity set does not exist in this GC trace for this GC activity
     * name, it will be created. This version should be used for concurrent
     * GC activities.
     *
     * @param id The id of the GC activity to be added.
     * @param startSec The time stamp of the start of the GC activity to
     * be added, in seconds.
     * @param durationSec The duration of the GC activity, in seconds.
     */
    public void addGcActivity(
            int id,
            double startSec,
            double durationSec) {
        ArgumentChecking.withinBounds(id, 0, size() - 1, "id");

        String gcActivityName = gcActivityNames.get(id);
        addGcActivity(id,
                new GcActivity(gcActivityName, startSec, durationSec));
    }

    /**
     * It adds a new GC activity to this GC trace. The GC activity will be added
     * to the GC activity set that corresponds to the given GC activity name.
     * If a GC activity set does not exist in this GC trace for this GC activity
     * name, it will be created. This version should be used for stop-the-world
     * GC activities.
     *
     * @param id The id of the GC activity to be added.
     * @param startSec The time stamp of the start of the GC activity to
     * be added, in seconds.
     * @param durationSec The duration of the GC activity, in seconds.
     * @param overheadPerc The concurrent overhead of the GC activity to
     * be added.
     */
    public void addGcActivity(
            int id,
            double startSec,
            double durationSec,
            double overheadPerc) {
        ArgumentChecking.withinBounds(id, 0, size() - 1, "id");

        String gcActivityName = gcActivityNames.get(id);
        addGcActivity(id, new GcActivity(
                gcActivityName,
                startSec, durationSec,
                overheadPerc));
    }

    /**
     * It adds a new GC activity to this GC trace. This is a private method
     * that is used by all the public ones.
     *
     * @param id The id of the GC activity to be added.
     * @param gcActivity The GC activity to be added.
     */
    synchronized private void addGcActivity(
            int id,
            GcActivity gcActivity) {
        assert 0 <= id && id < size();
        assert id < gcActivityNames.size();
        assert gcActivityNames.get(id).equals(gcActivity.getName());

        GcActivitySet gcActivitySet = get(id);
        gcActivitySet.addGCActivity(gcActivity);
        allGcActivities.addGCActivity(gcActivity);
        lastTimeStampSec = gcActivity.getEndSec();

        listeners.callGCActivityAdded(this, gcActivitySet, gcActivity);
    }

    public void addGcActivityName(int id, String gcActivityName) {
        assert gcActivityNames.size() == id;
        gcActivityNames.add(id, gcActivityName);
        assert gcActivityNames.size() == id + 1;

        assert size() == id;
        GcActivitySet gcActivitySet = new GcActivitySet(gcActivityName);
        add(id, gcActivitySet);
        assert size() == id + 1;

        listeners.callGCActivityNameAdded(this, id, gcActivityName);
    }

    /**
     * 添加监听器
     */
    synchronized public void addListener(GcTraceListener listener) {
        ArgumentChecking.notNull(listener, "listener");

        listeners.add(listener);
    }

    /**
     * 移除监听器
     */
    synchronized public void removeListener(GcTraceListener listener) {
        ArgumentChecking.notNull(listener, "listener");

        listeners.remove(listener);
    }

    /**
     * 添加之前
     */
    public void afterAddingToGcTraceSet() {
    // do nothing, unless overwritten
    }

    /**
     * 移除之前
     * do nothing, unless overwritten
     * this has to return before the GC trace is removed from the GC trace set
     */
    public void beforeRemovingFromGcTraceSet() {
    }

    /**
     * It creates a new GC trace instance.
     */
    public GcTrace() {
    }
}
