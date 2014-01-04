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

package org.eclipse.sapphire.tests.modeling.misc.t0014;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface RootElement extends Element
{
    ElementType TYPE = new ElementType( RootElement.class );
    
    // *** Child ***
    
    @Type( base = ChildElement.class )
    
    ElementProperty PROP_CHILD = new ElementProperty( TYPE, "Child" );
    
    ElementHandle<ChildElement> getChild();
    
    // *** ChildImplied ***

    @Type( base = ChildElement.class )
    
    ImpliedElementProperty PROP_CHILD_IMPLIED = new ImpliedElementProperty( TYPE, "ChildImplied" );

    ChildElement getChildImplied();
    
    // *** Children ***
    
    @Type( base = ChildElement.class )

    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ElementList<ChildElement> getChildren();

}