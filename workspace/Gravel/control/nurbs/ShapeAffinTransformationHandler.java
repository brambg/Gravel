package control.nurbs;

import io.GeneralPreferences;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;
import model.NURBSShape;
import model.NURBSShapeFragment;
import model.VHyperEdge;
import model.VHyperGraph;
import view.VCommonGraphic;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Observable;

/**
 * Class for handling Drags for Modification of the given HyperEdge in the VHyperGraph
 * <p>
 * The Modification Modes are
 * <p>
 * - Rotation: The beginning Position of a Drag is used as rotation Center and the angle
 * is computed by Difference to the Zero-Degree-Line, that is from the Center to the left
 * <p>
 * - Translation of the whole Shape
 * Where the Movement vector of the Drag is applied as movement to the Shape
 * <p>
 * - Scaling
 * The distance from the Drag Start Point in Relation to the size of the Shape is used
 * for calculation of the scaling factor
 * <p>
 * - Scaling with direction
 * <p>
 * <p>
 * Perhaps a second scaling would be nice where X and Y are treaded seperately to
 * change not only size but aspect ratio of the shape
 *
 * @author Ronny Bergmann
 */
public class ShapeAffinTransformationHandler implements ShapeModificationMouseHandler {

  private VHyperGraph vhg = null;
  private GeneralPreferences gp;
  private Point MouseOffSet = new Point(0, 0);
  private Point2D.Double DragOrigin;
  private boolean firstdrag = true;
  private NURBSShape temporaryShape = null, DragBeginShape = null;
  VHyperEdge HyperEdgeRef;
  float zoom;
  private int ModificationState = VCommonGraphic.NO_DETAIL;

  /**
   * The ShapeModificationDragListener
   * Handles Drags in an HyperGraphic-Environment and modifies a specific edge
   *
   * @param modstate       Init the Modus of Shape Transformation from the Values in VCommonGraphics
   * @param g              a HyperGraph the Hyper edge (and its shape belong to
   * @param hyperedgeindex the specific edge to be modified
   */
  public ShapeAffinTransformationHandler(int modstate, VHyperGraph g, int hyperedgeindex) {
    vhg = g;
    gp = GeneralPreferences.getInstance();
    gp.addObserver(this);
    zoom = gp.getFloatValue("zoom");
    if (vhg.modifyHyperEdges.get(hyperedgeindex) == null)
      return; //Nothing can be done here.
    HyperEdgeRef = vhg.modifyHyperEdges.get(hyperedgeindex);
    temporaryShape = HyperEdgeRef.getShape().clone();
    setModificationState(modstate);
  }

  public void removeGraphObservers() {
    gp.deleteObserver(this);
  }

  public Rectangle getSelectionRectangle() { //No Selections possible here
    return null;
  }

  /**
   * Reset shape to last state that was really saved in the graph - doeas not push any notification
   */
  public void resetShape() {
    HyperEdgeRef = vhg.modifyHyperEdges.get(HyperEdgeRef.getIndex());
    temporaryShape = HyperEdgeRef.getShape().clone(); //Clone with eventual Decorations (if that decoration clones)
  }

  public NURBSShape getShape() {
    return temporaryShape;
  }

  public Point2D getDragStartPoint() {
    if (!dragged())
      return null;
    return DragOrigin;
  }

  public Point2D getDragPoint() {
    if (!dragged())
      return null;
    return new Point2D.Double(MouseOffSet.getX() / ((double) zoom), MouseOffSet.getY() / ((double) zoom));
  }

  public boolean dragged() {
    return (DragOrigin != null) && (!firstdrag);
  }

  /**
   * Set the ShapeModificationHandler to a new Modus
   * See Final Values of ShapeModificaitonDragListener for Details
   *
   * @param newstate
   */
  public void setModificationState(int newstate) {
    ModificationState = newstate & VCommonGraphic.DETAIL_MASK;
  }

  public int getModification() {
    return ModificationState;
  }

  private void internalReset() {
    //Only if a Block was started: End it... with notification
    if (dragged()) {
      DragOrigin = null;
      //Set shape
      HyperEdgeRef.setShape(temporaryShape);
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, GraphConstraints.BLOCK_END));
    }
    DragOrigin = null;
    resetShape();
  }

  private double getDegreefromDirection(Point2D dir) {
    //Compute Degree
    double x = dir.getX(), y = dir.getY();
    double length = dir.distance(0d, 0d);
    if (x == 0d) //90 or 270 Degree
    {
      if (y < 0d) //Up
        return 90d;
      else if (y > 0d) //Down
        return 270d;
      else
        return 0d;
    }
    if (y == 0d) //0 or 180 Degree
    {
      if (x < 0d) //Left
        return 180d;
      else //right
        return 0d;
    }
    //Now both are nonzero,
    if (x > 0d)
      if (y < 0d) //  1. Quadrant
        return Math.asin(Math.abs(y) / length) * 180.d / Math.PI; //In Degree
      else //y>0  , 4. Quadrant
        return Math.acos(Math.abs(y) / length) * 180.d / Math.PI + 270d; //In Degree
    else //x<0 left side
      if (y < 0d) //2. Quadrant
        return 180.0d - Math.asin(Math.abs(y) / length) * 180.d / Math.PI; //In Degree
      else //y>0, 3. Quadrant
        return 270.0d - Math.acos(Math.abs(y) / length) * 180.d / Math.PI; //In Degree
  }

  //One every Click a potental Drag is initialized but firstdrag = true signals, that no Drag-Movement happened yet
  public void mousePressed(MouseEvent e) {
    firstdrag = true;
    boolean alt = ((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK); // alt ?
    boolean shift = ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) == InputEvent.SHIFT_DOWN_MASK); //shift ?
    if (alt || shift)
      return;
    MouseOffSet = e.getPoint(); //Actual Mouseposition
    //DragOrigin is the MousePosition in the graph, e.g. without zoom
    DragOrigin = new Point2D.Double((double) e.getPoint().x / ((double) zoom), (double) e.getPoint().y / ((double) zoom));
    DragBeginShape = temporaryShape.clone();
    //if (!temporaryShape.isPointOnCurve(DragOrigin, 2.0d)) //Are we near the Curve?
  }

  public void mouseDragged(MouseEvent e) {
    if (((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK) || ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) == InputEvent.SHIFT_DOWN_MASK)) {  //When someone presses alt or shift while drag...end it.
      internalReset();
      return;
    }

    //If the click was initiating the drag correctly,
    if (DragOrigin != null) {
      //Point in Graph (exact, double values) of the actual Mouse-Position
      Point2D exactPointInGraph = new Point2D.Double((double) e.getPoint().x / ((double) zoom), (double) e.getPoint().y / ((double) zoom));
      Point2D DragMov = new Point2D.Double(exactPointInGraph.getX() - DragOrigin.getX(), exactPointInGraph.getY() - DragOrigin.getY());
      //Handle this Movement-Vector depending on the specific state we're in
      temporaryShape = DragBeginShape.clone();
      switch (ModificationState) {
        case VCommonGraphic.ROTATE:
          temporaryShape.translate(-DragOrigin.getX(), -DragOrigin.getY()); //Origin
          temporaryShape.rotate(getDegreefromDirection(DragMov)); //Rotate
          temporaryShape.translate(DragOrigin.getX(), DragOrigin.getY()); //Back
          break;
        case VCommonGraphic.TRANSLATE: {

          temporaryShape.translate(DragMov.getX(), DragMov.getY()); //Origin
        }
        break;
        case VCommonGraphic.SCALE:
          //Factor is depending on Distance
          double dist = DragMov.distance(0d, 0d);
          //And increases size if getX() > 0 else decreases and depends on scale of shape so it does not
          //involve massive scaling by small mouse movement
          Point2D min = DragBeginShape.getMin();
          Point2D max = DragBeginShape.getMax();
          //Each side by half
          double origsizefactor = (max.getX() - min.getX() + max.getY() - min.getY()) / 4.0d;
          double factor = (dist) / origsizefactor;
          //	factor *= factor; //Increase scaling further away from 1.0
          temporaryShape.translate(-DragOrigin.getX(), -DragOrigin.getY()); //Origin
          temporaryShape.scale(factor);
          temporaryShape.translate(DragOrigin.getX(), DragOrigin.getY()); //Back
          break;
        case VCommonGraphic.SCALE_DIR:
          //Factor is depending on Distance
          double onedist = DragMov.distance(0d, 0d);
          temporaryShape.translate(-DragOrigin.getX(), -DragOrigin.getY()); //Translate to Origin
          temporaryShape.rotate(-getDegreefromDirection(DragMov)); //Rotate
          double minDir = DragBeginShape.getMin().getX();
          double maxDir = DragBeginShape.getMax().getX();
          double origDirfactor = (maxDir - minDir) / 2;
          double DirScale = (onedist) / origDirfactor;
          //	DirScale *=DirScale; //Increase change with distance from 1.0
          temporaryShape.scale(DirScale, 1d);
          temporaryShape.rotate(getDegreefromDirection(DragMov)); //Rotate back
          temporaryShape.translate(DragOrigin.getX(), DragOrigin.getY()); //Back
          break;
        default:
          break; //If there is no state, e.g. NO_MODIFICATION, do nothing
      }
      //Finally - notify Graph Observers to redraw, and on first modification start that as a block
      if (firstdrag) //If wirst drag - start Block
        vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(), GraphConstraints.BLOCK_START | GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE, GraphConstraints.HYPEREDGE));
      else    //continnue Block
        vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(), GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE, GraphConstraints.HYPEREDGE));
    }
    MouseOffSet = e.getPoint();
    firstdrag = false;
  }

  public void mouseReleased(MouseEvent e) {
    if (!firstdrag) //If in Drag - handle last moevemnt
    {
      if (!((e.getPoint().x == -1) || (e.getPoint().y == -1))) //If there was no external reset
        mouseDragged(e);
    }
    if ((temporaryShape.getDecorationTypes() & NURBSShape.FRAGMENT) == NURBSShape.FRAGMENT)
      ((NURBSShapeFragment) temporaryShape).refreshDecoration();
    internalReset();
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public Point getMouseOffSet() {
    return MouseOffSet;
  }

  //Ignore Grid
  public void setGrid(int x, int y) {
  }

  public void setGridOrientated(boolean b) {
  }

  public void update(Observable o, Object arg) {
    if ((o == gp) && (arg == "zoom"))
      zoom = gp.getFloatValue("zoom");
  }
}