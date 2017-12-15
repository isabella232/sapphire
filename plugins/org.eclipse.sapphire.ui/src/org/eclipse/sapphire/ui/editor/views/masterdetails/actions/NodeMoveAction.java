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

package org.eclipse.sapphire.ui.editor.views.masterdetails.actions;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentTree;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class NodeMoveAction

    extends NodeAction
    
{
    private ModelElementListener listPropertyListener = null;
    private MasterDetailsContentTree contentTree = null;
    private MasterDetailsContentTree.Listener contentTreeListener = null;
    
    @Override
    public boolean isEnabled()
    {
        return ( this.contentTree != null && this.contentTree.getFilterText().length() == 0 );
    }
    
    @Override
    public void setPart( final ISapphirePart part )
    {
        super.setPart( part );

        this.contentTree = getNode().getContentTree();
        
        this.contentTreeListener = new MasterDetailsContentTree.Listener()
        {
            @Override
            public void handleFilterChange( String newFilterText )
            {
                notifyChangeListeners();
            }
        };
        
        this.contentTree.addListener( this.contentTreeListener );
        
        final Runnable op = new Runnable()
        {
            public void run()
            {
                notifyChangeListeners();
            }
        };
        
        final ListProperty property = getList().getParentProperty();
        
        this.listPropertyListener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                if( event.getProperty() == property )
                {
                    Display.getDefault().asyncExec( op );
                }
            }
        };
        
        getNode().getParentNode().getModelElement().addListener( this.listPropertyListener );
    }
    
    protected final ModelElementList<?> getList()
    {
        final MasterDetailsContentNode node = getNode();
        final IModelElement modelElement = node.getModelElement();
        return (ModelElementList<?>) modelElement.getParent();
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        getNode().getModelElement().removeListener( this.listPropertyListener );
        
        if( this.contentTree != null )
        {
            this.contentTree.removeListener( this.contentTreeListener );
        }
    }

}
