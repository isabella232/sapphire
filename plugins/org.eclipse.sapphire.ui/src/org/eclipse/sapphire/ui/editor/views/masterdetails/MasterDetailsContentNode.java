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

package org.eclipse.sapphire.ui.editor.views.masterdetails;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.SapphireMultiStatus;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.ProblemOverlayImageDescriptor;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SapphireConditionManager;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartContext;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphirePropertyEnabledCondition;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireSection;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeDef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryDef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryEntry;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryRef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeListEntry;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeRef;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeAddAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeDeleteAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeMoveDownAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeMoveUpAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeShowInSourceAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.internal.ListPropertyNodeFactory;
import org.eclipse.sapphire.ui.internal.ActionsHostUtil;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNode

    extends SapphirePart
    
{
    public static final String HINT_HIDE_IF_DISABLED = "hide.if.disabled"; //$NON-NLS-1$
    private static final ImageDescriptor IMG_DESC_CONTAINER = SapphireImageCache.OBJECT_CONTAINER_NODE;
    private static final ImageDescriptor IMG_DESC_CONTAINER_WITH_ERROR = new ProblemOverlayImageDescriptor( IMG_DESC_CONTAINER, IStatus.ERROR );
    private static final ImageDescriptor IMG_DESC_CONTAINER_WITH_WARNING = new ProblemOverlayImageDescriptor( IMG_DESC_CONTAINER, IStatus.WARNING );
    private static final ImageDescriptor IMG_DESC_LEAF = SapphireImageCache.OBJECT_LEAF_NODE;
    private static final ImageDescriptor IMG_DESC_LEAF_WITH_ERROR = new ProblemOverlayImageDescriptor( IMG_DESC_LEAF, IStatus.ERROR );
    private static final ImageDescriptor IMG_DESC_LEAF_WITH_WARNING = new ProblemOverlayImageDescriptor( IMG_DESC_LEAF, IStatus.WARNING );
    
    private MasterDetailsContentTree contentTree;
    private IMasterDetailsTreeNodeDef definition;
    private IModelElement modelElement;
    private ElementProperty modelElementProperty;
    private ModelElementListener modelElementListener;
    private MasterDetailsContentNode parentNode;
    private ValueProperty labelProperty;
    private Set<String> listProperties;
    private ImageDescriptor imageDescriptor;
    private ImageDescriptor imageDescriptorWithError;
    private ImageDescriptor imageDescriptorWithWarning;
    private SapphirePartListener childPartListener;
    private List<Object> rawChildren;
    private List<SapphireSection> sections;
    private List<SapphireSection> sectionsReadOnly;
    private List<ActionGroup> menuActions = null;
    private List<ActionGroup> toolbarActions = null;
    private boolean expanded;
    private SapphireConditionManager visibleWhenCondition;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ISapphirePart parent = getParentPart();

        if( parent instanceof MasterDetailsContentNode )
        {
            this.parentNode = (MasterDetailsContentNode) parent;
        }
        else
        {
            this.parentNode = null;
        }
        
        this.contentTree = getNearestPart( MasterDetailsPage.class ).getContentTree();
        this.definition = (IMasterDetailsTreeNodeDef) super.definition;
        
        this.modelElementProperty = (ElementProperty) resolve( this.definition.getProperty().getContent() );
        
        if( this.modelElementProperty != null )
        {
            this.modelElement = (IModelElement) this.modelElementProperty.invokeGetterMethod( getModelElement() );
            
            this.modelElementListener = new ModelElementListener()
            {
                @Override
                public void propertyChanged( final ModelPropertyChangeEvent event )
                {
                    handleModelElementChange( event );
                }
            };
            
            this.modelElement.addListener( this.modelElementListener );
        }
        else
        {
            this.modelElement = getModelElement();
        }
        
        this.labelProperty = (ValueProperty) resolve( this.definition.getDynamicLabelProperty().getContent() );        
        
        this.imageDescriptor = this.definition.getImagePath().resolve();
        this.imageDescriptorWithError = null;
        this.imageDescriptorWithWarning = null;
        
        this.visibleWhenCondition = null;

        Class<?> visibleWhenConditionClass = null;
        String visibleWhenConditionParameter = null;
        
        final IStatus visibleWhenConditionClassValidation = this.definition.getVisibleWhenConditionClass().validate();
        
        if( visibleWhenConditionClassValidation.getSeverity() != IStatus.ERROR )
        {
            visibleWhenConditionClass = this.definition.getVisibleWhenConditionClass().resolve();
            visibleWhenConditionParameter = this.definition.getVisibleWhenConditionParameter().getText();
        }
        else
        {
            SapphireUiFrameworkPlugin.log( visibleWhenConditionClassValidation );
        }
        
        if( visibleWhenConditionClass == null && this.modelElementProperty != null )
        {
            final String hideIfDisabled 
                = this.definition.getHint( IMasterDetailsTreeNodeDef.HINT_HIDE_IF_DISABLED );
            
            if( Boolean.parseBoolean( hideIfDisabled ) )
            {
                visibleWhenConditionClass = SapphirePropertyEnabledCondition.class;
                visibleWhenConditionParameter = this.modelElementProperty.getName();
            }
        }
        
        if( visibleWhenConditionClass != null )
        {
            final Runnable onConditionChangeCallback = new Runnable()
            {
                public void run()
                {
                    getContentTree().refresh();
                }
            };
            
            this.visibleWhenCondition = SapphireConditionManager.create( this, visibleWhenConditionClass, visibleWhenConditionParameter, onConditionChangeCallback );
        }
        
        this.expanded = false;
        
        this.childPartListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final IStatus oldValidateState,
                                                   final IStatus newValidationState )
            {
                updateValidationState();
            }
        };
        
        final SapphirePartListener validationStateListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final IStatus oldValidateState,
                                                   final IStatus newValidationState )
            {
                getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
            }
        };
        
        addListener( validationStateListener );
        
        // Sections
        
        this.sections = new ArrayList<SapphireSection>();
        this.sectionsReadOnly = Collections.unmodifiableList( this.sections );
        
        for( ISapphireSectionDef secdef : this.definition.getSections() )
        {
            final SapphireSection section = new SapphireSection();
            section.init( this, this.modelElement, secdef, Collections.<String,String>emptyMap() );
            section.addListener( this.childPartListener );
            
            this.sections.add( section );
        }
        
        // Child Nodes
        
        this.rawChildren = new ArrayList<Object>();
        
        for( IMasterDetailsTreeNodeListEntry entry : this.definition.getChildNodes() )
        {
            if( entry instanceof IMasterDetailsTreeNodeDef || entry instanceof IMasterDetailsTreeNodeRef )
            {
                final IMasterDetailsTreeNodeDef def;
                
                if( entry instanceof IMasterDetailsTreeNodeDef )
                {
                    def = (IMasterDetailsTreeNodeDef) entry;
                }
                else
                {
                    def = ( (IMasterDetailsTreeNodeRef) entry ).resolve();
                }
                
                final MasterDetailsContentNode node = new MasterDetailsContentNode();
                node.init( this, this.modelElement, def, this.params );
                node.addListener( this.childPartListener );
                
                this.rawChildren.add( node );
            }
            else if( entry instanceof IMasterDetailsTreeNodeFactoryDef || entry instanceof IMasterDetailsTreeNodeFactoryRef )
            {
                final IMasterDetailsTreeNodeFactoryDef def;
                
                if( entry instanceof IMasterDetailsTreeNodeFactoryDef )
                {
                    def = (IMasterDetailsTreeNodeFactoryDef) entry;
                }
                else
                {
                    def = ( (IMasterDetailsTreeNodeFactoryRef) entry ).resolve();
                }
                
                final ListProperty listProperty = (ListProperty) resolve( getLocalModelElement(), def.getListProperty().getContent() );
                
                SapphireCondition factoryVisibleWhenCondition = null;
                
                final Class<?> factoryVisibleWhenConditionClass = def.getVisibleWhenConditionClass().resolve();
                
                if( factoryVisibleWhenConditionClass != null )
                {
                    try
                    {
                        factoryVisibleWhenCondition = (SapphireCondition) factoryVisibleWhenConditionClass.newInstance();
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                    }
                    
                    if( factoryVisibleWhenCondition != null )
                    {
                        final String parameter = def.getVisibleWhenConditionParameter().getText();
                        factoryVisibleWhenCondition.init( new SapphirePartContext( this ), parameter );                
                    }
                }
                
                final ListPropertyNodeFactory factory = new ListPropertyNodeFactory( this.modelElement, listProperty, factoryVisibleWhenCondition )
                {
                    protected MasterDetailsContentNode createNode( final IModelElement listEntryModelElement )
                    {
                        IMasterDetailsTreeNodeDef listEntryNodeDef = null;
                        
                        for( IMasterDetailsTreeNodeFactoryEntry entry : def.getTypeSpecificDefinitions() )
                        {
                            final Class<?> type = entry.getType().resolve();
                            
                            if( type == null || type.isAssignableFrom( listEntryModelElement.getClass() ) )
                            {
                                listEntryNodeDef = entry;
                                break;
                            }
                        }
                        
                        if( listEntryNodeDef == null )
                        {
                            throw new RuntimeException();
                        }
                        
                        final MasterDetailsContentNode node = new MasterDetailsContentNode();
                        node.init( MasterDetailsContentNode.this, listEntryModelElement, listEntryNodeDef, MasterDetailsContentNode.this.params );
                        node.addListener( MasterDetailsContentNode.this.childPartListener );
                        
                        return node;
                    }
                };
                
                this.rawChildren.add( factory );
            }
        }
        
        // Listeners
        
        this.listProperties = new HashSet<String>();
        
        for( Object entry : this.rawChildren )
        {
            if( entry instanceof ListPropertyNodeFactory )
            {
                this.listProperties.add( ( (ListPropertyNodeFactory) entry ).getListProperty().getName() );
            }
        }
    }
    
    public MasterDetailsContentTree getContentTree()
    {
        return this.contentTree;
    }

    public MasterDetailsContentNode getParentNode()
    {
        return this.parentNode;
    }

    public boolean isAncestorOf( final MasterDetailsContentNode node )
    {
        MasterDetailsContentNode n = node;
        
        while( n != null )
        {
            if( n == this )
            {
                return true;
            }
            
            n = n.getParentNode();
        }
        
        return false;
    }

    public IModelElement getLocalModelElement()
    {
        return this.modelElement;
    }
    
    public String getLabel()
    {
        if( this.labelProperty != null )
        {
            final Method labelPropertyGetter = this.labelProperty.getGetterMethod();            
            final Value<?> value;
            
            try
            {
                value = (Value<?>) labelPropertyGetter.invoke( this.modelElement );
            }
            catch( Exception e )
            {
                throw new RuntimeException( e );
            }
            
            String label = value.getText( false );
            
            if( label == null )
            {
                label = this.definition.getDynamicLabelNullValueText().getLocalizedText();
            }
            
            return label;
        }
        else
        {
            return this.definition.getLabel().getLocalizedText();
        }
    }

    public ImageDescriptor getImageDescriptor()
    {
        final IStatus st = getValidationState();
        final int severity = st.getSeverity();
        final ImageDescriptor base;
    
        if( this.definition.getUseModelElementImage().getContent() )
        {
            final Image img = getImageCache().getImage( getLocalModelElement() );
            base = ImageDescriptor.createFromImage( img );
        }
        else
        {
            base = this.imageDescriptor;
        }
        
        if( base == null )
        {
            if( severity == IStatus.ERROR )
            {
                if( hasChildNodes() )
                {
                    return IMG_DESC_CONTAINER_WITH_ERROR;
                }
                else
                {
                    return IMG_DESC_LEAF_WITH_ERROR;
                }
            }
            else if( severity == IStatus.WARNING )
            {
                if( hasChildNodes() )
                {
                    return IMG_DESC_CONTAINER_WITH_WARNING;
                }
                else
                {
                    return IMG_DESC_LEAF_WITH_WARNING;
                }
            }
            else
            {
                if( hasChildNodes() )
                {
                    return IMG_DESC_CONTAINER;
                }
                else
                {
                    return IMG_DESC_LEAF;
                }
            }
        }
        else
        {
            if( severity == IStatus.ERROR )
            {
                if( this.imageDescriptorWithError == null )
                {
                    this.imageDescriptorWithError = new ProblemOverlayImageDescriptor( base, Status.ERROR );
                }
                
                return this.imageDescriptorWithError;
            }
            else if( severity == IStatus.WARNING )
            {
                if( this.imageDescriptorWithWarning == null )
                {
                    this.imageDescriptorWithWarning = new ProblemOverlayImageDescriptor( base, Status.WARNING );
                }
                
                return this.imageDescriptorWithWarning;
            }
            else
            {
                return base;
            }
        }
    }

    public boolean isVisible()
    {
        if( this.visibleWhenCondition != null )
        {
            return this.visibleWhenCondition.getConditionState();
        }
        
        return true;
    }

    public boolean isExpanded()
    {
        return this.expanded;
    }
    
    public void setExpanded( final boolean expanded )
    {
        setExpanded( expanded, false );
    }
    
    public void setExpanded( final boolean expanded,
                             final boolean applyToChildren )
    {
        if( this.parentNode != null && ! this.parentNode.isExpanded() && expanded == true )
        {
            this.parentNode.setExpanded( true );
        }
        
        if( this.expanded != expanded )
        {
            if( ! expanded )
            {
                final MasterDetailsContentNode selection = getContentTree().getSelectedNode();
                
                if( selection != null && isAncestorOf( selection ) )
                {
                    select();
                }
            }
            
            if( expanded )
            {
                this.expanded = expanded;
                getContentTree().notifyOfNodeExpandedStateChange( this );
            }
        }
            
        if( applyToChildren )
        {
            for( MasterDetailsContentNode child : getChildNodes() )
            {
                if( child.hasChildNodes() )
                {
                    child.setExpanded( expanded, applyToChildren );
                }
            }
        }

        if( this.expanded != expanded )
        {
            if( ! expanded )
            {
                this.expanded = expanded;
                getContentTree().notifyOfNodeExpandedStateChange( this );
            }
        }
    }
    
    public List<MasterDetailsContentNode> getExpandedNodes()
    {
        final List<MasterDetailsContentNode> result = new ArrayList<MasterDetailsContentNode>();
        getExpandedNodes( result );
        return result;
    }
    
    public void getExpandedNodes( final List<MasterDetailsContentNode> result )
    {
        if( isExpanded() )
        {
            result.add( this );
            
            for( MasterDetailsContentNode child : getChildNodes() )
            {
                child.getExpandedNodes( result );
            }
        }
    }
    
    public void select()
    {
        getContentTree().setSelectedNode( this );
    }
    
    public List<ActionGroup> getMenuActions()
    {
        if( this.menuActions == null )
        {
            this.menuActions = new ArrayList<ActionGroup>();
            
            final boolean isSameModelElementAsParent 
                = ( this.parentNode != null ? this.modelElement == this.parentNode.getLocalModelElement() : false );
            
            final ActionGroup addDeleteActionGroup = new ActionGroup();
            
            if( ! getChildListProperties().isEmpty() )
            {
                addDeleteActionGroup.addAction( new NodeAddAction() );
            }
            
            if( ! isSameModelElementAsParent && this.modelElement instanceof IRemovable )
            {
                addDeleteActionGroup.addAction( new NodeDeleteAction() );
            }
            
            if( addDeleteActionGroup.getActions().size() > 0 )
            {
                this.menuActions.add( addDeleteActionGroup );
            }
            
            if( ! isSameModelElementAsParent )
            {
                final IModelParticle parent = this.modelElement.getParent();
                
                if( parent instanceof ModelElementList<?> )
                {
                    final ActionGroup moveActionGroup = new ActionGroup();
                    this.menuActions.add( moveActionGroup );

                    moveActionGroup.addAction( new NodeMoveUpAction() );
                    moveActionGroup.addAction( new NodeMoveDownAction() );
                }

                final ActionGroup goToSourceActionGroup = new ActionGroup();
                goToSourceActionGroup.addAction( new NodeShowInSourceAction() );
                this.menuActions.add( goToSourceActionGroup );
            }

            ActionsHostUtil.initActions( this.menuActions, this.definition.getActionSetDef() );
            
            for( ActionGroup group : this.menuActions )
            {
                for( Action action : group.getActions() )
                {
                    action.setPart( this );
                }
            }
        }
        
        return this.menuActions;
    }

    public List<ActionGroup> getToolbarActions()
    {
        if( this.toolbarActions == null )
        {
            this.toolbarActions = new ArrayList<ActionGroup>();
            
            for( ActionGroup group : this.toolbarActions )
            {
                for( Action action : group.getActions() )
                {
                    action.setPart( this );
                }
            }
        }
        
        return this.toolbarActions;
    }
    
    @Override
    public Action getAction( final String id )
    {
        for( ActionGroup group : getMenuActions() )
        {
            final Action action = group.getAction( id );
            
            if( action != null )
            {
                return action;
            }
        }

        for( ActionGroup group : getToolbarActions() )
        {
            final Action action = group.getAction( id );
            
            if( action != null )
            {
                return action;
            }
        }
        
        final ISapphirePart parent = getParentPart();
        
        if( parent != null )
        {
            return parent.getAction( id );
        }
        else
        {
            return null;
        }
    }
    
    public List<SapphireSection> getSections()
    {
        return this.sectionsReadOnly;
    }
    
    public List<ListProperty> getChildListProperties()
    {
        final ArrayList<ListProperty> listProperties = new ArrayList<ListProperty>();
        
        for( Object object : this.rawChildren )
        {
            if( object instanceof ListPropertyNodeFactory )
            {
                final ListPropertyNodeFactory factory = (ListPropertyNodeFactory) object;
                
                if( factory.isVisible() )
                {
                    listProperties.add( factory.getListProperty() );
                }
            }
        }
        
        return listProperties;
    }
    
    public boolean hasChildNodes()
    {
        return ! this.rawChildren.isEmpty();
    }
    
    public List<MasterDetailsContentNode> getChildNodes()
    {
        final ArrayList<MasterDetailsContentNode> nodes = new ArrayList<MasterDetailsContentNode>();
        
        for( Object entry : this.rawChildren )
        {
            if( entry instanceof MasterDetailsContentNode )
            {
                final MasterDetailsContentNode node = (MasterDetailsContentNode) entry;
                
                if( node.isVisible() )
                {
                    nodes.add( node );
                }
            }
            else if( entry instanceof ListPropertyNodeFactory )
            {
                final ListPropertyNodeFactory factory = (ListPropertyNodeFactory) entry;
                
                if( factory.isVisible() )
                {
                    nodes.addAll( factory.createNodes() );
                }
            }
            else
            {
                throw new IllegalStateException( entry.getClass().getName() );
            }
        }
        
        return nodes;
    }
    
    public MasterDetailsContentNode getChildNodeByLabel( final String label )
    {
        for( MasterDetailsContentNode child : getChildNodes() )
        {
            if( label.equals( child.getLabel() ) )
            {
                return child;
            }
        }
        
        return null;
    }
    
    @Override
    protected IStatus computeValidationState()
    {
        final SapphireMultiStatus st = new SapphireMultiStatus();
        
        for( SapphirePart child : this.sections )
        {
            st.add( child.getValidationState() );
        }

        for( SapphirePart child : getChildNodes() )
        {
            st.add( child.getValidationState() );
        }
        
        return st;
    }
    
    @Override
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        super.handleModelElementChange( event );
        
        final ModelProperty property = event.getProperty();
        
        if( this.labelProperty == property )
        {
            final Runnable notifyOfUpdateOperation = new Runnable()
            {
                public void run()
                {
                    getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
                }
            };
            
            Display.getDefault().asyncExec( notifyOfUpdateOperation );
        }
        
        if( this.listProperties != null && this.listProperties.contains( property.getName() ) )
        {
            final Runnable notifyOfStructureChangeOperation = new Runnable()
            {
                public void run()
                {
                    getContentTree().notifyOfNodeStructureChange( MasterDetailsContentNode.this );
                    updateValidationState();
                }
            };
            
            Display.getDefault().asyncExec( notifyOfStructureChangeOperation );
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.modelElementListener != null )
        {
            this.modelElement.removeListener( this.modelElementListener );
        }
        
        for( SapphirePart child : this.sections )
        {
            child.dispose();
        }
        
        for( SapphirePart child : getChildNodes() )
        {
            child.dispose();
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        throw new UnsupportedOperationException();
    }
    
}
