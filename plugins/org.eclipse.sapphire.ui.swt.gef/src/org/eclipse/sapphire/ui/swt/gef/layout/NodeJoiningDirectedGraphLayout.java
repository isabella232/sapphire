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

import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

/**
 * Extended version of DirectedGraphLayout which joins the isolated nodes by
 * creating dummy edges between them. The purpose of it is to make horizontal layout
 * appear "horizontal" and vertical layout appear "vertical" for isolated nodes
 */

public class NodeJoiningDirectedGraphLayout extends DirectedGraphLayout
{
	public void visit(DirectedGraph graph)
	{				
		//add dummy edges so that graph does not fall over because some nodes
		// are not in relationships
		new DummyEdgeCreator().visit(graph);
				
		super.visit(graph);
	}
}
