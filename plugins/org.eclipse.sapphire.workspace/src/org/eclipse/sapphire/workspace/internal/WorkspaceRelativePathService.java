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

package org.eclipse.sapphire.workspace.internal;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.services.RelativePathService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.workspace.WorkspaceRelativePath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WorkspaceRelativePathService extends RelativePathService
{
    @Override
    public List<Path> roots()
    {
        final ListFactory<Path> paths = ListFactory.start();
        
        for( IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects() )
        {
            if( project.isAccessible() )
            {
                paths.add( new Path( project.getLocation().toPortableString() ) );
            }
        }
        
        return paths.result();
    }
    
    @Override
    public Path convertToRelative( final Path path )
    {
        for( IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects() )
        {
            if( project.isAccessible() )
            {
                final Path location = new Path( project.getLocation().toPortableString() );
                
                if( location.isPrefixOf( path ) )
                {
                    return new Path( project.getName() ).append( path.makeRelativeTo( location ) );
                }
            }
        }
        
        return null;
    }

    @Override
    public Path convertToAbsolute( final Path path )
    {
        if( path.segmentCount() > 0 )
        {
            final String projectName = path.segment( 0 );
            
            for( IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects() )
            {
                if( project.isAccessible() )
                {
                    if( projectName.equals( project.getName() ) )
                    {
                        return new Path( project.getLocation().toPortableString() ).append( path.removeFirstSegments( 1 ) );
                    }
                }
            }
        }
        
        return null;
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Path.class.isAssignableFrom( property.getTypeClass() ) && property.hasAnnotation( WorkspaceRelativePath.class ) );
        }
    }
    
}
