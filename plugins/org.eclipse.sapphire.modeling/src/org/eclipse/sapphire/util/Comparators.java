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

package org.eclipse.sapphire.util;

import java.util.Comparator;

/**
 * A collection of common comparators.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Comparators
{
    private static final Comparator<String> IGNORE_CASE_COMPARATOR = new Comparator<String>()
    {
        public int compare( final String str1,
                            final String str2 )
        {
            return str1.compareToIgnoreCase( str2 );
        }
    };
    
    /**
     * This class is not intended to be instantiated.
     */
    
    private Comparators() {}
    
    /**
     * Creates a case-insensitive string comparator based on String.compareToIgnoreCase( String ) API. 
     * 
     * @return the created comparator
     */
    
    public static Comparator<String> createIgnoreCaseComparator()
    {
        return IGNORE_CASE_COMPARATOR;
    }
    
}
