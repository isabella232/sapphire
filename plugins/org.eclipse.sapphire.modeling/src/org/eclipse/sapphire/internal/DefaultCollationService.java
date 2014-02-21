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

import java.util.Comparator;

import org.eclipse.sapphire.CollationService;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * A {@link CollationService} implementation that is active when an explicit collation is not specified. The provided
 * collation matches Java's default string comparison. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DefaultCollationService extends CollationService
{
    public static final Comparator<String> COMPARATOR = new Comparator<String>()
    {
        public int compare( final String str1, final String str2 )
        {
            return str1.compareTo( str2 );
        }
    };
    
    @Override
    protected Comparator<String> compute()
    {
        return COMPARATOR;
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return ( context.find( ValueProperty.class ) != null );
        }
    }

}
