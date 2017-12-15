/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt;

import static org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil.toImageDescriptor;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import org.eclipse.help.IContext;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardContainer2;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireWizardPagePart;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireWizardPage extends WizardPage
{
    private final SapphireWizardPagePart part;
    private final Listener listener;
    
    public SapphireWizardPage( final SapphireWizardPagePart part )
    {
        super( part.definition().getId().getContent() );
        
        this.part = part;
        
        setTitle( this.part.getLabel() );
        setDescription( this.part.getDescription() );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof SapphirePart.ImageChangedEvent )
                {
                    refreshImage();
                }
            }
        };
        
        this.part.attach( this.listener );
        
        refreshImage();
    }
    
    public void createControl( final Composite parent )
    {
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( gdfill() );
        composite.setLayout( glayout( 1, 0, 0 ) );
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayout( glayout( 2, 0, 0 ) );
        innerComposite.setLayoutData( gdfill() );
        
        final SapphireRenderingContext context = new SapphireRenderingContext( this.part, innerComposite )
        {
            @Override
            public void layout()
            {
                super.layout();
                
                final IWizardContainer container = getContainer();
                
                if( container instanceof IWizardContainer2 )
                {
                    ( (IWizardContainer2) container ).updateSize();
                }
            }
        };
        
        this.part.render( context );
        
        final Runnable messageUpdateOperation = new Runnable()
        {
            public void run()
            {
                final Status st = SapphireWizardPage.this.part.getValidationState();
                
                if( st.severity() == Status.Severity.ERROR )
                {
                    setMessage( st.message(), ERROR );
                    setPageComplete( false );
                }
                else if( st.severity() == Status.Severity.WARNING )
                {
                    setMessage( st.message(), WARNING );
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
        
        final Listener messageUpdateListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof SapphirePart.ValidationChangedEvent )
                {
                    messageUpdateOperation.run();
                }
            }
        };
        
        this.part.attach( messageUpdateListener );
        
        final ISapphireDocumentation doc = this.part.definition().getDocumentation().element();
        
        if( doc != null )
        {
            ISapphireDocumentationDef docdef = null;
            
            if( doc instanceof ISapphireDocumentationDef )
            {
                docdef = (ISapphireDocumentationDef) doc;
            }
            else
            {
                docdef = ( (ISapphireDocumentationRef) doc ).resolve();
            }
            
            if( docdef != null )
            {
                SapphireHelpSystem.setHelp( composite, docdef );
            }
        }

        setControl( composite );
    }
    
    public final void performHelp() 
    {
        final IContext documentationContext = this.part.getDocumentationContext();
        
        if ( documentationContext != null  )
        {
            PlatformUI.getWorkbench().getHelpSystem().displayHelp( documentationContext );
        }
    }

    @Override
    public void setVisible( final boolean visible )
    {
        super.setVisible( visible );
        
        this.part.setVisible( visible );
        
        if( visible )
        {
            final String initialFocusProperty = this.part.definition().getInitialFocus().getContent();
            
            if( initialFocusProperty != null )
            {
                this.part.setFocus( initialFocusProperty );
            }
        }
    }
    
    private final void refreshImage()
    {
        setImageDescriptor( toImageDescriptor( this.part.getImage() ) );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        this.part.detach( this.listener );
    }
    
}
