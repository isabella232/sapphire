/******************************************************************************
 * Copyright (c) 2012 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Greg Amerson - [343972] Support image in editor page header
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil.toImageDescriptor;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.ui.SapphirePart.ImageChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireEditorFormPage

    extends FormPage
    
{
    private final SapphireEditor editor;
    private final SapphireEditorPagePart part;
    private final SapphirePartListener listener;
    
    public SapphireEditorFormPage( final SapphireEditor editor,
                                   final SapphireEditorPagePart editorPagePart ) 
    {
        super( editor, null, null );

        this.editor = editor;
        this.part = editorPagePart;
        
        this.listener = new SapphirePartListener()
        {
            @Override
            public void handleEvent( final SapphirePartEvent event )
            {
                if( event instanceof ImageChangedEvent )
                {
                    refreshImage();
                }
            }
        };
        
        this.part.addListener( this.listener );
    }
    
    public final SapphireEditor getEditor()
    {
        return this.editor;
    }
    
    public SapphireEditorPagePart getPart()
    {
        return this.part;
    }
    
    public final IModelElement getModelElement()
    {
        return this.part.getModelElement();
    }
    
    @Override
    public void createPartControl( final Composite parent ) 
    {
       super.createPartControl( parent );
       
       refreshImage();
    } 
    
    private final void refreshImage()
    {
        if( getManagedForm() != null )
        {
            final ScrolledForm form = getManagedForm().getForm();
            final Image oldImage = form.getImage();
            
            if( oldImage != null )
            {
                oldImage.dispose();
            }
            
            final ImageData newImageData = this.part.getPageHeaderImage();
            
            if( newImageData == null )
            {
                form.setImage( null );
            }
            else
            {
                form.setImage( toImageDescriptor( newImageData ).createImage() );
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        this.part.removeListener( this.listener );
        
        if( getManagedForm() != null )
        {
            final Image image = getManagedForm().getForm().getImage();
            
            if( image != null )
            {
                image.dispose();
            }
        }
    }
    
    public abstract String getId();
    
    protected static final class FormEditorRenderingContext extends SapphireRenderingContext
    {
        private final FormToolkit toolkit;
        
        public FormEditorRenderingContext( final ISapphirePart part,
                                           final IManagedForm managedForm )
        {
            this( part, managedForm.getForm().getBody(), managedForm.getToolkit() );
        }
    
        public FormEditorRenderingContext( final ISapphirePart part,
                                           final Composite composite,
                                           final FormToolkit toolkit )
        {
            super( part, composite );
            this.toolkit = toolkit;
        }
    
        public void adapt( final Control control )
        {
            super.adapt( control );
            
            if( control instanceof Composite )
            {
                this.toolkit.adapt( (Composite) control );
            }
            else if( control instanceof Label )
            {
                this.toolkit.adapt( control, false, false );
            }
            else
            {
                this.toolkit.adapt( control, true, true );
            }
        }
    }
    
}