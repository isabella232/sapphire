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

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.util.Collections;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.FormComponentDef;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireForm extends Composite
{
    private Element element;
    private DefinitionLoader.Reference<FormComponentDef> definition;
    private FormComponentPart part;
    
    public SapphireForm( final Composite parent,
                         final Element element,
                         final DefinitionLoader.Reference<FormComponentDef> definition )
    {
        super( parent, SWT.NONE );
        
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.element = element;
        this.definition = definition;
        
        this.part = (FormComponentPart) SapphirePart.create( null, this.element, this.definition.resolve(), Collections.<String,String>emptyMap() );
        
        setLayout( glayout( 2, 0, 0 ) );
        
        final Presentation presentation = this.part.createPresentation( null, this );
        
        presentation.render();
        
        addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    presentation.dispose();
                    
                    SapphireForm.this.element = null;
                    
                    SapphireForm.this.part.dispose();
                    SapphireForm.this.part = null;
                    
                    SapphireForm.this.definition.dispose();
                    SapphireForm.this.definition = null;
                }
            }
        );
    }
    
    public Element element()
    {
        return this.element;
    }
    
    public FormComponentDef definition()
    {
        return this.definition.resolve();
    }
    
    public FormComponentPart part()
    {
        return this.part;
    }
    
}
