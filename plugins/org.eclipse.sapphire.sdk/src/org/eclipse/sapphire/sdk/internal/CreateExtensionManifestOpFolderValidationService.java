/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.sdk.internal;

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

public final class CreateExtensionManifestOpFolderValidationService extends ValidationService
{
    @Override
    public Status validate()
    {
        final Value<Path> target = context( IModelElement.class ).read( context( ValueProperty.class ) );
        final Path path = target.getContent();
        
        if( path != null )
        {
            final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember( path.toPortableString() );
            
            if( resource != null )
            {
                if( ! resource.getProjectRelativePath().equals( new Path( "META-INF" ) ) )
                {
                    final String msg = NLS.bind( Resources.notInRightLocation, path.toPortableString() );
                    return Status.createWarningStatus( msg );
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
    private static final class Resources extends NLS
    {
        public static String notInRightLocation;  
        
        static
        {
            initializeMessages( CreateExtensionManifestOpFolderValidationService.class.getName(), Resources.class );
        }
    }
    
}
