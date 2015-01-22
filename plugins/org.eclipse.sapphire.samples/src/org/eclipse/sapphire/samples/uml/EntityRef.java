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

package org.eclipse.sapphire.samples.uml;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementReference;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface EntityRef extends Element
{
    ElementType TYPE = new ElementType( EntityRef.class );
    
    // *** Entity ***
    
    @Reference( target = Entity.class )
    @ElementReference( list = "/Entities", key = "Name" )
    @Required

    ValueProperty PROP_ENTITY = new ValueProperty( TYPE, "Entity" );

    ReferenceValue<String,Entity> getEntity();
    void setEntity( String value );
    
}
