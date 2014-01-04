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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.services.DependenciesService;
import org.eclipse.sapphire.services.DependenciesServiceData;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Implementation of DependenciesService that exposes dependencies specified by the @DependsOn annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeDependenciesService extends DependenciesService
{
    @Override
    protected DependenciesServiceData compute()
    {
        final Set<String> dependenciesAsStrings = new HashSet<String>();
        
        final DependsOn dependsOnAnnotation = context( PropertyDef.class ).getAnnotation( DependsOn.class );
        
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
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            dependencies.add( path );
        }
        
        return new DependenciesServiceData( dependencies );
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return context.find( PropertyDef.class ).hasAnnotation( DependsOn.class );
        }
    }
    
}
