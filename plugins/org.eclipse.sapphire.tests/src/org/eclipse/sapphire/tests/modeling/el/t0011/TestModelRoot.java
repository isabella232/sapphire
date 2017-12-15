/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.t0011;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestModelRoot extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestModelRoot.class );

    // *** List1 ***
    
    @Type( base = TestModelElementA.class )

    ListProperty PROP_LIST_1 = new ListProperty( TYPE, "List1" );
    
    ModelElementList<TestModelElementA> getList1();
     
    // *** List2 ***
    
    @Type( base = TestModelElementB.class )

    ListProperty PROP_LIST_2 = new ListProperty( TYPE, "List2" );
    
    ModelElementList<TestModelElementB> getList2();
     
}
