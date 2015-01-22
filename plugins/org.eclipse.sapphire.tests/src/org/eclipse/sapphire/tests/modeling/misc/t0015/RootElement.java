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

package org.eclipse.sapphire.tests.modeling.misc.t0015;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface RootElement extends Element
{
    ElementType TYPE = new ElementType( RootElement.class );
    
    // *** Switch ***
    
    ValueProperty PROP_SWITCH = new ValueProperty( TYPE, "Switch" );
    
    Value<String> getSwitch();
    void setSwitch( String value );
    
    // *** For ***
    
    @Type( base = ChildElement.class )

    ElementProperty PROP_FOR = new ElementProperty( TYPE, "For" );
    
    ElementHandle<ChildElement> getFor();
    
    // *** Final ***
    
    @Type( base = ChildElement.class )
    
    ImpliedElementProperty PROP_FINAL = new ImpliedElementProperty( TYPE, "Final" );
    
    ChildElement getFinal();
    
    // *** Interface ***
    
    @Type( base = ChildElement.class )
    
    ListProperty PROP_INTERFACE = new ListProperty( TYPE, "Interface" );
    
    ElementList<ChildElement> getInterface();
    
    // *** Public ***
    
    @Type( base = ChildElement.class )

    TransientProperty PROP_PUBLIC = new TransientProperty( TYPE, "Public" );
    
    Transient<ChildElement> getPublic();
    void setPublic( ChildElement value );

}