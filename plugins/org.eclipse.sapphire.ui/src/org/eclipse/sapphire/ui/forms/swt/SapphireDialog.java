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

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.DialogPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class SapphireDialog extends Dialog
{
    private Element element;
    private DefinitionLoader.Reference<DialogDef> definition;
    private DialogPart part;
    private Button okButton;
    
    public SapphireDialog( final Shell shell,
                           final Element element,
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
        
        this.part = new DialogPart();
        this.part.init( null, this.element, this.definition.resolve(), Collections.<String,String>emptyMap() );
        this.part.initialize();
    }
    
    public final Element element()
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
        
        shell.setText( this.part.getLabel() );
        
        final Composite composite = (Composite) super.createDialogArea( parent );
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0 ) );
        innerComposite.setLayoutData( gdfill() );
        
        final Presentation presentation = this.part.createPresentation( null, innerComposite );
        
        presentation.render();
        
        final String initialFocusProperty = this.part.definition().getInitialFocus().content();
        
        if( initialFocusProperty != null )
        {
            this.part.setFocus( initialFocusProperty );
        }
        
        shell.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    presentation.dispose();
                    
                    SapphireDialog.this.element = null;
                    
                    SapphireDialog.this.part.dispose();
                    SapphireDialog.this.part = null;
                    
                    SapphireDialog.this.definition.dispose();
                    SapphireDialog.this.definition = null;
                }
            }
        );
        
        return composite;
    }

    @Override
    protected Control createContents( final Composite parent )
    {
        final Composite composite = (Composite) super.createContents( parent );

        composite.setBackground( this.part.getSwtResourceCache().color( this.part.getBackgroundColor() ) );
        composite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        
        return composite;
    }

    @Override
    protected Control createButtonBar( final Composite parent )
    {
        final Composite composite = (Composite) super.createButtonBar( parent );
        
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
    protected boolean isResizable()
    {
        return true;
    }
    
    @Override
    protected final void okPressed()
    {
        DelayedTasksExecutor.sweep();
        
        if( this.part.validation().severity() == Status.Severity.ERROR )
        {
            return;
        }
        
        if( performOkOperation() )
        {
            super.okPressed();
        }
    }
    
    /**
     * Performs any custom tasks that need to run when user closes the dialog by pressing on the ok button. The default
     * implementation does nothing. 
     *  
     * @return true if the dialog can be dismissed or false if an issue was encountered that requires user's attention
     */
    
    protected boolean performOkOperation()
    {
        // The default implementation does nothing.
        
        return true;
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
