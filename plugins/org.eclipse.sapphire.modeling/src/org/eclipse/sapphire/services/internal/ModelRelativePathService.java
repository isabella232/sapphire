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

package org.eclipse.sapphire.services.internal;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelRelativePath;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.RelativePathService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelRelativePathService extends RelativePathService
{
    @Override
    public List<Path> roots()
    {
        final File file = context( IModelElement.class ).adapt( File.class );
        
        if( file == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.singletonList( new Path( file.getParent() ) );
        }
    }

    @Override
    public boolean enclosed()
    {
        return false;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Path.class.isAssignableFrom( property.getTypeClass() ) && property.hasAnnotation( ModelRelativePath.class ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new ModelRelativePathService();
        }
    }
    
}
