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

public class Bounds extends Point
{
    private int width;
    private int height;
    
    public Bounds()
    {
    	this(-1, -1, -1, -1);    	
    }
    
    public Bounds(int x, int y, int width, int height)
    {
        super(x, y);
        this.width = width;
        this.height = height;
    }
    
    // Copy constructor
    public Bounds(Bounds another)
    {
    	super(another.getX(), another.getY());
    	this.width = another.getWidth();
    	this.height = another.getHeight();
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
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof Bounds && super.equals( obj ) )
        {
            final Bounds b = (Bounds) obj;
            
            return EqualsFactory
                    .start()
                    .add( this.width, b.width )
                    .add( this.height, b.height )
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
    
    @Override
    public String toString()
    {
    	return "Rectangle(" + getX() + ", " + getY() + ", " + 
    			getWidth() + ", " + getHeight() + ")";
    }
}
