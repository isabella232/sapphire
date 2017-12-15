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

package org.eclipse.sapphire.modeling.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StaticValuesProvider

    extends PossibleValuesProviderImpl
    
{
    private final List<String> values;
    
    public StaticValuesProvider( final String[] values )
    {
        final List<String> list = new ArrayList<String>();
        
        for( String item : values )
        {
            if( item != null )
            {
                list.add( item );
            }
        }
        
        this.values = Collections.unmodifiableList( list );
    }

    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        values.addAll( this.values );
    }
    
}
