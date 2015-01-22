/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A light-weight container for element content. It does not provide the rich facilities of an actual
 * element in a model, but can be useful when element content needs to be represented in memory without
 * adding it to a model (at least not immediately). 
 * 
 * @since 8.1
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementData
{
    private final ElementType type;
    private final Map<String,Object> properties = new HashMap<String,Object>();
    
    /**
     * Creates a new {@link ElementData} object.
     * 
     * @param type the element type
     * @throws IllegalArgumentException if element type is null
     */
    
    public ElementData( final ElementType type )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.type = type;
    }
    
    /**
     * Returns the element type.
     */
    
    public ElementType type()
    {
        return this.type;
    }
    
    /**
     * Reads a property.
     * 
     * @param property the property name
     * @return the property content or null
     * @throws IllegalArgumentException if property is null
     */
    
    public Object read( final String property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return this.properties.get( property );
    }
    
    /**
     * Writes a property.
     * 
     * @param property the property name
     * @param content the property content
     * @throws IllegalArgumentException if property is null
     */
    
    public void write( final String property, final Object content )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( content != null )
        {
            this.properties.put( property, content );
        }
        else
        {
            this.properties.remove( property );
        }
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        toString( buf, "" );
        return buf.toString();
    }
    
    private void toString( final StringBuilder buf, final String indent )
    {
        buf.append( this.type.getQualifiedName() ).append( '\n' );
        buf.append( indent ).append( "{\n" );
        
        final String indentPlusOne = indent + "    ";
        
        for( final Map.Entry<String,Object> entry : this.properties.entrySet() )
        {
            buf.append( indentPlusOne ).append( entry.getKey() ).append( " = " );
            
            final Object content = entry.getValue();
            
            if( content instanceof String )
            {
                buf.append( '"' ).append( (String) content ).append( '"' );
            }
            else if( content instanceof ElementData )
            {
                ( (ElementData) content ).toString( buf, indentPlusOne );
            }
            else if( content instanceof List )
            {
                buf.append( '\n' );
                buf.append( indentPlusOne ).append( '[' );
                
                final String indentPlusTwo = indentPlusOne + "    ";
                boolean first = true;
                
                for( final Object item : (List<?>) content )
                {
                    if( ! first )
                    {
                        buf.append( ',' );
                    }
                    
                    buf.append( '\n' );
                    buf.append( indentPlusTwo );
                    
                    ( (ElementData) item ).toString( buf, indentPlusTwo );
                    
                    first = false;
                }
                
                buf.append( '\n' );
                buf.append( indentPlusOne ).append( ']' );
            }
            else
            {
                buf.append( content.toString() );
            }
            
            buf.append( '\n' );
        }
        
        buf.append( indent ).append( '}' );
    }
    
}
