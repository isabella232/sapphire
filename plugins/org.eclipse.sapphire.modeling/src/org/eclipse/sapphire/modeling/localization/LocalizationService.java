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

package org.eclipse.sapphire.modeling.localization;

import static org.eclipse.sapphire.modeling.localization.LocalizationUtil.transformCamelCaseToLabel;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Label;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class LocalizationService
{
    public abstract String text( String sourceLangText,
                                 CapitalizationType capitalizationType,
                                 boolean includeMnemonic );
    
    public final String label( final Class<?> cl,
                               final CapitalizationType capitalizationType,
                               final boolean includeMnemonic )
    {
        String sourceLangString = null;
        
        final Label labelAnnotation = cl.getAnnotation( Label.class );
        
        if( labelAnnotation != null )
        {
            sourceLangString = labelAnnotation.standard().trim();
        }
        
        if( sourceLangString == null || sourceLangString.length() == 0 )
        {
            String className = cl.getName();
            final int lastDot = className.lastIndexOf( '.' );
            
            if( lastDot != -1 )
            {
                className = className.substring( lastDot + 1 );
            }
            
            sourceLangString = transformCamelCaseToLabel( className );
        }
        
        return text( sourceLangString, capitalizationType, includeMnemonic );
    }
    
    public String transform( final String string,
                             final CapitalizationType capitalizationType,
                             final boolean includeMnemonic )
    {
        if( string == null )
        {
            return null;
        }
        
        final StringBuilder result = new StringBuilder( string );
        
        final int mnemonicDesignatorPos = string.indexOf( '&' );
        
        if( mnemonicDesignatorPos != -1 )
        {
            result.deleteCharAt( mnemonicDesignatorPos );
        }
        
        capitalizationType.changeTo( result );
        
        if( includeMnemonic && mnemonicDesignatorPos != -1 )
        {
            result.insert( mnemonicDesignatorPos, '&' );
        }
        
        return result.toString();
    }
    
}
