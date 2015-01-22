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

import org.eclipse.sapphire.ui.diagram.editor.ShapeFactoryPart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapeFactoryPresentation;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ShapeFactoryModel extends ShapeModel 
{
	private List<ShapeModel> children;
	private List<ShapeModel> separators;
	private ShapeFactoryPresentation shapeFactoryPresentation;
	public final static String SHAPE_ADD = "SHAPE_ADD";
	public final static String SHAPE_DELETE = "SHAPE_DELETE";
	public final static String SHAPE_REORDER = "SHAPE_REORDER";	
	
	public ShapeFactoryModel(DiagramNodeModel nodeModel, ShapeModel parent, ShapeFactoryPresentation presentation)
	{
		super(nodeModel, parent, presentation);
		this.shapeFactoryPresentation = presentation;
		children = new ArrayList<ShapeModel>();
		if (this.shapeFactoryPresentation.part().visible()) 
		{
			List<ShapePresentation> presentations = this.shapeFactoryPresentation.getChildren();
			int size = presentations.size();
			for (int i = 0; i < size; i++)
			{
				ShapePresentation shapePresentation = presentations.get(i);
				ShapeModel childModel = ShapeModelFactory.createShapeModel(nodeModel, this, shapePresentation);
				assert childModel != null;
	        	this.children.add(childModel);
	        	
	        	ShapeModel separatorModel = getSeparatorModel(nodeModel, i);
	        	if (separatorModel != null && i < (size - 1)) {
	        		this.children.add(separatorModel);
	        	}
			}
		}
		presentation.init(this);
	}
	
	private ShapeModel getSeparatorModel(DiagramNodeModel nodeModel, int index) {
		ShapePresentation separatorPresentation = this.shapeFactoryPresentation.getSeparator();
		if (separatorPresentation != null) {
			if (separators == null) {
				separators = new ArrayList<ShapeModel>();
			}
			int size = separators.size();
			if (index + 1 > size) {
				for (int i = size; i < index + 1; i++) {
					ShapeModel separatorModel = ShapeModelFactory.createShapeModel(nodeModel, this, separatorPresentation);
					separators.add(separatorModel);
				}
			}
			return separators.get(index);
		}
		return null;
	}
	
	public void handleAddShape(ShapePart shapePart) 
	{
		refreshChildren();
		firePropertyChange(SHAPE_ADD, null, null);
	}
	
	public void handleDeleteShape(ShapePart shapePart) 
	{
		refreshChildren();
		firePropertyChange(SHAPE_DELETE, null, null);
	}

	public void handleReorderShapes(ShapeFactoryPart shapeFactory) 
	{
		refreshChildren();
		firePropertyChange(SHAPE_REORDER, null, null);
	}
	
	public void refreshChildren()
	{
		this.shapeFactoryPresentation.refreshChildren();
		List<ShapeModel> refreshedChildren = new ArrayList<ShapeModel>();
		if (this.shapeFactoryPresentation.part().visible()) 
		{
			List<ShapePresentation> presentations = this.shapeFactoryPresentation.getChildren();
			int size = presentations.size();
			for (int i = 0; i < size; i++)
			{
				ShapePresentation shapePresentation = presentations.get(i);

				// find existing ShapeModel
				ShapeModel childModel = getChildShapeModel(shapePresentation);
				if (childModel == null) 
				{
					childModel = ShapeModelFactory.createShapeModel(getNodeModel(), this, shapePresentation);
				}
				assert childModel != null;
        		refreshedChildren.add(childModel);

        		ShapeModel separatorModel = getSeparatorModel(getNodeModel(), i);
	        	if (separatorModel != null && i < (size - 1)) {
	        		refreshedChildren.add(separatorModel);
	        	}
			}
		}
		children = refreshedChildren;
	}
	
	private ShapeModel getChildShapeModel(ShapePresentation shapePresentation) {
		for (ShapeModel model : getChildren()) {
			if (model.getShapePresentation().part() == shapePresentation.part()) {
				return model;
			}
		}
		return null;
	}

	public List<ShapeModel> getChildren()
	{
		return this.children;
	}
	
}
