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

package org.eclipse.sapphire.services.internal;

import java.lang.annotation.Annotation;
import java.util.List;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ElementServiceContext extends AnnotationsAwareServiceContext
{
    private final ElementType elementMetaModel;
    
    public ElementServiceContext( final String contextType,
                                  final ServiceContext parent,
                                  final ElementType elementMetaModel,
                                  final Object lock )
    {
        super( contextType, parent, lock );
        
        this.elementMetaModel = elementMetaModel;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public <T> T find( final Class<T> type )
    {
        T obj = super.find( type );
        
        if( obj == null )
        {
            if( type == ElementType.class )
            {
                obj = (T) this.elementMetaModel;
            }
        }
        
        return obj;
    }
    
    @Override
    
    protected <A extends Annotation> List<A> annotations( final Class<A> type )
    {
        return this.elementMetaModel.getAnnotations( type );
    }
    
}
