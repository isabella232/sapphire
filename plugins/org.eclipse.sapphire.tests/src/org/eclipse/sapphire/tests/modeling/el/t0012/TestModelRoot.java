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

package org.eclipse.sapphire.tests.modeling.el.t0012;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestModelRoot extends Element
{
    ElementType TYPE = new ElementType( TestModelRoot.class );

    // *** List1 ***
    
    @Type( base = TestModelElementA.class )

    ListProperty PROP_LIST_1 = new ListProperty( TYPE, "List1" );
    
    ElementList<TestModelElementA> getList1();
     
    // *** List2 ***
    
    @Type( base = TestModelElementB.class )

    ListProperty PROP_LIST_2 = new ListProperty( TYPE, "List2" );
    
    ElementList<TestModelElementB> getList2();
     
}
