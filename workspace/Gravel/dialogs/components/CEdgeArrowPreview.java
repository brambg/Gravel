package dialogs.components;

import io.GeneralPreferences;
import model.*;
import view.VCommonGraphic;
import view.VGraphic;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * This Class contains the Preview for an edge, that is edited. It is an Observer,
 * so that it might react on Input Fields, for example the Input Fields of the Arrow Properties
 *
 * @author ronny
 */
public class CEdgeArrowPreview extends Container implements Observer {

  private static final long serialVersionUID = 1L;

  private VGraphic ArrowG;
  private VEdge ArrowEdge;

  /**
   * Constrcts the preview for the values of a given edge v
   * If the edge is null, the global Std Values are used
   *
   * @param e
   */
  public CEdgeArrowPreview(VEdge e) {
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.WEST;

    VGraph ArrowTest = new VGraph(true, false, false); //directed without loops nor multiple
    ArrowTest.modifyNodes.add(new VNode(1, 30, 15, 20, 0, 0, 0, false), new MNode(1, "T1"));
    ArrowTest.modifyNodes.add(new VNode(2, 170, 15, 20, 0, 0, 0, false), new MNode(2, "T2"));
    ArrowTest.modifySubgraphs.add(new VSubgraph(1, new Color(0.5f, 0.5f, 0.5f, 1f)), new MSubgraph(1, "white"));
    ArrowTest.modifySubgraphs.addNodetoSubgraph(1, 1);
    ArrowTest.modifySubgraphs.addNodetoSubgraph(2, 1);
    ArrowEdge = new VStraightLineEdge(1, 2);
    ArrowTest.modifyEdges.add(ArrowEdge, new MEdge(ArrowEdge.getIndex(), 1, 2, 1, ""), new Point(30, 15), new Point(170, 15));
    ArrowTest.getMathGraph().modifyEdges.get(ArrowEdge.getIndex()).name = "e";
    ArrowTest.modifyEdges.get(ArrowTest.getMathGraph().modifyEdges.indicesBetween(1, 2).firstElement()).setTextProperties(new VEdgeText());
    ArrowTest.modifyEdges.get(ArrowTest.getMathGraph().modifyEdges.indicesBetween(1, 2).firstElement()).setLinestyle(new VEdgeLinestyle());
    ArrowG = new VGraphic(new Dimension(200, 60), ArrowTest);
    ArrowG.setZoom(100);
    ArrowG.setMouseHandling(VCommonGraphic.NO_MOUSEHANDLING);
    ArrowG.setPreferredSize(new Dimension(200, 62));
    GeneralPreferences.getInstance().deleteObserver(ArrowG);
    add(ArrowG, c);
    InitValues(e);
  }

  /**
   * Initialize the Preview with the Values of a given Edge
   * Choose null to get the global Preferencex
   *
   * @param e
   */
  public void InitValues(VEdge e) {
    if (e != null) {
      ArrowEdge = ArrowG.getGraph().modifyEdges.get(1);
      ArrowEdge.getArrow().setAngle(e.getArrow().getAngle());
      ArrowEdge.getArrow().setPart(e.getArrow().getPart());
      ArrowEdge.getArrow().setSize(e.getArrow().getSize());
      ArrowEdge.getArrow().setPos(e.getArrow().getPos());

      ArrowEdge.getTextProperties().setDistance(e.getTextProperties().getDistance());
      ArrowEdge.getTextProperties().setPosition(e.getTextProperties().getPosition());
      ArrowEdge.getTextProperties().setSize(e.getTextProperties().getSize());
      ArrowEdge.getTextProperties().setshowvalue(e.getTextProperties().isshowvalue());
      ArrowEdge.getTextProperties().setVisible(e.getTextProperties().isVisible());

      ArrowEdge.getLinestyle().setType(e.getLinestyle().getType());
      ArrowEdge.getLinestyle().setLength(e.getLinestyle().getLength());
      ArrowEdge.getLinestyle().setDistance(e.getLinestyle().getDistance());
    } else {
      GeneralPreferences gp = GeneralPreferences.getInstance();
      ArrowEdge.getArrow().setAngle(gp.getIntValue("edge.arrow_alpha"));
      ArrowEdge.getArrow().setPart(gp.getFloatValue("edge.arrow_part"));
      ArrowEdge.getArrow().setSize(gp.getIntValue("edge.arrow_size"));
      ArrowEdge.getArrow().setPos(gp.getFloatValue("edge.arrow_pos"));

      ArrowEdge.getTextProperties().setDistance(gp.getIntValue("edge.text_distance"));
      ArrowEdge.getTextProperties().setPosition(gp.getFloatValue("edge.text_position"));
      ArrowEdge.getTextProperties().setSize(gp.getIntValue("edge.text_size"));
      ArrowEdge.getTextProperties().setshowvalue(gp.getBoolValue("edge.text_showvalue"));
      ArrowEdge.getTextProperties().setVisible(gp.getBoolValue("edge.text_visible"));

      ArrowEdge.getLinestyle().setType(gp.getIntValue("edge.line_type"));
      ArrowEdge.getLinestyle().setLength(gp.getIntValue("edge.line_length"));
      ArrowEdge.getLinestyle().setDistance(gp.getIntValue("edge.line_distance"));
    }
    ArrowG.repaint();
    ArrowG.validate();
    this.validate();
  }

  @SuppressWarnings("unchecked")
  public void update(Observable arg0, Object arg1) {
    try {
      VEdge e = (VEdge) arg1;
      if (e != null) {
        InitValues(e);
      }
    } catch (Exception e) {
    }

    try {
      Vector<Integer> v = (Vector<Integer>) arg1;

      if (v != null) {
        if (v.firstElement() == CEdgeArrowParameters.CEDGEARROWPARAMETERS) {
          ArrowEdge.getArrow().setSize(v.get(CEdgeArrowParameters.ARROW_SIZE));
          ArrowEdge.getArrow().setAngle(v.get(CEdgeArrowParameters.ARROW_ANGLE));
          ArrowEdge.getArrow().setPos(v.get(CEdgeArrowParameters.ARROW_POS) / 100.0f);
          ArrowEdge.getArrow().setPart(v.get(CEdgeArrowParameters.ARROW_PART) / 100.0f);
          ArrowG.repaint();

        }
        if (v.firstElement() == CEdgeTextParameters.CEDGETEXTPARAMETERS) {
          VEdgeText m = new VEdgeText(v.get(CEdgeTextParameters.TEXTDISTANCE),
              (float) v.get(CEdgeTextParameters.TEXTPOSITION) / 100f,
              v.get(CEdgeTextParameters.TEXTSIZE),
              v.get(CEdgeTextParameters.BTEXTVISIBLE) == 1,
              v.get(CEdgeTextParameters.BSHOWTEXTVALUE) == 1);
          ArrowEdge.setTextProperties(m);
          ArrowG.repaint();
        }
        if (v.firstElement() == CEdgeLineParameters.CEDGELINEPARAMETERS) {
          VEdgeLinestyle l = new VEdgeLinestyle(v.get(CEdgeLineParameters.LINETYPE) + 1,
              v.get(CEdgeLineParameters.LINELENGTH),
              v.get(CEdgeLineParameters.LINEDISTANCE));
          ArrowEdge.setLinestyle(l);
          ArrowG.repaint();
        }
      }
    } catch (Exception e) {
    }

    ArrowG.repaint();
  }

}
