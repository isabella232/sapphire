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

package org.eclipse.sapphire.modeling;

import java.io.IOException;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class LayeredModelStore

    extends ModelStore
    
{
    private final IModel[] models;
    
    public LayeredModelStore( final IModel... models )
    {
        this.models = models;
    }
    
    public IModel getModel( final int index )
    {
        return this.models[ index ];
    }
    
    @Override
    public void open() {}
    
    @Override
    public void save() throws IOException
    {
        if( validateEdit() )
        {
            for( IModel model : this.models )
            {
                model.save();
            }
        }
    }
    
    @Override
    public boolean validateEdit()
    {
        for( IModel model : this.models )
        {
            if( model.getModelStore().validateEdit() == false )
            {
                return false;
            }
        }
        
        return true;
    }
    
}
