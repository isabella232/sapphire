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

package org.eclipse.sapphire.samples.calendar.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.annotations.BasePathsProviderImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class EventAttachmentLocalCopyBasePathsProvider

    extends BasePathsProviderImpl
    
{
    @Override
    public List<IPath> getBasePaths( final IModelElement modelElement )
    {
        final IFile file = modelElement.adapt( IFile.class );
            
        if( file != null )
        {
            return Collections.<IPath>singletonList( new Path( file.getParent().getLocation().toPortableString() ) );
        }

        return Collections.emptyList();
    }
    
}
