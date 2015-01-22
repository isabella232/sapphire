/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class FormComponentPart extends SapphirePart
{
    @Override
    public FormComponentDef definition()
    {
        return (FormComponentDef) super.definition();
    }
    
    public Color getBackgroundColor()
    {
        return definition().getBackgroundColor().content();
    }
    
    public final boolean getScaleVertically()
    {
        return definition().getScaleVertically().content();
    }
    
    public abstract FormComponentPresentation createPresentation( SwtPresentation parent, Composite composite );
    
    
}
