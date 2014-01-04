/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.editor.ImagePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.figures.TextFigure;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModelUtil;
import org.eclipse.sapphire.ui.swt.gef.presentation.ContainerShapePresentation;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPresentation;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ShapeEditPart extends AbstractGraphicalEditPart 
			implements IConfigurationManagerHolder, PropertyChangeListener
{
	protected static final String DOUBLE_TAB_ACTION = "Sapphire.DoubleTap";
	
	private DiagramConfigurationManager configManager;

	public ShapeEditPart(DiagramConfigurationManager configManager) 
	{
    	this.configManager = configManager;
    }
    
    public DiagramConfigurationManager getConfigurationManager() 
    {
    	return this.configManager;
    }

	@Override
	public boolean isSelectable() {
		ShapePresentation shapePresentation = getShapePresentation();
		if (shapePresentation != null && shapePresentation.isSeparator()) {
			return false;
		}
		
		return super.isSelectable();
	}

	@Override
	public void activate() 
	{
		if (!isActive()) 
		{
			super.activate();
			if (getModel() instanceof ShapeModel)
			{
				ShapeModel shapeModel = (ShapeModel)getModel();
				//shapeModel.getNodeModel().addPropertyChangeListener(this);
				shapeModel.addPropertyChangeListener(this);
			}
		}
	}
    
	@Override
	public void deactivate() 
	{
		if (isActive()) 
		{
			if (getModel() instanceof ShapeModel)
			{
				ShapeModel shapeModel = (ShapeModel)getModel();
				//shapeModel.getNodeModel().removePropertyChangeListener(this);
				shapeModel.removePropertyChangeListener(this);
				super.deactivate();
			}
		}
	}
	
	@Override
	protected IFigure createFigure() 
	{
		ShapeModel shapeModel = (ShapeModel)getModel();
		ShapePresentation shapePresentation = shapeModel.getShapePresentation();
		shapePresentation.render();
//		IFigure fig = ShapeUtil.createFigureForShape(shapePresentation,  
//					nodeEditPart.getCastedModel().getDiagramModel().getResourceCache(), getConfigurationManager());
		return shapePresentation.getFigure();
	}

	@Override
	protected void createEditPolicies() 
	{
		// TODO Auto-generated method stub
	}

	public void propertyChange(PropertyChangeEvent evt) 
	{
	}
		
	public DiagramNodeEditPart getNodeEditPart()
	{
		EditPart parent = this;
		while (!(parent instanceof DiagramNodeEditPart))
		{
			parent = parent.getParent();
		}
		return (DiagramNodeEditPart)parent;
	}
	
	public SapphirePart getSapphirePart()
	{
		if (getModel() instanceof ShapeModel) 
		{
			ShapeModel shapeModel = (ShapeModel)getModel();
			return shapeModel.getSapphirePart();
		}
		return null;		
	}
	
	public ShapePresentation getShapePresentation()
	{
		if (getModel() instanceof ShapeModel) {
			ShapeModel shapeModel = (ShapeModel)getModel();
			ShapePresentation shapePresentation = shapeModel.getShapePresentation();
			return shapePresentation;
		}
		return null;
	}
	
	protected ContainerShapePresentation getParentContainer(ShapePresentation shapePresentation)
	{
		DiagramPresentation parentPresentation = shapePresentation.parent();
		while (!(parentPresentation instanceof ContainerShapePresentation) && parentPresentation != null)
		{
			parentPresentation = parentPresentation.parent();
		}
		return (ContainerShapePresentation)parentPresentation;
	}
	
	protected IFigure getPartFigure(ShapePart shapePart)
	{
		ShapePresentation shapePresentation = ShapeModelUtil.getChildShapePresentation(
				getNodeEditPart().getCastedModel().getShapePresentation(), shapePart);
		return shapePresentation != null ? shapePresentation.getFigure() : null;
		
	}
	
	protected void invokeDoubleTapAction(final ShapePart shapePart)
	{
		List<SapphireActionHandler> actionHandlers = shapePart.getAction(DOUBLE_TAB_ACTION).getActiveHandlers();	
		if (!actionHandlers.isEmpty())
		{
			final SapphireActionHandler firstHandler = actionHandlers.get(0);
            getShapePresentation().display().asyncExec
            (
                new Runnable()
                {
                    public void run()
                    {
                        firstHandler.execute(getShapePresentation());
                    }
                }
            );
		}
	}
	protected List<TextPart> getContainedTextParts()
	{
		return Collections.emptyList();
	}
	
	protected List<ImagePart> getContainedImageParts()
	{
	    return Collections.emptyList();
	}

	protected TextPart getTextPart(Point mouseLocation)
	{
		Point realLocation = this.getConfigurationManager().getDiagramEditor().calculateRealMouseLocation(mouseLocation);
		List<TextPart> textParts = getContainedTextParts();
		for (TextPart textPart : textParts)
		{
			TextFigure textFigure = (TextFigure)getPartFigure(textPart);
			if (textFigure != null && textFigure.getBounds().contains(realLocation))
			{
				return textPart;
			}
		}
		return null;
	}
	
	protected ImagePart getImagePart(Point mouseLocation)
	{
		Point realLocation = this.getConfigurationManager().getDiagramEditor().calculateRealMouseLocation(mouseLocation);
		List<ImagePart> imageParts = getContainedImageParts();
		for (ImagePart imagePart : imageParts)
		{
			ImageFigure imageFigure = (ImageFigure)getPartFigure(imagePart);
			if (imageFigure != null && imageFigure.getBounds().contains(realLocation))
			{
				return imagePart;
			}
		}
		return null;
	}
	
			
}
