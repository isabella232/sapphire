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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphirePartDefMethods
{
    public static String getHint( final PartDef def,
                                  final String hintName )
    {
        String hintValue = null;
        
        for( ISapphireHint hint : def.getHints() )
        {
            if( hintName.equals( hint.getName().text() ) )
            {
                hintValue = hint.getValue().text();
                break;
            }
        }
        
        if( hintValue == null )
        {
            hintValue = SapphireHintValueDefaultValueService.getDefaultValue( hintName );
        }
        
        return hintValue;
    }
    
    public static String getHint( final PartDef def,
                                  final String hintName,
                                  final String defaultValue )
    {
        final String hintValueStr = getHint( def, hintName );
        return ( hintValueStr == null ? defaultValue : hintValueStr );
    }
    
    public static boolean getHint( final PartDef def,
                                   final String hintName,
                                   final boolean defaultValue )
    {
        final String hintValueStr = getHint( def, hintName );
        
        if( hintValueStr != null )
        {
            return Boolean.parseBoolean( hintValueStr );
        }
        
        return defaultValue;
    }
    
    public static int getHint( final PartDef def,
                               final String hintName,
                               final int defaultValue )
    {
        final String hintValueStr = getHint( def, hintName );
        
        if( hintValueStr != null )
        {
            try
            {
                return Integer.parseInt( hintValueStr );
            }
            catch( NumberFormatException e )
            {
                // The desired behavior here is to fall-through and return the default value.
            }
        }
        
        return defaultValue;
    }
    
}
