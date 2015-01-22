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

package org.eclipse.sapphire.ui.swt.gef.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sapphire.ui.DragAndDropService;
import org.eclipse.sapphire.ui.DragAndDropService.DropContext;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DndObjectCommand extends Command 
{
	private DiagramModel diagramModel;
	private DragAndDropService dndService;
	private List<Object> droppedObjs;
	private Point location;
	
	public DndObjectCommand(DiagramModel diagramModel, ISelection sel, Point location)
	{
		this.diagramModel = diagramModel;
		this.dndService = this.diagramModel.getSapphirePart().service(DragAndDropService.class);
		this.location = location;
		
		this.droppedObjs = new ArrayList<Object>();
		
		IStructuredSelection s = (IStructuredSelection) sel;
		if (s == null) 
		{
			s = StructuredSelection.EMPTY;
		}

		for (Iterator<?> iter = s.iterator(); iter.hasNext();) 
		{
			Object next = iter.next();
			this.droppedObjs.add(next);
		}
	}
	
	@Override
	public boolean canExecute() 
	{
		if (this.dndService == null)
		{
			return false;
		}
		boolean canDrop = true;
		for (Object droppedObj : this.droppedObjs)
		{
			if (!this.dndService.droppable(new DropContext(droppedObj, null)))
			{
				canDrop = false;
				break;
			}
		}
		return canDrop;
	}
	
	@Override
	public void execute() 
	{
		int currX = this.location.x;
		int currY = this.location.y;
		for (Object droppedObj : this.droppedObjs)
		{
			DropContext context = new DropContext(droppedObj, new org.eclipse.sapphire.ui.Point(currX, currY));
			this.dndService.drop(context);
			currX += 20;
			currY += 20;
		}
	}
}
