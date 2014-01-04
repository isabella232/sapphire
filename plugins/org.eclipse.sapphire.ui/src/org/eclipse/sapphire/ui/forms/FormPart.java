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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.FormPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class FormPart extends ContainerPart<FormComponentPart>
{
    protected Children initChildren()
    {
        return new Children()
        {
            @Override
            protected void init( final ListFactory<FormComponentPart> childPartsListFactory )
            {
                final Element element = getLocalModelElement();
                
                for( final PartDef childPartDef : definition().getContent() )
                {
                    childPartsListFactory.add( (FormComponentPart) createWithoutInit( FormPart.this, element, childPartDef, FormPart.this.params ) );
                }
            }
        };
    }
    
    @Override
    public FormDef definition()
    {
        return (FormDef) super.definition();
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new FormPresentation( this, parent, composite );
    }
    
}
