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

package org.eclipse.sapphire.ui.diagram.editor;

import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramNodeBounds extends Bounds 
{
	private boolean autoLayout;
	private boolean defaultPosition;
	
	public DiagramNodeBounds()
	{
		this(-1, -1, -1, -1, false, false);
	}

	public DiagramNodeBounds(Bounds bounds)
	{
		this(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), false, false);
	}
		
	public DiagramNodeBounds(int x, int y)
	{
		super(x, y, -1, -1);
		this.autoLayout = false;
	}
	
	public DiagramNodeBounds(int x, int y, int width, int height, boolean autoLayout, boolean defaultPosition)
	{
		super(x, y, width, height);
		this.autoLayout = autoLayout;
		this.defaultPosition = defaultPosition;
	}
	
	public DiagramNodeBounds(DiagramNodeBounds nodeBounds)
	{
		this(nodeBounds.getX(), nodeBounds.getY(), nodeBounds.getWidth(), nodeBounds.getHeight(), 
				nodeBounds.isAutoLayout(), nodeBounds.defaultPosition);
	}
	
	public boolean isAutoLayout()
	{
		return this.autoLayout;
	}
	
	public void setAutoLayout(boolean autoLayout)
	{
		this.autoLayout = autoLayout;
	}

	public boolean isDefaultPosition()
	{
		return this.defaultPosition;
	}
	
	public void setDefaultPosition(boolean defaultPosition)
	{
		this.defaultPosition = defaultPosition;
	}
	
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof DiagramNodeBounds && super.equals( obj ) )
        {
            final DiagramNodeBounds b = (DiagramNodeBounds) obj;
            
            return EqualsFactory
                    .start()
                    .add( this.autoLayout, b.autoLayout )
                    .add( this.defaultPosition, b.defaultPosition )
                    .result();
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return HashCodeFactory
                .start()
                .add( super.hashCode() )
                .add( this.autoLayout )
                .add( this.defaultPosition )
                .result();
    }

}
