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

package org.eclipse.sapphire.ui.swt.gef.policies;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.sapphire.ui.diagram.editor.ShapeFactoryPart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.swt.gef.commands.MoveShapeInFactoryCommand;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeEditPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ShapeFactoryLayoutEditPolicy extends SequenceLayoutEditPolicy 
{	
	@Override
	protected Command createAddCommand(EditPart child, EditPart after) 
	{
		return null;
	}

	@Override
	protected Command createMoveChildCommand(EditPart child, EditPart after) 
	{
		if (!(child instanceof ShapeEditPart))
		{
			return null;
		}
		ShapeEditPart toMove = (ShapeEditPart)child;
		ShapePart toMovePart = (ShapePart)(((ShapeModel)toMove.getModel()).getSapphirePart());
		ShapeFactoryPart factoryPart = (ShapeFactoryPart)toMovePart.parent();
		List<ShapePart> childShapes = factoryPart.getChildren();

		if (!(after instanceof ShapeEditPart))
		{
	   		return new MoveShapeInFactoryCommand(factoryPart, toMovePart, -1);
		} 
		else 
		{
			ShapeEditPart afterShape = (ShapeEditPart)after;
			ShapePart afterShapePart = (ShapePart)(((ShapeModel)afterShape.getModel()).getSapphirePart());
			
			int oldIndex = childShapes.indexOf(toMovePart);
			int newIndex = childShapes.indexOf(afterShapePart);
			// subtract self from the index
			if (newIndex > oldIndex) {
				newIndex--;
			}
	   		return new MoveShapeInFactoryCommand(factoryPart, toMovePart, newIndex);
		}
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) 
	{
		return new ShapeSelectionEditPolicy();
	}


	@Override
	protected Command getCreateCommand(CreateRequest request) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
