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

package org.eclipse.sapphire.tests.modeling.el.t0001;

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

public interface TestExpr0001ModelElement

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( TestExpr0001ModelElement.class );
    
    // *** Element ***

    @Type( base = TestExpr0001ModelElement.class )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ModelElementHandle<TestExpr0001ModelElement> getElement();

    // *** List ***
    
    @Type( base = TestExpr0001ModelElement.class )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<TestExpr0001ModelElement> getList();
    
}
