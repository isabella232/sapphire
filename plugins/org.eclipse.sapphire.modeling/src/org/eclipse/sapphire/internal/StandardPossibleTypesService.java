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

package org.eclipse.sapphire.internal;

import java.util.Set;

import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PossibleTypesService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Type;

/**
 * Implementation of PossibleTypesService using information specified by @Type annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardPossibleTypesService extends PossibleTypesService
{
    private boolean ordered;
    
    @Override
    protected void initPossibleTypesService()
    {
        final PropertyDef property = context( PropertyDef.class );
        final Type typeAnnotation = property.getAnnotation( Type.class );
        
        if( typeAnnotation != null )
        {
            this.ordered = typeAnnotation.ordered();
        }
    }

    @Override
    protected void compute( final Set<ElementType> types )
    {
        final PropertyDef property = context( PropertyDef.class );
        final Type typeAnnotation = property.getAnnotation( Type.class );
        
        if( ( property instanceof ElementProperty || property instanceof ListProperty ) && typeAnnotation != null )
        {
            if( typeAnnotation.possible().length == 0 )
            {
                types.add( ElementType.read( typeAnnotation.base() ) );
            }
            else
            {
                for( final Class<?> cl : typeAnnotation.possible() )
                {
                    types.add( ElementType.read( cl ) );
                }
            }
        }
    }

    @Override
    public boolean ordered()
    {
        return this.ordered;
    }

}
