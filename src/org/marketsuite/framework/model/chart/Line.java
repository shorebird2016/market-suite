package org.marketsuite.framework.model.chart;

import java.awt.*;

//objects used in chart to draw lines
public class Line {
    public Line(Point begin_pnt) { this.beginPoint = begin_pnt; }

    //----- public methods -----
    public boolean isEmpty() { return beginPoint == null || endPoint == null; }
    public void clear() { beginPoint = endPoint = null; }
    public void draw(Graphics2D g2d) {
        g2d.setPaint(paint); g2d.setStroke(stroke);
        g2d.drawLine(beginPoint.x, beginPoint.y, endPoint.x, endPoint.y);
    }

    //----- accessors -----
    public void setBeginPoint(Point beginPoint) { this.beginPoint = beginPoint; }
    public void setEndPoint(Point endPoint) { this.endPoint = endPoint; }
    public void setStroke(Stroke stroke) { this.stroke = stroke; }
    public void setPaint(Paint paint) { this.paint = paint; }

    //----- variables -----
    private Point beginPoint, endPoint;//null = empty line
    private Stroke stroke = new BasicStroke(4);
    private Paint paint = new Color(213, 140, 82, 155);
}
