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

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphirePropertyEditorCondition;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AbsoluteFolderPathBrowseActionHandlerCondition 

    extends SapphirePropertyEditorCondition
    
{
    @Override
    protected boolean evaluate( final SapphirePropertyEditor part )
    {
        final ModelProperty property = part.getProperty();
        
        if( property.isOfType( IPath.class ) && property.hasAnnotation( AbsolutePath.class ) )
        {
            final ValidFileSystemResourceType validFileSystemResourceType 
                = property.getAnnotation( ValidFileSystemResourceType.class );
            
            if( validFileSystemResourceType != null && validFileSystemResourceType.value() == FileSystemResourceType.FOLDER )
            {
                return true;
            }
        }
        
        return false;
    }

}