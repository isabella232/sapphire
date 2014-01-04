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

import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnumPropertyEditorPresentationFactory extends PropertyEditorPresentationFactory
{
    @Override
    public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
    {
        final Property property = part.property();
        
        if( property instanceof Value && property.definition().isOfType( Enum.class ) )
        {
            final Enum<?>[] enumValues = (Enum<?>[]) property.definition().getTypeClass().getEnumConstants();
            
            if( enumValues.length > 3 )
            {
                return new PopUpListFieldPropertyEditorPresentation( part, parent, composite, PopUpListFieldStyle.STRICT );
            }
            else
            {
                return new RadioButtonGroupPropertyEditorPresentation( part, parent, composite, Orientation.HORIZONTAL );
            }
        }
        
        return null;
    }

}
