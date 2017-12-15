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

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationAggregationService;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ElementValidationAggregationService extends ValidationAggregationService
{
    @Override
    protected void initDataService()
    {
        final IModelElement element = context( IModelElement.class );
        
        final Listener validationServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        for( ValidationService service : element.services( ValidationService.class ) )
        {
            service.attach( validationServiceListener );
        }
    }

    @Override
    protected Status compute()
    {
        final IModelElement element = context( IModelElement.class );
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        for( ValidationService service : element.services( ValidationService.class ) )
        {
            try
            {
                factory.merge( service.validate() );
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
        }
        
        return factory.create();
    }
    
    @Override
    protected Status data()
    {
        refresh();
        return super.data();
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
            return new ElementValidationAggregationService();
        }
    }

}
