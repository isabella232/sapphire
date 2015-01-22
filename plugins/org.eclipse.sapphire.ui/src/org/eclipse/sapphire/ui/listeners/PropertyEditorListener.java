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

package org.eclipse.sapphire.ui.listeners;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyEditorListener
{
    private PropertyEditorPresentation presentation = null;
    
    public final void initialize( final PropertyEditorPresentation presentation )
    {
        this.presentation = presentation;
    }
    
    public final Element getModelElement()
    {
        return this.presentation.part().property().element();
    }
    
    public final PropertyDef getProperty()
    {
        return this.presentation.part().property().definition();
    }
    
    public PropertyEditorPresentation presentation()
    {
        return this.presentation;
    }
    
}
