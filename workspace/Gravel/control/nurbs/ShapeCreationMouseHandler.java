package control.nurbs;

import control.DragMouseHandler;
import model.Messages.NURBSCreationMessage;
import model.NURBSShape;

/**
 * mouse drag handling for all mouse modes implementing shape creation
 * these are the first modi of HyperEdgeShape-Stuff where the basic shape is created
 * <p>
 * Each implementing Class should provide one way to create shapes
 *
 * @author Ronny Bergmann
 */
public interface ShapeCreationMouseHandler extends DragMouseHandler {
  /**
   * Set Shape to an empty/null object
   */
  void resetShape();

  /**
   * Get Parameters for NURBSShapeFactory of the actually created Shape
   *
   * @return
   */
  NURBSCreationMessage getShapeParameters();

  /**
   * Set the Shape externally with NURBSShapeFactory-Parameters
   * This method also updates the internal shape, which is provided by @see getShape();
   *
   * @param nm
   */
  void setShapeParameters(NURBSCreationMessage nm);

  /**
   * Get Shape for drawing, if not null, else null is returned
   *
   * @return
   */
  NURBSShape getShape();
}
