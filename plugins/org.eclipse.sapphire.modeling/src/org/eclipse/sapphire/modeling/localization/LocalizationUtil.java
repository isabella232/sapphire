/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.localization;

import java.security.MessageDigest;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
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
                if( label.length() > 0 )
                {
                    label.append( ' ' );
                }
                
                label.append( Character.toLowerCase( ch ) );
            }
            else
            {
                label.append( ch );
            }
        }
        
        return label.toString();
    }
    
    public static final String createStringDigest( final String str )
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance( "SHA-256" );
            final byte[] input = str.getBytes( "UTF-8" );
            final byte[] digest = md.digest( input );
            
            final StringBuilder buf = new StringBuilder();
            
            for( int i = 0; i < digest.length; i++ )
            {
                String hex = Integer.toHexString( 0xFF & digest[ i ] );
                
                if( hex.length() == 1 )
                {
                    buf.append( '0' );
                }
                
                buf.append( hex );
            }
            
            return buf.toString();
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
}
