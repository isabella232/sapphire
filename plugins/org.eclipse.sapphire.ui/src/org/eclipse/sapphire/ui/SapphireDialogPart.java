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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.def.DialogDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireDialogPart

    extends CompositePart
    
{
    private DialogDef def;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.def = (DialogDef) this.definition;
    }

    @Override
    public DialogDef definition()
    {
        return this.def;
    }
    
    public String getLabel()
    {
        return this.def.getLabel().getLocalizedText( CapitalizationType.TITLE_STYLE, false );
    }
    
}
