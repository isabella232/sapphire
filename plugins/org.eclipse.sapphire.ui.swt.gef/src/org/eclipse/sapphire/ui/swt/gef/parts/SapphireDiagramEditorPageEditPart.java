/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Turn on anti-aliasing on diagram connection layer;
 *                   Move DiagramXYLayoutEditPolicy inner class to its own file.
 *                   SnapToHelper Adapter and Snap feedback policy.
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.policies.DiagramXYLayoutEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.policies.SapphireSnapFeedbackPolicy;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPagePresentation;
import org.eclipse.swt.SWT;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireDiagramEditorPageEditPart extends AbstractGraphicalEditPart 
		implements PropertyChangeListener, IConfigurationManagerHolder {

	private DiagramConfigurationManager configManager;
	
	public SapphireDiagramEditorPageEditPart(DiagramConfigurationManager configManager) {
		this.configManager = configManager;
	}
	
	public DiagramConfigurationManager getConfigurationManager() {
		return this.configManager;
	}

	public DiagramPagePresentation getPresentation()
	{
		if (getModel() instanceof DiagramModel) {
			DiagramModel diagramModel = (DiagramModel)getModel();
			return diagramModel.getPresentation();
		}
		return null;
	}

	@Override
	protected IFigure createFigure() 
	{
		getPresentation().render();
		IFigure f = getPresentation().getFigure();
 
		// Create the static router for the connection layer
		ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));
		connLayer.setAntialias(SWT.ON);

		return f;
	}
	
	@Override
	protected void createEditPolicies() {
		// disallows the removal of this edit part from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());

		// handles constraint changes (e.g. moving and/or resizing) of model
		// elements and creation of new model elements
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new DiagramXYLayoutEditPolicy(getCastedModel().getPresentation()));
		installEditPolicy("Snap Feedback", new SapphireSnapFeedbackPolicy(getPresentation().getResourceCache())); //$NON-NLS-1$
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

	private DiagramModel getCastedModel() {
		return (DiagramModel)getModel();
	}
	
	@Override
	protected List<DiagramNodeModel> getModelChildren() {
		return getCastedModel().getNodes();
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (DiagramModel.NODE_ADDED.equals(prop)) {
			refreshChildren();
		} else if (DiagramModel.NODE_REMOVED.equals(prop)) {
			refreshChildren();
		} 
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == SnapToHelper.class) {
			List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
			Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGuides(this));
			val = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGeometry(this));
			val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGrid(this));

			if (snapStrategies.size() == 0)
				return null;
			if (snapStrategies.size() == 1)
				return snapStrategies.get(0);

			SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
			for (int i = 0; i < snapStrategies.size(); i++)
				ss[i] = snapStrategies.get(i);
			return new CompoundSnapToHelper(ss);
		}
		return super.getAdapter(adapter);
	}
	
}
