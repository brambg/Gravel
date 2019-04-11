package model;

import model.Messages.GraphConstraints;

public interface MGraphInterface {

  int GRAPH = GraphConstraints.MATH | GraphConstraints.GRAPH;
  int HYPERGRAPH = GraphConstraints.MATH | GraphConstraints.HYPERGRAPH;

  //Implementing Methods
  MNodeSet modifyNodes = null;
  MSubgraphSet modifySubgraphs = null;

  /**
   * get the Type of the
   *
   * @return
   */
  int getType();
}
