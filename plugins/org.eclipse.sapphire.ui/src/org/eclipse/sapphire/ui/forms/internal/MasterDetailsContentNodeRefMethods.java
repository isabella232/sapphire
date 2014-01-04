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

package org.eclipse.sapphire.ui.forms.internal;

import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodeChildDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodeRef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNodeRefMethods
{
    public static MasterDetailsContentNodeChildDef resolve( final MasterDetailsContentNodeRef ref )
    {
        final ISapphireUiDef rootdef = ref.nearest( ISapphireUiDef.class );
        return (MasterDetailsContentNodeChildDef) rootdef.getPartDef( ref.getPart().text(), true, MasterDetailsContentNodeChildDef.class );
    }
    
}
