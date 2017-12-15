/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt;

import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;

import java.util.Collections;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.SapphireComposite;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireControl

    extends Composite
    
{
    private final SapphireComposite part;
    private final SapphireRenderingContext context;
    
    public SapphireControl( final Composite parent,
                            final IModelElement modelElement,
                            final String compositeDefPath )
    {
        this( parent, modelElement, compositeDefPath, null );
    }
    
    public SapphireControl( final Composite parent,
                            final IModelElement modelElement,
                            final String compositeDefPath,
                            final String helpContextIdPrefix )
    {
        this( parent, modelElement, SapphireUiDefFactory.getCompositeDef( compositeDefPath ), helpContextIdPrefix );
    }
    
    public SapphireControl( final Composite parent,
                            final IModelElement modelElement,
                            final ISapphireCompositeDef definition )
    {
        this( parent, modelElement, definition, null );
    }

    public SapphireControl( final Composite parent,
                            final IModelElement modelElement,
                            final ISapphireCompositeDef definition,
                            final String helpContextIdPrefix )
    {
        this( parent, (SapphireComposite) SapphirePart.create( null, modelElement, definition, Collections.<String,String>emptyMap() ), helpContextIdPrefix );
    }

    public SapphireControl( final Composite parent,
                            final SapphireComposite part )
    {
        this( parent, part, null );
    }

    public SapphireControl( final Composite parent,
                            final SapphireComposite part,
                            final String helpContextIdPrefix )
    {
        super( parent, SWT.NONE );
        
        this.part = part;
        
        setLayout( glayout( 1, 0, 0 ) );
        
        this.context = new SapphireRenderingContext( this.part, this );
        this.context.setHelpContextIdPrefix( helpContextIdPrefix );
        this.part.render( this.context );
        
        for( Control child : getChildren() )  // should just be one
        {
            child.setLayoutData( gdfill() );
        }
    }
    
    public IModelElement getModelElement()
    {
        return this.part.getModelElement();
    }
    
    public SapphireComposite getPart()
    {
        return this.part;
    }
    
}
