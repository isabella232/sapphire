/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.services.DependenciesServiceData;
import org.eclipse.sapphire.services.DependenciesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Implementation of DependenciesService that exposes dependencies specified by the @DependsOn annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DeclarativeDependenciesService extends DependenciesService
{
    @Override
    protected DependenciesServiceData compute()
    {
        final Set<String> dependenciesAsStrings = new HashSet<String>();
        
        final DependsOn dependsOnAnnotation = context( ModelProperty.class ).getAnnotation( DependsOn.class );
        
        if( dependsOnAnnotation != null )
        {
            for( String dependsOnPropertyRef : dependsOnAnnotation.value() )
            {
                dependenciesAsStrings.add( dependsOnPropertyRef );
            }
        }
        
        final Set<ModelPath> dependencies = new HashSet<ModelPath>();
        
        for( String str : dependenciesAsStrings )
        {
            ModelPath path = null;
            
            try
            {
                path = new ModelPath( str );
            }
            catch( ModelPath.MalformedPathException e )
            {
                LoggingService.log( e );
            }
            
            dependencies.add( path );
        }
        
        return new DependenciesServiceData( dependencies );
    }

    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            return context.find( ModelProperty.class ).hasAnnotation( DependsOn.class );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new DeclarativeDependenciesService();
        }
    }
    
}
