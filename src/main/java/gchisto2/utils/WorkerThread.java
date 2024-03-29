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
package gchisto2.utils;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author tony
 */
public class WorkerThread extends Thread {
    
    private final Queue<WorkerTask> queue = new LinkedList<>();

    private static final WorkerThread instance;
    
    static {
        instance = new WorkerThread();
        instance.start();
    }
    
    static public WorkerThread instance() {
        return instance;
    }
    
    @Override
    public void run() {
        while (true) {
            WorkerTask task;
            synchronized (this) {
                task = queue.poll();
                while (task == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                    task = queue.poll();
                }
            }

            task.doIt();
        }
    }
    
    synchronized public void add(WorkerTask task) {
        queue.offer(task);
        notifyAll();
    }
    
    private WorkerThread() {
    }
    
}
