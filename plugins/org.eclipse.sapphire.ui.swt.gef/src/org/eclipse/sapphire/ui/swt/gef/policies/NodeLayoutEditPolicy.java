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

package org.eclipse.sapphire.ui.swt.gef.policies;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.sapphire.ui.diagram.editor.ShapeFactoryPart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeEditPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * This layout policy is needed for DiagramNodeEditPart for updating the edit part
 * for a request. It inherits "public EditPart getTargetEditPart(Request request)"
 * from LayoutEditPolicy parent class. It'll also be needed when we start to support
 * node containment.
 */

public class NodeLayoutEditPolicy extends LayoutEditPolicy 
{
	public NodeLayoutEditPolicy(DiagramNodeModel nodeModel)
	{
	}
	

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new ShapeSelectionEditPolicy();
	}


	@Override
	protected Command getCreateCommand(CreateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected Command getMoveChildrenCommand(Request request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EditPart getTargetEditPart(Request request) 
	{
		if (request instanceof ChangeBoundsRequest)
		{
			ChangeBoundsRequest cbr = (ChangeBoundsRequest)request;
			List<?> editParts = cbr.getEditParts();
			boolean moveShapeFactoryPart = false;
			for (Object obj : editParts)
			{
				EditPart editPart = (EditPart)obj;
				if (editPart instanceof ShapeEditPart)
				{
					ShapeEditPart shapeEditPart = (ShapeEditPart)editPart;
					if (shapeEditPart.getModel() instanceof ShapeModel)
					{
						ShapeModel shapeModel = (ShapeModel)shapeEditPart.getModel();
						ShapePart shapePart = (ShapePart)shapeModel.getSapphirePart();
						if (shapePart.parent() instanceof ShapeFactoryPart)
						{
							moveShapeFactoryPart = true;
							break;
						}
					}
				}
			}
			if (moveShapeFactoryPart)
			{
				return ((EditPart)(editParts.get(0))).getParent();
			}
		}
		if (REQ_ADD.equals(request.getType())
				|| REQ_MOVE.equals(request.getType())
				|| REQ_CREATE.equals(request.getType())
				|| REQ_CLONE.equals(request.getType()))
			return getHost();
		return null;
	}
	
}
