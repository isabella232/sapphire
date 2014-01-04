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

package org.eclipse.sapphire.modeling.localization;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LocalizationUtil
{
    private LocalizationUtil() {}
    
    public static String transformCamelCaseToLabel( final String value )
    {
        final StringBuilder label = new StringBuilder();
        
        for( int i = 0, n = value.length(); i < n; i++ )
        {
            final char ch = value.charAt( i );
            
            if( Character.isUpperCase( ch ) )
            {
                if( i > 0 )
                {
                    label.append( ' ' );
                }
                
                label.append( Character.toLowerCase( ch ) );
            }
            else if( Character.isDigit( ch ) && i > 0 && ! Character.isDigit( value.charAt( i - 1 ) ) )
            {
                label.append( ' ' );
                label.append( ch );
            }
            else
            {
                label.append( ch );
            }
        }
        
        return label.toString();
    }
    
}
