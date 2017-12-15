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

package org.eclipse.sapphire.samples.contacts.internal;

import org.eclipse.sapphire.modeling.DelimitedListController;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelProperty;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Connection

    extends ConnectionStub
    
{
    private DelimitedListController.Entry entry;
    
    public Connection( final IModelParticle parent,
                       final ModelProperty parentProperty,
                       final DelimitedListController.Entry entry )
    {
        super( parent, parentProperty );
        
        this.entry = entry;
    }
    
    public DelimitedListController.Entry getBase()
    {
        return this.entry;
    }

    @Override
    protected String readName()
    {
        return this.entry.getValue();
    }

    @Override
    protected void writeName( final String name )
    {
        this.entry.setValue( name );
    }
    
    @Override
    protected void doRemove()
    {
        this.entry.remove();
    }
    
}
