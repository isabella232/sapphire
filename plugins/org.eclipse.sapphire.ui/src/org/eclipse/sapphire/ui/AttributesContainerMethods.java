/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.ui.AttributesContainer.Attribute;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class AttributesContainerMethods
{
    public static Object getAttribute( final AttributesContainer state,
                                       final String name, 
                                       final Object def )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        Object value = null;
        
        for( Attribute attribute : state.getAttributes() )
        {
            if( name.equalsIgnoreCase( attribute.getName().content() ) )
            {
                value = attribute.getValue().content();
                break;
            }
        }
        
        if( value != null && def != null && def != String.class )
        {
            value = state.service( MasterConversionService.class ).convert( value, def.getClass() );
        }
        
        if( value == null )
        {
            value = def;
        }
        
        return value;
    }
    
    public static String getAttribute( final AttributesContainer state,
                                       final String name )
    {
        return (String) getAttribute( state, name, null );
    }

    public static void setAttribute( final AttributesContainer state,
                                     final String name, 
                                     final Object value )
    {
        if( name == null )
        {
            throw new IllegalArgumentException();
        }
        
        Attribute attribute = null;
        
        for( Attribute attr : state.getAttributes() )
        {
            if( name.equalsIgnoreCase( attr.getName().content() ) )
            {
                attribute = attr;
                break;
            }
        }
        
        String string = null;
        
        if( value != null )
        {
            string = state.service( MasterConversionService.class ).convert( value, String.class );
        }
        
        if( string == null )
        {
            if( attribute != null )
            {
                state.getAttributes().remove( attribute );
            }
        }
        else
        {
            if( attribute == null )
            {
                attribute = state.getAttributes().insert();
                attribute.setName( name );
            }
            
            attribute.setValue( string );
        }
    }

}
