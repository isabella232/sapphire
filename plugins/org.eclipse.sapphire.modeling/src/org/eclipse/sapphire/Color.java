/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - generalization and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Color 
{
    public static final Color WHITE = new Color( 255, 255, 255 );
    
	private final int red;
    private final int green;
    private final int blue;
    
    public Color( final int red, 
                  final int green, 
                  final int blue )
    {
        if( red < 0 || red > 255 )
        {
            throw new IllegalArgumentException();
        }
        
        this.red = red;
        
        if( green < 0 || green > 255 )
        {
            throw new IllegalArgumentException();
        }
        
        this.green = green;
        
        if( blue < 0 || blue > 255 )
        {
            throw new IllegalArgumentException();
        }
        
        this.blue = blue;
    }
    
    public int red()
    {
        return this.red;
    }
        
    public int green()
    {
        return this.green;
    }
    
    public int blue()
    {
        return this.blue;
    }
    
    @Override
    public boolean equals( final Object object )
    {
    	if( object instanceof Color )
    	{
    		Color color = (Color) object;
    		return EqualsFactory.start().add( this.red, color.red ).add( this.green, color.green ).add( this.blue, color.blue ).result();
    	}
    	
    	return false;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeFactory.start().add( this.red ).add( this.green ).add( this.blue ).result();
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        buf.append( '#' );
        
        String temp = Integer.toHexString( this.red ).toUpperCase();
        
        if( temp.length() < 2 )
        {
            buf.append( '0' );
        }
        
        buf.append( temp );
        
        temp = Integer.toHexString( this.green ).toUpperCase();
        
        if( temp.length() < 2 )
        {
            buf.append( '0' );
        }
        
        buf.append( temp );
        
        temp = Integer.toHexString( this.blue ).toUpperCase();
        
        if( temp.length() < 2 )
        {
            buf.append( '0' );
        }
        
        buf.append( temp );
        
        return buf.toString();
    }
    
}
