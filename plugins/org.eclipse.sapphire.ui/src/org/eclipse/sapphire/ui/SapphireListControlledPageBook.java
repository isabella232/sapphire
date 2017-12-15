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

package org.eclipse.sapphire.ui;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.ui.def.ISapphirePageBookExtDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.renderers.swt.DefaultListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.util.internal.MutableReference;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireListControlledPageBook

    extends SapphirePageBook
    
{
    private ListProperty property;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.property = (ListProperty) resolve( ( (ISapphirePageBookExtDef) this.definition ).getControlProperty().getContent() );
    }

    @Override
    protected Object parsePageKey( final String pageKeyString )
    {
        final ISapphireUiDef rootdef = (ISapphireUiDef) this.definition.getModel();
        final Class<?> cl = rootdef.resolveClass( pageKeyString );
        return ClassBasedKey.create( cl );
    }
    
    @Override
    public void render( final SapphireRenderingContext context )
    {
        super.render( context );
        
        final Table table = SapphirePropertyEditor.findControlForProperty( context.getComposite(), this.property, Table.class );
        
        final ISelectionProvider selectionProvider 
            = (ISelectionProvider) table.getData( DefaultListPropertyEditorRenderer.DATA_SELECTION_PROVIDER );
        
        final MutableReference<IModelElement> selectedModelElementRef = new MutableReference<IModelElement>();

        selectionProvider.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                    final IModelElement newModelElement;
                    final ClassBasedKey newPageKey;
                    
                    if( ! sel.isEmpty() )
                    {
                        newModelElement = (IModelElement) sel.getFirstElement();
                        newPageKey = ClassBasedKey.create( newModelElement );
                    }
                    else
                    {
                        newModelElement = getModelElement();
                        newPageKey = null;
                    }
                    
                    if( selectedModelElementRef.get() != newModelElement )
                    {
                        selectedModelElementRef.set( newModelElement );
                        
                        final Runnable inputChangeOperation = new Runnable()
                        {
                            public void run()
                            {
                                changePage( newModelElement, newPageKey );
                            }
                        };
                        
                        Display.getDefault().syncExec( inputChangeOperation );
                    }
                }
            }
        );
        
        changePage( getModelElement(), (String) null );
    }

}
