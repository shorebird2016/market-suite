package org.marketsuite.component.graph;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class AbstractGraphPanel extends JPanel {
    public void toggleCursor() {
        _pnlChart.setHorizontalAxisTrace(!_pnlChart.getHorizontalAxisTrace());
        _pnlChart.setVerticalAxisTrace(!_pnlChart.getVerticalAxisTrace());
        _pnlChart.repaint();
    }

    //TODO save this code for later re-factoring
    private void showY() {//when mouse moved inside JFreeChart, show X and Y real values
        JFreeChart _Chart = null;
        XYPlot _PricePlot = null;
        ChartMouseEvent event = null;
        int mouseX = event.getTrigger().getX();
        int mouseY = event.getTrigger().getY();
        System.out.println("x = " + mouseX + ", y = " + mouseY);
        Point2D p = _pnlChart.translateScreenToJava2D(new Point(mouseX, mouseY));
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) _Chart.getPlot();
        PlotRenderingInfo pri = _pnlChart.getChartRenderingInfo().getPlotInfo();
        int subplotindex = pri.getSubplotIndex(p);
        PlotRenderingInfo subplotinfo = pri.getSubplotInfo(subplotindex);
        Rectangle2D plotArea = subplotinfo.getDataArea();
//                Rectangle2D plotArea = _pnlChart.getScreenDataArea();
//                ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = _PricePlot.getRangeAxis();
//                RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
        RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
//                double chartX = domainAxis.java2DToValue(p.getX(), plotArea, domainAxisEdge);
        double chartY = rangeAxis.java2DToValue(p.getY(), plotArea, rangeAxisEdge);
//System.out.println("Chart: x = " + chartX + " Xl: "+plot.getDomainAxis().getLowerBound()+" Xu: "+plot.getDomainAxis().getUpperBound());
        System.out.println("Chart: y = " + chartY + " Yl: "+rangeAxis.getLowerBound()+" Yu: "+rangeAxis.getUpperBound());
        System.out.println("Y cross hair: "+_PricePlot.getRangeCrosshairValue());
//                java.util.List ListSubplots = plot.getSubplots();
//                for (int i = 0; i < ListSubplots.size(); i++) {
//                    XYPlot subplot = (XYPlot) ListSubplots.get(i);
//                    subplot.setDomainCrosshairValue(chartX);
//                    subplot.setRangeCrosshairValue(chartY);
//System.out.println("i: "+i+" x: " + subplot.getDomainAxis().java2DToValue(p.getX(),plotArea, subplot.getDomainAxisEdge()) + " Xl: "+subplot.getDomainAxis().getLowerBound()+" Xu: "+subplot.getDomainAxis().getUpperBound());
//System.out.println("i: "+i+" y: " + rangeAxis.java2DToValue(p.getY(),plotArea, subplot.getRangeAxisEdge()) + " Yl: "+rangeAxis.getLowerBound()+" Yu: "+subplot.getRangeAxis().getUpperBound());
//System.out.println("X cross hair: "+subplot.getDomainCrosshairValue());
//System.out.println("Y cross hair: "+subplot.getRangeCrosshairValue());
//                }

    }

    //SAVED for detecting Y coordinates when mouse moved
    public void chartMouseMoved(ChartMouseEvent event) {
//                int mouseX = event.getTrigger().getX();
//                int mouseY = event.getTrigger().getY();
//                Point2D p = _pnlChart.translateScreenToJava2D(new Point(mouseX, mouseY));
//                CombinedDomainXYPlot plot = (CombinedDomainXYPlot) _Chart.getPlot();
//                PlotRenderingInfo pri = _pnlChart.getChartRenderingInfo().getPlotInfo();
//                int subplotindex = pri.getSubplotIndex(p);
//                if (subplotindex == -1)
//                    return;
//
//                PlotRenderingInfo subplotinfo = pri.getSubplotInfo(subplotindex);
//                Rectangle2D plotArea = subplotinfo.getDataArea();
//                ValueAxis rangeAxis = _PricePlot.getRangeAxis();
//                RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
//                double chartY = rangeAxis.java2DToValue(p.getY(), plotArea, rangeAxisEdge);
//                System.out.println("Chart: y = " + chartY + " Yl: "+rangeAxis.getLowerBound()+" Yu: "+rangeAxis.getUpperBound());
//                System.out.println("Y cross hair: "+_PricePlot.getRangeCrosshairValue());
    }
    protected ChartPanel _pnlChart;
}
