/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.layout;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.internal.StandardEmbeddedConnectionPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ConnectionHashKey 
{
	private String nodeId, connectionId;

	private ConnectionHashKey(String nodeId, String connectionId) 
	{
		this.nodeId = nodeId;
		this.connectionId = connectionId;
	}

	public boolean equals(Object object) 
	{
		boolean isEqual = false;
		ConnectionHashKey hashKey;

		if (object instanceof ConnectionHashKey) 
		{
			hashKey = (ConnectionHashKey) object;
			String nodeId = hashKey.getNodeId();
			String connectionId = hashKey.getConnectionId();
			boolean nodeIdEqual = (nodeId == null && this.nodeId == null) ||
					(nodeId != null && this.nodeId != null && nodeId.equals(this.nodeId));
			boolean connIdEqual = connectionId.equals(this.connectionId);
			isEqual = nodeIdEqual && connIdEqual;
		}
		return isEqual;
	}

	public String getNodeId() 
	{
		return this.nodeId;
	}

	public String getConnectionId() 
	{
		return this.connectionId;
	}

	public int hashCode() 
	{
		if (this.nodeId != null)
		{
			return this.nodeId.hashCode() ^ this.connectionId.hashCode();
		}
		else
		{
			return this.connectionId.hashCode();
		}
	}
	
	public static ConnectionHashKey createKey(final DiagramConnectionPart connPart)
	{
		SapphireDiagramEditorPagePart diagramPart = connPart.nearest(SapphireDiagramEditorPagePart.class);
		String connId = connPart.getId();
		String nodeId = null;
		if (connPart instanceof StandardEmbeddedConnectionPart)
		{
			Element srcElement = connPart.getEndpoint1();
			DiagramNodePart nodePart = diagramPart.getDiagramNodePart(srcElement);
			nodeId = nodePart.getId();
		}
		ConnectionHashKey hashKey = new ConnectionHashKey(nodeId, connId);
		return hashKey;		
	}
	
	public static ConnectionHashKey createKey(final String nodeId, final String connId)
	{
		return new ConnectionHashKey(nodeId, connId);
	}
}

