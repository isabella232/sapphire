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

package org.eclipse.sapphire.ui.swt.gef.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.swt.gef.presentation.ContainerShapePresentation;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ContainerShapeModel extends ShapeModel 
{
	public final static String SHAPE_VISIBILITY_UPDATES = "SHAPE_VISIBILITY_UPDATES";

	private List<ShapeModel> children;
	
	public ContainerShapeModel(DiagramNodeModel nodeModel, ShapeModel parent, 
			ContainerShapePresentation presentation)
	{
		super(nodeModel, parent, presentation);
		
		children = new ArrayList<ShapeModel>();
		for (ShapePresentation shapePresentation : presentation.getChildren())
		{
			ShapeModel childModel = ShapeModelFactory.createShapeModel(nodeModel, this, shapePresentation);
        	if (childModel != null)
        	{        		
        		this.children.add(childModel);
        	}        				
		}
		presentation.init(this);
	}
		
	public void refreshChildren()
	{
		ContainerShapePresentation presentation = getContainerShapePresentation();
		presentation.refreshChildren();
		List<ShapeModel> refreshedChildren = new ArrayList<ShapeModel>();
		for (ShapePresentation shapePresentation : presentation.getChildren()) {
			// find existing ShapeModel
			ShapeModel childModel = getChildShapeModel(shapePresentation);
			if (childModel == null) {
				childModel = ShapeModelFactory.createShapeModel(getNodeModel(), this, shapePresentation);
			}
			
        	if (childModel != null) { 
        		refreshedChildren.add(childModel);
        	}        				
		}
		this.children = refreshedChildren;
	}
	
	private ShapeModel getChildShapeModel(ShapePresentation shapePresentation) {
		for (ShapeModel model : getChildren()) {
			if (model.getShapePresentation().part() == shapePresentation.part()) {
				return model;
			}
		}
		return null;
	}
	
	private ContainerShapePresentation getContainerShapePresentation() {
		return (ContainerShapePresentation)getShapePresentation();
	}
	
	public List<ShapeModel> getChildren()
	{
		return this.children;
	}
		
	public void handleVisibilityChange(ShapePart shapePart) {
		refreshChildren();
		firePropertyChange(SHAPE_VISIBILITY_UPDATES, null, shapePart);
	}
}
