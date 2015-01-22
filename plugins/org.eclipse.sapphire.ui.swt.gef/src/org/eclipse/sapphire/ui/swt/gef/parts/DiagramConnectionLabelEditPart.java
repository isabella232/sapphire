/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [371909] Double-click on connection labels does not place 
 *                   label in direct edit mode
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionLabelModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.policies.ConnectionLabelDirectEditPolicy;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionLabelEditPart extends AbstractGraphicalEditPart 
		implements PropertyChangeListener, IConfigurationManagerHolder {
	
	private DiagramConfigurationManager configManager;
    private ConnectionDirectEditManager manager;

    public DiagramConnectionLabelEditPart(DiagramConfigurationManager configManager) {
    	this.configManager = configManager;
    }
    
    public DiagramConfigurationManager getConfigurationManager() {
    	return this.configManager;
    }
    
    @Override
	protected IFigure createFigure() {
    	Label label = new Label() {

			@Override
			public Insets getInsets() {
				return new Insets(0,2,0,2);
			}
    		
    	};
    	DiagramResourceCache resourceCache = getCastedModel().getDiagramModel().getResourceCache();
    	label.setFont(resourceCache.getDefaultFont());
		
    	// let text change color when the parent connection is selected 
//    	DiagramResourceCache resourceCache = getCastedModel().getDiagramModel().getResourceCache();
//		DiagramConnectionPart connectionPart = getCastedModel().getModelPart();
//		label.setForegroundColor(resourceCache.getLineColor(connectionPart));

		return label;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ConnectionLabelDirectEditPolicy());
	}

	public DiagramConnectionPart getDiagramConnectionPart() {
		return ((DiagramConnectionLabelModel)getModel()).getModelPart();
	}
	
	public DiagramConnectionLabelModel getCastedModel() {
		return (DiagramConnectionLabelModel)getModel();
	}

	@Override
	public EditPart getParent() {
		EditPart parent = super.getParent();
		if (parent instanceof DiagramConnectionEditPart) {
			DiagramConnectionEditPart connectionEditPart = (DiagramConnectionEditPart)parent;
			if (connectionEditPart.getSource() != null) {
				return connectionEditPart.getSource().getParent(); 
			}
			if (connectionEditPart.getTarget() != null) {
				connectionEditPart.getTarget().getParent();
			}
		}
		return parent;
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

	private void performDirectEdit() {
		if (manager == null) {
			Label label = (Label)getFigure();
			manager = new ConnectionDirectEditManager(this, TextCellEditor.class, new ConnectionEditorLocator(this, label), label);
		}
		manager.show();
	}

	public void performRequest(Request request) 
	{
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT)
		{
			if (!(request instanceof DirectEditRequest))
			{
				// Direct edit invoked using key command
				performDirectEdit();
			}
		}
		else if (request.getType().equals(REQ_OPEN))
		{
			performDirectEdit();
		}		
	}
	
	private void refreshLabel() {
		((Label)getFigure()).setText(getDiagramConnectionPart().getLabel());
	}
	
	private void refreshLabelLocation() {
		PolylineConnection parent = (PolylineConnection)getFigure().getParent(); 
		Point position = getDiagramConnectionPart().getLabelPosition();
		SapphireMidpointLocator locator = position == null ? 
						new SapphireMidpointLocator(getConfigurationManager(), parent) : 
						new SapphireMidpointLocator(getConfigurationManager(), parent, position.getX(), position.getY());
		parent.getLayoutManager().setConstraint(getFigure(), locator);
	}

	@Override
	public void refresh() {
		super.refresh();

		refreshLabel();
		refreshLabelLocation();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (DiagramConnectionLabelModel.CONNECTION_LABEL.equals(prop)) {
			refreshLabel();
		} else if (DiagramConnectionLabelModel.CONNECTION_LABEL_MOVED.equals(prop)) {
			refreshLabelLocation();
			getFigure().revalidate();
		} else if (DiagramConnectionLabelModel.CONNECTION_START_EDITING.equals(prop)) {
			performDirectEdit();
		}
	}

}
