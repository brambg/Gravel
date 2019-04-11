package control.nurbs;

import io.GeneralPreferences;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;
import model.Messages.NURBSCreationMessage;
import model.*;
import view.VCommonGraphic;
import view.VHyperGraphic;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Class for handling Drags for Creation of a Circle
 * <p>
 * - Drag on Background begins new Circle (discarding old circle)
 * - Drag Begin Point is the origin. Distance from Origin to Mouse is the Circle.
 * - If Drag ends, the last MousePosition is the CircleRadius that remains
 *
 * @author Ronny Bergmann
 */
public class CircleCreationHandler implements ShapeCreationMouseHandler {
  VGraph vg = null;
  VHyperGraph vhg = null;
  VCommonGraphic vgc;
  GeneralPreferences gp;
  Point MouseOffSet;
  boolean firstdrag = true;
  Point2D CircleOrigin = null;
  Point2D LastCircleOrigin = null;
  Point DragOrigin = null; //Both Points CircleOrigin and DragOrigin keep the same point, the reset is at different stages and the tye differs
  int size = 0, hyperedgeindex;
  NURBSShape lastcircle = null;
  boolean shiftwaspressed = false;

  private void reInit() {
    CircleOrigin = null;
    LastCircleOrigin = null;
    size = 0;
  }

  /**
   * Initialize the Controller to a given VHYperGraphic and a specified VHyperEdge,
   * whose shape should be modified
   *
   * @param g
   * @param vheI
   */
  public CircleCreationHandler(VHyperGraphic g, int vheI) {
    vgc = g;
    vhg = g.getGraph();
    gp = GeneralPreferences.getInstance();
    MouseOffSet = new Point(0, 0);
    hyperedgeindex = vheI;
  }

  public void removeGraphObservers() {
  } //Nothing to remove

  public Rectangle getSelectionRectangle() { //No Selections possible here
    return null;
  }

  public void resetShape() {
    lastcircle = null;
  }

  public NURBSCreationMessage getShapeParameters() {
    if ((CircleOrigin == null) || (size <= 0))
      return new NURBSCreationMessage();
    //TODO: CreationCircleHandler : Degree, at least 4
    return new NURBSCreationMessage(4, new Point2D.Double(CircleOrigin.getX(), CircleOrigin.getY()), size);
  }

  /**
   * Set the internal values to another circle specified in the CreationMessage
   * <p>
   * If there is a drag-happening, nothing happens
   * If the Message is null, invalid or not a Circle-Message, the internal Values are reset
   * <p>
   * Else the values are set and can directly after this method be obtained by getShapeParameters
   * or to get the Shape after that use getShape();
   */
  public void setShapeParameters(NURBSCreationMessage nm) {
    if (dragged())
      return;
    if ((nm == null) || (!nm.isValid()) || (nm.getType() != NURBSCreationMessage.CIRCLE)) //nonsiutable
    { //Reset values
      reInit();
      return;
    }
    NURBSCreationMessage local = nm.clone();
    Point2D p = (Point2D) local.getPoints().firstElement().clone();
    int rad = local.getValues().firstElement();
    if ((p == null) || (rad <= 0)) {
      reInit();
      return;
    }
    CircleOrigin = p;
    size = rad;
    buildCircle();
    vhg.modifyHyperEdges.get(hyperedgeindex).setShape(lastcircle);
    if (vhg != null) //Hypergraph
      vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, hyperedgeindex, GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE | GraphConstraints.CREATION, GraphConstraints.HYPEREDGE));
  }

  public NURBSShape getShape() {
    return lastcircle;
  }

  public boolean dragged() {
    return (DragOrigin != null) && (!firstdrag);
  }

  private void buildCircle() {
    if ((CircleOrigin != null) && (size > 0)) {
      lastcircle = NURBSShapeFactory.CreateShape(getShapeParameters());
    } else
      lastcircle = new NURBSShape();
  }

  private void internalReset() {
    //Only if a Block was started: End it...
    if ((DragOrigin != null) && (!firstdrag)) //We had an Drag an a Circle was created, draw it one final time
    {
      if (vg != null)
        vg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, GraphConstraints.BLOCK_END));
      else if (vhg != null) {
        vhg.modifyHyperEdges.get(hyperedgeindex).setShape(lastcircle);
        vhg.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, GraphConstraints.BLOCK_END));
      }
    }
    resetShape();
    DragOrigin = null;
  }

  //One every Click a potental Drag is initialized but firstdrag = true signals, that no Drag-Movement happened yet
  public void mousePressed(MouseEvent e) {
    firstdrag = true;
    boolean alt = ((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK); // alt ?
    shiftwaspressed = ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) == InputEvent.SHIFT_DOWN_MASK); //shift ?
    if (alt)
      return;
    MouseOffSet = e.getPoint(); //Aktuelle Position merken für eventuelle Bewegungen while pressed
    DragOrigin = new Point(Math.round(e.getPoint().x / ((float) vgc.getZoom() / 100)), Math.round(e.getPoint().y / ((float) vgc.getZoom() / 100))); //Rausrechnen des zooms
    if (!shiftwaspressed) {
      CircleOrigin = new Point2D.Double((double) e.getPoint().x / (vgc.getZoom() / 100d), (double) e.getPoint().y / (vgc.getZoom() / 100d));
      size = 0;
    } else if (CircleOrigin != null)
      LastCircleOrigin = new Point2D.Double(CircleOrigin.getX(), CircleOrigin.getY());
  }

  public void mouseDragged(MouseEvent e) {

    if ((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK) {
      internalReset();
      return;
    }
    if ((!shiftwaspressed && ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) == InputEvent.SHIFT_DOWN_MASK))
        || (shiftwaspressed && ((InputEvent.SHIFT_DOWN_MASK & e.getModifiersEx()) != InputEvent.SHIFT_DOWN_MASK))) { //Shift toggle while drag
      internalReset();
      return;
    }
    //Handling selection Rectangle
    if (DragOrigin != null) {
      //Update Values
      Point pointInGraph = new Point(Math.round(e.getPoint().x / ((float) vgc.getZoom() / 100)), Math.round(e.getPoint().y / ((float) vgc.getZoom() / 100))); //Rausrechnen des zooms
//			Point LastpointInGraph = new Point(Math.round(e.getPoint().x/((float)vgc.getZoom()/100)),Math.round(e.getPoint().y/((float)vgc.getZoom()/100))); //Rausrechnen des zooms
      Point mov = new Point(pointInGraph.x - DragOrigin.x, pointInGraph.y - DragOrigin.y);
      MouseOffSet = e.getPoint(); //Aktuelle Position merken für eventuelle Bewegungen while pressed
      if (!shiftwaspressed)
        size = Math.round((float) CircleOrigin.distance(pointInGraph));
      else if (LastCircleOrigin != null)
        CircleOrigin = new Point2D.Double(LastCircleOrigin.getX() + mov.x, LastCircleOrigin.getY() + mov.y);
      buildCircle();
      VGraphInterface notify = null;
      if (vg != null) //Normal Graph
        notify = vg;
      else if (vhg != null) //Hypergraph
        notify = vhg;

      if (firstdrag) //If wirst drag - start Block
        notify.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, hyperedgeindex, GraphConstraints.BLOCK_START | GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE | GraphConstraints.CREATION, GraphConstraints.HYPEREDGE));
      else    //continnue Block
        notify.pushNotify(new GraphMessage(GraphConstraints.HYPEREDGE, hyperedgeindex, GraphConstraints.UPDATE | GraphConstraints.HYPEREDGESHAPE | GraphConstraints.CREATION, GraphConstraints.HYPEREDGE));
    }
    MouseOffSet = e.getPoint();
    firstdrag = false;
  }

  public void mouseReleased(MouseEvent e) {
    //nur falls schon gedragged wurde nochmals draggen
    if (!firstdrag) {
      if (!((e.getPoint().x == -1) || (e.getPoint().y == -1))) //kein Reset von außerhalb wegen modusumschaltung
        mouseDragged(e); //Das gleiche wie als wenn man bewegt, nur ist danach kein Knoten mehr bewegter Knoten
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
  }

  public Point getMouseOffSet() {
    if ((shiftwaspressed) && (LastCircleOrigin != null))
      return new Point(Math.round((float) (LastCircleOrigin.getX() * (vgc.getZoom() / 100d))), Math.round((float) (LastCircleOrigin.getY() * (vgc.getZoom() / 100d))));
    else
      return MouseOffSet;
  }

  //Ignore Grid
  public void setGrid(int x, int y) {
  }

  public void setGridOrientated(boolean b) {
  }
}
