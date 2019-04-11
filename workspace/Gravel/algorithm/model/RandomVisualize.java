package algorithm.model;

import io.GeneralPreferences;
import model.*;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class RandomVisualize implements VAlgorithmIF {

  //UrsprungsGraph
  MGraph mG;
  Iterator<MNode> nodeiter;
  //Parameter
  private int maxX, maxY;
  private boolean randNodes, randEdges;
  //Entstehender Graph
  VGraph ErgebnisGraph;
  int stepsize;
  Random generator;

  public boolean GraphOkay() {
    return true;
  }

  public VGraph getactualState() {
    return ErgebnisGraph;
  }

  public void run() {
  }

  public boolean isStepwiseRunable() {
    return true;
  }

  public String setParameters(HashMap<String, Object> m) {
    if (m == null)
      return "";
    if (m.get("MGraph") == null)
      return "Kein Graph angegeben";
    mG = (MGraph) m.get("MGraph");
    if (m.get("MaxX") == null)
      return "kein MaxX angegeben";
    if (m.get("MaxY") == null)
      return "kein MaxY angegeben";
    if (m.get("RandomizeEdges") == null)
      randEdges = true;
    else
      randEdges = (Boolean) m.get("RandomizeEdges");
    if (m.get("RandomizeNodes") == null)
      randNodes = true;
    else
      randNodes = (Boolean) m.get("RandomizeEdges");
    maxX = (Integer) m.get("MaxX");
    maxY = (Integer) m.get("MaxY");
    if ((maxX <= 0) && (maxY <= 0))
      return "Einer der Max-Werte ist zu klein";
    return "";
  }

  public void start() {
    ErgebnisGraph = new VGraph(mG.isDirected(), false, false);
    nodeiter = mG.modifyNodes.getIterator();
    generator = new Random();
    stepsize = 1;
  }

  public void step() {
    for (int i = 0; i < stepsize; i++) {
      if (nodeiter.hasNext()) {
        MNode actualNode = nodeiter.next();
        int posx = generator.nextInt(maxX);
        int posy = generator.nextInt(maxY);
        //Place the node
        int size;
        if (randNodes) {
          size = GeneralPreferences.getInstance().getIntValue("node.size") - 5 + generator.nextInt(11);
          if (size < 1)
            size = 1;
        } else
          size = GeneralPreferences.getInstance().getIntValue("node.size");

        ErgebnisGraph.modifyNodes.add(new VNode(actualNode.index, posx, posy, size, 35, 0, 14, false), new MNode(actualNode.index, actualNode.name));
        Iterator<MEdge> edgeiter = mG.modifyEdges.getIterator();
        while (edgeiter.hasNext()) {
          MEdge e = edgeiter.next();
          if ((ErgebnisGraph.modifyNodes.get(e.StartIndex) != null) && (ErgebnisGraph.modifyNodes.get(e.EndIndex) != null) && (ErgebnisGraph.modifyEdges.get(e.index) == null)) { //Knoten sind drin aber die Kante noch nicht
            int width;
            if (randEdges) {
              width = GeneralPreferences.getInstance().getIntValue("edge.width") - 5 + generator.nextInt(11);
              if (width < 1)
                width = 1;
            } else
              width = GeneralPreferences.getInstance().getIntValue("edge.width");
            ErgebnisGraph.modifyEdges.add(
                new VStraightLineEdge(e.index, width),
                e,
                ErgebnisGraph.modifyNodes.get(e.StartIndex).getPosition(),
                ErgebnisGraph.modifyNodes.get(e.EndIndex).getPosition());
          }
        }
        ErgebnisGraph.pushNotify(new GraphMessage(GraphConstraints.EDGE | GraphConstraints.NODE, GraphConstraints.UPDATE));
      } else
        return;
    }
  }

  public boolean finished() {
    return (!nodeiter.hasNext()) && (generator != null);
  }
}
