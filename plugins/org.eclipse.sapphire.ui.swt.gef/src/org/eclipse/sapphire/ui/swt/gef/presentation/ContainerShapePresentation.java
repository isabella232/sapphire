/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.presentation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.diagram.editor.ContainerShapePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeLayoutDef;
import org.eclipse.sapphire.ui.swt.gef.model.ContainerShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ContainerShapePresentation extends ShapePresentation 
{
	private List<ShapePresentation> children;
	private Listener partVisibilityListener;
	
	public ContainerShapePresentation(DiagramPresentation parent, ContainerShapePart containerShapePart,
				DiagramResourceCache resourceCache)
	{
		super(parent, containerShapePart, resourceCache);

		this.children = new ArrayList<ShapePresentation>();
		ShapePresentation childPresentation = null;
		for (ShapePart shapePart : containerShapePart.getChildren())
		{
			if (canAddShapePart(shapePart))
			{
				childPresentation = ShapePresentationFactory.createShapePresentation(this, shapePart, resourceCache);
				this.children.add(childPresentation);
			}
		}
	}
	
	public void init(final ContainerShapeModel model) {
		partVisibilityListener = new FilteredListener<PartVisibilityEvent>() {
			@Override
			protected void handleTypedEvent(PartVisibilityEvent event) {
				ShapePart shapePart = (ShapePart)event.part();
				model.handleVisibilityChange(shapePart);
			}
		};
		part().attach(partVisibilityListener);
	}
	
	protected boolean canAddShapePart(ShapePart shapePart) {
		if (shapePart instanceof ContainerShapePart) {
			return shapePart.visible();
		}
		return true;
	}
	
	public List<ShapePresentation> getChildren()
	{
		return this.children;
	}
	
	@Override
	public ContainerShapePart part()
	{
		return (ContainerShapePart) super.part();
	}
		
	public ShapeLayoutDef getLayout()
	{
		return part().getLayout();
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		
		part().detach(partVisibilityListener);
		
		for (ShapePresentation shapePresentation : getChildren())
		{
			shapePresentation.dispose();
		}
	}
	
	public void refreshChildren()
	{
		List<ShapePresentation> refreshedChildren = new ArrayList<ShapePresentation>();
		ContainerShapePart containerShapePart = part();
		for (ShapePart shapePart : containerShapePart.getChildren())
		{
			if (canAddShapePart(shapePart))
			{
				ShapePresentation childPresentation = getChildShapePresentation(shapePart);
				if (childPresentation == null) {
					childPresentation = ShapePresentationFactory.createShapePresentation(this, shapePart, getResourceCache());
				}
				refreshedChildren.add(childPresentation);
			}
		}		
		this.children = refreshedChildren;
	}
	
	private ShapePresentation getChildShapePresentation(ShapePart shapePart) {
		for (ShapePresentation presentation : getChildren()) {
			if (presentation.part() == shapePart) {
				return presentation;
			}
		}
		return null; 
	}

	@Override
	public void refreshVisuals()
	{
		super.refreshVisuals();
		for (ShapePresentation shapePresentation : getChildren())
		{
			shapePresentation.refreshVisuals();
		}
	}

	public int getChildFigureIndex(ShapePresentation childShapePresentation)
	{
		int shapeIndex = getChildren().indexOf(childShapePresentation);
		if (shapeIndex == -1)
		{
			return -1;
		}

		int figureIndex = 0;
		for (int i = 0; i < shapeIndex; i++)
		{
			if (getChildren().get(i).getFigure() != null)
			{
				figureIndex++;
			}
		}
		return figureIndex;
	}
	
}

