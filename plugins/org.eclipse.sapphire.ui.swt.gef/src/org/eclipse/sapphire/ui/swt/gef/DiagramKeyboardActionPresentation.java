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
package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.forms.swt.SapphireHotSpotsActionPresentation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramKeyboardActionPresentation extends
		SapphireHotSpotsActionPresentation 
{

	private final GraphicalEditPart editPart;
	private final SapphireDiagramEditor diagramEditor;
	
    public DiagramKeyboardActionPresentation(final SapphireActionPresentationManager actionPresentationManager,
    		final SapphireDiagramEditor diagramEditor, final GraphicalEditPart editPart)
    {
    	super(actionPresentationManager);
    	this.diagramEditor = diagramEditor;
    	this.editPart = editPart;
    }

	@Override
	public void render()
	{
		for (final SapphireAction action : getActions())
		{
			registerHotSpot(action, new EditPartHotSpot( this.editPart, this.diagramEditor ));
		}

	}

    private static final class EditPartHotSpot extends HotSpot
	{
    	private GraphicalEditPart editPart;
    	private SapphireDiagramEditor diagramEditor;
    	
	    public EditPartHotSpot( final GraphicalEditPart editPart, final SapphireDiagramEditor diagramEditor )
	    {
	        this.editPart = editPart;
	        this.diagramEditor = diagramEditor;
	    }
	
	    @Override
	    public Rectangle getBounds()
	    {
	    	IFigure partFigure = this.editPart.getFigure();
	        final org.eclipse.draw2d.geometry.Rectangle bounds = partFigure.getBounds().getCopy();
	        partFigure.translateToAbsolute(bounds);
	        Point viewerOrigin = this.diagramEditor.getGraphicalViewer().getControl().getLocation();
	        viewerOrigin = this.diagramEditor.getGraphicalViewer().getControl().toDisplay(viewerOrigin);
	        bounds.x += viewerOrigin.x;
	        bounds.y += viewerOrigin.y;
	        return new Rectangle( bounds.x, bounds.y, bounds.width, 20 );
	    }
	}
}
