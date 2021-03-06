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

import gchisto2.jfreechart.extensions.ChangingCategoryDatasetWithTTG;
import gchisto2.jfreechart.extensions.ChartLocker;
import gchisto2.jfreechart.extensions.SwappingCategoryDatasetWithTTG;
import java.awt.BorderLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

/**
 * A panel that contains a chart that shows the breakdown of a particular 
 * metric over all GC activities. This panel should be used when more than
 * one GC traces have been loaded. The chart is implemented as a stacked bar
 * chart, one bar per GC trace. Each such panel will be added to the tabbed
 * pane of the main GC stats panel.
 *
 * @author Tony Printezis
 * @see BreakdownChartPanelSingle
 */
public class BreakdownChartPanelMulti extends GCStatsChartPanel {

    final ChangingCategoryDatasetWithTTG dataset;
    final ChartLocker locker;

    /**
     * It creates a chart for the given dataset and adds the chart to the panel.
     */
    private void addChart() {
        JFreeChart chart = ChartFactory.createStackedBarChart3D(
                getTitle(), null, "Breakdown" + unitSuffix(),
                dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        chart.addProgressListener(locker);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseToolTipGenerator(dataset);
        
        mainPanel().add(BorderLayout.CENTER, new ChartPanel(chart));
    }

    @Override
    public void refreshDataset() {
        updateDataset();
    }

    @Override
    public void updateDataset() {
        locker.doWhileLocked(new Runnable() {

            @Override
            public void run() {
                dataset.datasetChanged();
            }
        });
    }

    /**
     * It creates a new instance of this panel and adds a chart into it.
     * 
     * @param title The name of the metric name.
     * @param unitName The name of the unit of the metric.
     * @param dataset The dataset that will provide the values for the chart.
     */
    public BreakdownChartPanelMulti(
            String title, String unitName,
            ChangingCategoryDatasetWithTTG dataset,
            ChartLocker locker) {
        super(title, unitName);

        this.dataset = new SwappingCategoryDatasetWithTTG(dataset);
        this.locker = locker;

        addChart();
    }
}
