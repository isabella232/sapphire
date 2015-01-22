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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.ui.def.internal.LocationHintBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "before location hint" )
@Image( path = "ActionLocationHintBefore.png" )

public interface ActionLocationHintBefore extends ActionLocationHint
{
    ElementType TYPE = new ElementType( ActionLocationHintBefore.class );
    
    // *** ReferenceEntityId ***
    
    @CustomXmlValueBinding( impl = LocationHintBinding.class, params = "before:" )
    
    ValueProperty PROP_REFERENCE_ENTITY_ID = new ValueProperty( TYPE, ActionLocationHint.PROP_REFERENCE_ENTITY_ID );
    
}
