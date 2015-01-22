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

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ui.forms.ContainerPart;
import org.eclipse.sapphire.ui.forms.ContainerPart.VisibleChildrenEvent;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class FormPresentation extends FormComponentPresentation
{
    private List<FormComponentPresentation> children;
    
    public FormPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public ContainerPart<?> part()
    {
        return (ContainerPart<?>) super.part();
    }
    
    @Override
    public void render()
    {
        final ListFactory<FormComponentPresentation> childrenListFactory = ListFactory.start();
        
        for( final FormComponentPart child : part().children().visible() )
        {
            childrenListFactory.add( child.createPresentation( this, composite() ) );
        }
        
        this.children = childrenListFactory.result();
        
        for( final FormComponentPresentation child : this.children )
        {
            child.render();
        }
        
        attachPartListener
        (
            new FilteredListener<VisibleChildrenEvent>()
            {
                @Override
                protected void handleTypedEvent( final VisibleChildrenEvent event )
                {
                    refresh();
                }
            }
        );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.children != null )
        {
            for( final FormComponentPresentation child : this.children )
            {
                child.dispose();
            }
            
            this.children = null;
        }
    }

}
