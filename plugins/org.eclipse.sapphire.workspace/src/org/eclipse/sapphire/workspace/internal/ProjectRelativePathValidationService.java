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

package org.eclipse.sapphire.workspace.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.workspace.ProjectRelativePath;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ProjectRelativePathValidationService extends ValidationService
{
    @Override
    public Status validate()
    {
        final IModelElement element = context( IModelElement.class );
        
        if( element.read( context( ValueProperty.class ) ).getText() != null )
        {
            final IProject project = element.adapt( IProject.class );
            
            if( project == null )
            {
                return Status.createErrorStatus( Resources.message );
            }
        }
        
        return Status.createOkStatus();
    }
        
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Path.class.isAssignableFrom( property.getTypeClass() ) && property.hasAnnotation( ProjectRelativePath.class ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new ProjectRelativePathValidationService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String message;
        
        static
        {
            initializeMessages( ProjectRelativePathValidationService.class.getName(), Resources.class );
        }
    }
    
}
