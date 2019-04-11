package control.nurbs;

import io.GeneralPreferences;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;
import model.*;
import view.VCommonGraphic;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Observable;

/**
 * This Handler implements all actions for handling the selection of a subcurve
 * <p>
 * The actions are - Clicking on the curve once keeps that value as a possivle
 * startvalue A second click on the curve is interpreted as an endvalue and the
 * subcurve exists
 * <p>
 * - Drag begun near start/end of existing subcurve puts that element to the
 * mouse cursor In every movement the Projection onto the curve is computed and
 * set as the value where the drag started (begin or endvalue)
 * <p>
 * - Double click on the curve at any point inverts any existing subcurve
 *
 * @author ronny
 */
public class ShapeSubcurveSelectionHandler implements
    ShapeModificationMouseHandler {

  private VHyperGraph vhg = null;
  private float zoom;
  private GeneralPreferences gp;
  private Point MouseOffSet = new Point(0, 0);
  private Point2D.Double DragOrigin;
  private boolean firstdrag = true;
  private NURBSShape temporaryShape = null;
  double tempStart = Double.NaN, tempEnd = Double.NaN;
  //Only used for double click, because double click is no fun in java
  double lastStart, lastEnd;
  int actualState = 0;
  //For single clicks which value to handle next
  boolean setStartNext = true, DragsetsStart, toggleOnClick = true;
  private VHyperEdge HyperEdgeRef;

  /**
   * The ShapeSubcurveSelectionHanlder Handlers MouseActions for the selection
   * of a subcurve
   *
   * @param modstate       specifies the type of modification, specified in VCommonGraphic
   * @param g              the Hypergraph containing the Hyperedge and its shape
   * @param hyperedgeindex the specific edge to be modified
   */
  public ShapeSubcurveSelectionHandler(int modstate, VHyperGraph g, int hyperedgeindex) {
    vhg = g;
    gp = GeneralPreferences.getInstance();
    gp.addObserver(this);
    zoom = gp.getFloatValue("zoom");
    if (vhg.modifyHyperEdges.get(hyperedgeindex) == null)
      return; // Nothing can be done here.
    HyperEdgeRef = vhg.modifyHyperEdges.get(hyperedgeindex);
    temporaryShape = HyperEdgeRef.getShape().clone();
    if ((temporaryShape.getDecorationTypes() & NURBSShape.FRAGMENT) == NURBSShape.FRAGMENT) {
      NURBSShapeFragment t = (NURBSShapeFragment) temporaryShape;
      tempStart = t.getStart();
      tempEnd = t.getEnd();
    } else {
      tempStart = Double.NaN;
      tempEnd = Double.NaN;
    }
    setModificationState(modstate);
  }

  public void removeGraphObservers() {
    gp.deleteObserver(this);
  }

  /**
   * Reset shape to last state that was really saved in the graph - doeas not
   * push any notification
   */
  public void resetShape() {
    //	if ((!Double.isNaN(tempStart)) && (!Double.isNaN(tempEnd)) && (!temporaryShape.isEmpty()))
    //	{
    HyperEdgeRef = vhg.modifyHyperEdges.get(HyperEdgeRef.getIndex());
    temporaryShape = HyperEdgeRef.getShape().clone(); // Reset to actual Edge Shape as NURBSShape
    NURBSShapeFragment t = (NURBSShapeFragment) temporaryShape;
    tempStart = t.getStart();
    tempEnd = t.getEnd();
    //	}
  }

  public NURBSShape getShape() {
    return new NURBSShapeFragment(temporaryShape.stripDecorations(), tempStart, tempEnd); //maybe subcurve is empty...that does not matter
  }

  public Point2D getDragStartPoint() {
    if (!dragged())
      return null;
    return DragOrigin;
  }

  public Point2D getDragPoint() {
    if (!dragged())
      return null;
    return new Point2D.Double(MouseOffSet.getX() / ((double) zoom),
        MouseOffSet.getY() / ((double) zoom));
  }

  public boolean dragged() {
    return (DragOrigin != null) && (!firstdrag);
  }

  private void internalReset() {
    // Only if a Block was started: End it... with notification
    if (dragged()) {
      DragOrigin = null;
      HyperEdgeRef.setShape(getShape()); //Set shape to NURBSShapeFragment
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, GraphConstraints.BLOCK_END));
      resetShape();
    }
    DragOrigin = null;
  }

  public void setModificationState(int i) {
    //Get actual Edge from graph
    if ((i & VCommonGraphic.SET_START) == VCommonGraphic.SET_START) {
      setStartNext = true;
      toggleOnClick = false;
    } else if ((i & VCommonGraphic.SET_END) == VCommonGraphic.SET_END) {
      setStartNext = false;
      toggleOnClick = false;
    } else if ((i & VCommonGraphic.TOGGLE) == VCommonGraphic.TOGGLE) {
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(), GraphConstraints.BLOCK_START | GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE, GraphConstraints.HYPEREDGE));
      double toggle = tempStart;
      tempStart = tempEnd;
      tempEnd = toggle;
      if ((!Double.isNaN(tempStart)) && (!Double.isNaN(tempEnd)) && ((temporaryShape.getDecorationTypes() & NURBSShape.FRAGMENT) == NURBSShape.FRAGMENT)) {
        ((NURBSShapeFragment) temporaryShape).setStart(tempStart);
        ((NURBSShapeFragment) temporaryShape).setEnd(tempEnd);
        ((NURBSShapeFragment) temporaryShape).refreshDecoration();
        HyperEdgeRef.setShape(getShape()); //Set shape to NURBSShapeFragment
      }
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(), GraphConstraints.BLOCK_END, GraphConstraints.HYPEREDGE));
    } else //Default
    {
      setStartNext = true;
      toggleOnClick = true;
    }
  }

  public void mousePressed(MouseEvent e) {
    firstdrag = true;
    boolean alt = ((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK); // alt
    // ?
    boolean shift = ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) == InputEvent.SHIFT_DOWN_MASK); // shift
    // ?
    if (alt || shift)
      return;
    MouseOffSet = e.getPoint(); // Actual Mouseposition
    // DragOrigin is the MousePosition in the graph, e.g. without zoom
    DragOrigin = new Point2D.Double((double) e.getPoint().x
        / ((double) zoom), (double) e.getPoint().y
        / ((double) zoom));
    double tol = (new Integer(gp.getIntValue("vgraphic.selwidth"))).doubleValue() + ((double) HyperEdgeRef.getWidth() / 2d);
    //Project with real stripped curve
    NURBSShapeProjection proj = new NURBSShapeProjection(temporaryShape.stripDecorations().clone(), DragOrigin);
    //One Value not given - set it
    if ((Double.isNaN(tempStart)) || (Double.isNaN(Double.NaN))) {
      if (proj.getResultPoint().distance(DragOrigin) <= tol) {
        // clicked on curve with tol.
        DragsetsStart = setStartNext; //Alternierend
      } else
        DragOrigin = null;
    } else {
      if (DragOrigin.distance(temporaryShape.CurveAt(tempStart)) <= tol)
        DragsetsStart = true;
      else if (DragOrigin.distance(temporaryShape.CurveAt(tempEnd)) <= tol)
        DragsetsStart = false;
      else //Existing Subcurve but not clicked on Start or end -> no drag
        DragOrigin = null;
    }
  }

  public void mouseDragged(MouseEvent e) {

    if (((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK)
        || ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) == InputEvent.SHIFT_DOWN_MASK)) {
      // When someone presses alt or shift while drag...end it.
      internalReset();
      return;
    }

    // If the click was initiating the drag correctly,
    if (DragOrigin != null) {
      // Point in Graph (exact, double values) of the actual
      // Mouse-Position
      Point2D exactPointInGraph = new Point2D.Double((double) e
          .getPoint().x
          / ((double) zoom), (double) e.getPoint().y
          / ((double) zoom));
      NURBSShapeProjection proj = new NURBSShapeProjection(temporaryShape.stripDecorations().clone(), exactPointInGraph);
      if (DragsetsStart)
        tempStart = proj.getResultParameter();
      else
        tempEnd = proj.getResultParameter();
      //		temporaryShape = new NURBSShapeFragment(DragBeginShape.clone(),tempStart,tempEnd);

      // Finally - notify Graph Observers to redraw, and on first
      // modification start that as a block
      if (firstdrag) // If wirst drag - start Block
      {
        vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(),
            GraphConstraints.BLOCK_START | GraphConstraints.UPDATE
                | GraphConstraints.HYPEREDGESHAPE,
            GraphConstraints.HYPEREDGE));
        if ((Double.isNaN(tempStart)) || (Double.isNaN(Double.NaN)))
          setStartNext ^= toggleOnClick; //We will really set a value so toggle to next
      } else
        // continnue Block
        vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(),
            GraphConstraints.UPDATE
                | GraphConstraints.HYPEREDGESHAPE,
            GraphConstraints.HYPEREDGE));
    }
    MouseOffSet = e.getPoint();
    firstdrag = false;
  }

  public void mouseReleased(MouseEvent e) {
    if (!firstdrag) // If in Drag - handle last moevemnt
    {
      if (!((e.getPoint().x == -1) || (e.getPoint().y == -1)))
        // If there was no external reset
        mouseDragged(e);
    }
    internalReset();
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
    // Point in Graph (exact, double values) of the actual Mouse-Position
    Point2D exactPointInGraph = new Point2D.Double((double) e.getPoint().x
        / ((double) zoom), (double) e.getPoint().y
        / ((double) zoom));
    NURBSShapeProjection proj = new NURBSShapeProjection(temporaryShape.stripDecorations().clone(), exactPointInGraph);
    double tol = (new Integer(gp.getIntValue("vgraphic.selwidth"))).doubleValue() + ((double) HyperEdgeRef.getWidth() / 2d);
    if (proj.getResultPoint().distance(exactPointInGraph) <= tol) {      // clicked on curve with tol. -> Update subcurve in a block so that after that VHyperShapeGraphic redraws
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(), GraphConstraints.BLOCK_START | GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE, GraphConstraints.HYPEREDGE));
      if (setStartNext)
        tempStart = proj.getResultParameter();
      else
        tempEnd = proj.getResultParameter();
      setStartNext ^= toggleOnClick; //Toggle if toggleonclick
//			}
      HyperEdgeRef.setShape(getShape()); //Set shape to NURBSShapeFragment
      internalReset();
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, HyperEdgeRef.getIndex(), GraphConstraints.BLOCK_END, GraphConstraints.HYPEREDGE));
    }
  }

  public Point getMouseOffSet() {
    return MouseOffSet;
  }

  // Ignore Grid & Rectangle
  public void setGrid(int x, int y) {
  }

  public void setGridOrientated(boolean b) {
  }

  public Rectangle getSelectionRectangle() {
    return null;
  }

  public void update(Observable o, Object arg) {
    if ((o == gp) && (arg == "zoom"))
      zoom = gp.getFloatValue("zoom");
  }
}
