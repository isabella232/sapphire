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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.SapphireDialogPart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireDialogDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireDialog

    extends Dialog
    
{
    private final SapphireDialogPart part;
    private final boolean preferFormStyle;
    
    public SapphireDialog( final Shell shell,
                           final IModelElement modelElement,
                           final String dialogDefPath )
    {
        this( shell, modelElement, SapphireUiDefFactory.getDialogDef( dialogDefPath ) );
    }
    
    public SapphireDialog( final Shell shell,
                           final IModelElement modelElement,
                           final ISapphireDialogDef definition )
    {
        this( shell, (SapphireDialogPart) SapphirePart.create( null, modelElement, definition, Collections.<String,String>emptyMap() ) );
    }

    public SapphireDialog( final Shell shell,
                           final SapphireDialogPart part )
    {
        super( shell );
        
        this.part = part;
        this.preferFormStyle = part.getPreferFormStyle();
    }
    
    public final IModelElement getModelElement()
    {
        return this.part.getModelElement();
    }
    
    @Override
    protected Control createDialogArea( final Composite parent )
    {
        getShell().setText( this.part.getLabel() );
        
        final Composite composite = (Composite) super.createDialogArea( parent );
        
        if( this.preferFormStyle )
        {
            composite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0 ) );
        innerComposite.setLayoutData( gdfill() );
        
        final SapphireRenderingContext context = new SapphireRenderingContext( this.part, innerComposite );
        
        this.part.render( context );
        
        final String initialFocusProperty = this.part.getDefinition().getInitialFocus().getContent();
        
        if( initialFocusProperty != null )
        {
            this.part.setFocus( initialFocusProperty );
        }
        
        return composite;
    }

    @Override
    protected Control createContents( final Composite parent )
    {
        final Composite composite = (Composite) super.createContents( parent );
        
        if( this.preferFormStyle )
        {
            composite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        return composite;
    }

    @Override
    protected Control createButtonBar( final Composite parent )
    {
        final Composite composite = (Composite) super.createButtonBar( parent );
        
        if( this.preferFormStyle )
        {
            composite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        return composite;
    }
    
    @Override
    protected Button createButton( final Composite parent,
                                   final int id,
                                   final String label,
                                   final boolean defaultButton )
    {
        final Button button = super.createButton( parent, id, label, defaultButton );
        
        if( this.preferFormStyle )
        {
            button.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        return button;
    }

    @Override
    protected boolean isResizable()
    {
        return true;
    }
    
}
