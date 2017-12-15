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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphireDialogPart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.DialogDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireDialog extends Dialog
{
    private IModelElement element;
    private DefinitionLoader.Reference<DialogDef> definition;
    private SapphireDialogPart part;
    private Button okButton;
    
    public SapphireDialog( final Shell shell,
                           final IModelElement element,
                           final DefinitionLoader.Reference<DialogDef> definition )
    {
        super( shell );
        
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
        
        this.part = new SapphireDialogPart();
        this.part.init( null, this.element, this.definition.resolve(), Collections.<String,String>emptyMap() );
    }
    
    public final IModelElement element()
    {
        return this.element;
    }
    
    public final DialogDef definition()
    {
        return this.definition.resolve();
    }
    
    @Override
    protected Control createDialogArea( final Composite parent )
    {
        final Shell shell = getShell();
        
        shell.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    SapphireDialog.this.element = null;
                    
                    SapphireDialog.this.part.dispose();
                    SapphireDialog.this.part = null;
                    
                    SapphireDialog.this.definition.dispose();
                    SapphireDialog.this.definition = null;
                }
            }
        );
        
        shell.setText( this.part.getLabel() );
        
        final Composite composite = (Composite) super.createDialogArea( parent );
        
        if( this.part.getPreferFormStyle() )
        {
            composite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0 ) );
        innerComposite.setLayoutData( gdfill() );
        
        final SapphireRenderingContext context = new SapphireRenderingContext( this.part, innerComposite );
        
        this.part.render( context );
        
        final String initialFocusProperty = this.part.definition().getInitialFocus().getContent();
        
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
        
        if( this.part.getPreferFormStyle() )
        {
            composite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        return composite;
    }

    @Override
    protected Control createButtonBar( final Composite parent )
    {
        final Composite composite = (Composite) super.createButtonBar( parent );
        
        if( this.part.getPreferFormStyle() )
        {
            composite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }
        
        this.okButton = getButton( IDialogConstants.OK_ID );
        
        this.part.attach
        (
            new FilteredListener<PartValidationEvent>()
            {
                @Override
                protected void handleTypedEvent( PartValidationEvent event )
                {
                    updateOkButtonEnablement();
                }
            }
        );
        
        updateOkButtonEnablement();
        
        return composite;
    }
    
    @Override
    protected Button createButton( final Composite parent,
                                   final int id,
                                   final String label,
                                   final boolean defaultButton )
    {
        final Button button = super.createButton( parent, id, label, defaultButton );
        
        if( this.part.getPreferFormStyle() )
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
    
    @Override
    protected void okPressed()
    {
        DelayedTasksExecutor.sweep();
        super.okPressed();
    }

    private void updateOkButtonEnablement()
    {
        if( ! this.okButton.isDisposed() )
        {
            final boolean expected = ( this.part.validation().severity() != Status.Severity.ERROR );
            final boolean actual = this.okButton.isEnabled();
            
            if( expected != actual )
            {
                this.okButton.setEnabled( expected );
            }
        }
    }
    
}
