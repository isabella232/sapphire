/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.sapphire.services.DataService;
import org.eclipse.sapphire.util.Filters;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.sapphire.util.SortedSetFactory;

/**
 * Enumerates the possible child element types for a list or an element property. Each 
 * returned type is required to derive from the property's base type.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class PossibleTypesService extends DataService<Set<ElementType>>
{
    private static final Comparator<ElementType> COMPARATOR = new Comparator<ElementType>()
    {
        public int compare( final ElementType x, final ElementType y )
        {
            return x.getSimpleName().compareTo( y.getSimpleName() );
        }
    };

    @Override
    protected final void initDataService()
    {
        initPossibleTypesService();
    }

    protected void initPossibleTypesService()
    {
    }
    
    public final Set<ElementType> types()
    {
        return data();
    }
    
    @Override
    protected final Set<ElementType> compute()
    {
        if( ordered() )
        {
            final Set<ElementType> values = new LinkedHashSet<ElementType>();
            compute( values );
            return SetFactory.<ElementType>start().filter( Filters.createNotNullFilter() ).add( values ).result();
        }
        else
        {
            final Set<ElementType> values = new TreeSet<ElementType>( COMPARATOR );
            compute( values );
            return SortedSetFactory.start( COMPARATOR ).filter( Filters.createNotNullFilter() ).add( values ).result();
        }
    }
    
    protected abstract void compute( Set<ElementType> types );
    
    /**
     * Specifies if the possible types are already ordered as intended. By default, the order
     * is not treated as significant and the possible types are sorted alphabetically by the type
     * name when presented.
     * 
     * @return true if the possible types are already ordered as intended and false otherwise
     */
    
    public boolean ordered()
    {
        return false;
    }
    
}
