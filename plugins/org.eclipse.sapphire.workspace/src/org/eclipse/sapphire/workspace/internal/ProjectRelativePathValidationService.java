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

package org.eclipse.sapphire.workspace.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ModelPropertyValidationService;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.workspace.ProjectRelativePath;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ProjectRelativePathValidationService extends ModelPropertyValidationService<Value<Path>>
{
    @Override
    public Status validate()
    {
        if( target().getText() != null )
        {
            final IProject project = adapt( IProject.class );
            
            if( project == null )
            {
                return Status.createErrorStatus( Resources.message );
            }
        }
        
        return Status.createOkStatus();
    }
        
    public static final class Factory extends ModelPropertyServiceFactory
    {
        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            return ( property instanceof ValueProperty && Path.class.isAssignableFrom( property.getTypeClass() ) && property.hasAnnotation( ProjectRelativePath.class ) );
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
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
