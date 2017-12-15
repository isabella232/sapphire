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

package org.eclipse.sapphire.tests.modeling.misc.t0003;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Transient;
import org.eclipse.sapphire.modeling.TransientProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface Misc0003TestRootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( Misc0003TestRootElement.class );
    
    // *** ValueProperty1 ***

    ValueProperty PROP_VALUE_PROPERTY_1 = new ValueProperty(TYPE, "ValueProperty1");

    Value<String> getvalueproperty1();
    void sEtVaLuEpRoPeRtY1( String value );
    
    // *** ValueProperty2 ***

    @Type( base = Integer.class )

    ValueProperty PROP_VALUE_PROPERTY_2 = new ValueProperty(TYPE, "ValueProperty2");

    Value<Integer> GETVALUEPROPERTY2();
    void sEtVaLuEpRoPeRtY2( String value );
    void SeTvAlUePrOpErTy2( Integer value );
    
    // *** TransientProperty ***

    @Type( base = Object.class )
    
    TransientProperty PROP_TRANSIENT_PROPERTY = new TransientProperty( TYPE, "TransientProperty" );

    Transient<Object> gettransientproperty();
    void SetTrAnSiEnTpRoPeRtY( Object value );
    
    // *** ElementProperty ***

    @Type( base = Misc0003TestChildElement.class )
    
    ElementProperty PROP_ELEMENT_PROPERTY = new ElementProperty(TYPE, "ElementProperty");

    ModelElementHandle<Misc0003TestChildElement> gEtElEmEnTpRoPeRtY();
    
    // *** ImpliedElementProperty ***

    @Type( base = Misc0003TestChildElement.class )
    
    ImpliedElementProperty PROP_IMPLIED_ELEMENT_PROPERTY = new ImpliedElementProperty(TYPE, "ImpliedElementProperty");

    Misc0003TestChildElement GETIMPLIEDELEMENTPROPERTY();
    
    // *** ListProperty ***

    @Type( base = Misc0003TestChildElement.class )
    
    ListProperty PROP_LIST_PROPERTY = new ListProperty(TYPE, "ListProperty");

    ModelElementList<Misc0003TestChildElement> gEtLiStPrOpErTy();

}
