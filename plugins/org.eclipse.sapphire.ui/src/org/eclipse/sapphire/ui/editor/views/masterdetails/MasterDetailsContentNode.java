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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.SapphireMultiStatus;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.ProblemOverlayImageDescriptor;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphirePropertyEnabledCondition;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireSection;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeDef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryDef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryEntry;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeFactoryRef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeListEntry;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeRef;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.editor.views.masterdetails.internal.ListPropertyNodeFactory;
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
    private ImpliedElementProperty modelElementProperty;
    private ModelElementListener modelElementListener;
    private MasterDetailsContentNode parentNode;
    private FunctionResult labelFunctionResult;
    private Set<String> listProperties;
    private ImageDescriptor imageDescriptor;
    private ImageDescriptor imageDescriptorWithError;
    private ImageDescriptor imageDescriptorWithWarning;
    private SapphirePartListener childPartListener;
    private List<Object> rawChildren;
    private List<SapphireSection> sections;
    private List<SapphireSection> sectionsReadOnly;
    private boolean expanded;
    private SapphireCondition visibleWhenCondition;
    private final List<SapphireCondition> allConditions = new ArrayList<SapphireCondition>();
    private boolean transformLabelCase = true;
    
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
        
        this.modelElementProperty = (ImpliedElementProperty) resolve( this.definition.getProperty().getContent() );
        
        if( this.modelElementProperty != null )
        {
            this.modelElement = getModelElement().read( this.modelElementProperty );
            
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
        
        this.labelFunctionResult = initExpression
        ( 
            this.modelElement, 
            this.definition.getLabel(), 
            new Runnable()
            {
                public void run()
                {
                    getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
                }
            }
        );
        
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
            this.visibleWhenCondition = SapphireCondition.create( this, visibleWhenConditionClass, visibleWhenConditionParameter );
            
            if( this.visibleWhenCondition != null )
            {
                this.allConditions.add( this.visibleWhenCondition );
                
                this.visibleWhenCondition.addListener
                (
                    new SapphireCondition.Listener()
                    {
                        @Override
                        public void handleConditionChanged()
                        {
                            getContentTree().refresh();
                        }
                    }
                );
            }
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
                    final String parameter = def.getVisibleWhenConditionParameter().getText();
                    factoryVisibleWhenCondition = SapphireCondition.create( this, factoryVisibleWhenConditionClass, parameter );
                    
                    if( factoryVisibleWhenCondition != null )
                    {
                        this.allConditions.add( factoryVisibleWhenCondition );
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
                        node.transformLabelCase = false;
                        
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
        String label = null;
        
        if( this.labelFunctionResult != null )
        {
            label = (String) this.labelFunctionResult.value();
        }
        
        if( label == null )
        {
            label = "#null#";
        }
        else
        {
            label = label.trim();
            
            final CapitalizationType capType = ( this.transformLabelCase ? CapitalizationType.TITLE_STYLE : CapitalizationType.NO_CAPS );
            label = this.definition.adapt( LocalizationService.class ).transform( label, capType, false );
        }
        
        return label;
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
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_NODE );
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
        
        for( SapphireCondition condition : this.allConditions )
        {
            condition.dispose();
        }
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        throw new UnsupportedOperationException();
    }
    
}
