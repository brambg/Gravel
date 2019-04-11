package algorithm.model;

import model.MEdge;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;
import model.VEdge;
import model.VGraph;
import model.VNode;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

public class MagnetismAndSprings implements VAlgorithmIF {
  private VGraph vg;
  //Factor of the length of an edge (multiplied with its value if given)
  private double edgelength;
  private double edgestrength = 10.0d; //srength of the spring
  private boolean useedgevalue = false;
  private double movepart = 0.01d;
  //Factor of the repuslion between two nodes (multiplied with the root of the size-product of both?)
  private double nodestrength = 10.0d;
  private int movement = 0;
  private boolean finished = false;

  //The Graph must be straightlined
  //Direction ist ignored
  public boolean GraphOkay() {
    Iterator<VEdge> edgeiterator = vg.modifyEdges.getIterator();
    while (edgeiterator.hasNext()) {
      if (edgeiterator.next().getEdgeType() != VEdge.STRAIGHTLINE) {
        return false;
      }
    }
    return true;
  }

  public boolean finished() {
    return finished;
  }

  public VGraph getactualState() {
    return vg;
  }

  public boolean isStepwiseRunable() {
    return true;
  }

  public void run() {
  }

  public String setParameters(HashMap<String, Object> m) {
    if (m == null)
      return "";
    if (m.get("VGraph") == null)
      return "Kein Graph angegeben";
    if (m.get("EdgeSizeFactor") == null)
      return "keine Kantengröße gegeben";
    if (m.get("EdgeValueUsed") == null)
      return "nicht angegeben, ob Kanten-Werte genutzt werden sollen";
    edgelength = (Double) m.get("EdgeSizeFactor");
    vg = (VGraph) m.get("VGraph");
    this.useedgevalue = (Boolean) m.get("EdgeValueUsed");
    if (edgelength <= 0.0d)
      return "Kantengröße zu gering";
    if (!GraphOkay())
      return "Der Graph enthält Kanten, die keine direkten geraden Kanten sind";
    return "";
  }

  public void start() {
  }

  public void step() {
    movement = 0;
    //Calulate for each node the Force indicated by all edges and nodes
    Iterator<VNode> mainiterator = vg.modifyNodes.getIterator();
    while (mainiterator.hasNext()) {
      VNode v = mainiterator.next(); //Actual Node to be moved
      double force_x = 0, force_y = 0; //Movement of the node
      Iterator<VEdge> edgeiterator = vg.modifyEdges.getIterator();
      while (edgeiterator.hasNext()) {
        VEdge e = edgeiterator.next();
        MEdge me = vg.getMathGraph().modifyEdges.get(e.getIndex());
        int start = me.StartIndex, ende = me.EndIndex;
        int uindex = 0;
        if (start == v.getIndex())
          uindex = ende;
        else if (ende == v.getIndex())
          uindex = start;
        if (uindex != 0) //Edge is connected with u and so a force is added
        {
          VNode u = vg.modifyNodes.get(uindex);
          double distance = v.getPosition().distance(u.getPosition());
          double elength = edgelength; //length the edge wishes to have
          if (useedgevalue)
            elength *= me.Value;
          force_x += edgestrength * (distance - elength) * (u.getPosition().x - v.getPosition().x) / distance;
          force_y += edgestrength * (distance - elength) * (u.getPosition().y - v.getPosition().y) / distance;
        }
      }
      Iterator<VNode> nodeiterator = vg.modifyNodes.getIterator();
      while (nodeiterator.hasNext()) {
        VNode u = nodeiterator.next();
        if (u.getIndex() != v.getIndex()) {
          double distance = v.getPosition().distance(u.getPosition());
          force_x += nodestrength / (distance * distance) * (u.getPosition().x - v.getPosition().x) / distance;
          force_y += nodestrength / (distance * distance) * (u.getPosition().y - v.getPosition().y) / distance;
        }
      }
      int x = v.getPosition().x + Math.round((new Double(force_x * movepart)).floatValue());
      int y = v.getPosition().y + Math.round((new Double(force_y * movepart)).floatValue());
      movement += Math.abs(Math.round((new Double(force_x * movepart)).floatValue())) + Math.abs(Math.round((new Double(force_y * movepart)).floatValue()));
      v.setPosition(new Point(x, y));
      vg.pushNotify(new GraphMessage(GraphConstraints.NODE | GraphConstraints.EDGE, GraphConstraints.UPDATE));
    } //End while
    //All Nodes moved by a part of its force
    if (movement == 0)
      finished = true;
  }
}
