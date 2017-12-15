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

package org.eclipse.sapphire.tests.modeling.misc.t0014;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
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

public interface RootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( RootElement.class );
    
    // *** Child ***
    
    @Type( base = ChildElement.class )
    
    ElementProperty PROP_CHILD = new ElementProperty( TYPE, "Child" );
    
    ModelElementHandle<ChildElement> getChild();
    
    // *** ChildImplied ***

    @Type( base = ChildElement.class )
    
    ImpliedElementProperty PROP_CHILD_IMPLIED = new ImpliedElementProperty( TYPE, "ChildImplied" );

    ChildElement getChildImplied();
    
    // *** Children ***
    
    @Type( base = ChildElement.class )

    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ModelElementList<ChildElement> getChildren();

}