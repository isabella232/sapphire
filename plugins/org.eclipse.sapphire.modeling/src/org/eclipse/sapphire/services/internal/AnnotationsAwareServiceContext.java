/******************************************************************************
 * Copyright (c) 2012 Oracle and Red Hat
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactoryProxy;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:rcernich@redhat.com">Rob Cernich</a>
 */

public abstract class AnnotationsAwareServiceContext extends ServiceContext
{
    public AnnotationsAwareServiceContext( final String type,
                                           final ServiceContext parent )
    {
        super( type, parent );
    }
    
    @Override
    
    protected final List<ServiceFactoryProxy> local()
    {
        final List<ServiceFactoryProxy> local = new ArrayList<ServiceFactoryProxy>();
        
        final List<org.eclipse.sapphire.modeling.annotations.Service> serviceAnnotations 
            = new ArrayList<org.eclipse.sapphire.modeling.annotations.Service>();
        
        final org.eclipse.sapphire.modeling.annotations.Service serviceAnnotation 
            = annotation( org.eclipse.sapphire.modeling.annotations.Service.class );
        
        if( serviceAnnotation != null )
        {
            serviceAnnotations.add( serviceAnnotation );
        }
        
        final Services servicesAnnotation = annotation( Services.class );
        
        if( servicesAnnotation != null )
        {
            for( org.eclipse.sapphire.modeling.annotations.Service svc : servicesAnnotation.value() )
            {
                serviceAnnotations.add( svc );
            }
        }
        
        for( org.eclipse.sapphire.modeling.annotations.Service svc : serviceAnnotations )
        {
            final Class<? extends Service> cl = svc.impl();
            
            final String[] overridesInAnnotation = svc.overrides();
            final Set<String> overrides;
            
            if( overridesInAnnotation.length == 0 )
            {
                overrides = Collections.emptySet();
            }
            else if( overridesInAnnotation.length == 1 )
            {
                overrides = Collections.singleton( overridesInAnnotation[ 0 ] );
            }
            else
            {
                final Set<String> temp = new HashSet<String>();
                overrides = Collections.unmodifiableSet( temp );

                for( String override : overridesInAnnotation )
                {
                    temp.add( override );
                }
            }
            
            final org.eclipse.sapphire.modeling.annotations.Service.Param[] paramsInAnnotation = svc.params();
            final Map<String,String> params;
            
            if( paramsInAnnotation.length == 0 )
            {
                params = Collections.emptyMap();
            }
            else if( paramsInAnnotation.length == 1 )
            {
                params = Collections.singletonMap( paramsInAnnotation[ 0 ].name(), paramsInAnnotation[ 0 ].value() );
            }
            else
            {
                final Map<String,String> temp = new HashMap<String,String>();
                
                for( org.eclipse.sapphire.modeling.annotations.Service.Param param : paramsInAnnotation )
                {
                    temp.put( param.name(), param.value() );
                }
                
                params = Collections.unmodifiableMap( temp );
            }
            
            final ServiceFactoryProxy proxy = new ServiceFactoryProxy()
            {
                @Override
                public Class<? extends Service> type()
                {
                    return cl;
                }

                @Override
                public Set<String> overrides()
                {
                    return overrides;
                }

                @Override
                public Map<String,String> parameters()
                {
                    return params;
                }

                @Override
                protected boolean applicableHandOff( final ServiceContext context,
                                                     final Class<? extends Service> service )
                {
                    return true;
                }

                @Override
                protected Service createHandOff( final ServiceContext context,
                                                 final Class<? extends Service> service )
                {
                    Service instance = null;
                    
                    try
                    {
                        instance = cl.newInstance();
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                    
                    return instance;
                }
            };
            
            local.add( proxy );
        }
        
        return local;
    }
    
    protected abstract <A extends Annotation> A annotation( Class<A> type );
    
}
