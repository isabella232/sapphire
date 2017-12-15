/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.FormComponentPart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.FormComponentDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireForm extends Composite
{
    private IModelElement element;
    private DefinitionLoader.Reference<FormComponentDef> definition;
    private FormComponentPart part;
    private SapphireRenderingContext context;
    
    public SapphireForm( final Composite parent,
                         final IModelElement element,
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
        
        this.context = new SapphireRenderingContext( this.part, this );
        this.part.render( this.context );
        
        addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    SapphireForm.this.element = null;
                    
                    SapphireForm.this.part.dispose();
                    SapphireForm.this.part = null;
                    
                    SapphireForm.this.definition.dispose();
                    SapphireForm.this.definition = null;
                    
                    SapphireForm.this.context = null;
                }
            }
        );
    }
    
    public IModelElement element()
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
