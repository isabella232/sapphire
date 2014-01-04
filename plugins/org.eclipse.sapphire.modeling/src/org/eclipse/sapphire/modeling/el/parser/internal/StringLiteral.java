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

package org.eclipse.sapphire.modeling.el.parser.internal;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StringLiteral
{
    public static final String fromToken( final String str )
    {
        final StringBuilder buf = new StringBuilder();
        boolean escapeInEffect = false;        
        
        for( int i = 1, n = str.length() - 1; i < n; i++ )
        {
            final char ch = str.charAt( i );
            
            if( ch == '\\' )
            {
                if( escapeInEffect )
                {
                    buf.append( ch );
                    escapeInEffect = false;
                }
                else
                {
                    escapeInEffect = true;
                }
            }
            else
            {
                buf.append( ch );
                escapeInEffect = false;
            }
        }
        
        return buf.toString();
    }
    
}
