/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.layout;

import java.util.Iterator;

import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;


/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

/**
 * Creates dummy edges between isolated nodes, to be used with NodeJoiningDirectedGraphLayout
 */

public class DummyEdgeCreator 
{
	private NodeList nodeList;
	private EdgeList edgeList;

	public void visit(DirectedGraph g)
	{
		init(g);
		setDummyEdges();
	}

	/**
	 * @param graph
	 */
	private void init(DirectedGraph graph)
	{
		this.nodeList = graph.nodes;
		this.edgeList = graph.edges;
	}

	protected void setDummyEdges()
	{

		int nodeCount = nodeList.size();


		//if node count is only one then we don't have to worry about whether
		// the nodes are connected
		if (nodeCount > 1)
		{
			NodeList candidateList = new NodeList();
			for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();)
			{
				Node sourceNode = iter.next();

				//we will need to set up a dummy relationship for any node not
				// in one already
				if (sourceNode.outgoing.size() == 0 && sourceNode.incoming.size() == 0)
				{
					candidateList.add(sourceNode);
					sourceNode.setRowConstraint(2);
				}
				else
				{
					sourceNode.setRowConstraint(1);
				}
			}
			if (candidateList.size() > 1)
			{
				int index = 0;
				while (index < candidateList.size() - 1)
				{
					Node sourceNode = candidateList.getNode(index++);
					Node targetNode = candidateList.getNode(index);
					Edge edge = newDummyEdge(targetNode, sourceNode);
				}
			}
		}
	}

	/**
	 * creates a new dummy edge to be used in the graph
	 */
	private Edge newDummyEdge(Node targetNode, Node sourceNode)
	{
		DummyEdgePart edgePart = new DummyEdgePart();
		Edge edge = new Edge(edgePart, sourceNode, targetNode);
		edge.weight = 1;
		this.edgeList.add(edge);
		return edge;
	}

}