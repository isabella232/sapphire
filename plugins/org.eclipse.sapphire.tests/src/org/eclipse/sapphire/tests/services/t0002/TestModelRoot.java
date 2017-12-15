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

package org.eclipse.sapphire.tests.services.t0002;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestModelRoot extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestModelRoot.class );
    
    // *** List ***
    
    @Type( base = TestModel.class )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<TestModel> getList();
    
    // *** Element ***
    
    @Type( base = TestModel.class )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ModelElementHandle<TestModel> getElement();
    
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
