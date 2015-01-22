/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363765] Page book control property should handle model paths
 *    Gregory Amerson - [377329] SapphireListControlledPageBook fails to render with model path for control property
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.ListSelectionService;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.util.IdentityHashSet;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Greg Amerson</a>
 */

public final class DetailSectionPart extends PageBookPart
{
    @Text( "Page book's property reference path \"{0}\" is invalid." )
    private static LocalizableText invalidPath;

    static
    {
        LocalizableText.init( DetailSectionPart.class );
    }

    private Element element;
    private ListProperty property;
    private PropertyEditorPart listPropertyEditorPart;

    @Override
    protected void init()
    {
        super.init();

        final String pathString = ( (DetailSectionDef) this.definition ).getProperty().content();
        final String pathStringSubstituted = substituteParams( pathString, this.params );
        final ModelPath path = new ModelPath( pathStringSubstituted );
        
        this.element = getLocalModelElement();

        for( int i = 0, n = path.length(); i < n; i++ )
        {
            final ModelPath.Segment segment = path.segment( i );

            if( segment instanceof ModelPath.ModelRootSegment )
            {
                this.element = this.element.root();
            }
            else if( segment instanceof ModelPath.ParentElementSegment )
            {
                this.element = this.element.parent().element();
            }
            else if( segment instanceof ModelPath.PropertySegment )
            {
                this.property = (ListProperty) resolve( this.element, ( (ModelPath.PropertySegment) segment ).getPropertyName() );

                if ( i + 1 != n )
                {
                    throw new RuntimeException( invalidPath.format( pathString ) );
                }
            }
            else
            {
                throw new RuntimeException( invalidPath.format( pathString ) );
            }
        }
        
        ISapphirePart ancestor = parent();
        SapphirePart highestUninitializedAncestor = null;
        
        while( ancestor instanceof SapphirePart && ! ( (SapphirePart) ancestor ).initialized() )
        {
            highestUninitializedAncestor = (SapphirePart) ancestor;
            ancestor = ancestor.parent();
        }
        
        if( highestUninitializedAncestor == null )
        {
            initListSelectionServiceListener();
        }
        else
        {
            highestUninitializedAncestor.attach
            (
                new FilteredListener<PartInitializationEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final PartInitializationEvent event )
                    {
                        event.part().detach( this );
                        initListSelectionServiceListener();
                    }
                }
            );
        }
    }
    
    @Override
    protected Function initVisibleWhenFunction()
    {
        final Function function = new Function()
        {
            @Override
            public String name()
            {
                return "VisibleIfListEditorVisible";
            }

            @Override
            public FunctionResult evaluate( final FunctionContext context )
            {
                return new FunctionResult( this, context )
                {
                    private Listener listPropertyEditorListener;
                    
                    @Override
                    protected void init()
                    {
                        this.listPropertyEditorListener = new FilteredListener<PartVisibilityEvent>()
                        {
                            @Override
                            protected void handleTypedEvent( final PartVisibilityEvent event )
                            {
                                refresh();
                            }
                        };
                        
                        DetailSectionPart.this.listPropertyEditorPart.attach( this.listPropertyEditorListener );
                    }

                    @Override
                    protected Object evaluate()
                    {
                        return DetailSectionPart.this.listPropertyEditorPart.visible();
                    }

                    @Override
                    public void dispose()
                    {
                        DetailSectionPart.this.listPropertyEditorPart.detach( this.listPropertyEditorListener );
                        this.listPropertyEditorListener = null;
                        
                        super.dispose();
                    }
                };
            }
        };
        
        function.init();
        
        final Function base = super.initVisibleWhenFunction();
        
        if( base == null )
        {
            return function;
        }
        else
        {
            return AndFunction.create( base, function );
        }
    }

    private void initListSelectionServiceListener()
    {
        this.listPropertyEditorPart = findPropertyEditor( this, this.element, this.property );
        
        if( this.listPropertyEditorPart == null )
        {
            final String msg = "DetailsSectionPart did not find " + this.property;
            throw new RuntimeException( msg );
        }
        
        final ListSelectionService listSelectionService = this.listPropertyEditorPart.service( ListSelectionService.class );
        
        final MutableReference<Element> selectedElementRef = new MutableReference<Element>();

        final Listener listSelectionServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                final List<Element> selectedElements = listSelectionService.selection();
                final Element selectedElement = ( selectedElements.isEmpty() ? null : selectedElements.get( 0 ) );
                
                if( selectedElementRef.get() != selectedElement )
                {
                    selectedElementRef.set( selectedElement );
                    
                    final Runnable inputChangeOperation = new Runnable()
                    {
                        public void run()
                        {
                            changePage( selectedElement );
                        }
                    };
                    
                    Display.getDefault().syncExec( inputChangeOperation );
                }
            }
        };
        
        listSelectionService.attach( listSelectionServiceListener );
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        listSelectionService.detach( listSelectionServiceListener );
                    }
                }
            }
        );
        
        changePage( null );
    }
    
    private PropertyEditorPart findPropertyEditor( final ISapphirePart part,
                                                   final Element element,
                                                   final PropertyDef property )
    {
        return findPropertyEditor( part, element, property, new IdentityHashSet<ISapphirePart>() );
    }

    private PropertyEditorPart findPropertyEditor( final ISapphirePart part,
                                                   final Element element,
                                                   final PropertyDef property,
                                                   final Set<ISapphirePart> searchedParts )
    {
        if( searchedParts.contains( part ) )
        {
            return null;
        }
        
        if( part instanceof PropertyEditorPart )
        {
            final PropertyEditorPart propertyEditorPart = (PropertyEditorPart) part;
            
            if( propertyEditorPart.getLocalModelElement() == element && propertyEditorPart.property().definition() == property )
            {
                return propertyEditorPart;
            }
        }
        
        searchedParts.add( part );
        
        if( part instanceof ContainerPart )
        {
            final ContainerPart<?> container = (ContainerPart<?>) part;
            
            for( SapphirePart child : container.children().all() )
            {
                final PropertyEditorPart propertyEditorPart = findPropertyEditor( child, element, property, searchedParts );
                
                if( propertyEditorPart != null )
                {
                    return propertyEditorPart;
                }
            }
        }
        
        final ISapphirePart parent = part.parent();
        
        if( parent != null )
        {
            return findPropertyEditor( parent, element, property, searchedParts );
        }
        
        return null;
    }

}
