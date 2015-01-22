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

package org.eclipse.sapphire.ui.swt.gef.presentation;

import org.eclipse.draw2d.IFigure;
import org.eclipse.sapphire.ui.diagram.editor.ImagePart;
import org.eclipse.sapphire.ui.diagram.editor.LinePart;
import org.eclipse.sapphire.ui.diagram.editor.RectanglePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapeFactoryPart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.diagram.editor.SpacerPart;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.diagram.editor.ValidationMarkerPart;
import org.eclipse.sapphire.ui.diagram.shape.def.LayoutConstraintDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SelectionPresentation;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ShapePresentation extends DiagramPresentation
{
	private DiagramResourceCache resourceCache;
	private boolean separator = false;
	
	public ShapePresentation(DiagramPresentation parent, ShapePart shapePart, DiagramResourceCache resourceCache)
	{
	    super( shapePart, parent, parent.getConfigurationManager(), 
	    		parent.shell() );
	    
		this.resourceCache = resourceCache;
	}
	
	@Override
    public ShapePart part()
    {
        return (ShapePart) super.part();
    }
    
	public DiagramResourceCache getResourceCache()
	{
		return this.resourceCache;
	}
	
    @Override
    public DiagramPresentation parent()
    {
        return (DiagramPresentation) super.parent();
    }
    
    public SapphireDiagramEditor page()
    {
        return getConfigurationManager().getDiagramEditor();
    }
		
	public IFigure getParentFigure()
	{
		IFigure parentFigure = null;
		DiagramPresentation parentPresentation = parent();
		while (parentPresentation != null)
		{
			parentFigure = parentPresentation.getFigure();
			if (parentFigure != null)
			{
				break;
			}
			parentPresentation = parentPresentation.parent();
		}
		return parentFigure;
	}
	
	public IFigure getNodeFigure()
	{
		IFigure parentFigure = getFigure();
		DiagramPresentation parentPresentation = parent();
		while (!(parentPresentation instanceof DiagramNodePresentation))
		{
			parentFigure = parentPresentation.getFigure();
			parentPresentation = parentPresentation.parent();
		}
		return parentFigure;		
	}
	
	public LayoutConstraintDef getLayoutConstraint()
	{
		return part().getLayoutConstraint();
	}
	
	public boolean visible()
	{
		return part().visible();
	}
	
	public SelectionPresentation getSelectionPresentation()
	{
		return part().getSelectionPresentation();
	}
	
	@Override
    public void render()
    {
	    // Konstantin: The figure should be created here...
	    
	    throw new UnsupportedOperationException();
    }

    public void dispose()
	{		
	}
	
	public void refreshVisuals()
	{
	}
	
    public boolean isSeparator() {
		return separator;
	}

	public void setSeparator(boolean separator) {
		this.separator = separator;
	}

	public static final class ShapePresentationFactory
    {
    	public static ShapePresentation createShapePresentation(DiagramPresentation parent, ShapePart shapePart, DiagramResourceCache resourceCache)
    	{
    		ShapePresentation shapePresentation = null;
        	if (shapePart instanceof TextPart)
        	{
        		shapePresentation = new TextPresentation(parent, (TextPart)shapePart, resourceCache);
        	}
        	else if (shapePart instanceof ImagePart)
        	{
        		shapePresentation = new ImagePresentation(parent, (ImagePart)shapePart, resourceCache);
        	}
        	else if (shapePart instanceof ValidationMarkerPart)
        	{
        		shapePresentation = new ValidationMarkerPresentation(parent, (ValidationMarkerPart)shapePart, resourceCache);
        	}
        	else if (shapePart instanceof LinePart)
        	{
        		shapePresentation = new LineShapePresentation(parent, (LinePart)shapePart, resourceCache);
        	}
        	else if (shapePart instanceof RectanglePart)
        	{
        		shapePresentation = new RectanglePresentation(parent, (RectanglePart)shapePart, resourceCache);        		
        	}
        	else if (shapePart instanceof ShapeFactoryPart)
        	{
        		shapePresentation = new ShapeFactoryPresentation(parent, (ShapeFactoryPart)shapePart, resourceCache);        		
        	}
        	else if (shapePart instanceof SpacerPart)
        	{
        		shapePresentation = new SpacerPresentation(parent, (SpacerPart)shapePart, resourceCache);
        	}
    		return shapePresentation;
    	}
    }
}
