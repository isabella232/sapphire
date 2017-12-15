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

package org.eclipse.sapphire.ui.swt.renderer.actions.internal;

import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphirePropertyEditorCondition;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AbsoluteFilePathBrowseActionHandlerCondition 

    extends SapphirePropertyEditorCondition
    
{
    @Override
    protected boolean evaluate( final PropertyEditorPart part )
    {
        final ModelProperty property = part.getProperty();
        
        if( property.isOfType( Path.class ) && property.hasAnnotation( AbsolutePath.class ) )
        {
            final ValidFileSystemResourceType validFileSystemResourceType = property.getAnnotation( ValidFileSystemResourceType.class );
            
            if( validFileSystemResourceType != null && validFileSystemResourceType.value() == FileSystemResourceType.FILE )
            {
                return true;
            }
        }
        
        return false;
    }

}