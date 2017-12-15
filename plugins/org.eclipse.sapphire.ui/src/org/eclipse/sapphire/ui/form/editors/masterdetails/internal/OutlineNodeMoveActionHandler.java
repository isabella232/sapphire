/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails.internal;

import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsContentOutline;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class OutlineNodeMoveActionHandler extends SapphireActionHandler
{
    private MasterDetailsContentOutline contentTree = null;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );

        this.contentTree = ( (MasterDetailsContentNode) getPart() ).getContentTree();
        
        final Listener contentTreeListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof MasterDetailsContentOutline.FilterChangedEvent )
                {
                    refreshEnabledState();
                }
            }
        };
        
        this.contentTree.attach( contentTreeListener );
        
        final Runnable op = new Runnable()
        {
            public void run()
            {
                refreshEnabledState();
            }
        };
        
        final IModelElement parent = ( (MasterDetailsContentNode) getPart().getParentPart() ).getLocalModelElement();
        final ListProperty property = getList().getParentProperty();
        
        final Listener listPropertyListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                Display.getDefault().asyncExec( op );
            }
        };
        
        parent.attach( listPropertyListener, property.getName() );
        
        refreshEnabledState();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        parent.detach( listPropertyListener, property.getName() );
                        OutlineNodeMoveActionHandler.this.contentTree.detach( contentTreeListener );
                    }
                }
            }
        );
    }
    
    protected final ModelElementList<?> getList()
    {
        return (ModelElementList<?>) getModelElement().parent();
    }
    
    private void refreshEnabledState()
    {
        setEnabled( computeEnabledState() );
    }
    
    protected boolean computeEnabledState()
    {
        return ( this.contentTree != null && this.contentTree.getFilterText().length() == 0 );
    }

}
