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

package org.eclipse.sapphire.tests.services.t0002;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestModelRoot extends Element
{
    ElementType TYPE = new ElementType( TestModelRoot.class );
    
    // *** List ***
    
    @Type( base = TestModel.class )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<TestModel> getList();
    
    // *** Element ***
    
    @Type( base = TestModel.class )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ElementHandle<TestModel> getElement();
    
    // *** ElementImplied ***
    
    @Type( base = TestModel.class )
    
    ImpliedElementProperty PROP_ELEMENT_IMPLIED = new ImpliedElementProperty( TYPE, "ElementImplied" );
    
    TestModel getElementImplied();

    // *** LossyCompression ***
    
    @Type( base = Boolean.class )

    ValueProperty PROP_LOSSY_COMPRESSION = new ValueProperty( TYPE, "LossyCompression" );
    
    Value<Boolean> getLossyCompression();
    void setLossyCompression( String value );
    void setLossyCompression( Boolean value );

}
