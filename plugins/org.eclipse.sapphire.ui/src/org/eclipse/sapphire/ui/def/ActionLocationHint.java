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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Required;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "action location hint" )

public interface ActionLocationHint extends Element
{
    ElementType TYPE = new ElementType( ActionLocationHint.class );
    
    // *** ReferenceEntityId ***
    
    @Label( standard = "reference entity ID" )
    @Required
    @PossibleValues( property = "../#/Id", invalidValueSeverity = Status.Severity.OK )
    
    ValueProperty PROP_REFERENCE_ENTITY_ID = new ValueProperty( TYPE, "ReferenceEntityId" );
    
    Value<String> getReferenceEntityId();
    void setReferenceEntityId( String value );
    
}
