/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.def.ConnectionEndpointType;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.figures.DiagramConnectionFigure;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionLabelModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.policies.DiagramConnectionBendpointEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.policies.DiagramConnectionEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.policies.DiagramConnectionEndpointEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.policies.DiagramConnectionLabelEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramConnectionPresentation;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionEditPart extends AbstractConnectionEditPart 
		implements PropertyChangeListener, IConfigurationManagerHolder {
	
	private List<DiagramConnectionLabelModel> modelChildren;
	private DiagramConfigurationManager configManager;
	
	public DiagramConnectionEditPart(DiagramConfigurationManager configManager) {
		this.configManager = configManager;
	}
	
	public DiagramConfigurationManager getConfigurationManager() {
		return this.configManager;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new DiagramConnectionEndpointEditPolicy(getCastedModel().getDiagramModel().getResourceCache()));
		installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new DiagramConnectionBendpointEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new DiagramConnectionLayoutEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DiagramConnectionEditPolicy());
	}

	@Override
	protected IFigure createFigure() {
		PolylineConnection connection = new DiagramConnectionFigure();
		IDiagramConnectionDef def = getCastedModel().getModelPart().getConnectionDef();
		
		ConnectionEndpointType type1 = ConnectionEndpointType.NONE;
		if (def.getEndpoint1() != null) {
			type1 = def.getEndpoint1().getType().content();
		}
		if (type1.equals(ConnectionEndpointType.ARROW)) {
			connection.setSourceDecoration(new PolygonDecoration());
		}
		
		ConnectionEndpointType type2 = ConnectionEndpointType.NONE;
		if (def.getEndpoint2() != null) {
			type2 = def.getEndpoint2().getType().content();
		}
		if (type2.equals(ConnectionEndpointType.ARROW)) {
			connection.setTargetDecoration(new PolygonDecoration());
		}

		updateStyle(connection);
		return connection;
	}
	
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			getCastedModel().addPropertyChangeListener(this);
		}
	}

	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getCastedModel().removePropertyChangeListener(this);
		}
	}
	
	public DiagramConnectionModel getCastedModel() {
		return (DiagramConnectionModel)getModel();
	}
	
	public DiagramConnectionPresentation getPresentation()
	{
		return getCastedModel().getPresentation();
	}
	
	public void updateStyle(PolylineConnection connection) {
		DiagramResourceCache resourceCache = getCastedModel().getDiagramModel().getResourceCache();
		DiagramConnectionPart connectionPart = getCastedModel().getModelPart();
		IDiagramConnectionDef def = connectionPart.getConnectionDef();
		connection.setLineStyle(resourceCache.getLinkStyle(def));
		connection.setLineWidth(def.getLineWidth().content());
		connection.setForegroundColor(resourceCache.getLineColor(connectionPart));
	}
	
	private void refreshBendpoints() {
		DiagramConnectionPart connectionPart = getCastedModel().getModelPart();

		List<AbsoluteBendpoint> figureConstraint = new ArrayList<AbsoluteBendpoint>();
		for (Point point : connectionPart.getBendpoints()) {
			AbsoluteBendpoint bendpoint = new AbsoluteBendpoint(point.getX(), point.getY());
			figureConstraint.add(bendpoint);
		}
		getConnectionFigure().setRoutingConstraint(figureConstraint);
	}
	
	@Override
	protected void refreshVisuals() {
		refreshBendpoints();
	}

	@Override
	protected List<DiagramConnectionLabelModel> getModelChildren() {
		if (modelChildren == null) {
			// add the label
			modelChildren = new ArrayList<DiagramConnectionLabelModel>(1);
			if (getCastedModel().getModelPart().canEditLabel()) {
				modelChildren.add(new DiagramConnectionLabelModel(getCastedModel()));
			}
		}
		return modelChildren;
	}
	
	private void updateLabel() {
		for (DiagramConnectionLabelModel child : getModelChildren()) {
			child.handleUpdateLabel();
		}
	}
	
	private void updateLabelMoved() {
		for (DiagramConnectionLabelModel child : getModelChildren()) {
			child.handleUpdateLabelMoved();
		}
	}

	private void startEditing() {
		for (DiagramConnectionLabelModel child : getModelChildren()) {
			child.handleStartEditing();
		}
	}

	
	private static final class DiagramConnectionLayoutEditPolicy extends LayoutEditPolicy {

		@Override
		protected EditPolicy createChildEditPolicy(EditPart child) {
			if (child instanceof DiagramConnectionLabelEditPart) {
				return new DiagramConnectionLabelEditPolicy();
			}
			return new NonResizableEditPolicy();
		}

		@Override
		protected Command getCreateCommand(CreateRequest request) {
			return null;
		}

		@Override
		protected Command getMoveChildrenCommand(Request request) {
			return null;
		}

		@Override
		protected Command getAddCommand(Request request) {
			return super.getAddCommand(request);
		}
		
	}


	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (DiagramConnectionModel.CONNECTION_UPDATES.equals(prop)) {
			updateLabel();
		} else if (DiagramConnectionModel.CONNECTION_LABEL_MOVED.equals(prop)) {
			updateLabelMoved();
		} else if (DiagramConnectionModel.CONNECTION_BEND_POINTS.equals(prop)) {
			refreshVisuals();
		} else if (DiagramConnectionModel.CONNECTION_START_EDITING.equals(prop)) {
			startEditing();
		}
	}
	
}
