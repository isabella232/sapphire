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

package org.eclipse.sapphire.ui.swt.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.tools.SapphireDragEditPartsTracker;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ImageEditPart extends ShapeEditPart
{
	public ImageEditPart(DiagramConfigurationManager configManager)
	{
		super(configManager);
	}

	@Override
	protected IFigure createFigure() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DragTracker getDragTracker(Request request) {
		return new SapphireDragEditPartsTracker(this);
	}
	
}
