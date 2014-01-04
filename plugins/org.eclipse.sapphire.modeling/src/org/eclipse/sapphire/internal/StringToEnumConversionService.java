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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.UniversalConversionService;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.annotations.EnumSerialization;

/**
 * ConversionService implementation for String to Enum conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToEnumConversionService extends UniversalConversionService
{
    @Override
    public <T> T convert( final Object object, final Class<T> type )
    {
        T result = null;
        
        if( object instanceof String && Enum.class.isAssignableFrom( type ) && type != Enum.class )
        {
            final String string = (String) object;
            final EnumValueType enumValueType = new EnumValueType( type );
            
            for( Enum<?> enumItem : enumValueType.getItems() )
            {
                final EnumSerialization enumSerializationAnnotation = enumValueType.getAnnotation( enumItem, EnumSerialization.class );
                
                if( enumSerializationAnnotation == null )
                {
                    if( enumItem.name().equalsIgnoreCase( string ) )
                    {
                        result = type.cast( enumItem );
                    }
                }
                else
                {
                    if( enumSerializationAnnotation.caseSensitive() )
                    {
                        if( enumSerializationAnnotation.primary().equals( string ) )
                        {
                            result = type.cast( enumItem );
                        }
                        else
                        {
                            for( String x : enumSerializationAnnotation.alternative() )
                            {
                                if( x.equals( string ) )
                                {
                                    result = type.cast( enumItem );
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        if( enumSerializationAnnotation.primary().equalsIgnoreCase( string ) )
                        {
                            result = type.cast( enumItem );
                        }
                        else
                        {
                            for( String x : enumSerializationAnnotation.alternative() )
                            {
                                if( x.equalsIgnoreCase( string ) )
                                {
                                    result = type.cast( enumItem );
                                    break;
                                }
                            }
                        }
                    }
                }
    
                if( result != null )
                {
                    break;
                }
            }
        }

        return result;
    }

}
