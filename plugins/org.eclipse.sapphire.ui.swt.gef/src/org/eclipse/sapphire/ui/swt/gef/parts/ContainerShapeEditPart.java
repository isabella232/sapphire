/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.sapphire.ui.diagram.editor.ContainerShapePart;
import org.eclipse.sapphire.ui.diagram.editor.ImagePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.figures.TextFigure;
import org.eclipse.sapphire.ui.swt.gef.internal.DirectEditorManagerFactory;
import org.eclipse.sapphire.ui.swt.gef.model.ContainerShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModelUtil;
import org.eclipse.sapphire.ui.swt.gef.policies.NodeLabelDirectEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.presentation.ContainerShapePresentation;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;
import org.eclipse.sapphire.ui.swt.gef.tools.SapphireDragEditPartsTracker;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ContainerShapeEditPart extends ShapeEditPart 
{	
	public ContainerShapeEditPart(DiagramConfigurationManager configManager) 
	{
    	super(configManager);    	
    }
	
	@Override
	protected void createEditPolicies() 
	{
		ContainerShapeModel model = getCastedModel();
		ContainerShapePart containerPart = (ContainerShapePart)model.getSapphirePart();
		// Create direct edit policy if it contains non-active sapphire text part. Active 
		// Sapphire text part has its own GEF editpart.
		if (containerPart.isEditable())
		{
			installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new NodeLabelDirectEditPolicy());
		}
	}
	
	@Override
	public boolean isSelectable() {
		ContainerShapeModel model = getCastedModel();
		if (model.getShapePresentation().part().isActive()) {
			return true;
		}
		return false;
	}

	@Override
	protected void addChildVisual(EditPart childEditPart, int index) 
	{
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		if (child == null)
			return;
		
		ShapeModel shapeModel = (ShapeModel)childEditPart.getModel();
		ShapePresentation shapePresentation = shapeModel.getShapePresentation();
		ContainerShapePresentation parentPresentation = getParentContainer(shapePresentation);
		IFigure parentFigure = parentPresentation.getFigure();
		Object layoutConstraint = ShapeUtil.getLayoutConstraint(shapePresentation, 
				parentPresentation.getLayout());
		// find the offset for figure in presentation without an editpart
		int offset = ShapeUtil.getPresentationCount(parentPresentation, shapePresentation);
		parentFigure.add(child, layoutConstraint, index + offset);
	}
	
	@Override
	protected void removeChildVisual(EditPart childEditPart) 
	{
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		if (child == null)
			return;
		
		ShapeModel shapeModel = (ShapeModel)childEditPart.getModel();
		ContainerShapePresentation parentPresentation = getParentContainer(shapeModel.getShapePresentation());
		IFigure parentFigure = parentPresentation.getFigure();
		parentFigure.remove(child);		
	}

	@Override
	protected List<ShapeModel> getModelChildren() 
	{
		List<ShapeModel> returnedModelChildren = new ArrayList<ShapeModel>();
		ContainerShapeModel containerModel = getCastedModel();
		returnedModelChildren.addAll(ShapeModelUtil.collectActiveChildrenRecursively(containerModel));
		return returnedModelChildren;
	}
	
	public ContainerShapeModel getCastedModel() 
	{
		return (ContainerShapeModel)getModel();
	}
	
	@Override
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
		else if (request.getType().equals(REQ_OPEN) && (request instanceof SelectionRequest))
		{
			SelectionRequest selRequest = (SelectionRequest)request;
			Point pt = selRequest.getLocation();
			TextPart textPart = getTextPart(pt);
			if (textPart != null)
			{
				performDirectEdit(textPart);
			}
			else
			{
				ImagePart imagePart = getImagePart(pt);
				if (imagePart != null)
				{
					invokeDoubleTapAction(imagePart);					
				}
			}
		}
		else
		{
			super.performRequest(request);
		}
	}
		
	private void performDirectEdit() 
	{
		List<TextPart> textParts = getContainedTextParts();
		if (!textParts.isEmpty())
		{
			performDirectEdit(textParts.get(0));
		}
	}
	
	private void performDirectEdit(TextPart textPart)
	{
		if (textPart.isEditable())
		{
			TextFigure textFigure = (TextFigure)getPartFigure(textPart);
			if (textFigure != null)
			{
				DirectEditManager manager = DirectEditorManagerFactory.createDirectEditorManager(this, textPart, 
						new NodeCellEditorLocator(getConfigurationManager(), textFigure), textFigure);
				manager.show();
			}
		}
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String prop = evt.getPropertyName();
		if (prop.equals(ShapeModel.SHAPE_START_EDITING))
		{
			if (evt.getNewValue() instanceof TextPart)
			{
				performDirectEdit((TextPart)evt.getNewValue());
			}
			else
			{
				performDirectEdit();
			}
		}
		else if (ContainerShapeModel.SHAPE_VISIBILITY_UPDATES.equals(prop)) 
		{
			Object obj = evt.getNewValue();
			if (obj instanceof ShapePart) 
			{
				ShapePart shapePart = (ShapePart)obj;
				ShapePresentation parentPresentation = getCastedModel().getShapePresentation();
				ShapePresentation shapePresentation = ShapeModelUtil.getChildShapePresentation(parentPresentation, shapePart);
				ShapeUtil.updateFigureForShape(shapePresentation, getCastedModel().getNodeModel().getDiagramModel().getResourceCache(),
						getConfigurationManager());
				
				refresh();
			}
		}		
	}

	@Override
	protected List<TextPart> getContainedTextParts()
	{
		ContainerShapePart containerPart = (ContainerShapePart)getCastedModel().getSapphirePart();
		return (ShapePart.getContainedShapeParts(containerPart, TextPart.class));
	}
	
	@Override
	protected List<ImagePart> getContainedImageParts()
	{
		ContainerShapePart containerPart = (ContainerShapePart)getCastedModel().getSapphirePart();
		return (ShapePart.getContainedShapeParts(containerPart, ImagePart.class));
	}


	@Override
	public DragTracker getDragTracker(Request request) {
		return new SapphireDragEditPartsTracker(this);
	}	
	
}
