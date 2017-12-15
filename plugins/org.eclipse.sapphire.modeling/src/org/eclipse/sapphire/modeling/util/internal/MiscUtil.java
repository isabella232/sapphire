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

package org.eclipse.sapphire.modeling.util.internal;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class MiscUtil
{
    public static final String EMPTY_STRING = ""; //$NON-NLS-1$
    
    public static final boolean equal( final Object obj1, 
                                       final Object obj2 )
    {
        if( obj1 == obj2 )
        {
            return true;
        }
        else if( obj1 != null && obj2 != null )
        {
            return obj1.equals( obj2 );
        }

        return false;
    }
    

    public static boolean contains( final Object[] array,
                                    final Object object )
    {
        for( int i = 0; i < array.length; i++ )
        {
            if( array[ i ].equals( object ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static int indexOf( final Object[] array,
                               final Object object )
    {
        for( int i = 0; i < array.length; i++ )
        {
            if( array[ i ].equals( object ) )
            {
                return i;
            }
        }
        
        throw new IllegalArgumentException();
    }
    
}
