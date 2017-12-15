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

package org.eclipse.sapphire.ui.form.editors.masterdetails.def.internal;

import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeChildDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeInclude;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNodeIncludeMethods
{
    public static MasterDetailsContentNodeChildDef resolve( final MasterDetailsContentNodeInclude ref )
    {
        final ISapphireUiDef rootdef = ref.nearest( ISapphireUiDef.class );
        return (MasterDetailsContentNodeChildDef) rootdef.getPartDef( ref.getPart().getText(), true, MasterDetailsContentNodeChildDef.class );
    }
    
}
