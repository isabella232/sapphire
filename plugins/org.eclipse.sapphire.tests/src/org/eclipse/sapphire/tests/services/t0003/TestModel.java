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

package org.eclipse.sapphire.tests.services.t0003;

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

public interface TestModel extends Element
{
    ElementType TYPE = new ElementType( TestModel.class );
    
    // *** Item ***
    
    @Type( base = TestModelItem.class )

    ElementProperty PROP_ITEM = new ElementProperty( TYPE, "Item" );
    
    ElementHandle<TestModelItem> getItem();
    
    // *** ItemImplied ***
    
    @Type( base = TestModelItem.class )
    
    ImpliedElementProperty PROP_ITEM_IMPLIED = new ImpliedElementProperty( TYPE, "ItemImplied" );
    
    TestModelItem getItemImplied();
    
    // *** Items ***
    
    @Type( base = TestModelItem.class )
    
    ListProperty PROP_ITEMS = new ListProperty( TYPE, "Items" );
    
    ElementList<TestModelItem> getItems();
    
}
