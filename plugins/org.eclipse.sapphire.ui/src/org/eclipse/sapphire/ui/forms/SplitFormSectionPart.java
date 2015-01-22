/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.SplitFormSectionPresentation;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SplitFormSectionPart extends CompositePart
{
    public int weight;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.weight = definition().getWeight().content();
    }
    
    @Override
    public SplitFormPart parent()
    {
        return (SplitFormPart) super.parent();
    }

    @Override
    public SplitFormSectionDef definition()
    {
        return (SplitFormSectionDef) super.definition();
    }
    
    public int weight()
    {
        return this.weight;
    }
    
    public void weight( final int weight )
    {
        if( weight == 0 )
        {
            throw new IllegalArgumentException();
        }
        
        this.weight = weight;
    }
    
    @Override
    public SplitFormSectionPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        return new SplitFormSectionPresentation( this, parent, composite );
    }
    
}
