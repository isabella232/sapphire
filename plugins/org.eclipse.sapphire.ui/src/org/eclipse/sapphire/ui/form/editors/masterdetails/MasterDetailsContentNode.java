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

package org.eclipse.sapphire.ui.form.editors.masterdetails;

import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twd;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.runOnDisplayThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ElementValidationEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.IPropertiesViewContributorPart;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePropertyEnabledCondition;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireSection;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeChildDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeFactoryCaseDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeFactoryDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeInclude;
import org.eclipse.sapphire.util.ReadOnlyListFactory;
import org.eclipse.sapphire.util.ReadOnlyMapFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNode

    extends SapphirePart
    implements IPropertiesViewContributorPart
    
{
    private static final ImageData IMG_CONTAINER_NODE
        = ImageData.createFromClassLoader( MasterDetailsContentNode.class, "ContainerNode.png" );

    private static final ImageData IMG_LEAF_NODE
        = ImageData.createFromClassLoader( MasterDetailsContentNode.class, "LeafNode.png" );

    public static final String HINT_HIDE_IF_DISABLED = "hide.if.disabled"; //$NON-NLS-1$
    
    private MasterDetailsContentOutline contentTree;
    private IMasterDetailsContentNodeDef definition;
    private IModelElement modelElement;
    private ImpliedElementProperty modelElementProperty;
    private Listener modelElementListener;
    private MasterDetailsContentNode parentNode;
    private FunctionResult labelFunctionResult;
    private ImageManager imageManager;
    private Listener childPartListener;
    private List<Object> rawChildren;
    private List<SapphireSection> sections;
    private List<SapphireSection> sectionsReadOnly;
    private PropertiesViewContributionManager propertiesViewContributionManager;
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
        
        this.contentTree = nearest( MasterDetailsEditorPagePart.class ).outline();
        this.definition = (IMasterDetailsContentNodeDef) super.definition;
        
        this.modelElementProperty = (ImpliedElementProperty) resolve( this.definition.getProperty().getContent() );
        
        if( this.modelElementProperty != null )
        {
            this.modelElement = getModelElement().read( this.modelElementProperty );
            
            this.modelElementListener = new FilteredListener<PropertyEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyEvent event )
                {
                    handleModelElementChange( event );
                }
            };
            
            this.modelElement.attach( this.modelElementListener );
        }
        else
        {
            this.modelElement = getModelElement();
        }
        
        this.visibleWhenCondition = null;

        Class<?> visibleWhenConditionClass = null;
        String visibleWhenConditionParameter = null;
        
        final Status visibleWhenConditionClassValidation = this.definition.getVisibleWhenConditionClass().validation();
        
        if( visibleWhenConditionClassValidation.severity() != Status.Severity.ERROR )
        {
            final JavaType visibleWhenConditionType = this.definition.getVisibleWhenConditionClass().resolve();
            
            if( visibleWhenConditionType != null )
            {
                visibleWhenConditionClass = visibleWhenConditionType.artifact();
                visibleWhenConditionParameter = this.definition.getVisibleWhenConditionParameter().getText();
            }
        }
        else
        {
            LoggingService.log( visibleWhenConditionClassValidation );
        }
        
        if( visibleWhenConditionClass == null && this.modelElementProperty != null )
        {
            final String hideIfDisabled 
                = this.definition.getHint( IMasterDetailsContentNodeDef.HINT_HIDE_IF_DISABLED );
            
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
                
                this.visibleWhenCondition.attach
                (
                    new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            getContentTree().refresh();
                        }
                    }
                );
            }
        }
        
        this.expanded = false;
        
        final Listener elementValidationListener = new FilteredListener<ElementValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( final ElementValidationEvent event )
            {
                updateValidationState();
            }
        };
        
        this.modelElement.attach( elementValidationListener );
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof DisposeEvent )
                    {
                        getModelElement().detach( elementValidationListener );
                    }
                }
            }
        );
        
        this.childPartListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof ValidationChangedEvent )
                {
                    updateValidationState();
                }
            }
        };
        
        final Listener validationStateListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof ValidationChangedEvent )
                {
                    getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
                }
            }
        };
        
        attach( validationStateListener );
        
        // Label
        
        this.labelFunctionResult = initExpression
        ( 
            this.modelElement, 
            this.definition.getLabel().getContent(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
                }
            }
        );
        
        // Image
        
        final Literal defaultImageLiteral = Literal.create( ( this.definition.getChildNodes().isEmpty() ? IMG_LEAF_NODE : IMG_CONTAINER_NODE ) );
        final Function imageFunction = this.definition.getImage().getContent();
        
        this.imageManager = new ImageManager( this.modelElement, imageFunction, defaultImageLiteral );
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof SapphirePart.ImageChangedEvent )
                    {
                        getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
                    }
                }
            }
        );

        // Sections and Child Nodes
        
        this.sections = new ArrayList<SapphireSection>();
        this.sectionsReadOnly = Collections.unmodifiableList( this.sections );
        this.rawChildren = new ArrayList<Object>();
        
        for( ISapphireSectionDef secdef : this.definition.getSections() )
        {
            final SapphireSection section = new SapphireSection()
            {
                @Override
                protected Object createSectionLayoutData()
                {
                    return twd();
                }
            };
            
            section.init( this, this.modelElement, secdef, this.params );
            section.attach( this.childPartListener );
            
            this.sections.add( section );
        }
        
        for( IMasterDetailsContentNodeChildDef entry : this.definition.getChildNodes() )
        {
            final Map<String,String> params = new HashMap<String,String>( this.params );
            
            if( entry instanceof IMasterDetailsContentNodeInclude )
            {
                final IMasterDetailsContentNodeInclude inc = (IMasterDetailsContentNodeInclude) entry;
                entry = inc.resolve();
                
                if( entry == null )
                {
                    final String msg = NLS.bind( Resources.couldNotResolveNode, inc.getPart() );
                    throw new RuntimeException( msg );
                }

                for( ISapphireParam param : inc.getParams() )
                {
                    final String paramName = param.getName().getText();
                    final String paramValue = param.getValue().getText();
                    
                    if( paramName != null && paramValue != null )
                    {
                        params.put( paramName, paramValue );
                    }
                }
            }

            if( entry instanceof IMasterDetailsContentNodeDef )
            {
                final IMasterDetailsContentNodeDef def = (IMasterDetailsContentNodeDef) entry;
                
                final MasterDetailsContentNode node = new MasterDetailsContentNode();
                node.init( this, this.modelElement, def, params );
                node.attach( this.childPartListener );
                
                this.rawChildren.add( node );
            }
            else if( entry instanceof IMasterDetailsContentNodeFactoryDef )
            {
                final IMasterDetailsContentNodeFactoryDef def = (IMasterDetailsContentNodeFactoryDef) entry;
                
                final ModelProperty property = resolve( getLocalModelElement(), def.getProperty().getContent(), params );
                final NodeFactory factory;
                
                if( property instanceof ListProperty )
                {
                    final ListProperty prop = (ListProperty) property;
                    
                    factory = new NodeFactory( def, params )
                    {
                        @Override
                        public ModelProperty property()
                        {
                            return prop;
                        }
    
                        @Override
                        protected List<IModelElement> elements()
                        {
                            return ReadOnlyListFactory.create( getLocalModelElement().read( prop ) );
                        }
                    };
                }
                else if( property instanceof ElementProperty )
                {
                    final ElementProperty prop = (ElementProperty) property;
                    
                    factory = new NodeFactory( def, params )
                    {
                        @Override
                        public ModelProperty property()
                        {
                            return prop;
                        }
    
                        @Override
                        protected List<IModelElement> elements()
                        {
                            final IModelElement element = getLocalModelElement().read( prop ).element();
                            
                            if( element == null )
                            {
                                return Collections.emptyList();
                            }
                            else
                            {
                                return Collections.singletonList( element );
                            }
                        }
                    };
                }
                else
                {
                    throw new IllegalStateException();
                }
                
                this.rawChildren.add( factory );
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }
    
    public MasterDetailsContentOutline getContentTree()
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

    @Override
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

    public ImageDescriptor getImage()
    {
        return this.imageManager.getImage();
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
    
    public List<ModelProperty> getChildNodeFactoryProperties()
    {
        final ArrayList<ModelProperty> properties = new ArrayList<ModelProperty>();
        
        for( Object object : this.rawChildren )
        {
            if( object instanceof NodeFactory )
            {
                final NodeFactory factory = (NodeFactory) object;
                
                if( factory.visible() )
                {
                    properties.add( factory.property() );
                }
            }
        }
        
        return properties;
    }
    
    public boolean isChildNodeFactoryProperty( final ModelProperty property )
    {
        if( this.rawChildren != null )
        {
            for( Object object : this.rawChildren )
            {
                if( object instanceof NodeFactory )
                {
                    final NodeFactory factory = (NodeFactory) object;
                    
                    if( factory.visible() && factory.property() == property )
                    {
                        return true;
                    }
                }
            }
        }
        
        return false;
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
            else if( entry instanceof NodeFactory )
            {
                final NodeFactory factory = (NodeFactory) entry;
                
                if( factory.visible() )
                {
                    nodes.addAll( factory.nodes() );
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
    
    public MasterDetailsContentNode findNodeByModelElement( final IModelElement element )
    {
        if( getModelElement() == element )
        {
            return this;
        }

        for( MasterDetailsContentNode child : getChildNodes() )
        {
            final MasterDetailsContentNode res = child.findNodeByModelElement( element );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }
    
    public PropertiesViewContributionPart getPropertiesViewContribution()
    {
        if( this.propertiesViewContributionManager == null )
        {
            this.propertiesViewContributionManager = new PropertiesViewContributionManager( this, getLocalModelElement() );
        }
        
        return this.propertiesViewContributionManager.getPropertiesViewContribution();
    }
    
    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_NODE );
    }

    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        if( basis() )
        {
            factory.merge( getModelElement().validation() );
        }
        
        for( SapphirePart child : this.sections )
        {
            factory.merge( child.getValidationState() );
        }

        for( SapphirePart child : getChildNodes() )
        {
            factory.merge( child.getValidationState() );
        }
        
        return factory.create();
    }
    
    private boolean basis()
    {
        final IModelElement element = getModelElement();
        
        if( element.parent() instanceof ModelElementList<?> )
        {
            final ISapphirePart parentPart = getParentPart();
            
            if( parentPart != null && parentPart instanceof MasterDetailsContentNode )
            {
                final MasterDetailsContentNode parentNode = (MasterDetailsContentNode) parentPart;
                
                return ( element != parentNode.getLocalModelElement() );
            }
        }
        
        return false;
    }
    
    @Override
    protected void handleModelElementChange( final Event event )
    {
        super.handleModelElementChange( event );
        
        if( event instanceof PropertyEvent && isChildNodeFactoryProperty( ( (PropertyEvent) event ).property() ) )
        {
            if( event instanceof PropertyContentEvent )
            {
                runOnDisplayThread
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            getContentTree().notifyOfNodeStructureChange( MasterDetailsContentNode.this );
                        }
                    }
                );
            }
            else if( event instanceof PropertyValidationEvent )
            {
                runOnDisplayThread
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            updateValidationState();
                        }
                    }
                );
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.modelElementListener != null )
        {
            this.modelElement.detach( this.modelElementListener );
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
        
        if( this.imageManager != null )
        {
            this.imageManager.dispose();
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        throw new UnsupportedOperationException();
    }
    
    public boolean controls( final IModelElement element )
    {
        if( element == getModelElement() )
        {
            final ISapphirePart parentPart = getParentPart();
            
            if( parentPart != null && parentPart instanceof MasterDetailsContentNode )
            {
                final MasterDetailsContentNode parentNode = (MasterDetailsContentNode) parentPart;
                
                return ( element != parentNode.getLocalModelElement() );
            }
        }
        
        return false;
    }
    
    private abstract class NodeFactory
    {
        private final IMasterDetailsContentNodeFactoryDef definition;
        private final Map<String,String> params;
        private SapphireCondition visibleWhenCondition;
        private final Map<IModelElement,MasterDetailsContentNode> nodesCache = new IdentityHashMap<IModelElement,MasterDetailsContentNode>();
        
        public NodeFactory( final IMasterDetailsContentNodeFactoryDef definition,
                            final Map<String,String> params )
        {
            this.definition = definition;
            this.params = params;
            
            final JavaType visibleWhenConditionClass = this.definition.getVisibleWhenConditionClass().resolve();
            
            if( visibleWhenConditionClass != null )
            {
                final String parameter = this.definition.getVisibleWhenConditionParameter().getText();
                this.visibleWhenCondition = SapphireCondition.create( MasterDetailsContentNode.this, visibleWhenConditionClass.artifact(), parameter );
                
                if( this.visibleWhenCondition != null )
                {
                    MasterDetailsContentNode.this.allConditions.add( this.visibleWhenCondition );
                }
            }
        }
        
        public final boolean visible()
        {
            if( this.visibleWhenCondition != null )
            {
                return this.visibleWhenCondition.getConditionState();
            }
            
            return true;
        }
        
        public abstract ModelProperty property();
        
        protected abstract List<IModelElement> elements();
        
        public final List<MasterDetailsContentNode> nodes()
        {
            final Map<IModelElement,MasterDetailsContentNode> oldCache = ReadOnlyMapFactory.create( this.nodesCache );
            final ReadOnlyListFactory<MasterDetailsContentNode> nodes = ReadOnlyListFactory.create();
            
            for( IModelElement element : elements() )
            {
                MasterDetailsContentNode node = this.nodesCache.get( element );
                
                if( node == null )
                {
                    IMasterDetailsContentNodeDef relevantCaseDef = null;
                    
                    for( IMasterDetailsContentNodeFactoryCaseDef entry : this.definition.getCases() )
                    {
                        final JavaType type = entry.getType().resolve();
                        
                        if( type == null )
                        {
                            relevantCaseDef = entry;
                            break;
                        }
                        else
                        {
                            final Class<?> cl = type.artifact();
        
                            if( cl == null || cl.isAssignableFrom( element.getClass() ) )
                            {
                                relevantCaseDef = entry;
                                break;
                            }
                        }
                    }
                    
                    if( relevantCaseDef == null )
                    {
                        throw new RuntimeException();
                    }
                    
                    node = new MasterDetailsContentNode();
                    
                    // It is very important to put the node into the cache prior to initializing the node as
                    // initialization can case a re-entrant call into this function and we must avoid creating
                    // two nodes for the same element.
                    
                    this.nodesCache.put( element, node );
                    
                    node.init( MasterDetailsContentNode.this, element, relevantCaseDef, this.params );
                    node.attach( MasterDetailsContentNode.this.childPartListener );
                    node.transformLabelCase = false;
                }
                
                nodes.add( node );
            }
            
            for( Map.Entry<IModelElement,MasterDetailsContentNode> entry : oldCache.entrySet() )
            {
                if( ! this.nodesCache.containsKey( entry.getKey() ) )
                {
                    entry.getValue().dispose();
                }
            }
            
            return nodes.export();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String couldNotResolveNode;
        
        static
        {
            initializeMessages( MasterDetailsContentNode.class.getName(), Resources.class );
        }
    }
    
}
