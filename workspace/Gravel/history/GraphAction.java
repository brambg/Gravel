package history;

import java.util.Iterator;

import model.*;
/**
 * GraphAction represents one single action that can be performed to manipulate a VGraph.
 * Besides a standard action, that replaces a graph with a new one (because 
 * there were many changes), there are specific Actions to reduce memory and computation usage
 * 
 * The given Values are Actions that happened to the Graph:
 * - Replace, where the change is given in a new graph, node, edge or subset that is replaced
 * - create, where an Object is created in the graph (a node, edge or subset)
 * - delete, where an Object is deleted
 * 
 *  The Action specified is the action DONE to the graph.
 *  
 *  If no specific action fits, it is recommended to use REPLACE with a complete graph, this suits e.g.
 *  for the manipulation of a node, where the ID changes.
 * @author Ronny Bergmann
 */
public class GraphAction {

	//Acion that happened to the Graph
	public static final int UPDATE = 1;
	public static final int ADDITION = 2;
	public static final int REMOVAL = 4;
	
	//Encoding of the internal object used
	private static final int NODE = 1;
	private static final int EDGE = 2;
	private static final int SUBSET = 4;
	private static final int GRAPH = 128;
	
	private Object ActionObject;
	private MSubSet mathsubset;
	private int Objecttype;
	private int Action,StartNode=0, EndNode=0, Value=0;
	private String name;
	//Environement
	private VGraph env;
	/**
	 * Create a New Action with whole Graph
	 *  
	 * @param o VGraph
	 * @param action Action that happened
	 * @throws GraphActionException E.g. a Graph can not be Created or Deleted within a Graph
	 */
	public GraphAction(VGraph o, int action) throws GraphActionException
	{
		if (o==null)
			throw new GraphActionException("Could not Create Action: Graph must not be null.");
		ActionObject=o.clone();
		Action=action;
		if ((action&(ADDITION|REMOVAL))>0) //Create or delete is active
		{
			ActionObject=null;
			Action=0;
			throw new GraphActionException("Creating Graph or Deleting it is not possible as an Trackable Action.");
		}
		Objecttype=GRAPH;
	}
	/**
	 * Create New Action inducted by a Node
	 * @param o the node
	 * @param action the action
	 * @param environment Graph containing the Subsets the node belongs to and (at least) andjacent edges and their second nodes
	 * @throws GraphActionException
	 */
	public GraphAction(VNode o, int action, VGraph environment) throws GraphActionException
	{
		if ((o==null)||(environment==null))
			throw new GraphActionException("Could not Create Action: Node and environment must not be null.");
		if (environment.getNode(o.index)==null)
			throw new GraphActionException("Could not Create Action: Environment must contains at least the node itself.");			
		ActionObject = o;
		Action=action;
		name=environment.getNodeName(o.index);
		env = environment;
		Objecttype=NODE;
	}
	/**
	 * Create a New Action induced by a Subset
	 * @param o VSubSet manipulated
	 * @param action what was done?
	 * @param s name of Subset
	 * @param c Color.
	 * @throws GraphActionException
	 */
	public GraphAction(VSubSet o, int action, MSubSet m, String s) throws GraphActionException
	{
		if ((o==null)||(m==null))
			throw new GraphActionException("Could not Create Action: SubSet must not be null.");
		ActionObject = o.clone();
		mathsubset = m.clone();
		Action=action;
		name=s;
		Objecttype=SUBSET;
	}
	/**
	 * Create an Action for Manipulation of an Edge
	 * @param o The Visual Information of the Edge
	 * @param action Action Happening to it
	 * @param environment VGraph containins at least the Start- and Endnode and the Subsets the Edge belongs to
	 * @throws GraphActionException
	 */
	public GraphAction(VEdge o, int action, VGraph environment) throws GraphActionException
	{
		if ((o==null)||(environment==null))
			throw new GraphActionException("Could not Create Action: Edge and Environment must not be null.");
		if (environment.getEdge(o.index)==null)
			throw new GraphActionException("Could not Create Action: Environment must contain edge");
		ActionObject=o.clone();
		Action=action;
		name=environment.getEdgeName(o.index);
		StartNode=environment.getEdgeProperties(o.index).get(MGraph.EDGESTARTINDEX);
		EndNode=environment.getEdgeProperties(o.index).get(MGraph.EDGEENDINDEX);
		Value=environment.getEdgeProperties(o.index).get(MGraph.EDGEVALUE);
		env = environment;
		Objecttype=EDGE;
	}

	/**
	 * Exchange the Memberships of a node or edge to Subsets between two graphs
	 * The subsets of the first graph are iterated (its assumed they have the same subsets)
	 * and for every subset the membership of the item is exchanged.
	 * 
	 * @param ItemType NODE or EDGE
	 * @param itemindex index of the node or edge
	 * @param first first graph, where the subsets are iterated
	 * @param second second graph
	 * @throws GraphActionException
	 */
	private void exchangeSubSetMembership(int ItemType, int itemindex, VGraph first, VGraph second) throws GraphActionException
	{
		Iterator<VSubSet> si = first.getSubSetIterator();
		while (si.hasNext())
		{
			VSubSet s = si.next();
			if (ItemType==NODE)
			{
				boolean wasfirst = first.SubSetcontainsNode(itemindex, s.getIndex());
				boolean wassecond = second.SubSetcontainsNode(itemindex, s.getIndex());
				if (wasfirst)
					second.addNodetoSubSet(itemindex, s.getIndex());
				else
					second.removeNodefromSubSet(itemindex, s.getIndex());
				if (wassecond)
					first.addNodetoSubSet(itemindex, s.getIndex());
				else
					first.removeNodefromSubSet(itemindex, s.getIndex());
			}
			else if (ItemType==EDGE)
			{
				boolean wasfirst = first.SubSetcontainsEdge(itemindex, s.getIndex());
				boolean wassecond = second.SubSetcontainsEdge(itemindex, s.getIndex());
				if (wasfirst)
					second.addEdgetoSubSet(itemindex, s.getIndex());
				else
					second.removeEdgefromSubSet(itemindex, s.getIndex());
				if (wassecond)
					first.addEdgetoSubSet(itemindex, s.getIndex());
				else
					first.removeEdgefromSubSet(itemindex, s.getIndex());
			}
			else
				throw new GraphActionException("Unknown ItemType "+ItemType);
		}
	}
	/**
	 * Recreate the Subsets the edge belonged to, depending on the actual environment
	 * 
	 * @param e Edge 
	 * @param g Graph the edge should be recreated in and the colors should be restored in
	 */
	private void recreateEdgeColor(VEdge e, VGraph g) throws GraphActionException
	{
		Iterator<VSubSet> si =  env.getSubSetIterator();
		while (si.hasNext())
		{
			VSubSet s = si.next();
			if (env.SubSetcontainsEdge(e.index, s.getIndex()))
			{
				if (g.getSubSet(s.getIndex())==null)
					throw new GraphActionException("Can't replace edge, replacements belongs to Subsets, that don't exists in given parameter graph");
				g.addEdgetoSubSet(e.index, s.getIndex());
			}
		}
	}
	/**
	 * Replace this object in or with a Graph.
	 * 
	 * The replaced element is stored in the action part, so that another replace restores the first situation.
	 * 
	 */
	private VGraph doReplace(VGraph graph) throws GraphActionException
	{
		VGraph ret;
		switch(Objecttype)
		{
			case GRAPH: //Replace 
				ret = new VGraph(((VGraph)ActionObject).isDirected(), ((VGraph)ActionObject).isLoopAllowed(), ((VGraph)ActionObject).isMultipleAllowed());
				ret.replace((VGraph)ActionObject);
				((VGraph)ActionObject).replace(graph);
				graph.replace(ret);				
			break;
			case NODE:
				VNode n = (VNode)ActionObject;
				if (graph.getNode(n.index)==null) //node does not exists
					throw new GraphActionException("Can't replace node, none there.");
				ActionObject = graph.getNode(n.index).clone(); //save old node
				String tempnn = name;
				name = graph.getNodeName(n.index); //save old name
				graph.replaceNode(n,tempnn);
				env.replaceNode((VNode)ActionObject,name);
				exchangeSubSetMembership(NODE,n.index,env,graph);
				ret = graph; //return graph
			break;
			case EDGE:
				VEdge e = (VEdge)ActionObject;
				if (graph.getEdge(e.index)==null) //edge does not exists
					throw new GraphActionException("Can't replace edge, none there.");
				ActionObject = graph.getEdge(e.index).clone(); //save old edge
				int temps = StartNode;
				StartNode = graph.getEdgeProperties(e.index).get(MGraph.EDGESTARTINDEX);
				int tempe = EndNode;
				EndNode = graph.getEdgeProperties(e.index).get(MGraph.EDGEENDINDEX);
				int tempv = Value;
				Value = graph.getEdgeProperties(e.index).get(MGraph.EDGEVALUE);
				String tempn = name;
				name = graph.getEdgeName(e.index);
				graph.replaceEdge(e, temps, tempe, tempv, tempn);
				env.replaceEdge((VEdge)ActionObject, StartNode, EndNode, Value, name);
				exchangeSubSetMembership(EDGE,e.index,env,graph);
				ret = graph;
			break;
			case SUBSET:
				VSubSet newSubSet = (VSubSet)ActionObject;
				if (graph.getSubSet(newSubSet.getIndex())==null) //edge does not exists
					throw new GraphActionException("Can't replace subset, none there.");
				ActionObject = graph.getSubSet(newSubSet.getIndex()); //Old one
				String newname = name;
				name = graph.getSubSetName(newSubSet.getIndex()); //Old Name
				graph.removeSubSet(newSubSet.getIndex()); //Remove old SubSet.
				graph.addSubSet(newSubSet.getIndex(), newname, newSubSet.getColor());
				graph.pushNotify(new GraphMessage(GraphMessage.SUBSET,newSubSet.getIndex(),GraphMessage.UPDATE|GraphMessage.BLOCK_START,GraphMessage.ALL_ELEMENTS));
				Iterator<VNode> ni = graph.getNodeIterator();
				while (ni.hasNext())
				{
					VNode n2 = ni.next();
					if (mathsubset.containsNode(n2.index))
						graph.addNodetoSubSet(n2.index, newSubSet.getIndex());
				}
				Iterator<VEdge> ei = graph.getEdgeIterator();
				while (ei.hasNext())
				{
					VEdge e2 = ei.next();
					if (mathsubset.containsEdge(e2.index))
						graph.addEdgetoSubSet(e2.index, newSubSet.getIndex());
				}
				graph.pushNotify(new GraphMessage(GraphMessage.SUBSET,newSubSet.getIndex(),GraphMessage.BLOCK_END,GraphMessage.ALL_ELEMENTS));
				ret = graph;
				break;
			default: throw new GraphActionException("GraphAction::doReplace(); Unknown ActionObject");
		}
		return ret;
	}
	/**
	 * Perform a Create - after that the message is directly without manipulation its own undo
	 * @param graph
	 * @return
	 * @throws GraphActionException
	 */
	private VGraph doCreate(VGraph graph) throws GraphActionException
	{
		switch(Objecttype)
		{
			case NODE:
				VNode n = (VNode)ActionObject;	
				if (graph.getNode(n.index)!=null) //node exists
					throw new GraphActionException("Can't create node, already exists.");
				graph.addNode(n,name);
				//Recreate all Subsets
				Iterator<VSubSet> si = env.getSubSetIterator();
				while (si.hasNext())
				{
					VSubSet s = si.next();
					if (env.SubSetcontainsNode(n.index, s.getIndex()))
						graph.addNodetoSubSet(n.index, s.getIndex());
				}
				//Recreate adjacent edges and their subsets
				Iterator<VEdge> ei = env.getEdgeIterator();
				while (ei.hasNext())
				{
					VEdge e = ei.next();
					if ((env.getEdgeProperties(e.index).get(MGraph.EDGESTARTINDEX)==n.index)||(env.getEdgeProperties(e.index).get(MGraph.EDGEENDINDEX)==n.index))
					{
						graph.addEdge(e, env.getEdgeProperties(e.index).get(MGraph.EDGESTARTINDEX),
								env.getEdgeProperties(e.index).get(MGraph.EDGEENDINDEX),
								env.getEdgeProperties(e.index).get(MGraph.EDGEVALUE), env.getEdgeName(e.index));
						recreateEdgeColor(e,graph);
					}
				}
				break;
			case EDGE:
				VEdge e = (VEdge)ActionObject;
				if ((graph.getEdge(e.index)!=null)||(graph.getNode(StartNode)==null)||(graph.getNode(EndNode)==null)) //edge exists or one of its Nodes does not
					throw new GraphActionException("Can't create edge, it already exists or one of its Nodes does not.");
				graph.addEdge(e, StartNode, EndNode, Value, name);
				recreateEdgeColor(e,graph);				
				break;
			case SUBSET:
				VSubSet vs = (VSubSet)ActionObject;
				if ((graph.getSubSet(vs.getIndex())!=null)) //subset exists or one of its Nodes does not
					throw new GraphActionException("Can't create subset, it already exists or one of its Nodes does not.");
				graph.addSubSet(vs.getIndex(), name, vs.getColor());
				//Add Nodes and Edges again
				Iterator<VNode> nit = graph.getNodeIterator();
				while (nit.hasNext())
				{
					int nindex = nit.next().index;
					if (mathsubset.containsNode(nindex))
						graph.addNodetoSubSet(nindex,vs.getIndex());
				}
				Iterator<VEdge> eit = graph.getEdgeIterator();
				while (eit.hasNext())
				{
					int eindex = eit.next().index;
					if (mathsubset.containsEdge(eindex))
						graph.addEdgetoSubSet(eindex,vs.getIndex());
				}
				break;
		}
		return graph;
	}
	/**
	 * Perform a Delete
	 * The Action itself is not manipulated, because it is its own undo
	 * @param graph
	 * @return
	 * @throws GraphActionException
	 */
	private VGraph doDelete(VGraph graph) throws GraphActionException
	{
		switch(Objecttype)
		{
			case NODE:
				VNode n = (VNode)ActionObject;
				if (graph.getNode(n.index)==null) //node does not exists
					throw new GraphActionException("Can't delete node, none there.");
				graph.removeNode(n.index);
				break;
			case EDGE:
				VEdge e = (VEdge)ActionObject;
				if (graph.getEdge(e.index)==null) //edge does not exists
					throw new GraphActionException("Can't delete edge, none there.");
				graph.removeEdge(e.index);
				break;
			case SUBSET:
				VSubSet vs = (VSubSet)ActionObject;
				if (graph.getSubSet(vs.getIndex())==null) //subset does not exists
					throw new GraphActionException("Can't delete subset, none there.");
				graph.removeSubSet(vs.getIndex());
				break;
			}
		return graph;
	}	
	/**
	 * Apply this Action to a Graph. The graph given as argument is manipulated directly, though returned, too.
	 * The action itself is transofrmed to be its own undo-Message
	 * @param graph the graph to be manipulated
	 * @return the manipulated graph
	 */
	public VGraph redoAction(VGraph graph) throws GraphActionException
	{
		if (Action==0)
			throw new GraphActionException("No Action given");
		if (ActionObject==null)
			throw new GraphActionException("No Object available for the Action");
		switch(Action) 
		{
			case UPDATE: 
				return doReplace(graph);
			case ADDITION:
				return doCreate(graph);
			case REMOVAL:
				return doDelete(graph);
		}
		throw new GraphActionException("No Action given.");
	}
	/**
	 * Apply the action to the Clone of a Graph. The given Parameter Graph is not manipulated but cloned
	 * The Clone then gets manipulated and is returned
	 * @param g A Graph
	 * @return The Manipulated Clone
	 */
	public VGraph redoActionOnClone(VGraph g) throws GraphActionException
	{
		return redoAction(g.clone());
	}
	/**
	 * Undo The Action on a given Graph:
	 * - Created Elements are Deleted
	 * - Deleted Elements are Created again
	 * - Replaced Elements are Rereplaced again (because replace is its own undo)
	 * @param graph Graph the undo is performed in
	 * @return the same graph as the parameter, only the action is undone.
	 * @throws GraphActionException
	 */
	public VGraph UnDoAction(VGraph graph) throws GraphActionException
	{
		if (Action==0)
			throw new GraphActionException("No Action given");
		if (ActionObject==null)
			throw new GraphActionException("No Object available for the Action");
		switch(Action) 
		{
			case UPDATE:  //Undo a replace is repace itself
				return doReplace(graph);
			case ADDITION: //Undo a Create is a delete
				return doDelete(graph);
			case REMOVAL: //Undo Delete is Create
				return doCreate(graph);
		}
		throw new GraphActionException("No Action given.");

	}
	/**
	 * Undo The Action on the copy of the given graph
	 * does the same as UndoAction, but on a clone, so the parameter Graph given is unchanged
	 * @param g
	 * @return
	 * @throws GraphActionException
	 */
	public VGraph UnDoActionOnClone(VGraph g) throws GraphActionException
	{
		return UnDoAction(g.clone());
	}
	
	/**
	 * Return Type of Action.
	 * @return
	 */
	public int getActionType()
	{
		return Action;
	}
}