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

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class AbsoluteFolderPathBrowseActionHandlerCondition extends PropertyEditorCondition
{
    @Override
    protected boolean evaluate( final PropertyEditorPart part )
    {
        final PropertyDef property = part.property().definition();
        
        if( property.isOfType( Path.class ) && property.hasAnnotation( AbsolutePath.class ) )
        {
            final ValidFileSystemResourceType validFileSystemResourceType = property.getAnnotation( ValidFileSystemResourceType.class );
            
            if( validFileSystemResourceType != null && validFileSystemResourceType.value() == FileSystemResourceType.FOLDER )
            {
                return true;
            }
        }
        
        return false;
    }

}