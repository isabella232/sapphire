/******************************************************************************
 * Copyright (c) 2015 Oracle
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

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about property's version compatibility by using semantic information specified
 * by @Since and @VersionCompatibility annotations. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class VersionCompatibilityFactsService extends FactsService
{
    @Text( "For {0} {1}" )
    private static LocalizableText forVersionsFact;
    
    @Text( "Since {0} {1}" )
    private static LocalizableText sinceVersionFact;
    
    static
    {
        LocalizableText.init( VersionCompatibilityFactsService.class );
    }
    
    @Override
    protected void facts( final SortedSet<String> facts )
    {
        final DeclarativeVersionCompatibilityService service = findDeclarativeVersionCompatibilityService( context( Property.class ) );
        
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
                    facts.add( sinceVersionFact.format( versioned, since ) );
                }
                else
                {
                    facts.add( forVersionsFact.format( versioned, constraint ) );
                }
            }
        }
    }
    
    private static DeclarativeVersionCompatibilityService findDeclarativeVersionCompatibilityService( final Property property )
    {
        DeclarativeVersionCompatibilityService service = property.service( DeclarativeVersionCompatibilityService.class );
        
        if( service == null )
        {
            final Property parent = property.element().parent();
            
            if( parent != null )
            {
                service = findDeclarativeVersionCompatibilityService( parent );
            }
        }
        
        return service;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            return ( findDeclarativeVersionCompatibilityService( context.find( Property.class ) ) != null );
        }
    }
    
}
