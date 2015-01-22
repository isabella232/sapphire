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

package org.eclipse.sapphire.tests.modeling.properties.element;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    interface Child extends Element
    {
        ElementType TYPE = new ElementType( Child.class );
    }
    
    interface ChildVariant1 extends Child
    {
        ElementType TYPE = new ElementType( ChildVariant1.class );
    }

    interface ChildVariant2 extends Child
    {
        ElementType TYPE = new ElementType( ChildVariant2.class );
    }
    
    interface ChildVariant3 extends Child
    {
        ElementType TYPE = new ElementType( ChildVariant3.class );
    }
    
    // *** Homogeneous ***
    
    @Type( base = Child.class )
    
    ElementProperty PROP_HOMOGENEOUS = new ElementProperty( TYPE, "Homogeneous" );
    
    ElementHandle<Child> getHomogeneous();
    
    // *** Heterogeneous ***
    
    @Type( base = Child.class, possible = { ChildVariant1.class, ChildVariant2.class } )

    ElementProperty PROP_HETEROGENEOUS = new ElementProperty( TYPE, "Heterogeneous" );
    
    ElementHandle<Child> getHeterogeneous();

}