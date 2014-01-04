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

package org.eclipse.sapphire.sdk.extensibility;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "service context reference" )

public interface ServiceContextRef extends Element
{
    ElementType TYPE = new ElementType( ServiceContextRef.class );
    
    // *** Context ***
    
    @Label( standard = "context" )
    @Required
    @NoDuplicates
    @XmlBinding( path = "" )
    
    @PossibleValues
    (
        values = 
        {
            ServiceContext.ID_ROOT,
            ServiceContext.ID_ELEMENT_INSTANCE,
            ServiceContext.ID_ELEMENT_METAMODEL,
            ServiceContext.ID_PROPERTY_INSTANCE,
            ServiceContext.ID_PROPERTY_METAMODEL,
            "Sapphire.Part"
        },
        invalidValueSeverity = Severity.OK
    )
    
    ValueProperty PROP_CONTEXT = new ValueProperty( TYPE, "Context" );
    
    Value<String> getContext();
    void setContext( String value );
    
}
