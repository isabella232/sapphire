/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.def.FormEditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarManagerActionPresentation;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a> 
 */

public final class FormEditorPage extends SapphireEditorFormPage
{
    public FormEditorPage( final SapphireEditor editor,
                           final IModelElement element,
                           final DefinitionLoader.Reference<EditorPageDef> definition ) 
    {
        this( editor, element, definition, null );
    }

    public FormEditorPage( final SapphireEditor editor,
                           final IModelElement element,
                           final DefinitionLoader.Reference<EditorPageDef> definition,
                           final String pageName ) 
    {
        super( editor, element, definition );
        
        String partName = pageName;
        
        if( partName == null )
        {
            partName = getDefinition().getPageName().getLocalizedText( CapitalizationType.TITLE_STYLE, false );
        }
        
        setPartName( partName );
    }

    @Override
    public FormEditorPagePart getPart()
    {
        return (FormEditorPagePart) super.getPart();
    }

    public FormEditorPageDef getDefinition()
    {
        return getPart().definition();
    }
    
    @Override
    public String getId()
    {
        return getPartName();
    }

    @Override
    protected void createFormContent( final IManagedForm managedForm )
    {
        final FormEditorPagePart part = getPart();
        final ScrolledForm form = managedForm.getForm();
        
        final FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading( managedForm.getForm().getForm() );
        managedForm.getForm().getBody().setLayout( glayout( 2, 0, 0 ) );
        
        final FormEditorRenderingContext context = new FormEditorRenderingContext( getPart(), managedForm );
        
        for( SapphirePart child : part.getChildParts() )
        {
            child.render( context );
        }

        final ISapphireDocumentation doc = part.definition().getDocumentation().element();
        
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
                SapphireHelpSystem.setHelp( managedForm.getForm().getBody(), docdef );
            }
        }
        
        final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE );
        final SapphireToolBarManagerActionPresentation actionPresentation = new SapphireToolBarManagerActionPresentation( part, getSite().getShell(), actions );
        actionPresentation.setToolBarManager( form.getToolBarManager() );
        actionPresentation.render();
    }
    
}
