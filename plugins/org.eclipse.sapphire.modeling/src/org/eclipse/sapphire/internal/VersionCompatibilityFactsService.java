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

package org.eclipse.sapphire.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * Creates fact statements about property's version compatibility by using semantic information specified
 * by @Since and @VersionCompatibility annotations. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class VersionCompatibilityFactsService extends FactsService
{
    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final IModelElement element = context( IModelElement.class );
        final ModelProperty property = context( ModelProperty.class );
        final DeclarativeVersionCompatibilityService service = findDeclarativeVersionCompatibilityService( element, property );
        
        if( service != null )
        {
            final String versioned = service.versioned();
            final VersionConstraint constraint = service.constraint();
            
            if( versioned != null && constraint != null )
            {
                Version since = null;
                
                if( constraint.ranges().size() == 1 )
                {
                    final VersionConstraint.Range range = constraint.ranges().get( 0 );
                    final VersionConstraint.Range.Limit min = range.min();
                    final VersionConstraint.Range.Limit max = range.max();
                    
                    if( min != null && min.inclusive() && max == null )
                    {
                        since = min.version();
                    }
                }
                
                if( since != null )
                {
                    facts.add( NLS.bind( Resources.sinceVersionFact, versioned, since ) );
                }
                else
                {
                    facts.add( NLS.bind( Resources.forVersionsFact, versioned, constraint ) );
                }
            }
        }
    }
    
    private static DeclarativeVersionCompatibilityService findDeclarativeVersionCompatibilityService( final IModelElement element,
                                                                                                      final ModelProperty property )
    {
        DeclarativeVersionCompatibilityService service = element.service( property, DeclarativeVersionCompatibilityService.class );
        
        if( service == null )
        {
            final ModelProperty parentProperty = element.getParentProperty();
            
            if( parentProperty != null )
            {
                IModelParticle parent = element.parent();
                
                if( ! ( parent instanceof IModelElement ) )
                {
                    parent = parent.parent();
                }
                
                final IModelElement parentElement = (IModelElement) parent;
                
                service = findDeclarativeVersionCompatibilityService( parentElement, parentProperty );
            }
        }
        
        return service;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final IModelElement element = context.find( IModelElement.class );
            final ModelProperty property = context.find( ModelProperty.class );
            
            return ( findDeclarativeVersionCompatibilityService( element, property ) != null );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new VersionCompatibilityFactsService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String forVersionsFact;
        public static String sinceVersionFact;
        
        static
        {
            initializeMessages( VersionCompatibilityFactsService.class.getName(), Resources.class );
        }
    }
    
}
