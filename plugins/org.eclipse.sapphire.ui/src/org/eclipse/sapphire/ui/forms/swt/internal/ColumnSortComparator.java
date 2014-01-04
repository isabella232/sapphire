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

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.text.Collator;
import java.util.Comparator;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ColumnSortComparator implements Comparator<Object>
{
    public int compare( final Object x,
                        final Object y )
    {
        final String a = convertToString( x );
        final String b = convertToString( y );
        
        final boolean aEmpty = ( a.trim().length() == 0 );
        final boolean bEmpty = ( b.trim().length() == 0 );
        
        if( aEmpty && bEmpty )
        {
            return 0;
        }
        else if( aEmpty )
        {
            return 1;
        }
        else if( bEmpty )
        {
            return -1;
        }
        else
        {
            return Collator.getInstance().compare( a, b );
        }
    }
    
    protected String convertToString( final Object obj )
    {
        return (String) obj;
    }
    
}
