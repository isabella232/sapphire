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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.RelativePathService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.workspace.ProjectRelativePath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ProjectRelativePathService extends RelativePathService
{
    @Override
    public List<Path> roots()
    {
        final IProject project = adapt( IProject.class );
        
        if( project == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.singletonList( new Path( project.getLocation().toPortableString() ) );
        }
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
            return new ProjectRelativePathService();
        }
    }
    
}
