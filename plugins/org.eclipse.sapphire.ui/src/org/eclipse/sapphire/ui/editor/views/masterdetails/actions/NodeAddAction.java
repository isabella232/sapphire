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

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentTree;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class NodeAddAction

    extends NodeAction
    
{
    public static final String ACTION_ID = "node:add"; //$NON-NLS-1$
    public static final String ACTION_ID_PREFIX = "node:add:"; //$NON-NLS-1$
    
    private MasterDetailsContentTree contentTree = null;
    private MasterDetailsContentTree.Listener contentTreeListener = null;
    
    public NodeAddAction()
    {
        setId( ACTION_ID );
        setImageDescriptor( SapphireImageCache.ACTION_ADD );
    }
    
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
        
        final MasterDetailsContentNode node = getNode();
        final List<ListProperty> listProperties = node.getChildListProperties();
        final String label;
        
        if( listProperties.size() > 1 || listProperties.get( 0 ).getAllPossibleTypes().size() > 1 )
        {
            label = Resources.addActionLabel;
        }
        else
        {
            final ModelElementType type = listProperties.get( 0 ).getAllPossibleTypes().get( 0 );
            label = NLS.bind( Resources.addActionLabelQualified, type.getLabel( true, CapitalizationType.TITLE_STYLE, false ) );
        }
        
        setLabel( label );

        createChildActions();
    }
    
    protected void createChildActions()
    {
        final MasterDetailsContentNode node = getNode();
        final List<ListProperty> listProperties = node.getChildListProperties();
        
        if( listProperties.size() > 1 || listProperties.get( 0 ).getAllPossibleTypes().size() > 1 )
        {
            final ActionGroup mainAddActionGroup = new ActionGroup();
            addChildActionGroup( mainAddActionGroup );
            
            for( final ListProperty listProperty : listProperties )
            {
                for( final ModelElementType memberType : listProperty.getAllPossibleTypes() )
                {
                    final TypeSpecificAddAction typeSpecificAddAction
                        = new TypeSpecificAddAction( listProperty, memberType );
                    
                    typeSpecificAddAction.setPart( getPart() );
                    mainAddActionGroup.addAction( typeSpecificAddAction );
                }
            }
        }
    }

    @Override
    protected Object run( final Shell shell )
    {
        if( ! getChildActionGroups().isEmpty() )
        {
            throw new UnsupportedOperationException();
        }
        else
        {
            final MasterDetailsContentNode node = getNode();
            final ListProperty property = node.getChildListProperties().get( 0 );
            
            return performAddAction( shell, property, property.getType() );
        }
    }
    
    private IModelElement performAddAction( final Shell shell,
                                            final ListProperty property,
                                            final ModelElementType type )
    {
        if( checkFilter( shell ) == false )
        {
            return null;
        }
        
        final MasterDetailsContentNode node = getNode();
        final IModelElement modelElement = node.getLocalModelElement();
        final ModelElementList<?> list = (ModelElementList<?>) property.invokeGetterMethod( modelElement );
        final IModelElement newModelElement = list.addNewElement( type );
        
        node.getContentTree().notifyOfNodeStructureChange( node );
        
        for( MasterDetailsContentNode n : node.getChildNodes() )
        {
            if( n.getModelElement() == newModelElement )
            {
                n.select();
                getPart().getNearestPart( MasterDetailsPage.class ).setFocusOnDetails();
                break;
            }
        }
        
        return newModelElement;
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.contentTree != null )
        {
            this.contentTree.removeListener( this.contentTreeListener );
        }
    }
    
    protected boolean checkFilter( final Shell shell )
    {
        if( this.contentTree.getFilterText().length() > 0 )
        {
            MessageDialog.openError( shell, Resources.filterPresentDialogTitle, Resources.filterPresentDialogMessage );
            return false;
        }
        
        return true;
    }

    private final class TypeSpecificAddAction
    
        extends NodeAction
        
    {
        private final ListProperty listProperty;
        private final ModelElementType type;
        
        public TypeSpecificAddAction( final ListProperty listProperty,
                                      final ModelElementType type )
        {
            this.listProperty = listProperty;
            this.type = type;

            ImageDescriptor typeSpecificAddImage = NodeAddAction.this.getPart().getImageCache().getImageDescriptor( this.type );
            
            if( typeSpecificAddImage == null )
            {
                typeSpecificAddImage = NodeAddAction.this.getImageDescriptor();
            }

            setId( ACTION_ID_PREFIX + this.type.getModelElementClass().getSimpleName() );
            setLabel( this.type.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
            setImageDescriptor( typeSpecificAddImage );
        }

        @Override
        protected Object run( final Shell shell )
        {
            if( checkFilter( shell ) == false )
            {
                return null;
            }
            
            return performAddAction( shell, this.listProperty, this.type );
        }
    }

    private static final class Resources
        
        extends NLS
    
    {
        public static String addActionLabel;
        public static String addActionLabelQualified;
        public static String filterPresentDialogTitle;
        public static String filterPresentDialogMessage;
        
        static
        {
            initializeMessages( NodeAddAction.class.getName(), Resources.class );
        }
    }
    
}

