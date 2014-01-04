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

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DefinitionReferenceService extends ReferenceService
{
    private String reference;
    private DefinitionLoader.Reference<ISapphireUiDef> handle;

    @Override
    public Object resolve( final String reference )
    {
        if( ! equal( this.reference, reference ) )
        {
            if( this.handle != null )
            {
                this.handle.dispose();
                this.handle = null;
            }

            this.reference = reference;
            
            if( reference != null )
            {
                final Context context = context( Element.class ).adapt( Context.class );
                
                try
                {
                    this.handle = DefinitionLoader.context( context ).sdef( reference ).root();
                }
                catch( IllegalArgumentException e )
                {
                    // This means the reference could not be resolved. Ignoring the exception to
                    // return null and signal as much.
                }
            }
        }
        
        return ( this.handle == null ? null : this.handle.resolveIgnoringValidation() );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.handle != null )
        {
            this.handle.dispose();
        }
    }
    
}
