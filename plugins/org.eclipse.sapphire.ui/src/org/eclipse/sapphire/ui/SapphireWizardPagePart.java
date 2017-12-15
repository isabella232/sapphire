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

package org.eclipse.sapphire.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireWizardPagePart

    extends SapphireComposite
    
{
    private ISapphireWizardPageDef def;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.def = (ISapphireWizardPageDef) this.definition;
    }

    @Override
    public ISapphireWizardPageDef getDefinition()
    {
        return this.def;
    }
    
    public String getLabel()
    {
        return this.def.getLabel().getContent();
    }
    
    public String getDescription()
    {
        return this.def.getDescription().getContent();
    }
    
    public ImageDescriptor getImageDescriptor()
    {
        return this.def.getImage().resolve();
    }
    
}
