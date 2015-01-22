/******************************************************************************
 * Copyright (c) 2015 Oracle and Red Hat
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Rob Cernich - [360369] Parameters not passed through to service
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.EventDeliveryJob;
import org.eclipse.sapphire.JobQueue;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceProxy;
import org.eclipse.sapphire.util.MapFactory;
import org.eclipse.sapphire.util.SetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:rcernich@redhat.com">Rob Cernich</a>
 */

public abstract class AnnotationsAwareServiceContext extends ServiceContext
{
    public AnnotationsAwareServiceContext( final String type, final ServiceContext parent, final Object lock, final JobQueue<EventDeliveryJob> queue )
    {
        super( type, parent, lock, queue );
    }
    
    @Override
    protected final List<ServiceProxy> local()
    {
        final List<ServiceProxy> local = new ArrayList<ServiceProxy>();
        
        final List<org.eclipse.sapphire.modeling.annotations.Service> serviceAnnotations 
            = new ArrayList<org.eclipse.sapphire.modeling.annotations.Service>();
        
        serviceAnnotations.addAll( annotations( org.eclipse.sapphire.modeling.annotations.Service.class ) );

        for( Services servicesAnnotation : annotations( Services.class ) )
        {
            for( org.eclipse.sapphire.modeling.annotations.Service svc : servicesAnnotation.value() )
            {
                serviceAnnotations.add( svc );
            }
        }
        
        final boolean isInstanceContext = type().endsWith( ".Instance" );
        
        for( org.eclipse.sapphire.modeling.annotations.Service svc : serviceAnnotations )
        {
            if( ( isInstanceContext && svc.context() == org.eclipse.sapphire.modeling.annotations.Service.Context.INSTANCE ) ||
                ( ! isInstanceContext && svc.context() == org.eclipse.sapphire.modeling.annotations.Service.Context.METAMODEL ) )
            {
                final Class<? extends Service> cl = svc.impl();
                
                final SetFactory<String> overridesSetFactory = SetFactory.start();
                final MapFactory<String,String> paramsMapFactory = MapFactory.start();
                
                for( String override : svc.overrides() )
                {
                    overridesSetFactory.add( override );
                }
                
                for( org.eclipse.sapphire.modeling.annotations.Service.Param param : svc.params() )
                {
                    paramsMapFactory.add( param.name(), param.value() );
                }
                
                local.add
                (
                    new ServiceProxy
                    (
                        this,
                        cl.getName(),
                        cl,
                        null,
                        overridesSetFactory.result(),
                        paramsMapFactory.result()
                    )
                );
            }
        }
        
        return local;
    }
    
    protected abstract <A extends Annotation> List<A> annotations( Class<A> type );
    
}
