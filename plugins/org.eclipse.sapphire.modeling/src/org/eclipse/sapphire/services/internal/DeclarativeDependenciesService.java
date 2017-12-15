/******************************************************************************
 * Copyright (c) 2011 Oracle
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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.services.DependenciesService;

/**
 * Implementation of DependenciesService that exposes dependencies specified by the @DependsOn annotation.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DeclarativeDependenciesService extends DependenciesService
{
    @Override
    protected void compute( final Set<ModelPath> dependencies )
    {
        final Set<String> dependenciesAsStrings = new HashSet<String>();
        
        final DependsOn dependsOnAnnotation = property().getAnnotation( DependsOn.class );
        
        if( dependsOnAnnotation != null )
        {
            for( String dependsOnPropertyRef : dependsOnAnnotation.value() )
            {
                dependenciesAsStrings.add( dependsOnPropertyRef );
            }
        }
        
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
    }

    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            return property.hasAnnotation( DependsOn.class );
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            return new DeclarativeDependenciesService();
        }
    }
    
}
