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

package org.eclipse.sapphire.workspace.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.swt.RelativePathBrowseActionHandler;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WorkspaceRelativePathBrowseActionHandler extends RelativePathBrowseActionHandler
{
    @Text( "&workspace relative path" )
    private static LocalizableText label;

    static 
    {
        LocalizableText.init( WorkspaceRelativePathBrowseActionHandler.class );
    }

    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );
        
        setLabel( label.text() );
        addImage( ImageData.readFromClassLoader( WorkspaceRelativePathBrowseActionHandler.class, "Project.png" ).required() );
    }

    @Override
    public List<Path> getBasePaths()
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
        if( path != null && path.segmentCount() > 0 )
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

}