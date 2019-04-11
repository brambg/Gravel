package dialogs;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Class for the propertie dialog of a PNG-Export, mainly the dimensions of the picture
 * can be set and the background of the picture
 *
 * @author ronny
 */
public class ExportPNGDialog extends JDialog implements ActionListener, CaretListener {
  private IntegerTextField iSizeX, iSizeY;
  private JButton bOk, bCancel;
  private JRadioButton selX, selY, rTrans, rWhite;
  private boolean accepted = false;
  private int origx, origy;
  private static final long serialVersionUID = 1L;

  /**
   * Initialize the Dialog with a parent frame below (this Dialog is modal)
   * and a preset size of the picture
   *
   * @param parent Frame which is deaactivated by this dialog
   * @param ox     Presetz size of the picture - x-value
   * @param oy     y-value
   */
  public ExportPNGDialog(Frame parent, int ox, int oy) {
    super(parent, "Einstellungen für PNG-Export");
    origx = ox;
    origy = oy;
    Container content = getContentPane();
    GridBagConstraints c = new GridBagConstraints();
    Container ContentPane = this.getContentPane();
    ContentPane.removeAll();
    ContentPane.setLayout(new GridBagLayout());
    c.insets = new Insets(7, 7, 7, 7);
    c.anchor = GridBagConstraints.WEST;
    c.gridy = 0;
    c.gridx = 0;
    c.gridwidth = 3;
    content.add(new JLabel("<html>Alle Angaben sind in Pixel<br>Ausgehend von der selektierten Gr" + main.CONST.html_oe + "" + main.CONST.html_sz + "e<br> wird die andere Berechnet</html>"), c);
    c.gridy++;
    c.gridwidth = 1;
    selX = new JRadioButton("Breite :");
    selX.addActionListener(this);
    content.add(selX, c);
    c.gridx = 1;
    c.gridwidth = 2;
    iSizeX = new IntegerTextField();
    iSizeX.setPreferredSize(new Dimension(200, 20));
    iSizeX.addCaretListener(this);
    content.add(iSizeX, c);
    c.gridwidth = 1;
    c.gridy++;
    c.gridx = 0;
    selY = new JRadioButton("<html>H" + main.CONST.html_oe + "he :</html>");
    selY.addActionListener(this);
    content.add(selY, c);
    c.gridx = 1;
    c.gridwidth = 2;
    iSizeY = new IntegerTextField();
    iSizeY.setPreferredSize(new Dimension(200, 20));
    iSizeY.addCaretListener(this);
    content.add(iSizeY, c);
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.EAST;
    c.gridy++;
    c.gridx = 1;

    ButtonGroup b = new ButtonGroup();
    b.add(selX);
    b.add(selY);

    c.gridy++;
    c.gridx = 0;

    ButtonGroup BgChoice = new ButtonGroup();
    rTrans = new JRadioButton("transparent");
    rWhite = new JRadioButton("<html>wei" + main.CONST.html_sz + "</html>");
    BgChoice.add(rTrans);
    BgChoice.add(rWhite);
    rWhite.setSelected(true);
    content.add(new JLabel("Hintergrund"), c);
    c.gridx++;
    content.add(rWhite, c);
    c.gridx++;
    content.add(rTrans, c);

    c.gridy++;
    c.gridx = 0;

    bCancel = new JButton("Abbrechen");
    bCancel.addActionListener(this);
    content.add(bCancel, c);
    //Add ESC-Handling
    InputMap iMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

    ActionMap aMap = getRootPane().getActionMap();
    aMap.put("escape", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        accepted = false;
        dispose();
      }
    });
    c.gridx = 2;
    bOk = new JButton("Speichern");
    bOk.addActionListener(this);
    content.add(bOk, c);

    this.getRootPane().setDefaultButton(bOk);
    selX.doClick();
    iSizeX.setValue(ox);
    setResizable(false);
    this.setModal(true);
    pack();
    Point p = new Point(0, 0);
    p.y += Math.round(parent.getHeight() / 2);
    p.x += Math.round(parent.getWidth() / 2);
    p.y -= Math.round(getHeight() / 2);
    p.x -= Math.round(getWidth() / 2);
    setLocation(p.x, p.y);
    this.setVisible(true);
  }

  /**
   * returns the x-Value of the actual user input size
   *
   * @return hoizontal size of the picture in px
   */
  public int getSizeX() {
    return iSizeX.getValue();
  }

  /**
   * returns the y-Value of the actual user input size
   *
   * @return vertical size of the picture in px
   */
  public int getSizeY() {
    return iSizeY.getValue();
  }

  /**
   * Indicates, whether the user pressed okay in the dialog
   *
   * @return
   */
  public boolean IsAccepted() {
    return accepted;
  }

  public boolean isTransparent() {
    return rTrans.isSelected();
  }

  /**
   * handling user input action on the (radio-) buttons
   */
  public void actionPerformed(ActionEvent e) {
    if ((e.getSource() == selX) || (e.getSource() == selY)) {
      iSizeX.setEnabled(selX.isSelected());
      iSizeY.setEnabled(selY.isSelected());
    } else if (e.getSource() == bOk) {
      if ((iSizeX.getValue() == -1) || (iSizeY.getValue() == -1)) {
        JOptionPane.showMessageDialog(this, "<html><p>Bitte eine positive Zahl angeben.</p></hmtl>", "Fehler", JOptionPane.ERROR_MESSAGE);
      } else {
        accepted = true;
        dispose();
      }
    } else if (e.getSource() == bCancel) {
      accepted = false;
      dispose();
    }
  }

  /**
   * handle caret updates in the two integertextfield-Components and calculate the deactivated value
   */
  public void caretUpdate(CaretEvent e) {
    if ((e.getSource() == iSizeX) || (e.getSource() == iSizeY)) {
      if ((selX.isSelected()) && (iSizeX.getValue() != -1)) {
        iSizeY.removeCaretListener(this); //So it doesn't ivoke itself
        iSizeY.setValue(Math.round((float) iSizeX.getValue() * (float) origy / (float) origx));
        iSizeY.addCaretListener(this); //So it doesn't ivoke itself
      }
      if ((selY.isSelected()) && (iSizeY.getValue() != -1)) {
        iSizeX.removeCaretListener(this); //So it doesn't evoke itself
        iSizeX.setValue(Math.round((float) iSizeY.getValue() * (float) origx / (float) origy));
        iSizeX.addCaretListener(this);
      }
    }
  }
}
