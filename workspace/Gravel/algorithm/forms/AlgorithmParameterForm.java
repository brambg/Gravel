package algorithm.forms;

import model.MGraph;
import model.VGraph;
import view.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;

public abstract class AlgorithmParameterForm extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  public AlgorithmParameterForm(MGraph g) {

  }

  public AlgorithmParameterForm(VGraph g) {

  }

  protected void alignCenter() {
    Point p = new Point(0, 0);
    p.y += Math.round(Gui.getInstance().getParentWindow().getHeight() / 2);
    p.x += Math.round(Gui.getInstance().getParentWindow().getWidth() / 2);
    p.y -= Math.round(getHeight() / 2);
    p.x -= Math.round(getWidth() / 2);
    setLocation(p.x, p.y);
  }

  /**
   * starts the dialog and returns
   *
   * @return
   */
  public abstract HashMap showDialog();
}
