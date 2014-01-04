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

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.parser.ExpressionLanguageParser;

/**
 * ConversionService implementation for String to Function conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringToFunctionConversionService extends ConversionService<String,Function>
{
    public StringToFunctionConversionService()
    {
        super( String.class, Function.class );
    }

    @Override
    public Function convert( final String string )
    {
        Function result = null;
        
        try
        {
            result = ExpressionLanguageParser.parse( string );
        }
        catch( Exception e )
        {
            // Intentionally ignored.
        }
        
        if( result != null )
        {
            result.initOrigin( context( Element.class ), true );
        }
        
        return result;
    }
    
}
