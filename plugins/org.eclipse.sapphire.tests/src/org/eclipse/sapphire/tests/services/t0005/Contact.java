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

package org.eclipse.sapphire.tests.services.t0005;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Service;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Service( impl = ContactEqualityService.class )

public interface Contact extends Element
{
    ElementType TYPE = new ElementType( Contact.class );
    
    // *** FirstName ***
    
    ValueProperty PROP_FIRST_NAME = new ValueProperty( TYPE, "FirstName" );
    
    Value<String> getFirstName();
    void setFirstName( String value );
    
    // *** LastName ***
    
    ValueProperty PROP_LAST_NAME = new ValueProperty( TYPE, "LastName" );
    
    Value<String> getLastName();
    void setLastName( String value );
    
}
