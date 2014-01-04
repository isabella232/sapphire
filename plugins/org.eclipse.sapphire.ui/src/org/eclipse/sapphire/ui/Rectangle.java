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

import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class Rectangle extends Point
{
	private int width, height;
	
	public Rectangle()
	{
		this(0, 0, 0, 0);
	}
	
	public Rectangle(int x, int y, int width, int height)
	{
		super(x, y);
		this.width = width;
		this.height = height;
	}
	
	public Rectangle(Rectangle other) 
	{
		this(other.getX(), other.getY(), other.getWidth(), other.getHeight());
	}
	
    public int getWidth()
    {
        return this.width;
    }
    
    public void setWidth(int w)
    {
        this.width = w;
    }
    
    public int getHeight()
    {
        return this.height;
    }
    
    public void setHeight(int h)
    {
        this.height = h;
    }
	
    public void set(int x, int y, int width, int height)
    {
    	setX(x);
    	setY(y);
    	this.width = width;
    	this.height = height;
    }
    
	public boolean contains(int x, int y) 
	{
		return y >= getY() && y < getY() + getHeight() && x >= getX() && x < getX() + getWidth();
	}
	
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof Rectangle && super.equals( obj ) )
        {
            final Rectangle r = (Rectangle) obj;
            
            return EqualsFactory
                    .start()
                    .add( this.width, r.width )
                    .add( this.height, r.height )
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
                .add( this.width )
                .add( this.height )
                .result();
    }
    
}
