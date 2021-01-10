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
package gchisto2.gui.panels.gcstats;

import gchisto2.gcactivity.GcActivity;
import gchisto2.gcactivity.GcActivitySet;
import gchisto2.gctrace.GcTrace;
import gchisto2.gctrace.GcTraceListener;
import gchisto2.gctrace.GcTraceSet;
import gchisto2.gctrace.GcTraceSetListener;
import gchisto2.gui.panels.VisualizationPanel;
import gchisto2.jfreechart.extensions.ChangingCategoryDatasetWithTTG;
import gchisto2.jfreechart.extensions.ChangingPieDatasetWithTTG;
import gchisto2.jfreechart.extensions.ChartLocker;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/**
 * This is the main GC Stats panel that contains summary GC statistics for the
 * loaded GC traces, in both tables and bar charts.
 *
 * @author Tony Printezis
 */
public class Panel extends javax.swing.JPanel
        implements VisualizationPanel, GcTraceSetListener, GcTraceListener {

    /**
     * The prefix string that will be added to the title of all the tabbed panes,
     * if they contain a chart.
     */
    static final private String CHART_PREFIX = "Chart: ";
    /**
     * The GC trace set that will provide the data for this panel.
     */
    private GcTraceSet gcTraceSet;
    final private List<GCStatsChartPanel> panelsSingle =
            new LinkedList<>();
    final private List<GCStatsChartPanel> panelsMulti =
            new LinkedList<>();
    final private List<GCStatsChartPanel> panelsAll =
            new LinkedList<>();
    private DatasetGenerator datasetGenerator;
    final private ChartLocker locker = new ChartLocker();

    private void createBreakdownChartPanelSingle(
            int metric,
            DatasetGenerator datasetGenerator,
            List<GCStatsChartPanel> list) {
        ChangingPieDatasetWithTTG dataset =
                datasetGenerator.newPieDatsetWithTTG(metric);
        GCStatsChartPanel panel = new BreakdownChartPanelSingle(
                CHART_PREFIX + DatasetGenerator.getMetricName(metric),
                DatasetGenerator.getUnitName(metric),
                dataset, locker);
        list.add(panel);
    }

    private void createBreakdownChartPanelMulti(
            int metric,
            DatasetGenerator datasetGenerator,
            List<GCStatsChartPanel> list) {
        ChangingCategoryDatasetWithTTG dataset =
                datasetGenerator.newCategoryDatasetWithTTG(metric, true);
        GCStatsChartPanel panel = new BreakdownChartPanelMulti(
                CHART_PREFIX + DatasetGenerator.getMetricName(metric),
                DatasetGenerator.getUnitName(metric),
                dataset, locker);
        list.add(panel);
    }

    /**
     * It adds a new metric chart panel to the tabbed pane set.
     *
     * @param metric The metric that will be shown in the chart.
     * @param datasetGenerator The generator that will produce the datasetGenerator
     * for this chart.
     * @param list It determines whether to ignore the aggregate
     * GC activity or not.
     */
    private void createMetricChartPanel(
            int metric,
            DatasetGenerator datasetGenerator,
            List<GCStatsChartPanel> list) {
        ChangingCategoryDatasetWithTTG dataset =
                datasetGenerator.newCategoryDatasetWithTTG(metric, false);
        GCStatsChartPanel panel =
                new MetricChartPanel(
                CHART_PREFIX + DatasetGenerator.getMetricName(metric),
                DatasetGenerator.getUnitName(metric),
                dataset, locker);
        list.add(panel);
    }

    @Override
    synchronized public void gcTraceAdded(GcTrace gcTrace) {
        gcTrace.addListener(this);

        update();
        int gcTraceNum = datasetGenerator.getGCTraceNum();
        if (gcTraceNum == 1) {
            // the main tabbed pane should be empty
            for (GCStatsChartPanel panel : panelsSingle) {
                tabbedPane.addTab(panel.getTitle(), panel);
            }
            for (GCStatsChartPanel panel : panelsAll) {
                tabbedPane.addTab(panel.getTitle(), panel);
            }
        // now the main tabbed pane should contain panelsSingle and panelsAll
        } else if (gcTraceNum == 2) {
            // the main tabbed pane should contain panelsSingle and panelsAll
            for (GCStatsChartPanel panel : panelsSingle) {
                int index = tabbedPane.indexOfComponent(panel);
                tabbedPane.removeTabAt(index);
            }
            int i = 0;
            for (GCStatsChartPanel panel : panelsMulti) {
                tabbedPane.add(panel, panel.getTitle(), i);
                i += 1;
            }
            tabbedPane.setSelectedIndex(0);
        // now the main tabbed pane should contain panelsMulti and panelsAll
        } else {
            assert gcTraceNum > 2;
        // the main tabbed pane should contain panelsMulti and panelsAll
        }
    }

    @Override
    synchronized public void gcTraceRenamed(GcTrace gcTrace) {
        update();
    }

    @Override
    synchronized public void gcTraceRemoved(GcTrace gcTrace) {
        update();
        int gcTraceNum = datasetGenerator.getGCTraceNum();
        if (gcTraceNum == 0) {
            // the main tabbed pane should contain panelsSingle and panelsAll
            tabbedPane.removeAll();
        // the main tabbed pane should be empty
        } else if (gcTraceNum == 1) {
            // now the main tabbed pane should contain panelsMultiand panelsAll
            for (GCStatsChartPanel panel : panelsMulti) {
                int index = tabbedPane.indexOfComponent(panel);
                tabbedPane.removeTabAt(index);
            }
            int i = 0;
            for (GCStatsChartPanel panel : panelsSingle) {
                tabbedPane.add(panel, panel.getTitle(), i);
                i += 1;
            }
            tabbedPane.setSelectedIndex(0);
        // now the main tabbed pane should contain panelsSingle and panelsAll
        } else {
            assert gcTraceNum > 1;
        // the main tabbed pane should contain panelsMulti and panelsAll
        }
    }

    @Override
    synchronized public void gcTraceMovedUp(GcTrace gcTrace) {
        update();
    }

    @Override
    synchronized public void gcTraceMovedDown(GcTrace gcTrace) {
        update();
    }

    @Override
    public void gcActivityAdded(
            GcTrace gcTrace,
            GcActivitySet gcActivitySet,
            GcActivity gcActivity) {
        refresh();
    }

    @Override
    public void gcActivityNameAdded(
            GcTrace gcTrace,
            int id,
            String gcActivityName) {
        update();
    }

    private void refresh() {
        locker.doWhileLocked(() -> {
            int gcTraceNum = gcTraceSet.size();
            assert gcTraceNum > 0;
            if (gcTraceNum == 1) {
                for (GCStatsChartPanel panel : panelsSingle) {
                    panel.refresh();
                }
            } else {
                for (GCStatsChartPanel panel : panelsMulti) {
                    panel.refresh();
                }
            }
            for (GCStatsChartPanel panel : panelsAll) {
                panel.refresh();
            }
        });
    }

    private void update() {
        locker.doWhileLocked(() -> {
            datasetGenerator.update();

            int gcTraceNum = gcTraceSet.size();
            if (gcTraceNum == 1) {
                for (GCStatsChartPanel panel : panelsSingle) {
                    panel.update();
                }
            } else {
                for (GCStatsChartPanel panel : panelsMulti) {
                    panel.update();
                }
            }
            for (GCStatsChartPanel panel : panelsAll) {
                panel.update();
            }
        });
    }

    private void create() {
        panelsSingle.add(new AllStatsTablePanelSingle(datasetGenerator, locker));
        panelsMulti.add(new AllStatsTablePanelMulti(datasetGenerator, locker));

        createBreakdownChartPanelSingle(DatasetGenerator.METRIC_NUM, datasetGenerator, panelsSingle);
        createBreakdownChartPanelSingle(DatasetGenerator.METRIC_TOTAL, datasetGenerator, panelsSingle);

        createBreakdownChartPanelMulti(DatasetGenerator.METRIC_NUM, datasetGenerator, panelsMulti);
        createBreakdownChartPanelMulti(DatasetGenerator.METRIC_NUM_PERC, datasetGenerator, panelsMulti);
        createBreakdownChartPanelMulti(DatasetGenerator.METRIC_TOTAL, datasetGenerator, panelsMulti);
        createBreakdownChartPanelMulti(DatasetGenerator.METRIC_TOTAL_PERC, datasetGenerator, panelsMulti);
        createBreakdownChartPanelMulti(DatasetGenerator.METRIC_OVERHEAD_PERC, datasetGenerator, panelsMulti);

        createMetricChartPanel(DatasetGenerator.METRIC_AVG, datasetGenerator, panelsAll);
        createMetricChartPanel(DatasetGenerator.METRIC_SIGMA, datasetGenerator, panelsAll);
        createMetricChartPanel(DatasetGenerator.METRIC_MIN, datasetGenerator, panelsAll);
        createMetricChartPanel(DatasetGenerator.METRIC_MAX, datasetGenerator, panelsAll);
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public String getPanelName() {
        return "GC Pause Stats";
    }

    @Override
    public GcTraceSetListener getListener() {
        return this;
    }

    @Override
    public void setGcTraceSet(GcTraceSet gcTraceSet) {
        this.gcTraceSet = gcTraceSet;
        this.datasetGenerator = new DatasetGenerator(gcTraceSet);
        create();
    }

    /**
     * It creates a new instance of the GC stats panel.
     */
    public Panel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        tabbedPane = new javax.swing.JTabbedPane();

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
