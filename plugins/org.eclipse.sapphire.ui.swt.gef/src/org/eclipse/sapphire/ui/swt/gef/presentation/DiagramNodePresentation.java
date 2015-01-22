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

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapeUpdateEvent;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModelUtil;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeUtil;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation.ShapePresentationFactory;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramNodePresentation extends DiagramPresentation 
{
	private DiagramResourceCache resourceCache;
	private ShapePresentation shapePresentation;
	private Listener partVisibilityListener;
	private Listener shapeUpdateListener;
	
	public DiagramNodePresentation(final DiagramNodePart nodePart, final DiagramPresentation parent, 
			final Shell shell, final DiagramConfigurationManager configManager, final DiagramResourceCache resourceCache)
	{
		super(nodePart, parent, configManager, shell);
		this.resourceCache = resourceCache;
		this.shapePresentation = ShapePresentationFactory.createShapePresentation(this, 
				nodePart.getShapePart(), this.resourceCache);
	}
	
	public void init(final DiagramNodeModel model) {
		partVisibilityListener = new FilteredListener<PartVisibilityEvent>() {
			@Override
			protected void handleTypedEvent(PartVisibilityEvent event) {
				ShapePart shapePart = (ShapePart)event.part();
				model.handleVisibilityChange(shapePart);
			}
		};
		getShapePresentation().part().attach(partVisibilityListener);
		shapeUpdateListener = new FilteredListener<ShapeUpdateEvent>() {
			@Override
			protected void handleTypedEvent(ShapeUpdateEvent event) {
				ShapePart shapePart = event.getPart();
            	ShapePresentation shapePresentation = ShapeModelUtil.getChildShapePresentation(getShapePresentation(), shapePart);
    			ShapeUtil.updateFigureForShape(shapePresentation, resourceCache, getConfigurationManager());
			}
		};
		getShapePresentation().part().attach(shapeUpdateListener);
	}	

	@Override
	public void dispose()
	{
		super.dispose();
		
		getShapePresentation().part().detach(partVisibilityListener);
		getShapePresentation().part().detach(shapeUpdateListener);
	}

	public ShapePresentation getShapePresentation()
	{
		return this.shapePresentation;
	}
	
	@Override
	public DiagramNodePart part()
	{
		return (DiagramNodePart)super.part();
	}
	
	@Override
    public void render()
    {
		this.shapePresentation.render();
		setFigure(this.shapePresentation.getFigure());
    }

}
