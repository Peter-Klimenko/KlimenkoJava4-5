package bsu.rfe.java.group10.lab4.Klimenko.varB4;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showHorizontalLines = true;

    private double minX, maxX, minY, maxY, scaleX, scaleY;
    private boolean isDragging = false;
    private Point dragStart = null;
    private Rectangle dragRect = null;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetScale(); // Восстановление исходного масштаба
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true; // Начало выделения
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    if (dragRect != null && dragRect.width > 0 && dragRect.height > 0) {
                        scaleToArea(dragRect); // Масштабирование выделенной области
                    }
                    isDragging = false;
                    dragRect = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                showPointCoordinates(e); // Отображение координат точки при наведении
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    dragRect = new Rectangle(dragStart);
                    dragRect.add(e.getPoint()); // Рисование рамки выделения
                    repaint();
                }
            }
        });
    }

    private void showPointCoordinates(MouseEvent e) {
        if (graphicsData == null) return;

        Point mousePoint = e.getPoint();
        for (Double[] point : graphicsData) {
            Point2D.Double graphPoint = xyToPoint(point[0], point[1]);
            if (Math.abs(graphPoint.x - mousePoint.x) < 5 && Math.abs(graphPoint.y - mousePoint.y) < 5) {
                Graphics2D g2d = (Graphics2D) getGraphics();
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("(%.2f, %.2f)", point[0], point[1]),
                        (int) graphPoint.x + 5, (int) graphPoint.y - 5);
                break;
            }
        }
    }

    private void scaleToArea(Rectangle rect) {
        double newMinX = minX + (rect.x / scaleX);
        double newMaxX = minX + ((rect.x + rect.width) / scaleX);
        double newMinY = maxY - ((rect.y + rect.height) / scaleY);
        double newMaxY = maxY - (rect.y / scaleY);

        minX = newMinX;
        maxX = newMaxX;
        minY = newMinY;
        maxY = newMaxY;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);

        repaint();
    }

    private void resetScale() {
        calculateBounds(); // Восстановление границ
        repaint();
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        calculateBounds();
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowHorizontalLines(boolean showHorizontalLines) {
        this.showHorizontalLines = showHorizontalLines;
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        Graphics2D canvas = (Graphics2D) g;
        if (showAxis) paintAxis(canvas);
        if (showHorizontalLines) paintHorizontalLines(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        if (dragRect != null) { // Рисование рамки выделения
            canvas.setColor(Color.BLACK);
            canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0));
            canvas.draw(dragRect);
        }
    }

    private void calculateBounds() {
        minX = graphicsData[0][0];
        maxX = graphicsData[0][0];
        minY = graphicsData[0][1];
        maxY = graphicsData[0][1];

        for (Double[] point : graphicsData) {
            if (point[0] < minX) minX = point[0];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] < minY) minY = point[1];
            if (point[1] > maxY) maxY = point[1];
        }

        maxX += maxX * 0.25;
        minX -= maxX * 0.25;
        maxY += maxX * 0.2;
        minY -= maxX * 0.1;

        scaleX = getWidth() / (maxX - minX);
        scaleY = getHeight() / (maxY - minY);
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2.0f));
        canvas.setColor(Color.BLACK);

        Point2D.Double xStart = xyToPoint(minX, 0);
        Point2D.Double xEnd = xyToPoint(maxX, 0);
        canvas.draw(new Line2D.Double(xStart, xEnd));

        Point2D.Double yStart = xyToPoint(0, minY);
        Point2D.Double yEnd = xyToPoint(0, maxY);
        canvas.draw(new Line2D.Double(yStart, yEnd));
    }

    protected void paintHorizontalLines(Graphics2D canvas) {
        canvas.setColor(Color.RED);
        float[] dashPattern = {4, 8};
        canvas.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));

        double[] fractions = {0.9, 0.5, 0.1};
        for (double fraction : fractions) {
            double y = minY + fraction * (maxY - minY);
            Point2D.Double start = xyToPoint(minX, y);
            Point2D.Double end = xyToPoint(maxX, y);
            canvas.draw(new Line2D.Double(start, end));
        }
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2.0f));
        canvas.setColor(Color.GREEN);

        GeneralPath graph = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i == 0) graph.moveTo(point.x, point.y);
            else graph.lineTo(point.x, point.y);
        }
        canvas.draw(graph);
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(1.0f));
        for (Double[] point : graphicsData) {
            Point2D.Double center = xyToPoint(point[0], point[1]);
            canvas.setColor(point[1] > (maxY + minY) / 2 ? Color.BLUE : Color.RED);
            GeneralPath triangle = new GeneralPath();
            triangle.moveTo(center.x+2, center.y+2 ); 
            triangle.lineTo(center.x+2 , center.y - 2); 
            triangle.lineTo(center.x -2, center.y - 2);
            triangle.lineTo(center.x -2, center.y +2);
            triangle.closePath();
            canvas.fill(triangle);
        }
}
}
