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
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeChildDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeInclude;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNodeIncludeMethods
{
    public static IMasterDetailsContentNodeChildDef resolve( final IMasterDetailsContentNodeInclude ref )
    {
        final ISapphireUiDef rootdef = ref.nearest( ISapphireUiDef.class );
        return (IMasterDetailsContentNodeChildDef) rootdef.getPartDef( ref.getPart().getText(), true, IMasterDetailsContentNodeChildDef.class );
    }
    
}
