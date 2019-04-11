package view.pieces;

import model.NURBSShape;
import model.NURBSShapeFragment;
import view.VCommonGraphic;
import view.VHyperShapeGraphic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Small class containing all Buttons and Actionhandling for the second Modus of HyperEdgeShape-Dialog
 *
 * @author ronny
 */
public class HESFreeModComponent implements ActionListener {
  private JButton bIncKnots, bDecKnots;
  private ButtonGroup bModificationModus;
  private JRadioButton rModGlobal, rModLocal;
  private JButton bRotation, bTranslation, bScaling, bScalingDir;
  private JButton bLocalSetStart, bLocalSetEnd, bLocalInvert;
  private Container FreeModFields;

  private VHyperShapeGraphic HShapeGraphicRef;
  private int HEdgeRefIndex;

  public HESFreeModComponent(int index, VHyperShapeGraphic vhg) {
    if (vhg.getGraph().modifyHyperEdges.get(index) == null)
      return;
    HShapeGraphicRef = vhg;
    HEdgeRefIndex = index;
    buildFreeModPanel();
  }

  public void resetModus() {
    setGlobalButtonsEnabled(true);
    NURBSShape shape = HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).getShape();
    if (rModGlobal.isSelected())
      shape = shape.stripDecorations();
    HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).setShape(shape);
    HShapeGraphicRef.setMouseHandling(VCommonGraphic.CURVEPOINT_MOVEMENT_MOUSEHANDLING);
    deselectGlobalButtons();
  }

  public Container getContent() {
    return FreeModFields;
  }

  public boolean isLocal() {
    return rModLocal.isSelected();
  }

  public void setVisible(boolean visible) {
    FreeModFields.setVisible(visible);
    if (visible) //init always with CP-Movement TODO Button for the CP-Movement-Mode to visualize it
    {
      HShapeGraphicRef.setMouseHandling(VCommonGraphic.CURVEPOINT_MOVEMENT_MOUSEHANDLING);
      //Deselect all Buttons
      deselectGlobalButtons();
    }
  }

  private void deselectGlobalButtons() {
    bRotation.setSelected(false);
    bTranslation.setSelected(false);
    bScaling.setSelected(false);
    bScalingDir.setSelected(false);
    bIncKnots.setSelected(false);
    bDecKnots.setSelected(false);
  }

  private void setGlobalButtonsEnabled(boolean enabled) {
    bIncKnots.setEnabled(enabled);
    bDecKnots.setEnabled(enabled);
    bRotation.setEnabled(enabled);
    bTranslation.setEnabled(enabled);
    bScaling.setEnabled(enabled);
    bScalingDir.setEnabled(enabled);
  }

  private void setLocalButtonsEnabled(boolean enabled) {
    bLocalSetStart.setEnabled(enabled);
    bLocalSetEnd.setEnabled(enabled);
    bLocalInvert.setEnabled(enabled);
  }

  public void setEnabled(boolean enabled) {
    setGlobalButtonsEnabled(enabled);
    setLocalButtonsEnabled(enabled);
    rModGlobal.setEnabled(enabled);
    rModLocal.setEnabled(enabled);
    if (enabled)
      refresh();
  }

  private void setLocalVisibility(boolean visible) {
    bLocalSetStart.setVisible(visible);
    bLocalSetEnd.setVisible(visible);
    bLocalInvert.setVisible(visible);
    bIncKnots.setVisible(!visible);
    bDecKnots.setVisible(!visible);
  }

  private void deselectLocalButtons() {
    bLocalSetStart.setSelected(false);
    bLocalSetEnd.setSelected(false);
    bLocalInvert.setSelected(false);
  }

  private void buildFreeModPanel() {
    String IconDir = System.getProperty("user.dir") + "/data/img/icon/";
    FreeModFields = new Container();
    FreeModFields.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 0, 5, 0);
    c.anchor = GridBagConstraints.CENTER;
    c.gridy = 0;
    c.gridx = 0;
    c.gridwidth = 1;
    c.gridheight = 1;

    c.gridwidth = 2;
    bModificationModus = new ButtonGroup();
    rModGlobal = new JRadioButton("global");
    rModGlobal.setToolTipText("Gesamten Umriss bearbeiten");
    rModGlobal.setSelected(true);
    rModGlobal.addActionListener(this);
    bModificationModus.add(rModGlobal);
    FreeModFields.add(rModGlobal, c);
    c.gridx += 2;
    rModLocal = new JRadioButton("lokal");
    rModLocal.setToolTipText("Anwenden der Transformationen auf einen Umrissteil");
    rModLocal.setSelected(false);
    rModLocal.addActionListener(this);
    bModificationModus.add(rModLocal);
    FreeModFields.add(rModLocal, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    bIncKnots = new JButton(new ImageIcon(IconDir + "plus16.png"));
    bIncKnots.setToolTipText("Knoten (und damit Kontrollpunkte) hinzufügen");
    bIncKnots.setSize(new Dimension(17, 17));
    bIncKnots.addActionListener(this);
    FreeModFields.add(bIncKnots, c);

    c.gridx++;
    bDecKnots = new JButton(new ImageIcon(IconDir + "minus16.png"));
    bDecKnots.setToolTipText("Knoten entfernen (falls möglch)");
    bDecKnots.setSize(new Dimension(17, 17));
    bDecKnots.addActionListener(this);
    FreeModFields.add(bDecKnots, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    bRotation = new JButton(new ImageIcon(IconDir + "rotate32.png"));
    bRotation.setToolTipText("Umriss(teil) rotieren.");
    bRotation.setSize(new Dimension(32, 32));
    bRotation.addActionListener(this);
    FreeModFields.add(bRotation, c);

    c.gridx++;
    c.gridwidth = 1;

    bTranslation = new JButton(new ImageIcon(IconDir + "translate32.png"));
    bTranslation.setToolTipText("Umriss(teil) verschieben.");
    bTranslation.setSize(new Dimension(32, 32));
    bTranslation.addActionListener(this);
    FreeModFields.add(bTranslation, c);

    c.gridx++;
    c.gridwidth = 1;
    bScaling = new JButton(new ImageIcon(IconDir + "scale32.png"));
    bScaling.setToolTipText("Umriss(teil) skalieren.");
    bScaling.setSize(new Dimension(32, 32));
    bScaling.addActionListener(this);
    FreeModFields.add(bScaling, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    bScalingDir = new JButton(new ImageIcon(IconDir + "scaledir32.png"));
    bScalingDir.setToolTipText("Umriss(teil) entlang einer Richtung skalieren.");
    bScalingDir.setSize(new Dimension(32, 32));
    bScalingDir.addActionListener(this);
    FreeModFields.add(bScalingDir, c);

    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;

    bLocalSetStart = new JButton(new ImageIcon(IconDir + "start32.png"));
    bLocalSetStart.setToolTipText("Startpunkt des Umrissteils setzen.");
    bLocalSetStart.setSize(new Dimension(32, 32));
    bLocalSetStart.addActionListener(this);
    FreeModFields.add(bLocalSetStart, c);

    c.gridx++;
    bLocalInvert = new JButton(new ImageIcon(IconDir + "invert32.png"));
    bLocalInvert.setToolTipText("Teilkurvenauswahl invertieren.");
    bLocalInvert.setSize(new Dimension(32, 32));
    bLocalInvert.addActionListener(this);
    FreeModFields.add(bLocalInvert, c);

    c.gridx++;
    bLocalSetEnd = new JButton(new ImageIcon(IconDir + "end32.png"));
    bLocalSetEnd.setToolTipText("Endpunkt des Umrissteils setzen.");
    bLocalSetEnd.setSize(new Dimension(32, 32));
    bLocalSetEnd.addActionListener(this);
    FreeModFields.add(bLocalSetEnd, c);

    setLocalVisibility(rModLocal.isSelected());
  }

  private void handleLocalAction(ActionEvent e) {
    if (e.getSource() == bLocalSetStart) {
      boolean wasSelected = bLocalSetStart.isSelected();
      deselectLocalButtons();
      deselectGlobalButtons();
      if (wasSelected) //back to default / no DETAIL
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING | VCommonGraphic.NO_DETAIL);
      else {
        bLocalSetStart.setSelected(true);
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING | VCommonGraphic.SET_START);
      }
    } else if (e.getSource() == bLocalSetEnd) {
      boolean wasSelected = bLocalSetEnd.isSelected();
      deselectLocalButtons();
      deselectGlobalButtons();
      if (wasSelected) //Back to default
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING | VCommonGraphic.NO_DETAIL);
      else {
        bLocalSetEnd.setSelected(true);
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING | VCommonGraphic.SET_END);
      }
    } else if (e.getSource() == bLocalInvert)
      HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING | VCommonGraphic.TOGGLE);
  }

  private void setToStandardMouseHandling() {
    if (rModLocal.isSelected()) {
      HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING);
      deselectLocalButtons();
    } else
      HShapeGraphicRef.setMouseHandling(VCommonGraphic.CURVEPOINT_MOVEMENT_MOUSEHANDLING);
  }

  private void updateLocal() {
    if (rModGlobal.isSelected()) {
      setGlobalButtonsEnabled(true);
      NURBSShape shape = HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).getShape();
      HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).setShape(shape.stripDecorations());
      HShapeGraphicRef.setMouseHandling(VCommonGraphic.CURVEPOINT_MOVEMENT_MOUSEHANDLING);
    } else {
      boolean selectionExists;
      NURBSShape shape = HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).getShape();
      if ((shape.getDecorationTypes() & NURBSShape.FRAGMENT) == NURBSShape.FRAGMENT) {
        selectionExists = !((NURBSShapeFragment) shape).getSubCurve().isEmpty();
      } else {
        HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).setShape(new NURBSShapeFragment(shape, Double.NaN, Double.NaN));
        selectionExists = false;
      }
      setGlobalButtonsEnabled(selectionExists);
      if (!selectionExists)
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SUBCURVE_MOUSEHANDLING);
    }
    setLocalVisibility(rModLocal.isSelected());
  }

  public void actionPerformed(ActionEvent e) {
    if ((e.getSource() == rModGlobal) || (e.getSource() == rModLocal)) {
      deselectGlobalButtons();
      updateLocal();
    }
    if (e.getSource() == bIncKnots) {
      boolean wasSel = bIncKnots.isSelected();
      if (wasSel)
        setToStandardMouseHandling();
      else {
        deselectGlobalButtons();
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.KNOT_MODIFICATION_MOUSEHANDLING | VCommonGraphic.ADD);
      }
      bIncKnots.setSelected(!wasSel);
    }
    if (e.getSource() == bDecKnots) {
      boolean wasSel = bDecKnots.isSelected();
      if (wasSel)
        setToStandardMouseHandling();
      else {
        deselectGlobalButtons();
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.KNOT_MODIFICATION_MOUSEHANDLING | VCommonGraphic.REMOVE);
      }
      bDecKnots.setSelected(!wasSel);
    } else if (e.getSource() == bRotation) {
      if (rModLocal.isSelected())
        deselectLocalButtons();
      if (bRotation.isSelected()) {
        bRotation.setSelected(false);
        setToStandardMouseHandling();
      } else {
        deselectGlobalButtons();
        bRotation.setSelected(true);
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SHAPE_MOUSEHANDLING | VCommonGraphic.ROTATE);
      }
    } else if (e.getSource() == bTranslation) {
      if (rModLocal.isSelected())
        deselectLocalButtons();
      if (bTranslation.isSelected()) {
        bTranslation.setSelected(false);
        setToStandardMouseHandling();
      } else {
        deselectGlobalButtons();
        bTranslation.setSelected(true);
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SHAPE_MOUSEHANDLING | VCommonGraphic.TRANSLATE);
      }
    } else if (e.getSource() == bScaling) {
      if (rModLocal.isSelected())
        deselectLocalButtons();
      if (bScaling.isSelected()) {
        bScaling.setSelected(false);
        setToStandardMouseHandling();
      } else {
        deselectGlobalButtons();
        bScaling.setSelected(true);
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SHAPE_MOUSEHANDLING | VCommonGraphic.SCALE);
      }
    } else if (e.getSource() == bScalingDir) {
      if (rModLocal.isSelected())
        deselectLocalButtons();
      if (bScalingDir.isSelected()) {
        bScalingDir.setSelected(false);
        setToStandardMouseHandling();
      } else {
        deselectGlobalButtons();
        bScalingDir.setSelected(true);
        HShapeGraphicRef.setMouseHandling(VCommonGraphic.SHAPE_MOUSEHANDLING | VCommonGraphic.SCALE_DIR);
      }
    } else
      handleLocalAction(e);
  }

  public void refresh() {
    //We have a fragment
    NURBSShape editshape = HShapeGraphicRef.getGraph().modifyHyperEdges.get(HEdgeRefIndex).getShape();
    if ((editshape.getDecorationTypes() & NURBSShape.FRAGMENT) == NURBSShape.FRAGMENT) {
      setGlobalButtonsEnabled(!((NURBSShapeFragment) editshape).getSubCurve().isEmpty());
      if (!rModLocal.isSelected())
        rModLocal.doClick();
    } else if (rModLocal.isSelected()) {
      setGlobalButtonsEnabled(true);
      rModGlobal.doClick();
    }
  }
}
