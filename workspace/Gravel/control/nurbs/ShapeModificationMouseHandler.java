package control.nurbs;

import control.DragMouseHandler;
import model.NURBSShape;

import java.awt.geom.Point2D;
import java.util.Observer;

/**
 * mouse drag handling for all mouse modes implementing shape modification
 * these are the scond modi of HyperEdgeShape-Stuff where the basic shape is modified
 * <p>
 * Each implementing Class should provide at least one way to Modify shapes
 * Each implementing Class is also an Observer, because it should react on changes in GeneralPreferences
 * mainly the zoomfactor, but they also might want to watch other changes
 *
 * @author Ronny Bergmann
 */
public interface ShapeModificationMouseHandler extends DragMouseHandler, Observer {

  /**
   * Reset Shape to last situation in the HyperEdge given as reference
   */
  void resetShape();

  /**
   * Get Shape for drawing, if not null, else null is returned
   *
   * @return
   */
  NURBSShape getShape();

  /**
   * get the startpoint (without zoom) in the graph, if a drag is active
   * else null is returned
   *
   * @return
   */
  Point2D getDragStartPoint();

  /**
   * get the actual mouseposition (without zoom) in the graph, if a drag is active
   * else null is returned
   *
   * @return
   */
  Point2D getDragPoint();

}
