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

package org.eclipse.sapphire.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.PossibleTypesServiceData;

/**
 * Implementation of PossibleTypesService using information specified by @Type annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StandardPossibleTypesService extends PossibleTypesService
{
    private Set<ElementType> possible;
    
    @Override
    protected void initPossibleTypesService()
    {
        final PropertyDef property = context( PropertyDef.class );
        final List<Class<?>> possible = new ArrayList<Class<?>>();
        
        final Type typeAnnotation = property.getAnnotation( Type.class );
        
        if( property instanceof ElementProperty || property instanceof ListProperty )
        {
            if( typeAnnotation != null )
            {
                if( typeAnnotation.possible().length == 0 )
                {
                    possible.add( typeAnnotation.base() );
                }
                else
                {
                    for( Class<?> cl : typeAnnotation.possible() )
                    {
                        possible.add( cl );
                    }
                }
            }
        
            if( possible.size() == 1 )
            {
                this.possible = Collections.singleton( ElementType.read( possible.get( 0 ) ) );
            }
            else
            {
                this.possible = new HashSet<ElementType>();
                
                for( Class<?> cl : possible )
                {
                    this.possible.add( ElementType.read( cl ) );
                }
            }
        }
    }

    @Override
    protected PossibleTypesServiceData compute()
    {
        return new PossibleTypesServiceData( this.possible );
    }

}
