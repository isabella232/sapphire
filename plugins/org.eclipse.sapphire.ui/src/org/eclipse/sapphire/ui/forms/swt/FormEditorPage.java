/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.FormEditorPageDef;
import org.eclipse.sapphire.ui.forms.FormEditorPagePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a> 
 */

public final class FormEditorPage extends SapphireEditorFormPage
{
    public FormEditorPage( final SapphireEditor editor,
                           final Element element,
                           final DefinitionLoader.Reference<EditorPageDef> definition ) 
    {
        this( editor, element, definition, null );
    }

    public FormEditorPage( final SapphireEditor editor,
                           final Element element,
                           final DefinitionLoader.Reference<EditorPageDef> definition,
                           final String pageName ) 
    {
        super( editor, element, definition );
        
        String partName = pageName;
        
        if( partName == null )
        {
            partName = getDefinition().getPageName().localized( CapitalizationType.TITLE_STYLE, false );
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
        final Composite body = form.getForm().getBody();
        
        final Presentation presentation = new SwtPresentation( part, null, body.getShell() )
        {
            @Override
            public void render()
            {
                body.setBackground( getPart().getSwtResourceCache().color( Color.WHITE ) );
                body.setBackgroundMode( SWT.INHERIT_DEFAULT );
                
                managedForm.getToolkit().decorateFormHeading( managedForm.getForm().getForm() );
                body.setLayout( glayout( 2, 0, 0 ) );
                
                for( final FormComponentPart child : part.getChildParts() )
                {
                    final Presentation childPresentation = child.createPresentation( null, body );
                    childPresentation.render();
                }

                final ISapphireDocumentation doc = part.definition().getDocumentation().content();
                
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
                        HelpSystem.setHelp( body, docdef );
                    }
                }
                
                final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE );
                final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( this, actions );
                final SapphireToolBarManagerActionPresentation actionPresentation = new SapphireToolBarManagerActionPresentation( actionPresentationManager );
                actionPresentation.setToolBarManager( form.getToolBarManager() );
                actionPresentation.render();
            }
        };
        
        presentation.render();
    }
    
}
