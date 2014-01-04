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

package org.eclipse.sapphire.tests.services.t0001;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestNoDuplicatesChildElement extends Element
{
    ElementType TYPE = new ElementType( TestNoDuplicatesChildElement.class );
    
    // *** NoDuplicates ***
    
    @NoDuplicates
    
    ValueProperty PROP_NO_DUPLICATES = new ValueProperty( TYPE, "NoDuplicates" );
    
    Value<String> getNoDuplicates();
    void setNoDuplicates( String value );

}
