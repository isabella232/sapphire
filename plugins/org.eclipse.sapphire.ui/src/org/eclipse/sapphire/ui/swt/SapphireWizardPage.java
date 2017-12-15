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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartContext;
import org.eclipse.sapphire.ui.SapphirePartEvent;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireWizardPageListener;
import org.eclipse.sapphire.ui.SapphireWizardPagePart;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireWizardPage 

    extends WizardPage
    
{
    private final SapphireWizardPagePart part;
    
    public SapphireWizardPage( final IModelElement rootModelElement,
                               final ISapphireWizardPageDef definition )
    {
        this( (SapphireWizardPagePart) SapphirePart.create( null, rootModelElement, definition, Collections.<String,String>emptyMap() ) );
    }

    public SapphireWizardPage( final SapphireWizardPagePart part )
    {
        super( part.getDefinition().getId().getContent() );
        
        this.part = part;
        
        setTitle( this.part.getLabel() );
        setDescription( this.part.getDescription() );
        
        final ImageDescriptor imageDescriptor = this.part.getImageDescriptor();
        
        if( imageDescriptor != null )
        {
            setImageDescriptor( imageDescriptor );
        }
    }
    
    public void createControl( final Composite parent )
    {
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( gdfill() );
        composite.setLayout( glayout( 1, 0, 0 ) );
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0 ) );
        innerComposite.setLayoutData( gdfill() );
        
        final SapphireRenderingContext context = new SapphireRenderingContext( this.part, innerComposite );
        
        this.part.render( context );
        
        final Runnable messageUpdateOperation = new Runnable()
        {
            public void run()
            {
                final IStatus st = SapphireWizardPage.this.part.getValidationState();
                
                if( st.getSeverity() == Status.ERROR )
                {
                    setMessage( st.getMessage(), ERROR );
                    setPageComplete( false );
                }
                else if( st.getSeverity() == Status.WARNING )
                {
                    setMessage( st.getMessage(), WARNING );
                    setPageComplete( true );
                }
                else
                {
                    setMessage( null );
                    setPageComplete( true );
                }
            }
        };
        
        messageUpdateOperation.run();
        
        final SapphirePartListener messageUpdateListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final IStatus oldValidateState,
                                                   final IStatus newValidationState )
            {
                messageUpdateOperation.run();
            }
        };
        
        this.part.addListener( messageUpdateListener );

        final String helpContextId = this.part.getDefinition().getHelpContextId().getText();
        
        if( helpContextId != null )
        {
            PlatformUI.getWorkbench().getHelpSystem().setHelp( innerComposite, helpContextId );
        }
        
        setControl( composite );
    }
    
    public final void performHelp() 
    {
        final String helpContextId = this.part.getDefinition().getHelpContextId().getText();
        
        if( helpContextId != null )
        {
            PlatformUI.getWorkbench().getHelpSystem().displayHelp( helpContextId );
        }
    }

    @Override
    public void setVisible( final boolean visible )
    {
        super.setVisible( visible );
        
        if( visible )
        {
            final String initialFocusProperty = this.part.getDefinition().getInitialFocus().getContent();
            
            if( initialFocusProperty != null )
            {
                this.part.setFocus( initialFocusProperty );
            }
        }

        final SapphirePartEvent event = new SapphirePartEvent( new SapphirePartContext( this.part ) );
        
        for( SapphirePartListener listener : this.part.getListeners() )
        {
            if( listener instanceof SapphireWizardPageListener )
            {
                final SapphireWizardPageListener wlnr = (SapphireWizardPageListener) listener;
                
                if( visible )
                {
                    wlnr.handleShowPageEvent( event );
                }
                else
                {
                    wlnr.handleHidePageEvent( event );
                }
            }
        }
    }
    
}
