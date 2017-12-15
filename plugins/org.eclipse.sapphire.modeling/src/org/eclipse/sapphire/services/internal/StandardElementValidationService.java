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

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardElementValidationService extends ValidationService
{
    @Override
    protected void init()
    {
        final IModelElement element = context( IModelElement.class );
        
        element.attach
        (
            new FilteredListener<PropertyValidationEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyValidationEvent event )
                {
                    broadcast();
                }
            }
        );
    }

    @Override
    public Status validate()
    {
        final IModelElement element = context( IModelElement.class );
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        for( ModelProperty property : element.properties() )
        {
            if( element.enabled( property ) )
            {
                factory.merge( element.validation( property ) );
            }
        }
        
        return factory.create();
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
            return new StandardElementValidationService();
        }
    }

}