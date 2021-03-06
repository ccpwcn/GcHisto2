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
import gchisto2.gctracegenerator.NopGcTraceGeneratorListener;
import gchisto2.utils.MessageReporter;
import java.io.File;

/**
 *
 * @author tony
 */
public class DynamicFileGcTrace extends FileGcTrace {

    volatile private boolean playing = false;
    
    volatile private boolean shouldPause = false;
    volatile private boolean paused = false;
    
    volatile private boolean shouldFinish = false;
    
    private PlaybackFrame frame = new PlaybackFrame(this);
    
    abstract private class AbstractThrottle implements GcLogFileReaderThrottle {

        static final private int SLEEP_MS = 10;
        
        protected double startTimeSec;
        protected double prevStartSec;
        
        protected int totalCount = 0;
        
        private double nowSec() {
            return (double) System.currentTimeMillis() / 1000.0;
        }
        
        protected void updateStatus() {
            setStatus(String.format("%d events in %1.2f secs",
                    totalCount, prevStartSec - startTimeSec));
        }
        
        protected void startTimer() {
            startTimeSec = nowSec();
            prevStartSec = startTimeSec;
        }
        
        protected void waitUntil(double timeSec) {
            double nowSec = nowSec();
            while (nowSec < timeSec && !shouldFinish) {
                try {
                    Thread.sleep(SLEEP_MS);
                } catch (InterruptedException e) {
                }
                maybePause();
                nowSec = nowSec();
            }
            prevStartSec = nowSec;
        }
        
        protected void stopTimer() {
            double nowSec = nowSec();
            prevStartSec = nowSec;
            
            setStatus(String.format("Completed %d events in %1.2f secs",
                    totalCount,
                    prevStartSec - startTimeSec));
        }
        
        @Override
        public void started() {
            startTimer();
            updateStatus();
        }
        
        @Override
        public boolean shouldContinue() {
            return DynamicFileGcTrace.this.shouldContinue();
        }
        
        @Override
        public void finished() {
            stopTimer();
        }
        
    }
    
    private class RealFileReaderThrottle extends AbstractThrottle {
        
        private int speed;
        private double speedMult;
        
        @Override
        public void beforeAddingGcActivity(double startSec) {
            double timeSec = startTimeSec + startSec / speedMult;
            waitUntil(timeSec);
            maybePause();
        }
        
        @Override
        public void afterAddingGcActivity(double startSec) {
            ++totalCount;
            updateStatus();
        }
        
        public RealFileReaderThrottle(int speedup) {
            this.speed = speedup;
            this.speedMult = (double) speedup / 100.0;
        }
        
    }
    
    private class FastFileReaderThrottle extends AbstractThrottle {
        
        private int eventNum;
        private double durationSec;
        
        @Override
        public void beforeAddingGcActivity(double startSec) {
            if (totalCount > 0 && totalCount % eventNum == 0) {
                double timeSec = startTimeSec +
                        (double) (totalCount / eventNum) * durationSec;
                waitUntil(timeSec);
            }
            maybePause();
        }
        
        @Override
        public void afterAddingGcActivity(double startSec) {
            totalCount += 1;
            if (totalCount % eventNum == 0) {
                updateStatus();
            }
        }
        
        public FastFileReaderThrottle(int eventNum, double durationSec) {
            this.eventNum = eventNum;
            this.durationSec = durationSec;
        }
        
    }
    
    private class FinishListener extends NopGcTraceGeneratorListener {

        @Override
        public void finished(GcTrace gcTrace) {
            DynamicFileGcTrace.this.finished();
        }
        
    }
    
    boolean playing() {
        return playing;
    }
    
    boolean paused() {
        return paused;
    }
    
    private void maybePause() {
        assert playing;
        assert !paused;
        
        if (shouldPause) {
            paused = true;
            shouldPause = false;
            frame.setPaused();
            
            synchronized(this) {
                while (paused && !shouldFinish) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            assert playing;
            if (shouldFinish) {
                paused = false;
            } else {
                assert !paused;
                frame.setPlaying();
            }
        }
    }
    
    void playReal(int speedup) {
        play(new RealFileReaderThrottle(speedup));
    }
    
    void playFast(int eventNum, double durationSec) {
        play(new FastFileReaderThrottle(eventNum, durationSec));
    }
    
    private boolean shouldContinue() {
        return !shouldFinish;
    }
    
    void play(GcLogFileReaderThrottle throttle) {
        assert !playing;
        assert !shouldPause;
        assert !paused;
        assert !shouldFinish;
        
        playing = true;
        frame.setPlaying();
        readFileConcurrently(new FinishListener(), throttle);
    }

    void shouldPause() {
        assert playing;
        assert !shouldPause;
        assert !paused;
        assert !shouldFinish;
        
        shouldPause = true;
    }

    void unpause() {
        assert playing;
        assert !shouldPause;
        assert paused;
        assert !shouldFinish;
        
        paused = false;
        synchronized(this) {
            notifyAll();
        }
    }
    
    void shouldFinish() {
        assert playing;
        assert !shouldFinish;

        shouldFinish = true;
        synchronized(this) {
            notifyAll();
        }
    }

    void finished() {
        assert playing;
        assert !shouldPause;
        assert !paused;
        
        playing = false;
        shouldFinish = false;
        synchronized(this) {
            notifyAll();
        }
        frame.setStopped();
    }
    
    private void setStatus(String status) {
        frame.setStatus(status);
    }
    
    @Override
    public void init(GcTraceGeneratorListener listener) {
        listener.started();
        MessageReporter.showMessage("Added dynamic file " + file.getAbsolutePath());
        listener.finished(this);
    }

    @Override
    public void afterAddingToGcTraceSet() {
        frame.setVisible(true);
    }
    
    @Override
    public void beforeRemovingFromGcTraceSet() {
        if (playing) {
            shouldFinish();
            synchronized(this) {
                while (playing) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        frame.setVisible(false);
        frame.dispose();
    }

    public DynamicFileGcTrace(File file, GCLogFileReader reader) {
        super(file, reader);
    }

}
