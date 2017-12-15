/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.workspace.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CreateWorkspaceFileOpFolderValidator extends ValidationService
{
    @Override
    public Status validate()
    {
        final Value<Path> target = context( IModelElement.class ).read( context( ValueProperty.class ) );
        final Path path = target.getContent();
        
        if( path != null && path.segmentCount() > 0 )
        {
            final String projectName = path.segment( 0 );
            final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( projectName );
            
            if( resource == null || ! ( resource instanceof IProject && resource.isAccessible() ) )
            {
                final String msg = NLS.bind( Resources.projectDoesNotExist, projectName );
                return Status.createErrorStatus( msg );
            }
        }
        
        return Status.createOkStatus();
    }
    
    private static final class Resources extends NLS
    {
        public static String projectDoesNotExist;
        
        static
        {
            initializeMessages( CreateWorkspaceFileOpFolderValidator.class.getName(), Resources.class );
        }
    }
    
}
