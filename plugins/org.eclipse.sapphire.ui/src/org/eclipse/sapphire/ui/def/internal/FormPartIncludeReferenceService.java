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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.ui.def.IDefinitionReference;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.forms.FormComponentDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FormPartIncludeReferenceService extends ReferenceService
{
    private static final String LISTENER_PATH = ISapphireUiDef.PROP_IMPORTED_DEFINITIONS.name() + "/" + IDefinitionReference.PROP_PATH.name();
    
    private ISapphireUiDef sdef;
    private Listener listener;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.sdef = context( ISapphireUiDef.class );
        
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                broadcast();
            }
        };
        
        this.sdef.attach( this.listener, LISTENER_PATH );
    }
    
    @Override
    public Object resolve( final String reference )
    {
        return context( ISapphireUiDef.class ).getPartDef( reference, true, FormComponentDef.class );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        this.sdef.detach( this.listener, LISTENER_PATH );
        
        this.sdef = null;
        this.listener = null;
    }
    
}
