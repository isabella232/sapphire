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

package org.eclipse.sapphire.tests.modeling.misc.t0012;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElementChildEx extends TestElementChild
{
    ElementType TYPE = new ElementType( TestElementChildEx.class );
    
    // *** ValueProperty2 ***
    
    ValueProperty PROP_VALUE_PROPERTY_2 = new ValueProperty( TYPE, "ValueProperty2" );
    
    Value<String> getValueProperty2();
    void setValueProperty2( String value );

}