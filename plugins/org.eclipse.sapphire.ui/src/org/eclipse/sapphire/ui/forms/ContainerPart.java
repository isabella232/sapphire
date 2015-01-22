/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import static org.eclipse.sapphire.util.CollectionsUtil.equalsBasedOnEntryIdentity;

import java.util.List;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ContainerPart<T extends FormComponentPart> extends FormComponentPart
{
    private Children children;
    
    @Override
    protected Function initVisibleWhenFunction()
    {
        return AndFunction.create
        (
            super.initVisibleWhenFunction(),
            new Function()
            {
                @Override
                public String name()
                {
                    return "VisibleIfChildrenVisible";
                }
    
                @Override
                public FunctionResult evaluate( final FunctionContext context )
                {
                    return new FunctionResult( this, context )
                    {
                        @Override
                        protected void init()
                        {
                            ContainerPart.this.attach
                            (
                                new FilteredListener<VisibleChildrenEvent>()
                                {
                                    @Override
                                    protected void handleTypedEvent( final VisibleChildrenEvent event )
                                    {
                                        refresh();
                                    }
                                }
                            );
                        }
    
                        @Override
                        protected Object evaluate()
                        {
                            return ! children().visible().isEmpty();
                        }
                    };
                }
            }
        );
    }
    
    protected abstract Children initChildren();
    
    public final Children children()
    {
        if( this.children == null )
        {
            this.children = initChildren();
        }
        
        return this.children;
    }

    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();

        for( SapphirePart child : children().visible() )
        {
            factory.merge( child.validation() );
        }
        
        return factory.create();
    }
    
    @Override
    public boolean setFocus()
    {
        broadcast( new FocusReceivedEvent( this ) );
        
        for( SapphirePart child : children().visible() )
        {
            if( child.setFocus() == true )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean setFocus( final ModelPath path )
    {
        broadcast( new FocusReceivedEvent( this ) );
        
        for( SapphirePart child : children().visible() )
        {
            if( child.setFocus( path ) == true )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void dispose()
    {
        if( this.children != null )
        {
            this.children.dispose();
            this.children = null;
        }
        
        super.dispose();
    }
    
    public abstract class Children
    {
        private List<T> all;
        private List<T> visible;
        private boolean initializingVisibleChildren;
        
        protected abstract void init( ListFactory<T> childPartsListFactory );
        
        public List<T> all()
        {
            if( this.all == null )
            {
                final ListFactory<T> factory = ListFactory.start();
                
                init( factory );
                
                this.all = factory.result();
                
                final Listener listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof PartValidationEvent )
                        {
                            refreshValidation();
                        }
                        else if( event instanceof PartVisibilityEvent )
                        {
                            refreshVisibleChildren();
                        }
                    }
                };
                
                for( final SapphirePart part : this.all )
                {
                    part.initialize();
                    part.attach( listener );
                }
            }
            
            return all;
        }
        
        public List<T> visible()
        {
            if( this.visible == null )
            {
                if( this.initializingVisibleChildren )
                {
                    this.visible = ListFactory.empty();
                }
                else
                {
                    this.initializingVisibleChildren = true;
                    
                    try
                    {
                        refreshVisibleChildren();
                    }
                    finally
                    {
                        this.initializingVisibleChildren = false;
                    }
                }
            }
            
            return this.visible;
        }
        
        private void refreshVisibleChildren()
        {
            final ListFactory<T> factory = ListFactory.start();
            
            for( final T child : all() )
            {
                if( child.visible() )
                {
                    factory.add( child );
                }
            }
            
            final List<T> fresh = factory.result();
            
            if( this.visible == null )
            {
                this.visible = fresh;
            }
            else if( ! equalsBasedOnEntryIdentity( this.visible, fresh ) )
            {
                this.visible = fresh;
                broadcast( new VisibleChildrenEvent( ContainerPart.this ) );
                refreshValidation();
            }
        }
        
        private void dispose()
        {
            if( this.all != null )
            {
                for( final FormComponentPart child : this.all )
                {
                    child.dispose();
                }
                
                this.all = null;
                this.visible = null;
            }
        }
    }

    public static final class VisibleChildrenEvent extends PartEvent
    {
        public VisibleChildrenEvent( final ContainerPart<?> part )
        {
            super( part );
        }

        @Override
        public ContainerPart<?> part()
        {
            return (ContainerPart<?>) super.part();
        }
    }

}
