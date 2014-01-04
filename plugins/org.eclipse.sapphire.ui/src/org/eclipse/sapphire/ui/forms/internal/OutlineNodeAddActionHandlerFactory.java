/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.internal;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFactory;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ActionHandlerFactoryDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentOutline;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class OutlineNodeAddActionHandlerFactory extends SapphireActionHandlerFactory
{
    public static final String ID_BASE = "Sapphire.Add.";
    
    private Listener possibleTypesServiceListener;
    private Listener nodeFactoryListener;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerFactoryDef def )
    {
        super.init( action, def );
        
        this.possibleTypesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                broadcast( new Event() );
            }
        };
        
        this.nodeFactoryListener = new FilteredListener<PartVisibilityEvent>()
        {
            @Override
            protected void handleTypedEvent( final PartVisibilityEvent event )
            {
                broadcast( new Event() );
            }
        };
        
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();

        node.executeAfterInitialization
        (
            new Runnable()
            {
                public void run()
                {
                    for( MasterDetailsContentNodePart.NodeFactory factory : node.factories() )
                    {
                        factory.attach( OutlineNodeAddActionHandlerFactory.this.nodeFactoryListener );
                        
                        final PossibleTypesService possibleTypesService = factory.property().service( PossibleTypesService.class );
                        possibleTypesService.attach( OutlineNodeAddActionHandlerFactory.this.possibleTypesServiceListener );
                    }
                    
                    broadcast( new Event() );
                }
            }
        );
    }

    @Override
    public List<SapphireActionHandler> create()
    {
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
        final List<SapphireActionHandler> handlers = new ArrayList<SapphireActionHandler>();
        
        for( MasterDetailsContentNodePart.NodeFactory factory : node.factories() )
        {
            final Property property = factory.property();
            
            if( factory.visible() && ! property.definition().isReadOnly() )
            {
                final PossibleTypesService possibleTypesService = property.service( PossibleTypesService.class );
    
                if( property instanceof ElementList )
                {
                    for( final ElementType memberType : possibleTypesService.types() )
                    {
                        final ListPropertyActionHandler handler = new ListPropertyActionHandler( (ElementList<?>) property, memberType );
                        handlers.add( handler );
                    }
                }
                else if( property instanceof ElementHandle && ! ( property.definition() instanceof ImpliedElementProperty ) )
                {
                    for( final ElementType memberType : possibleTypesService.types() )
                    {
                        final ElementPropertyActionHandler handler = new ElementPropertyActionHandler( (ElementHandle<?>) property, memberType );
                        handlers.add( handler );
                    }
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            
            sort
            (
                handlers,
                new Comparator<SapphireActionHandler>()
                {
                    @Override
                    public int compare( final SapphireActionHandler x, SapphireActionHandler y )
                    {
                        final String xLabel = ( (AbstractActionHandler) x ).type().getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String yLabel = ( (AbstractActionHandler) y ).type().getLabel( true, CapitalizationType.NO_CAPS, false );
                        return xLabel.compareToIgnoreCase( yLabel );
                    }
                }
            );
        }
        
        return handlers;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();

        for( final MasterDetailsContentNodePart.NodeFactory factory : node.factories() )
        {
            factory.detach( this.nodeFactoryListener );
            
            final Property property = factory.property();
            
            if( ! property.disposed() )
            {
                final PossibleTypesService possibleTypesService = property.service( PossibleTypesService.class );
                possibleTypesService.detach( this.possibleTypesServiceListener );
            }
        }
    }

    private static abstract class AbstractActionHandler extends SapphireActionHandler
    {
        private final Property property;
        private final ElementType type;
        private MasterDetailsContentOutline contentTree;
        
        public AbstractActionHandler( final Property property, final ElementType type )
        {
            this.property = property;
            this.type = type;
        }
    
        @Override
        public void init( final SapphireAction action,
                          final ActionHandlerDef def )
        {
            super.init( action, def );
            
            setId( ID_BASE + this.type.getSimpleName() );
            setLabel( this.type.getLabel( true, CapitalizationType.NO_CAPS, false ) );
            
            final ImageData typeSpecificAddImage = this.type.image();
            
            if( typeSpecificAddImage != null )
            {
                addImage( typeSpecificAddImage );
            }
            
            this.contentTree = ( (MasterDetailsContentNodePart) getPart() ).getContentTree();
            
            final Listener contentTreeListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof MasterDetailsContentOutline.FilterChangedEvent )
                    {
                        refreshEnablementState();
                    }
                }
            };
            
            this.contentTree.attach( contentTreeListener );
            
            refreshEnablementState();
            
            attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof DisposeEvent )
                        {
                            AbstractActionHandler.this.contentTree.detach( contentTreeListener );
                        }
                    }
                }
            );
        }
        
        public final ElementType type()
        {
            return this.type;
        }
    
        protected final void refreshEnablementState()
        {
            setEnabled( computeEnablementState() );
        }
        
        protected boolean computeEnablementState()
        {
            return ( this.contentTree != null && this.contentTree.getFilterText().length() == 0 );
        }
        
        public Property property()
        {
            return this.property;
        }
        
        @Override
        protected final Object run( final Presentation context )
        {
            final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) getPart();
            
            Element newModelElement = null;
            
            try
            {
                newModelElement = create( this.property, this.type );
            }
            catch( Exception e )
            {
                // Log this exception unless the cause is EditFailedException. These exception
                // are the result of the user declining a particular action that is necessary
                // before the edit can happen (such as making a file writable).
                
                final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                
                if( editFailedException == null )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }

            if( newModelElement != null )
            {
                for( MasterDetailsContentNodePart n : node.nodes().visible() )
                {
                    if( n.getModelElement() == newModelElement )
                    {
                        n.select();
                        getPart().nearest( MasterDetailsEditorPagePart.class ).setFocusOnDetails();
                        break;
                    }
                }
            }
            
            return newModelElement;
        }
        
        protected abstract Element create( Property property, ElementType type );
    }
    
    private static final class ListPropertyActionHandler extends AbstractActionHandler
    {
        public ListPropertyActionHandler( final ElementList<?> property, final ElementType type )
        {
            super( property, type );
        }
        
        @Override
        protected Element create( final Property property, final ElementType type )
        {
            return ( (ElementList<?>) property ).insert( type );
        }
    }

    private static final class ElementPropertyActionHandler extends AbstractActionHandler
    {
        public ElementPropertyActionHandler( final ElementHandle<?> property, final ElementType type )
        {
            super( property, type );
        }
        
        @Override
        public void init( final SapphireAction action, final ActionHandlerDef def )
        {
            super.init( action, def );
            
            final Listener listener = new FilteredListener<PropertyEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyEvent event )
                {
                    refreshEnablementState();
                }
            };
            
            property().attach( listener );
            
            attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof DisposeEvent )
                        {
                            property().detach( listener );
                        }
                    }
                }
            );
        }

        @Override
        public ElementHandle<?> property()
        {
            return (ElementHandle<?>) super.property();
        }
        
        @Override
        protected Element create( final Property property, final ElementType type )
        {
            return ( (ElementHandle<?>) property ).content( true, type );
        }

        @Override
        protected boolean computeEnablementState()
        {
            boolean state = super.computeEnablementState();
            
            if( state == true )
            {
                state = ( property().content() == null );
            }
            
            return state;
        }
    }

}

