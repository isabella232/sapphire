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

package org.eclipse.sapphire.java.jdt.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.modeling.ResourceStore;

/**
 * ConversionService implementation for ResourceStore to IJavaProject conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ResourceStoreToIJavaProjectConversionService extends ConversionService<ResourceStore,IJavaProject>
{
    public ResourceStoreToIJavaProjectConversionService()
    {
        super( ResourceStore.class, IJavaProject.class );
    }

    @Override
    public IJavaProject convert( final ResourceStore store )
    {
        final IProject project = store.adapt( IProject.class );
        
        if( project != null )
        {
            final IJavaProject jproject = JavaCore.create( project );
            
            if( jproject.exists() )
            {
                return jproject;
            }
        }
        
        return null;
    }

}
