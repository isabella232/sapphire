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

package org.eclipse.sapphire.ui.def.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.annotations.BasePathsProviderImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class ProjectRootBasePathsProvider

    extends BasePathsProviderImpl
    
{
    @Override
    public List<IPath> getBasePaths( final IModelElement element )
    {
        final IProject project = element.adapt( IProject.class );
        
        if( project != null )
        {
            return Collections.singletonList( project.getLocation() );
        }
        
        return Collections.emptyList();
    }
    
}
