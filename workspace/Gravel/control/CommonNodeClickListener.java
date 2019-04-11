package control;

import dialogs.JNodeDialog;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;
import model.VGraph;
import model.VHyperGraph;
import model.VNode;
import view.VCommonGraphic;
import view.VGraphic;
import view.VHyperGraphic;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Handle Click Actions on Nodes, that are Common to all Modes
 *
 * @author Ronny Bergmann
 * @since 0.4
 */
public class CommonNodeClickListener implements MouseListener {

  VCommonGraphic vgc;
  VGraph vg = null;
  VHyperGraph vhg = null;

  public CommonNodeClickListener(VGraphic g) {
    vgc = g;
    vg = g.getGraph();
  }

  public CommonNodeClickListener(VHyperGraphic g) {
    vgc = g;
    vhg = g.getGraph();
  }

  public void mouseClicked(MouseEvent e) {
    Point p = new Point(Math.round(e.getPoint().x / ((float) vgc.getZoom() / 100)), Math.round(e.getPoint().y / ((float) vgc.getZoom() / 100))); //rausrechnen
    //Double Click on Node
    VNode r = null;
    if (vg != null)
      r = vg.modifyNodes.getFirstinRangeOf(p);
    else if (vhg != null)
      r = vhg.modifyNodes.getFirstinRangeOf(p);
    boolean alt = ((InputEvent.ALT_DOWN_MASK & e.getModifiersEx()) == InputEvent.ALT_DOWN_MASK); // alt ?
    if (alt) //if alt+click -> toggle visibility of the text
    {
      if (r != null) //Doubleclick really on Node
      {
        r.setNameVisible(!r.isNameVisible());
        GraphMessage msg = new GraphMessage(GraphConstraints.NODE, r.getIndex(), GraphConstraints.UPDATE, GraphConstraints.NODE);
        if (vg != null)
          vg.pushNotify(msg);
        else if (vhg != null)
          vhg.pushNotify(msg);
      }
    } else if ((e.getClickCount() == 2) && (e.getModifiers() == MouseEvent.BUTTON1_MASK)) //Double click without alt
    {
      if (r != null) //Doubleclick really on Node
      {
        if (vg != null)
          new JNodeDialog(r, vg);
        else
          new JNodeDialog(r, vhg);
      }
    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }
}
