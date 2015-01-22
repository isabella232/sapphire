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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.annotations.EnumSerialization;

/**
 * ConversionService implementation for Enum to String conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "rawtypes" )

public final class EnumToStringConversionService extends ConversionService<Enum,String>
{
    public EnumToStringConversionService()
    {
        super( Enum.class, String.class );
    }

    @Override
    public String convert( final Enum enm )
    {
        final EnumValueType enumValueType = new EnumValueType( enm.getClass() );
        final EnumSerialization enumStringBindingAnnotation = enumValueType.getAnnotation( enm, EnumSerialization.class );
        
        final String result;
        
        if( enumStringBindingAnnotation == null )
        {
            result = enm.name();
        }
        else
        {
            result = enumStringBindingAnnotation.primary();
        }
        
        return result;
    }
    
}
