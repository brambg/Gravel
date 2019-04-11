package model;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import java.util.Vector;

/**
 * The segmented Edge consists of a vector of points and along there points the edge is plotted
 *
 * @author Ronny Bergmann
 */
public class VSegmentedEdge extends VEdge {
  Vector<Point> points;

  /**
   * Init the VSegmented Edge with an
   *
   * @param i index,
   * @param w a linewidth and
   * @param p a vector of controlpoints
   */
  public VSegmentedEdge(int i, int w, Vector<Point> p) {
    super(i, w);
    points = p;
  }

  public GeneralPath getPath(Point Start, Point End, float zoom) {
    GeneralPath p = new GeneralPath();
    p.moveTo(Start.x * zoom, Start.y * zoom);
    for (Point point : points) {
      p.lineTo(point.x * zoom, point.y * zoom);
    }
    p.lineTo(End.x * zoom, End.y * zoom);
    return p;
  }

  public Vector<Point> getControlPoints() {
    return points;
  }

  public void setControlPoints(Vector<Point> p) {
    if (p.size() > 0)
      points = p;
  }

  public int getEdgeType() {
    return VEdge.SEGMENTED;
  }

  public void translate(int x, int y) {
    for (Point point : points) {
      point.translate(x, y);
      if (point.x < 0)
        point.x = 0;
      if (point.y < 0)
        point.y = 0;

    }
  }

  public Point getMax() {
    Point max = new Point(0, 0);
    for (Point point : points) {
      if (point.x > max.x)
        max.x = point.x;
      if (point.y > max.y)
        max.y = point.y;
    }
    return max;
  }

  public Point getMin() {
    Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    for (Point point : points) {
      if (point.x < min.x)
        min.x = point.x;
      if (point.y < min.y)
        min.y = point.y;
    }
    return min;
  }

  @SuppressWarnings("unchecked")
  public VEdge clone() {
    Vector<Point> p = (Vector<Point>) points.clone();
    VEdge cloneedge = new VSegmentedEdge(getIndex(), width, p);
    return copyCommonProperties(cloneedge);
  }

  public boolean PathEquals(VEdge v) {
    if (v.getEdgeType() != VEdge.SEGMENTED)
      return false;
    Vector<Point> vPoints = v.getControlPoints();
    if (points.size() != vPoints.size())
      return false;
    //So they share the same number of controlpoints
    Iterator<Point> cpiter = points.iterator();
    Iterator<Point> vcpiter = vPoints.iterator();
    while (cpiter.hasNext()) {
      Point p = cpiter.next();
      Point vp = vcpiter.next();
      if ((p.x != vp.x) || (p.y != vp.y)) //they don't share the same path
        return false;
    }
    return true;
  }
}
