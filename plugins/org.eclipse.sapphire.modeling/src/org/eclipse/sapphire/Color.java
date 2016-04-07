/******************************************************************************
 * Copyright (c) 2016 Oracle
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
 * A representation of an RGB color.
 * 
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Color 
{
    public static final Color WHITE = new Color( 255, 255, 255 );
    
	private final int red;
    private final int green;
    private final int blue;
    
    /**
     * Creates a new color object.
     * 
     * @param red the value of the red channel, between 0 and 255
     * @param green the value of the green channel, between 0 and 255
     * @param blue the value of the blue channel, between 0 and 255
     * @throws IllegalArgumentException if a channel value is not within range
     */
    
    public Color( final int red, final int green, final int blue )
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
    
    /**
     * Creates a new color object from a string in #RRGGBB format, where RR, GG and BB are hex numbers
     * from 00 to FF corresponding to red, green and blue components of the color.
     * 
     * @param color a string representation of a color
     * @throws IllegalArgumentException if the color string is null or is not of the expected format
     * @since 8.2
     */
    
    public Color( final String color )
    {
        if( color == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( color.startsWith( "#" ) && color.length() == 7 )
        {
            this.red = Integer.valueOf( color.substring( 1, 3 ), 16 );
            this.green = Integer.valueOf( color.substring( 3, 5 ), 16 );
            this.blue = Integer.valueOf( color.substring( 5, 7 ), 16 );
        }
        else
        {
            throw new IllegalArgumentException( color );
        }
    }
    
    /**
     * Returns the value of the red channel as an integer between 0 and 255.
     * 
     * @return the value of the red channel
     */
    
    public int red()
    {
        return this.red;
    }
        
    /**
     * Returns the value of the green channel as an integer between 0 and 255.
     * 
     * @return the value of the green channel
     */
    
    public int green()
    {
        return this.green;
    }
    
    /**
     * Returns the value of the blue channel as an integer between 0 and 255.
     * 
     * @return the value of the blue channel
     */
    
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
