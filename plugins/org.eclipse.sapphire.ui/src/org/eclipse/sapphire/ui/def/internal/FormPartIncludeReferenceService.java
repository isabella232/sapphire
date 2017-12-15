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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.ReferenceService;
import org.eclipse.sapphire.ui.def.IFormPartDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class FormPartIncludeReferenceService

    extends ReferenceService
    
{
    @Override
    public Object resolve( final String reference )
    {
        final ISapphireUiDef rootdef = element().nearest( ISapphireUiDef.class );
        return rootdef.getPartDef( reference, true, IFormPartDef.class );
    }
    
}
