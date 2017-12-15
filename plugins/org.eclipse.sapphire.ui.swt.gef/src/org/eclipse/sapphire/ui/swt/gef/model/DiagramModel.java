/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramEmbeddedConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramImplicitConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramImplicitConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DiagramModel extends DiagramModelBase {
	
	public final static String NODE_ADDED = "NODE_ADDED";
	public final static String NODE_REMOVED = "NODE_REMOVED";

	private SapphireDiagramEditorPagePart part;
	private DiagramConfigurationManager configManager;
	private List<DiagramNodeModel> nodes = new ArrayList<DiagramNodeModel>();
	private List<DiagramConnectionModel> connections = new ArrayList<DiagramConnectionModel>();
	
	private DiagramResourceCache resourceCache;

	public DiagramModel(SapphireDiagramEditorPagePart part, DiagramConfigurationManager configManager) {
		this.part = part;
		this.configManager = configManager;
		this.resourceCache = new DiagramResourceCache();

		contructNodes();
		constructConnections();
	}
	
	public SapphirePart getSapphirePart() {
		return getModelPart();
	}

	public SapphireDiagramEditorPagePart getModelPart() {
		return this.part;
	}
	
	public DiagramResourceCache getResourceCache() {
		return resourceCache;
	}

	public List<DiagramNodeModel> getNodes() {
		return nodes;
	}
	
	public List<DiagramConnectionModel> getConnections() {
		return connections;
	}
	
	public DiagramNodeModel getDiagramNodeModel(DiagramNodePart nodePart) {
		for (DiagramNodeModel nodeModel : nodes) {
			if (nodeModel.getModelPart() == nodePart) {
				return nodeModel;
			}
		}
		return null;
	}
	
	public DiagramConnectionModel getDiagramConnectionModel(DiagramConnectionPart connectionPart) {
		for (DiagramConnectionModel connectionModel : connections) {
			if (connectionModel.getModelPart() == connectionPart) {
				return connectionModel;
			}
		}
		return null;
	}
	
	public void handleAddNode(DiagramNodePart nodePart) {
		DiagramNodeModel nodeModel = new DiagramNodeModel(this, nodePart);
		
		Bounds bounds = nodePart.getNodeBounds();
		if (bounds.getX() < 0 && bounds.getY() < 0) {
			org.eclipse.sapphire.ui.Point position = getDefaultPosition();
			nodePart.setNodeBounds(position.getX(), position.getY(), false, true);
		}
		
		nodes.add(nodeModel);
		firePropertyChange(NODE_ADDED, null, nodeModel);
		
	}
	
	public void handleDirectEditing(DiagramNodePart nodePart) {
		DiagramNodeModel nodeModel = getDiagramNodeModel(nodePart);
		if (nodeModel != null) {
			nodeModel.handleStartEditing();
		}
	}

	public void handleDirectEditing(DiagramConnectionPart connectionPart) {
		DiagramConnectionModel connectionModel = getDiagramConnectionModel(connectionPart);
		if (connectionModel != null) {
			connectionModel.handleStartEditing();
		}
	}

	public void handleRemoveNode(DiagramNodePart nodePart) {
		DiagramNodeModel nodeModel = getDiagramNodeModel(nodePart);
		if (nodeModel != null) {
			// first remove all connections, if they still exist
			List<DiagramConnectionModel> tobeRemoved = new ArrayList<DiagramConnectionModel>();
			for (DiagramConnectionModel connectionModel : connections) {
				if (connectionModel.getSourceNode().equals(nodeModel) || connectionModel.getTargetNode().equals(nodeModel)) {
					tobeRemoved.add(connectionModel);
				}
			}
			for (DiagramConnectionModel connectionModel : tobeRemoved) {
				removeConnection(connectionModel);
			}
			
			// next remove the node
			nodes.remove(nodeModel);
			firePropertyChange(NODE_REMOVED, null, nodePart);
		}
	}

	private void contructNodes() {
		for (DiagramNodeTemplate nodeTemplate : getModelPart().getNodeTemplates()) {
			if (nodeTemplate.visible()) {
				for (DiagramNodePart nodePart : nodeTemplate.getDiagramNodes()) {
					nodes.add(new DiagramNodeModel(this, nodePart));
				}
			}
		}
	}
	
	private void constructConnections() {
		// add the top level connections back to the diagram
		for (DiagramConnectionTemplate connTemplate : this.part.getConnectionTemplates()) {
			for (DiagramConnectionPart connPart : connTemplate.getDiagramConnections(null)) {
				addConnection(connPart);
			}
		}

		// Add embedded connections. This needs to be done after all the nodes
		// have been added.
		for (DiagramNodeTemplate nodeTemplate : this.part.getNodeTemplates()) {
			// Bug 381795 - Connections do not show correctly when editor is started with connections hidden
			if (nodeTemplate.visible()) {
				DiagramEmbeddedConnectionTemplate embeddedConnTemplate = nodeTemplate.getEmbeddedConnectionTemplate();
				if (embeddedConnTemplate != null) {
					for (DiagramConnectionPart connPart : embeddedConnTemplate.getDiagramConnections(null)) {
						addConnection(connPart);
					}
				}
			}
		}

		// Add Implicit connections
		for (DiagramImplicitConnectionTemplate implicitConnTemplate : this.part.getImplicitConnectionTemplates()) {
			for (DiagramImplicitConnectionPart implicitConn : implicitConnTemplate.getImplicitConnections()) {
				addConnection(implicitConn);
			}
		}
	}
	
	public void addConnection(DiagramConnectionPart connPart) {
		if (getDiagramConnectionModel(connPart) != null) {
			return;
		}
		
		IModelElement endpoint1 = connPart.getEndpoint1();
		IModelElement endpoint2 = connPart.getEndpoint2();
		DiagramNodePart nodePart1 = this.part.getDiagramNodePart(endpoint1);
		DiagramNodePart nodePart2 = this.part.getDiagramNodePart(endpoint2);
		if (nodePart1 != null && nodePart2 != null) {
			DiagramConnectionModel connectionModel = new DiagramConnectionModel(this, connPart);
			connections.add(connectionModel);

			DiagramNodeModel sourceNode = getDiagramNodeModel(nodePart1);
			DiagramNodeModel targetNode = getDiagramNodeModel(nodePart2);
			if (sourceNode != null && targetNode != null) {
				sourceNode.addSourceConnection(connectionModel);
				targetNode.addTargetConnection(connectionModel);
				
				connectionModel.setSourceNode(sourceNode);
				connectionModel.setTargetNode(targetNode);

				// add bendpoint if collision if the connection part doesn't have any bend points.
				// But we still route the connection for the purpose of calculating next "fan position" correctly.
				// See Bug 374793 - Additional bend points added to connection when visible-when condition on nodes change
								
				Point bendPoint = this.configManager.getConnectionRouter().route(connectionModel);
	        	if (bendPoint != null && connPart.getConnectionBendpoints().isEmpty()) 
	        	{
	        		List<org.eclipse.sapphire.ui.Point> bendPoints = new ArrayList<org.eclipse.sapphire.ui.Point>(1);
	        		bendPoints.add(new org.eclipse.sapphire.ui.Point(bendPoint.x, bendPoint.y));
        			connectionModel.getModelPart().resetBendpoints(bendPoints, false, true);
	        	}
			}
		}
	}
	
	public void removeConnection(DiagramConnectionPart connPart) {
		DiagramConnectionModel connectionModel = getDiagramConnectionModel(connPart);
		if (connectionModel != null) {
			removeConnection(connectionModel);
			// Shenxue There is only one case when removing bend points of the connection part is necessary:
			// When creating new connection, connection template receives property change event on the connection
			// list property and creates a new connection part. The newly created connection part listens on the endpoint
			// property change event and broadcasts the connection end point event. SapphireDiagramEditor listens on that
			// event and remove and recreate connection edit part upon such event.
			// SapphireDiagramEditor's updateConnectionEndpoint() has been modified to detect a true end point change
			// before recreates connection edit part. 
			// For other cases, this method is called on connection part that'll be disposed. So we don't need to remove 
			// its bend points.
			//connPart.removeAllBendpoints();
		}
	}
	
	private void removeConnection(DiagramConnectionModel connectionModel) {
		this.configManager.getConnectionRouter().removeConnectionFromCache(connectionModel);

		DiagramNodeModel sourceNode = connectionModel.getSourceNode();
		DiagramNodeModel targetNode = connectionModel.getTargetNode();
		if (sourceNode != null && targetNode != null) {
			sourceNode.removeSourceConnection(connectionModel);
			targetNode.removeTargetConnection(connectionModel);
			
			connectionModel.setSourceNode(null);
			connectionModel.setTargetNode(null);
		}
		connections.remove(connectionModel);
	}
	
	private org.eclipse.sapphire.ui.Point getDefaultPosition() {
		int x = 10;
		int y = -1;
		for (DiagramNodeModel nodeModel : nodes) {
			Bounds bounds = nodeModel.getNodeBounds();
			int height = Math.max(bounds.getHeight(), DiagramNodeModel.DEFAULT_NODE_HEIGHT);
			height += bounds.getY() + 10; 
			y = Math.max(y,  height);
		}
		return new org.eclipse.sapphire.ui.Point(x, y);
	}
	
	public SapphireDiagramEditor getSapphireDiagramEditor() {
		return this.configManager.getDiagramEditor();
	}

	public void dispose() {
		resourceCache.dispose();
	}
	
}
