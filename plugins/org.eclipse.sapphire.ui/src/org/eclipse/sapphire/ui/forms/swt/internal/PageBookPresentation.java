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

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.FormPart;
import org.eclipse.sapphire.ui.forms.PageBookPart;
import org.eclipse.sapphire.ui.forms.PageBookPart.PageChangedEvent;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class PageBookPresentation extends FormComponentPresentation
{
    private Composite composite;
    private Presentation currentPagePresentation;
    
    public PageBookPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    public PageBookPart part()
    {
        return (PageBookPart) super.part();
    }
    
    @Override
    public void render()
    {
        render( composite() );
    }
    
    protected void render( final Composite parent )
    {
        final PageBookPart part = part();
        
        this.composite = new Composite( parent, SWT.NONE );
        this.composite.setLayoutData( gdhspan( ( part.getScaleVertically() ? gdfill() : gdhfill() ), 2 ) );
        this.composite.setLayout( glayout( 2, 0, 0 ) );
        
        register( this.composite );
        
        attachPartListener
        (
            new FilteredListener<PageChangedEvent>()
            {
                @Override
                protected void handleTypedEvent( final PageChangedEvent event )
                {
                    refreshActivePage();
                }
            }
        );
        
        refreshActivePage();
    }

    private void refreshActivePage()
    {
        if( this.currentPagePresentation != null )
        {
            this.currentPagePresentation.dispose();
            this.currentPagePresentation = null;
        }
        
        for( final Control control : this.composite.getChildren() )
        {
            control.dispose();
        }
        
        final FormPart page = part().getCurrentPage();
        
        if( page != null )
        {
            this.currentPagePresentation = page.createPresentation( this, this.composite );
            this.currentPagePresentation.render();
        }
        
        layout();
    }
    
    @Override
    public void refresh()
    {
        refreshActivePage();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.currentPagePresentation != null )
        {
            this.currentPagePresentation.dispose();
            this.currentPagePresentation = null;
        }
        
        this.composite = null;
    }

}
