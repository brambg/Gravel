package model.Messages;

/**
 * These Constraints are used through the whole system of Messaging for identification of the type of action
 * 
 * The Element modified is determined by the status values
 * 
 * The Type of action is determined by the Modification Values
 * which might be extended by information about single parts and Block-Information
 * 
 * So using the action stuff should be done the following way:
 * 
 * @author ronny
 *
 */
public interface GraphConstraints {

	/*
	 * Element Information for Indication of changed Elements
	 */
  int NODE = 1;
	int EDGE = 2;
	int HYPEREDGE = 4;
	int SUBGRAPH = 8;
	int GRAPH_ALL_ELEMENTS=NODE|EDGE|SUBGRAPH;
	int HYPERGRAPH_ALL_ELEMENTS=NODE|HYPEREDGE|SUBGRAPH;
	int SELECTION = 16;
	int DIRECTION = 32;
	int LOOPS = 64;
	int MULTIPLE = 128;
	int ELEMENT_MASK = 0xffff;

	/*
	 * Graph Indicators for the types of Graph - may be used in same Indicator as the Element Values
	 */
  int MATH = 2048;
	int VISUAL = 4096;
	int GRAPH = 1024;
	int HYPERGRAPH = 512;
	
	/*
	 * Modification Indicators, that cover all possible changes that might have happened 
	 */
  int UPDATE = 1;
	int ADDITION = 2;
	int REMOVAL = 4;
	int HISTORY = 8;
	int TRANSLATION = 16;
	int REPLACEMENT = 32;
	int INDEXCHANGED = 64;

	int ACTIONMASK = UPDATE | ADDITION | REMOVAL | HISTORY | TRANSLATION | REPLACEMENT | INDEXCHANGED;
	
	
	/*
	 * Additional Special Indicators for Partial information of the Modification, e.g. whether it was local or not 
	 */
  int HYPEREDGESHAPE = 128;
	int CREATION = 256; //Is there still just Interpolation Parameters ?
	int LOCAL = 512;	//Local Shape change if not given, the action is assumed to be shape-global

	int PARTINFORMATIONMASK = HYPEREDGESHAPE|LOCAL|CREATION;
	
	/*
	 * Additional Special information about block - which may be used to accummulate some actions into one
	 * The complete action should be indicated by the blockstart, so that any method can ignroe the block and handle the actions individually,
	 * though they are blocked
	 * 
	 */
  int BLOCK_START = 1024;
	int BLOCK_END = 2048;
	int BLOCK_ABORT = 4096;
	int BLOCKMASK = BLOCK_START|BLOCK_END|BLOCK_ABORT; //All 3 Block-Stati
}
