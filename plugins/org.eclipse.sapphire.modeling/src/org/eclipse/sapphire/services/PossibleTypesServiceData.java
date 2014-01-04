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

package org.eclipse.sapphire.services;

import static org.eclipse.sapphire.modeling.util.MiscUtil.list;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.sapphire.ElementType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PossibleTypesServiceData
{
    private static final Comparator<ElementType> COMPARATOR = new Comparator<ElementType>()
    {
        public int compare( final ElementType x,
                            final ElementType y )
        {
            return x.getSimpleName().compareTo( y.getSimpleName() );
        }
    };
    
    private final SortedSet<ElementType> types;
    
    public PossibleTypesServiceData( final Collection<ElementType> types )
    {
        final SortedSet<ElementType> clean = new TreeSet<ElementType>( COMPARATOR );
        
        for( ElementType type : types )
        {
            if( type != null )
            {
                clean.add( type );
            }
        }

        this.types = Collections.unmodifiableSortedSet( clean );
    }
    
    public PossibleTypesServiceData( final ElementType... types )
    {
        this( list( types ) );
    }
    
    public SortedSet<ElementType> types()
    {
        return this.types;
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof PossibleTypesServiceData )
        {
            final PossibleTypesServiceData data = (PossibleTypesServiceData) obj;
            return this.types.equals( data.types );
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return this.types.hashCode();
    }
    
}
