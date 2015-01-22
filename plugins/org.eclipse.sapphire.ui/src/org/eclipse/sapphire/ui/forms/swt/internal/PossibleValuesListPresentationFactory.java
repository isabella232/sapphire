/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PossibleValuesListPresentationFactory extends PropertyEditorPresentationFactory
{
    protected boolean check( final PropertyEditorPart part )
    {
        final Property property = part.property();
        
        if( property instanceof ElementList &&
            property.service( PossibleValuesService.class ) != null &&
            property.service( PossibleTypesService.class ).types().size() == 1 )
        {
            final SortedSet<PropertyDef> properties = property.definition().getType().properties();
            
            if( properties.size() == 1 )
            {
                final PropertyDef memberProperty = properties.first();
                
                if( memberProperty instanceof ValueProperty && memberProperty.hasAnnotation( Unique.class ) )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
