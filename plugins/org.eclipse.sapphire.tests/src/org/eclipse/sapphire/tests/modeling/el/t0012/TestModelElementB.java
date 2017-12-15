/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.el.t0012;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestModelElementB extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestModelElementB.class );
    
    // *** Element1 ***
    
    @Type( base = TestModelElementB.class )
    
    ElementProperty PROP_ELEMENT_1 = new ElementProperty( TYPE, "Element1" );
    
    ModelElementHandle<TestModelElementB> getElement1();
    
    // *** List1 ***
    
    @Type( base = TestModelElementB.class )
    
    ListProperty PROP_LIST_1 = new ListProperty( TYPE, "List1" );
    
    ModelElementList<TestModelElementB> getList1();
    
}
