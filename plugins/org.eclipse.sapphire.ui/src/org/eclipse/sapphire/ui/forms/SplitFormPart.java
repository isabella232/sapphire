/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.def.Orientation;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.SplitFormPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SplitFormPart extends ContainerPart<SplitFormSectionPart>
{
    protected Children initChildren()
    {
        return new Children()
        {
            @Override
            protected void init( final ListFactory<SplitFormSectionPart> childPartsListFactory )
            {
                final Element element = getLocalModelElement();
                
                for( final PartDef childPartDef : definition().getSections() )
                {
                    childPartsListFactory.add( (SplitFormSectionPart) createWithoutInit( SplitFormPart.this, element, childPartDef, SplitFormPart.this.params ) );
                }
            }
        };
    }

    @Override
    public SplitFormDef definition()
    {
        return (SplitFormDef) super.definition();
    }
    
    public Orientation orientation()
    {
        return definition().getOrientation().content();
    }
    
    @Override
    public SplitFormPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new SplitFormPresentation( this, parent, composite );
    }
    
}
