/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEditorDefMethods
{
    public static PropertyEditorDef getChildPropertyEditor( final PropertyEditorDef propertyEditorDef,
                                                            final ModelProperty property )
    {
        final String propertyName = property.getName();
        
        for( PropertyEditorDef childPropertyEditorDef : propertyEditorDef.getChildProperties() )
        {
            if( propertyName.equals( childPropertyEditorDef.getProperty().getText() ) )
            {
                return childPropertyEditorDef;
            }
        }
        
        return null;
    }
    
}
