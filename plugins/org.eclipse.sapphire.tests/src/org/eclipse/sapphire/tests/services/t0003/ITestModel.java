/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.services.t0003;

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

public interface ITestModel extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ITestModel.class );
    
    // *** Item ***
    
    @Type( base = ITestModelItem.class )

    ElementProperty PROP_ITEM = new ElementProperty( TYPE, "Item" );
    
    ModelElementHandle<ITestModelItem> getItem();
    
    // *** ItemImplied ***
    
    @Type( base = ITestModelItem.class )
    
    ImpliedElementProperty PROP_ITEM_IMPLIED = new ImpliedElementProperty( TYPE, "ItemImplied" );
    
    ITestModelItem getItemImplied();
    
    // *** Items ***
    
    @Type( base = ITestModelItem.class )
    
    ListProperty PROP_ITEMS = new ListProperty( TYPE, "Items" );
    
    ModelElementList<ITestModelItem> getItems();
    
}
