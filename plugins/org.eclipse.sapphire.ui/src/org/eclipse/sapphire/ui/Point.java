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

public class Point 
{
    private int x;
    private int y;
    
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Point(Point another)
    {
    	this.x = another.getX();
    	this.y = another.getY();
    }
    
    public int getX() 
    { 
        return this.x; 
    }
    
    public void setX(int x) 
    {
        this.x = x;
    }
    
    public int getY()
    {
        return this.y;
    }
    
    public void setY(int y)
    {
        this.y = y;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
    	if( obj instanceof Point )
    	{
    		final Point pt = (Point) obj;
    		
    		return EqualsFactory
    		        .start()
    		        .add( this.x, pt.x )
    		        .add( this.y, pt.y )
    		        .result();
    	}
    	
    	return false;
    }

    @Override
    public int hashCode()
    {
        return HashCodeFactory
                .start()
                .add( this.x )
                .add( this.y )
                .result();
    }
    
}
