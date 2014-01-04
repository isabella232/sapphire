/******************************************************************************
 * Copyright (c) 2014 Oracle and Other Contributors
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

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.toImageDescriptor;

import java.util.Collections;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.PageHeaderImageEvent;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.PageHeaderTextEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireEditorFormPage extends FormPage implements EditorPagePresentation
{
    private final SapphireEditor editor;
    private Element element;
    private DefinitionLoader.Reference<EditorPageDef> definition;
    private SapphireEditorPagePart part;
    
    public SapphireEditorFormPage( final SapphireEditor editor,
                                   final Element element,
                                   final DefinitionLoader.Reference<EditorPageDef> definition ) 
    {
        super( editor, null, null );
        
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition == null )
        {
            throw new IllegalArgumentException();
        }

        this.editor = editor;
        this.element = element;
        this.definition = definition;
        this.part = (SapphireEditorPagePart) SapphirePart.create( editor, this.element, this.definition.resolve(), Collections.<String,String>emptyMap() );
        
        this.part.attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof PageHeaderTextEvent )
                    {
                        refreshPageHeaderText();
                    }
                    else if( event instanceof PageHeaderImageEvent )
                    {
                        refreshPageHeaderImage();
                    }
                }
            }
        );
    }
    
    public final SapphireEditor getEditor()
    {
        return this.editor;
    }
    
    public SapphireEditorPagePart getPart()
    {
        return this.part;
    }
    
    public final Element getModelElement()
    {
        return this.part.getModelElement();
    }
    
    @Override
    public void createPartControl( final Composite parent ) 
    {
       super.createPartControl( parent );
       
       refreshPageHeaderText();
       refreshPageHeaderImage();
    } 
    
    private final void refreshPageHeaderText()
    {
        if( getManagedForm() != null )
        {
            final ScrolledForm form = getManagedForm().getForm();
            form.setText( LabelTransformer.transform( this.part.getPageHeaderText(), CapitalizationType.TITLE_STYLE, false ) );
        }
    }

    private final void refreshPageHeaderImage()
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
        
        if( getManagedForm() != null )
        {
            final Image image = getManagedForm().getForm().getImage();
            
            if( image != null )
            {
                image.dispose();
            }
        }

        this.element = null;
        
        this.part.dispose();
        this.part = null;
        
        this.definition.dispose();
        this.definition = null;
    }
    
    public abstract String getId();
    
}