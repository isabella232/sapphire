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

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.FormComponentDef;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * A property page implementation that uses Sapphire to display and edit content.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class SapphirePropertyPage extends SapphirePreferencePage implements IWorkbenchPropertyPage
{
    public SapphirePropertyPage( final ElementType type, final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        super( type, definition );
    }

    public SapphirePropertyPage( final Element element, final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        super( element, definition );
    }

    protected SapphirePropertyPage()
    {
        super();
    }
    
    private IAdaptable object;

    /**
     * Returns the object that owns the properties shown on this page.
     */
    
    @Override
    
    public IAdaptable getElement()
    {
        return object;
    }

    /**
     * Sets the object that owns the properties shown on this page.
     *
     * @param element the element
     */
    
    @Override
    
    public void setElement( final IAdaptable element )
    {
        this.object = element;
    }
    
}
