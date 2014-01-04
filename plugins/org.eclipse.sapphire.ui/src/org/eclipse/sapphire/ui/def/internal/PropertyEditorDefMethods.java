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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyEditorDefMethods
{
    public static PropertyEditorDef getChildPropertyEditor( final PropertyEditorDef propertyEditorDef,
                                                            final ModelPath property )
    {
        for( PropertyEditorDef childPropertyEditorDef : propertyEditorDef.getChildProperties() )
        {
            if( property.equals( new ModelPath( childPropertyEditorDef.getProperty().text() ) ) )
            {
                return childPropertyEditorDef;
            }
        }
        
        return null;
    }
    
}
