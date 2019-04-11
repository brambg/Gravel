package model;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import model.Messages.GraphColorMessage;
import model.Messages.GraphConstraints;
import model.Messages.GraphMessage;

/**
 * Class for handling all Modifications only depending on visual Nodes and
 * the Set of visual Nodes.
 * 
 * In a Graph Scenario all Depending Sets (e.g. VEdges) should subscribe as Observers
 * to handle the actions happening here
 * 
 * The Graph containing this Set should also subscribe to send specific messages to 
 * other Entities observing the Graph
 * 
 * Visual Node Set is based on an mathematical structure, e.g. MGraph or MHyperGraph
 * 
 * @author Ronny Bergmann
 * @since 0.4
 *
 */
public class VNodeSet extends Observable implements Observer {
	private TreeSet<VNode> vNodes;
	//Every Modifying Action should Lock.
	private Lock NodeLock;
	private MGraphInterface mG;
	private MNodeSet mnodes;
	public VNodeSet(MGraphInterface g)
	{
		mG = g;
		vNodes = new TreeSet<VNode>(new VItem.IndexComparator());
		NodeLock = new ReentrantLock();
		if (mG.getType()==MGraphInterface.GRAPH)
		{
			mnodes = ((MGraph)mG).modifyNodes;
		}
		else
		{
			mnodes = ((MHyperGraph)mG).modifyNodes;
		}
	}
	/**
	 * Set all Nodes to not selected. Does'nt notify anybody.
	 */
	public void deselect()
	{
    for (final VNode vNode : vNodes) {
      vNode.deselect();
    }
	}
	/**
	 * Remove all Nodes, that are selected from the set.
	 */
	public void removeSelection()
	{
		Iterator<VNode> n = vNodes.iterator();
		HashSet<VNode> selected = new HashSet<VNode>();
		while (n.hasNext()) {
			VNode node = n.next();
			if ((node.getSelectedStatus()&VItem.SELECTED)==VItem.SELECTED)
				selected.add(node);
		}
		n = selected.iterator();
		while (n.hasNext())
		{
			remove(n.next().getIndex());
		}
		setChanged();
		notifyObservers(
			new GraphMessage(GraphConstraints.SELECTION|GraphConstraints.NODE, //Changed
							GraphConstraints.REMOVAL, //Status 
							GraphConstraints.NODE|GraphConstraints.EDGE|GraphConstraints.SELECTION) //HyperEdges (are (visually) not affected		
		);		
	}
	/**
	 * Adds a new Node to the VGraph (and the corresponding mnode to the MGraph underneath)
	 * If one of the arguments is null nothing happens
	 * If a Node with the index of the VNode already exists, nothing happens
	 * @param node
	 *            the new VNode
	 * @param mnode
	 *            the depending mnode. If its index differs from the VNode its set to the VNodes index
	 */
	public void add(VNode node, MNode mnode) {
		if ((node==null)||(mnode==null))
			return;
		if (get(node.getIndex()) == null) {
			if (mnode.index!=node.getIndex())
				mnode.index = node.getIndex();
			mnodes.add(mnode);
			vNodes.add(node);
			setChanged();
			//Graph changed with an add, only nodes affected
			notifyObservers(new GraphMessage(GraphConstraints.NODE,node.getIndex(),GraphConstraints.ADDITION,GraphConstraints.NODE));	
		}
	}
	/**
	 * Get the Node with a given index
	 * @param i the index of the Node
	 * @return if a node with the given index, if it doesn't exist, it returns null
	 */
	public VNode get(int i) {
    for (final VNode temp : vNodes) {
      if (temp.getIndex() == i) {
        return temp;
      }
    }
		return null;
	}
	/**
	 * Change the index of a node
	 * each other value of a node can be changed by replaceNode
	 * but an index update requires an modification of adjacent edges
	 * @param oldi
	 * @param newi
	 */
	public void changeIndex(int oldi, int newi)
	{
		if (oldi==newi) //booring
			return;
		if ((get(oldi)==null)||(get(newi)!=null)) //old not or new already in use
			return;
		mnodes.changeIndex(oldi, newi);
		get(oldi).setIndex(newi);
		setChanged();
		notifyObservers(new GraphMessage(GraphConstraints.NODE,GraphConstraints.INDEXCHANGED, GraphConstraints.GRAPH_ALL_ELEMENTS));	
	}
	/**
	 * Removes a node and push an Message that should be handled by other
	 * depending Sets (e.g. edges) to handle this removal (e.g. by deleting all adjacent edges)
	 * if no node with the given index exists, nothing happens
	 * @param i index of the node
	 * 
	 * @see M(Hyper)Graph.modifyNodes.removeNode(int i)
	 */
	public void remove(int i)
	{
		if (get(i)==null)
			return;
		mnodes.remove(i);
		vNodes.remove(get(i));
		setChanged();
		notifyObservers(new GraphMessage(GraphConstraints.NODE,i,GraphConstraints.REMOVAL,GraphConstraints.GRAPH_ALL_ELEMENTS));	
	}
	/**
	 * Replace the node with index given by an copy of Node with the parameter,
	 * if a node with same index as parameter exists, else do nothing
	 * 
	 * Its SubgraphStuff is not changed
	 * @param node - Node that is exchanged into the graph (if a node exists with same index)
	 * @param mnode - mnode to change e.g. the name - if its index differs from the node-index, the node index is taken
	 * 
	 */
	public void replace(VNode node, MNode mnode)
	{
		if (mnode.index!=node.getIndex())
			mnode.index = node.getIndex();
		mnodes.replace(new MNode(node.getIndex(), mnode.name));

		node = node.clone(); //Clone node to lose color
		NodeLock.lock(); //Knoten finden
		try
		{
      for (final VNode t : vNodes) {
        if (t.getIndex() == node.getIndex()) {
          vNodes.remove(t);
          t.copyColorStatus(node);
          vNodes.add(node);
          setChanged();
          notifyObservers(new GraphMessage(GraphConstraints.NODE, node.getIndex(), GraphConstraints.REPLACEMENT, GraphConstraints.NODE));
          break;
        }
      }
		}
		finally {NodeLock.unlock();}
	}
	/**
	 * get the node in Range of a given point.
	 * a node is in Range, if the distance from the node-position to the point p is smaller than the nodesize
	 * @param p a point
	 * 
	 * @return the first node in range, if there is one, else null
	 */
	public VNode getFirstinRangeOf(Point p) {
    for (final VNode temp : vNodes) {
      if (temp.getPosition().distance(p) <= temp.getSize() / 2) {
        return temp;
      }
    }
		return null; // keinen gefunden
	}
	/**
	 * Check, whether at least one node is selected
	 * @return true, if there is at least one selected node, else false
	 */
	public boolean hasSelection() {
    for (final VNode vNode : vNodes) {
      if ((vNode.getSelectedStatus() & VItem.SELECTED) == VItem.SELECTED)
        return true;
    }
		return false;
	}
	/**
	 * Get the one and simple selected Node iff such an edge exists, in every other case null is returned
	 * @return
	 */
	public VNode getSingleSelectedNode()
	{
		Iterator<VNode> n = vNodes.iterator();
		NodeLock.lock();
		VNode result = null;
		try
		{
			while (n.hasNext())
			{
				VNode vn = n.next();
				if ((vn.getSelectedStatus()&VItem.SELECTED)==VItem.SELECTED)
				{
					if (result!=null) //This is the second selected Edge
						return new VNode(-1,-1,-1,-1,-1,-1,-1,false);
					else
						result = vn;
				}
			}
		}
		finally{NodeLock.unlock();}	
		return result;
	}
	/**
	 * Get a new Node Iterator
	 * 
	 * @return the Iterator typed with VNode
	 */
	public Iterator<VNode> getIterator()
	{
		return vNodes.iterator();
	}
	/**
	 * Handle internal Color Update. A Subgraph has changed the color of a node
	 * so change that
	 * @param m
	 */
	private void Colorchange(GraphColorMessage m)
	{
		if (m.getModifiedElement()!=GraphConstraints.NODE)
			return; //Does not affect us
		VNode n = get(m.getElementID());
		switch(m.getModificationType()) {
			case GraphConstraints.REMOVAL:
				n.removeColor(m.getColor());
				break;
			case GraphConstraints.UPDATE:
				n.removeColor(m.getOldColor()); //After this its equal to addition
			case GraphConstraints.ADDITION:
				n.addColor(m.getColor());
				break;
		}
	}
	public void update(Observable o, Object arg)
	{
		if (arg instanceof GraphColorMessage)
		{
			GraphColorMessage m = (GraphColorMessage)arg;
			if (m==null) {
			}
			else
				Colorchange(m);
			
		}
	}
	
}