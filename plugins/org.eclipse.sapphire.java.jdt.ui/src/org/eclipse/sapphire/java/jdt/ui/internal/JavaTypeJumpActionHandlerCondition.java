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

package org.eclipse.sapphire.java.jdt.ui.internal;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JavaTypeJumpActionHandlerCondition extends PropertyEditorCondition
{
    @Override
    protected boolean evaluate( final PropertyEditorPart part )
    {
        final PropertyDef property = part.property().definition();
        
        if( property instanceof ValueProperty && property.isOfType( JavaTypeName.class ) )
        {
            final Reference referenceAnnotation = property.getAnnotation( Reference.class );
            
            if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
            {
                return true;
            }
        }
        
        return false;
    }
    
}