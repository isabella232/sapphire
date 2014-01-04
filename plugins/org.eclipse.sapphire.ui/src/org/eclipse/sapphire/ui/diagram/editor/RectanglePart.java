/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import org.eclipse.sapphire.ui.diagram.shape.def.BackgroundDef;
import org.eclipse.sapphire.ui.diagram.shape.def.BorderComponent;
import org.eclipse.sapphire.ui.diagram.shape.def.RectangleDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class RectanglePart extends ContainerShapePart
{
	private RectangleDef rectangleDef;
	
	@Override
    protected void init()
    {
        super.init();
        this.rectangleDef = (RectangleDef)super.definition;
    }

	public BorderComponent getTopBorder() 
	{
		return this.rectangleDef.getTopBorder();
	}
	
	public BorderComponent getBottomBorder() 
	{
		return this.rectangleDef.getBottomBorder();
	}

	public BorderComponent getLeftBorder() 
	{
		return this.rectangleDef.getLeftBorder();
	}
	
	public BorderComponent getRightBorder() 
	{
		return this.rectangleDef.getRightBorder();
	}
	
	public BackgroundDef getBackground()
	{
		return this.rectangleDef.getBackground().content();
	}
	
	public int getCornerRadius()
	{
		return this.rectangleDef.getCornerRadius().content();
	}
	
}
