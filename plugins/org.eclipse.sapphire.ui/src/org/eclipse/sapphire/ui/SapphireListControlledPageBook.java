/******************************************************************************
 * Copyright (c) 2012 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Gregory Amerson - [363765] Page book control property should handle model paths
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.util.MutableReference;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PageBookExtDef;
import org.eclipse.sapphire.ui.renderers.swt.DefaultListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.swt.SapphireControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Greg Amerson</a>
 */

public final class SapphireListControlledPageBook extends PageBookPart
{
    private IModelElement element;
    private ListProperty property;

    @Override
    protected void init()
    {
        super.init();

        final String pathString = ( (PageBookExtDef) this.definition ).getControlProperty().getContent();
        final String pathStringSubstituted = substituteParams( pathString, this.params );
        final ModelPath path = new ModelPath( pathStringSubstituted );
        
        this.element = getLocalModelElement();

        for( int i = 0, n = path.length(); i < n; i++ )
        {
            final ModelPath.Segment segment = path.segment( i );

            if( segment instanceof ModelPath.ModelRootSegment )
            {
                this.element = (IModelElement) this.element.root();
            }
            else if( segment instanceof ModelPath.ParentElementSegment )
            {
                IModelParticle parent = this.element.parent();

                if ( !( parent instanceof IModelElement ) )
                {
                    parent = parent.parent();
                }

                this.element = (IModelElement) parent;
            }
            else if( segment instanceof ModelPath.PropertySegment )
            {
                this.property = (ListProperty) resolve( this.element, ( (ModelPath.PropertySegment) segment ).getPropertyName() );

                if ( i + 1 != n )
                {
                    throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
                }
            }
            else
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
            }
        }
    }

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

        final Table table = findControlForProperty( context.getComposite(), this.element, this.property, Table.class );
        
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
                        newModelElement = SapphireListControlledPageBook.this.element;
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
        
        changePage( this.element, (String) null );
    }
    
    private static <T> T findControlForProperty( final Control context,
                                                 final IModelElement element,
                                                 final ModelProperty property,
                                                 final Class<T> type )
    {
        Control root = context;
        
        while( ! ( root instanceof SapphireControl ) )
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

    private static final class Resources extends NLS
    {
        public static String invalidPath;

        static
        {
            initializeMessages( SapphireListControlledPageBook.class.getName(), Resources.class );
        }
    }

}
