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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.services.Service;

/**
 * Provides means to implement drag-n-drop behavior in a diagram editor.
 * 
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public abstract class DragAndDropService extends Service 
{
	public abstract boolean droppable(DropContext context);
	
	public abstract void drop(DropContext context);
	
	public static class DropContext
	{
		private final Object droppedObj;
		private Point dropPosition;
		
		public DropContext(Object droppedObj, Point dropPosition)
		{
			this.droppedObj = droppedObj;
			if (dropPosition != null)
			{
				this.dropPosition = new Point(dropPosition);
			}
		}
		
		public Object object()
		{
			return this.droppedObj;
		}
		
		public Point position()
		{
			return this.dropPosition;
		}
	}
}
