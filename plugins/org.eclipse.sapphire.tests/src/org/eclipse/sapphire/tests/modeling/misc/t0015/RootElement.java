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

package org.eclipse.sapphire.tests.modeling.misc.t0015;

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

public interface RootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( RootElement.class );
    
    // *** Switch ***
    
    ValueProperty PROP_SWITCH = new ValueProperty( TYPE, "Switch" );
    
    Value<String> getSwitch();
    void setSwitch( String value );
    
    // *** For ***
    
    @Type( base = ChildElement.class )

    ElementProperty PROP_FOR = new ElementProperty( TYPE, "For" );
    
    ModelElementHandle<ChildElement> getFor();
    
    // *** Final ***
    
    @Type( base = ChildElement.class )
    
    ImpliedElementProperty PROP_FINAL = new ImpliedElementProperty( TYPE, "Final" );
    
    ChildElement getFinal();
    
    // *** Interface ***
    
    @Type( base = ChildElement.class )
    
    ListProperty PROP_INTERFACE = new ListProperty( TYPE, "Interface" );
    
    ModelElementList<ChildElement> getInterface();
    
    // *** Public ***
    
    @Type( base = ChildElement.class )

    TransientProperty PROP_PUBLIC = new TransientProperty( TYPE, "Public" );
    
    Transient<ChildElement> getPublic();
    void setPublic( ChildElement value );

}