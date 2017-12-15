/******************************************************************************
 * Copyright (c) 2013 Oracle
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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.services.PossibleTypesServiceData;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of PossibleTypesService using information specified by @Type annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardPossibleTypesService extends PossibleTypesService
{
    private Set<ModelElementType> possible;
    
    @Override
    protected void initPossibleTypesService()
    {
        final ModelProperty property = context( ModelProperty.class );
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
                this.possible = Collections.singleton( ModelElementType.read( possible.get( 0 ) ) );
            }
            else
            {
                this.possible = new HashSet<ModelElementType>();
                
                for( Class<?> cl : possible )
                {
                    this.possible.add( ModelElementType.read( cl ) );
                }
            }
        }
    }

    @Override
    protected PossibleTypesServiceData compute()
    {
        return new PossibleTypesServiceData( this.possible );
    }

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return true;
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new StandardPossibleTypesService();
        }
    }

}
