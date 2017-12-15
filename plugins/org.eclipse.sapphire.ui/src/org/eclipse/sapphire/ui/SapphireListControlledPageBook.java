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
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.util.MutableReference;
import org.eclipse.sapphire.ui.def.ISapphirePageBookExtDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.renderers.swt.DefaultListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.swt.SapphireControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireListControlledPageBook extends SapphirePageBook
{
    @Override
    protected Object parsePageKey( final String pageKeyString )
    {
        final ISapphireUiDef rootdef = this.definition.nearest( ISapphireUiDef.class );
        final Class<?> cl = rootdef.resolveClass( pageKeyString );
        return ClassBasedKey.create( cl );
    }
    
    @Override
    public void render( final SapphireRenderingContext context )
    {
        super.render( context );
        
        final IModelElement element = getModelElement();
        final ListProperty property = (ListProperty) resolve( ( (ISapphirePageBookExtDef) this.definition ).getControlProperty().getContent() );
        
        final Table table = findControlForProperty( context.getComposite(), element, property, Table.class );
        
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
                        newModelElement = ( (DefaultListPropertyEditorRenderer.TableRow) sel.getFirstElement() ).element();
                        newPageKey = ClassBasedKey.create( newModelElement );
                    }
                    else
                    {
                        newModelElement = element;
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
        
        changePage( element, (String) null );
    }
    
    private static <T> T findControlForProperty( final Control context,
                                                 final IModelElement element,
                                                 final ModelProperty property,
                                                 final Class<T> type )
    {
        Control root = context;
        
        while( ! ( root instanceof Section || root instanceof SapphireControl ) )
        {
            final Control parent = root.getParent();
            
            if( parent instanceof Shell )
            {
                break;
            }
            
            root = parent;
        }
        
        return findControlForPropertyHelper( root, element, property, type );
    }
    
    @SuppressWarnings( "unchecked" )
    
    private static <T> T findControlForPropertyHelper( final Control context,
                                                       final IModelElement element,
                                                       final ModelProperty property,
                                                       final Class<T> type )
    {
        if( context.getData( SapphirePropertyEditor.DATA_ELEMENT ) == element && 
            context.getData( SapphirePropertyEditor.DATA_PROPERTY ) == property && 
            type.isAssignableFrom( context.getClass() ) )
        {
            return (T) context;
        }
        else if( context instanceof Composite )
        {
            for( Control child : ( (Composite) context ).getChildren() )
            {
                final T control = findControlForPropertyHelper( child, element, property, type );
                
                if( control != null )
                {
                    return control;
                }
            }
        }
        
        return null;
    }

}
