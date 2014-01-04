/******************************************************************************
 * Copyright (c) 2014 SAP and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP - initial implementation
 *    Shenxue Zhou - adaptation for Sapphire and ongoing maintenance
 *    Gregory Amerson - [376200] Support floating palette around diagram node
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.contextbuttons;

import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToEmptyString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeEditPart;

/**
 * The context button manager shows and hides the context button pad. Mostly
 * showing/hiding the context button pad is triggered by mouse events.
 * 
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class ContextButtonManager {
	
	private static final String DIAGRAM_NODE_DEFAULT_ACTION = "Sapphire.Diagram.Node.Default";
	private static final String DIAGRAM_DELETE_ALL_BEND_POINTS = "Sapphire.Diagram.DeleteAllBendPoints";
	/**
	 * The context button pad is not shown, when the zoom level is below this
	 * minimum value.
	 */
	protected static final double MINIMUM_ZOOM_LEVEL = 0.75d;

	/**
	 * The editor on which this context button manager works, see
	 * {@link #getEditor()}. It is set in the constructor.
	 */
	private SapphireDiagramEditor editor;

	/**
	 * A backward-map from the edit-part figures to their edit-parts as
	 * described in {@link #getFigure2EditPart()}.
	 */
	private Map<IFigure, EditPart> figure2EditPart = new HashMap<IFigure, EditPart>();

	/**
	 * The currently active figure as described in
	 * {@link #getActiveContextButtonPad()}.
	 */
	private ContextButtonPad activeContextButtonPad;
	
	// ============================= listener =================================

	/**
	 * The zoom-listener is registered on the editor and calls
	 * {@link #handleZoomChanged()} on zoom level changes.
	 */
	private ZoomListener zoomListener = new ZoomListener() {
		public void zoomChanged(double newZoom) {
			handleZoomChanged();
		}
	};

	private FigureListener figureListener = new FigureListener()
	{
		public void figureMoved(IFigure source)
		{
			refresh();
		}
	};
	
	// ============================ constructor ===============================

	/**
	 * Creates a new ContextButtonManagerForPad.
	 * 
	 * @param editor
	 *            The editor on which this context button manager works, see
	 *            {@link #getEditor()}.
	 */
	public ContextButtonManager(SapphireDiagramEditor editor) {
		this.editor = editor;

		ZoomManager zoomMgr = (ZoomManager) getEditor().getGraphicalViewer().getProperty(ZoomManager.class.toString());
		if (zoomMgr != null) {
			zoomMgr.addZoomListener(zoomListener);
		}
	}

	// ====================== getter/setter for fields ========================

	/**
	 * Returns the editor this context button manager works on. It is set in the
	 * constructor and can not be changed.
	 * 
	 * @return The editor this context button manager works on.
	 */
	public SapphireDiagramEditor getEditor() {
		return editor;
	}

	/**
	 * Returns a backward-map from the edit-part figures to their edit-parts. So
	 * it delivers the opposite of GraphicalEditPart.getFigure(). This map is
	 * maintained in {@link #register(GraphicalEditPart)} and
	 * {@link #deRegister(GraphicalEditPart)}.
	 * 
	 * @return A backward-map from the edit-part figures to their edit-parts.
	 */
	private Map<IFigure, EditPart> getFigure2EditPart() {
		return figure2EditPart;
	}

	/**
	 * Returns the active context button pad as described in
	 * {@link #setActive(IFigure, ContextButtonPad)}.
	 * 
	 * @return The active context button pad as described in
	 *         {@link #setActive(IFigure, ContextButtonPad)}.
	 */
	private ContextButtonPad getActiveContextButtonPad() {
		return activeContextButtonPad;
	}
	
	private void setActiveContextButtonPad(ContextButtonPad contextButtonPad)
	{
		this.activeContextButtonPad = contextButtonPad;
	}

	// =================== interface IContextButtonManager ====================

	/**
	 * Registers a given edit-part. This means, that a context button pad will
	 * be shown for this edit-part when the mouse enters its figure. Typically
	 * this method is called, when an edit-part is activated.
	 */
	public void register(GraphicalEditPart graphicalEditPart) {
		getFigure2EditPart().put(graphicalEditPart.getFigure(), graphicalEditPart);

		graphicalEditPart.getFigure().addFigureListener(figureListener);
	}

	/**
	 * Deregisters a given edit-part, which is opposite to
	 * {@link #register(GraphicalEditPart)}. If a context-button pad is
	 * currently shown for this edit-part / figure, it is hidden first.
	 * Typically this method is called, when an edit-part is deactivated.
	 */
	public void deRegister(GraphicalEditPart graphicalEditPart) {
		getFigure2EditPart().remove(graphicalEditPart.getFigure());

		graphicalEditPart.getFigure().removeFigureListener(figureListener);
	}

	/**
	 * Hides the context button pad (if there is currently a context button pad
	 * active).
	 */
	public void hideContextButtonsInstantly() {
		if (getActiveContextButtonPad() != null) {
			synchronized (this) {
				ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) getEditor().getGraphicalViewer()
						.getRootEditPart();
				IFigure feedbackLayer = rootEditPart.getLayer(LayerConstants.HANDLE_LAYER);
				feedbackLayer.remove(getActiveContextButtonPad());
				setActiveContextButtonPad(null);
			}
		}
	}

	/**
	 * Is called when the zoom-level changes and hides the context buttons.
	 */
	private void handleZoomChanged() {
		//hideContextButtonsInstantly();
		refresh();
		// It would be possible to show a new context button pad, depending
		// on the new mouse location. But to avoid problems we skip this.
		// The scenario, that the zoom changes when context buttons are
		// visible is not so typical anyway.
	}
	
	/**
	 * Split the shape part actions into two sets: one set to be displayed along the 
	 * top edge, another set to be displayed along the right and bottom edge. Honor 
	 * actions groups when splitting actions.
	 * 
	 * @param nodeEditPart node edit part
	 * @return ContextButtonPadData in which the actions are splitted into two sets
	 */
	private ContextButtonPadData getContextButtonPad(List<GraphicalEditPart> editParts) 
	{
		ContextButtonPadData contextButtonPadData = new ContextButtonPadData();		
		DiagramNodeEditPart nodeEditPart = ((ShapeEditPart)editParts.get(0)).getNodeEditPart();
		org.eclipse.draw2d.geometry.Rectangle bounds = nodeEditPart.getFigure().getBounds();
		Point loc = bounds.getLocation();
		Point botRight = bounds.getBottomRight();
		contextButtonPadData.getPadLocation().set(loc.x, loc.y,
				botRight.x - loc.x, botRight.y - loc.y);
				
		SapphireActionGroup actionGroup = null;
		if (editParts.size() == 1)
		{
			ShapeEditPart shapeEditPart = (ShapeEditPart)editParts.get(0);
			if (shapeEditPart instanceof DiagramNodeEditPart)
			{
				DiagramNodePart nodePart = nodeEditPart.getCastedModel().getModelPart();
				actionGroup = nodePart.getActions(SapphireActionSystem.CONTEXT_DIAGRAM_NODE);
			}
			else 
			{
				ShapePart shapePart = (ShapePart)((ShapeModel)shapeEditPart.getModel()).getSapphirePart();
				actionGroup = shapePart.getActions(SapphireActionSystem.CONTEXT_DIAGRAM_NODE_SHAPE);
			}
		}
		else
		{
			SapphireDiagramEditorPagePart pagePart = getEditor().getPart();
			actionGroup = pagePart.getActions(SapphireActionSystem.CONTEXT_DIAGRAM_MULTIPLE_PARTS);
		}
		 
		List<SapphireAction> originalActions = actionGroup.getActions();		
		
		// Filter out the "default" action and actions without active handlers
		List<SapphireAction> actions = new ArrayList<SapphireAction>(originalActions.size());
		for (SapphireAction action : originalActions)
		{
			if (!(action.getId().equals(DIAGRAM_NODE_DEFAULT_ACTION) || action.getId().equals(DIAGRAM_DELETE_ALL_BEND_POINTS)) 
					&& action.getActiveHandlers().size() > 0)
			{
				actions.add(action);
			}
		}
		
		// Split actions into two sets according to their groups.
		
		int numOfActions = actions.size();
		int half = numOfActions / 2;

		final Map<String,List<SapphireAction>> buckets = new LinkedHashMap<String,List<SapphireAction>>();		
		for( SapphireAction action : actions )
        {
            final String group = normalizeToEmptyString( action.getGroup() );
            
            List<SapphireAction> bucket = buckets.get( group );
            
            if( bucket == null )
            {
                bucket = new ArrayList<SapphireAction>();
                buckets.put( group, bucket );
            }            
            bucket.add( action );
        }
		
		int numTopActions = 0;
		if (buckets.size() < 2)
		{
			numTopActions = half;
		}
		else 
		{	
			int i = 0;
			for( List<SapphireAction> bucket : buckets.values() )
			{
				numTopActions += bucket.size();
				if (buckets.size() == 2)
				{
					break;
				}
				if (numTopActions >= half || i == buckets.size() - 2)
				{
					break;
				}
				i++;
			}
			
		}
		// Add top actions in reverse order
		for (int i = numTopActions - 1; i >= 0; i--)
		{
			SapphireAction action = actions.get(i);

            contextButtonPadData.getTopContextButtons().add(action);
		}
		for (int i = numTopActions; i < numOfActions; i++)
		{
			SapphireAction action = actions.get(i);

		    contextButtonPadData.getRightContextButtons().add(action);
		}
		return contextButtonPadData;
	}

	public void refresh()
	{
		refreshInternal();
	}
	
	private void refreshInternal() 
	{
		hideContextButtonsInstantly();
		if (getEditor().isDirectEditingActive())
		{
			return;
		}
		List<ISapphirePart> selectedParts = this.getEditor().getSelectedParts();
		if (selectedParts.size() == 0)
		{
			return;			
		}
		Set<DiagramNodePart> selectedNodes = new HashSet<DiagramNodePart>();
		for (ISapphirePart part : selectedParts)
		{
			if (part instanceof DiagramConnectionPart || part instanceof SapphireDiagramEditorPagePart) 
			{
				// Don't display context pad if the selection includes connections
				return;
			}
			DiagramNodePart nodePart = part.nearest(DiagramNodePart.class);
			if (!(selectedNodes.contains(nodePart)))
			{
				selectedNodes.add(nodePart);
			}
		}
		// Don't display context pad if the selection includes multiple nodes
		if (selectedNodes.size() > 1)
		{
			return;
		}
		
		// determine zoom level
		ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) getEditor().getGraphicalViewer().getRootEditPart();
		double zoom = rootEditPart.getZoomManager().getZoom();
		if (zoom < MINIMUM_ZOOM_LEVEL) {
			return;
		}

		ContextButtonPadData contextButtonPadData = getContextButtonPad(getEditor().getSelectedEditParts());
		
		
		if (contextButtonPadData.getRightContextButtons().size() == 0
				&& contextButtonPadData.getTopContextButtons().size() == 0) {					
			return; // no context buttons to show
		}

		IContextButtonPadDeclaration declaration = new StandardContextButtonPadDeclaration(contextButtonPadData);

		// create context button pad and add to handle layer
		ContextButtonPad contextButtonPad = new ContextButtonPad(declaration, zoom, getEditor(), getEditor().getSelectedEditParts());
		setActiveContextButtonPad(contextButtonPad);
		IFigure feedbackLayer = rootEditPart.getLayer(LayerConstants.HANDLE_LAYER);
		feedbackLayer.add(contextButtonPad);
	}
	
}
