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
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ElementValidationEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.TransientProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.IPropertiesViewContributorPart;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SectionPart;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.SectionDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeChildDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeFactoryCaseDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeFactoryDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsContentNodeInclude;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.MapFactory;

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

    private MasterDetailsContentOutline contentTree;
    private MasterDetailsContentNodeDef definition;
    private IModelElement modelElement;
    private ImpliedElementProperty modelElementProperty;
    private Listener modelElementListener;
    private MasterDetailsContentNode parentNode;
    private FunctionResult labelFunctionResult;
    private ImageManager imageManager;
    private Listener childPartListener;
    private List<Object> rawChildren;
    private MasterDetailsContentNodeList nodes;
    private List<SectionPart> sections;
    private PropertiesViewContributionManager propertiesViewContributionManager;
    private boolean expanded;
    private boolean transformLabelCase = true;
    private final Function nodeFactoryVisibleFunction;
    
    public MasterDetailsContentNode()
    {
        this( null );
    }
    
    public MasterDetailsContentNode( final Function nodeFactoryVisibleFunction )
    {
        this.nodeFactoryVisibleFunction = nodeFactoryVisibleFunction;
    }
    
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
        this.definition = (MasterDetailsContentNodeDef) super.definition;
        
        this.modelElementProperty = (ImpliedElementProperty) resolve( this.definition.getProperty().getContent() );
        
        if( this.modelElementProperty != null )
        {
            this.modelElement = getModelElement().read( this.modelElementProperty ).element();
            
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
        
        this.expanded = false;
        
        final Listener elementValidationListener = new FilteredListener<ElementValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( final ElementValidationEvent event )
            {
                refreshValidation();
            }
        };
        
        this.modelElement.attach( elementValidationListener );
        
        this.childPartListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof PartValidationEvent || event instanceof PartVisibilityEvent )
                {
                    refreshValidation();
                }
            }
        };
        
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
                    broadcast( new LabelChangedEvent( MasterDetailsContentNode.this ) );
                }
            }
        );
        
        // Image
        
        final Literal defaultImageLiteral = Literal.create( ( this.definition.getChildNodes().isEmpty() ? IMG_LEAF_NODE : IMG_CONTAINER_NODE ) );
        final Function imageFunction = this.definition.getImage().getContent();
        
        this.imageManager = new ImageManager( this.modelElement, imageFunction, defaultImageLiteral );
        
        // Sections and Child Nodes
        
        this.rawChildren = new ArrayList<Object>();
        
        final ListFactory<SectionPart> sectionsListFactory = ListFactory.start();
        
        for( SectionDef secdef : this.definition.getSections() )
        {
            final SectionPart section = new SectionPart()
            {
                @Override
                protected Object createSectionLayoutData()
                {
                    return twd();
                }
            };
            
            section.init( this, this.modelElement, secdef, this.params );
            section.attach( this.childPartListener );
            
            sectionsListFactory.add( section );
        }
        
        this.sections = sectionsListFactory.result();
        
        for( MasterDetailsContentNodeChildDef entry : this.definition.getChildNodes() )
        {
            final Map<String,String> params = new HashMap<String,String>( this.params );
            
            if( entry instanceof MasterDetailsContentNodeInclude )
            {
                final MasterDetailsContentNodeInclude inc = (MasterDetailsContentNodeInclude) entry;
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

            if( entry instanceof MasterDetailsContentNodeDef )
            {
                final MasterDetailsContentNodeDef def = (MasterDetailsContentNodeDef) entry;
                
                final MasterDetailsContentNode node = new MasterDetailsContentNode();
                node.init( this, this.modelElement, def, params );
                node.attach( this.childPartListener );
                
                this.rawChildren.add( node );
            }
            else if( entry instanceof MasterDetailsContentNodeFactoryDef )
            {
                final NodeFactory factory = new NodeFactory( getLocalModelElement(), (MasterDetailsContentNodeFactoryDef) entry, params );
                this.rawChildren.add( factory );
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        
        refreshNodes();
        
        attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof PartVisibilityEvent || event instanceof NodeListEvent )
                    {
                        getContentTree().refreshSelection();
                    }
                    else if( event instanceof DisposeEvent )
                    {
                        getModelElement().detach( elementValidationListener );
                    }
                }
            }
        );
    }
    
    @Override
    protected Function initVisibleWhenFunction()
    {
        return AndFunction.create
        (
            super.initVisibleWhenFunction(),
            createVersionCompatibleFunction( getModelElement(), this.modelElementProperty ),
            (
                this.nodeFactoryVisibleFunction != null ?
                this.nodeFactoryVisibleFunction :
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
                                final Listener listener = new FilteredListener<PartVisibilityEvent>()
                                {
                                    @Override
                                    protected void handleTypedEvent( final PartVisibilityEvent event )
                                    {
                                        refresh();
                                    }
                                };
                                
                                for( SapphirePart section : getSections() )
                                {
                                    section.attach( listener );
                                }
                                
                                for( Object entry : MasterDetailsContentNode.this.rawChildren )
                                {
                                    if( entry instanceof MasterDetailsContentNode )
                                    {
                                        ( (MasterDetailsContentNode) entry ).attach( listener );
                                    }
                                    else if( entry instanceof NodeFactory )
                                    {
                                        ( (NodeFactory) entry ).attach( listener );
                                    }
                                }
                            }
    
                            @Override
                            protected Object evaluate()
                            {
                                boolean visible = false;
                                
                                for( SectionPart section : getSections() )
                                {
                                    if( section.visible() )
                                    {
                                        visible = true;
                                        break;
                                    }
                                }
                                
                                if( ! visible )
                                {
                                    visible = ( getChildNodeFactoryProperties().size() > 0 );
                                }
                                
                                if( ! visible )
                                {
                                    for( Object entry : MasterDetailsContentNode.this.rawChildren )
                                    {
                                        if( entry instanceof MasterDetailsContentNode )
                                        {
                                            final MasterDetailsContentNode node = (MasterDetailsContentNode) entry;
                                            
                                            if( node.visible() )
                                            {
                                                visible = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                
                                return visible;
                            }
                        };
                    }
                }
            )
        );
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
            for( MasterDetailsContentNode child : nodes() )
            {
                if( ! child.nodes().visible().isEmpty() )
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
            
            for( MasterDetailsContentNode child : nodes() )
            {
                child.getExpandedNodes( result );
            }
        }
    }
    
    public void select()
    {
        getContentTree().setSelectedNode( this );
    }
    
    public List<SectionPart> getSections()
    {
        return this.sections;
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
    
    public List<NodeFactory> factories()
    {
        final ListFactory<NodeFactory> factories = ListFactory.start();
        
        for( Object entry : this.rawChildren )
        {
            if( entry instanceof NodeFactory )
            {
                factories.add( (NodeFactory) entry );
            }
        }
        
        return factories.result();
    }
    
    public MasterDetailsContentNodeList nodes()
    {
        if( this.nodes == null )
        {
            this.nodes = new MasterDetailsContentNodeList( Collections.<MasterDetailsContentNode>emptyList() );
        }
        
        return this.nodes;
    }
    
    public MasterDetailsContentNode findNode( final String label )
    {
        for( MasterDetailsContentNode child : nodes() )
        {
            if( label.equalsIgnoreCase( child.getLabel() ) )
            {
                return child;
            }
        }
        
        return null;
    }
    
    public MasterDetailsContentNode findNode( final IModelElement element )
    {
        if( getModelElement() == element )
        {
            return this;
        }

        for( MasterDetailsContentNode child : nodes() )
        {
            final MasterDetailsContentNode res = child.findNode( element );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }
    
    private void refreshNodes()
    {
        final ListFactory<MasterDetailsContentNode> nodeListFactory = ListFactory.start();
        
        for( Object entry : this.rawChildren )
        {
            if( entry instanceof MasterDetailsContentNode )
            {
                nodeListFactory.add( (MasterDetailsContentNode) entry );
            }
            else if( entry instanceof NodeFactory )
            {
                nodeListFactory.add( ( ((NodeFactory) entry) ).nodes() );
            }
            else
            {
                throw new IllegalStateException( entry.getClass().getName() );
            }
        }
        
        final MasterDetailsContentNodeList nodes = new MasterDetailsContentNodeList( nodeListFactory.result() );
        
        if( this.nodes == null )
        {
            this.nodes = nodes;
        }
        else if( ! this.nodes.equals( nodes ) )
        {
            this.nodes = nodes;
            broadcast( new NodeListEvent( this ) );
        }
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
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        if( basis() )
        {
            factory.merge( getModelElement().validation() );
        }
        
        for( SapphirePart section : this.sections )
        {
            if( section.visible() )
            {
                factory.merge( section.validation() );
            }
        }

        for( SapphirePart node : nodes() )
        {
            if( node.visible() )
            {
                factory.merge( node.validation() );
            }
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
                refreshNodes();
            }
            else if( event instanceof PropertyValidationEvent )
            {
                runOnDisplayThread
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            refreshValidation();
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
        
        for( SapphirePart section : this.sections )
        {
            section.dispose();
        }
        
        for( SapphirePart node : nodes() )
        {
            node.dispose();
        }
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        if( this.imageManager != null )
        {
            this.imageManager.dispose();
        }
        
        for( Object object : this.rawChildren )
        {
            if( object instanceof NodeFactory )
            {
                ( (NodeFactory) object ).dispose();
            }
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
    
    public final class NodeFactory
    {
        private final IModelElement element;
        private final ModelProperty property;
        private final MasterDetailsContentNodeFactoryDef definition;
        private final Map<String,String> params;
        private final FunctionResult visibleWhenFunctionResult;
        private final Function visibleWhenFunctionForNodes;
        private final Map<IModelElement,MasterDetailsContentNode> nodesCache = new IdentityHashMap<IModelElement,MasterDetailsContentNode>();
        private final ListenerContext listeners = new ListenerContext();
        
        public NodeFactory( final IModelElement element,
                            final MasterDetailsContentNodeFactoryDef definition,
                            final Map<String,String> params )
        {
            this.element = element;
            this.property = resolve( element, definition.getProperty().getContent(), params );
            this.definition = definition;
            this.params = params;
            
            if( this.property instanceof ValueProperty || this.property instanceof ImpliedElementProperty || this.property instanceof TransientProperty )
            {
                throw new IllegalArgumentException();
            }
            
            this.visibleWhenFunctionResult = initExpression
            (
                this.element,
                AndFunction.create
                (
                    definition.getVisibleWhen().getContent(),
                    createVersionCompatibleFunction( this.element, this.property )
                ),
                Boolean.class,
                Literal.TRUE,
                new Runnable()
                {
                    public void run()
                    {
                        broadcast( new PartVisibilityEvent( null ) );
                    }
                }
            );
            
            this.visibleWhenFunctionForNodes = new Function()
            {
                @Override
                public String name()
                {
                    return "NodeFactoryVisible";
                }

                @Override
                public FunctionResult evaluate( final FunctionContext context )
                {
                    return new FunctionResult( this, context )
                    {
                        private Listener listener;
                        
                        @Override
                        protected void init()
                        {
                            this.listener = new Listener()
                            {
                                @Override
                                public void handle( final Event event )
                                {
                                    refresh();
                                }
                            };
                            
                            NodeFactory.this.visibleWhenFunctionResult.attach( this.listener );
                        }

                        @Override
                        protected Object evaluate()
                        {
                            return NodeFactory.this.visible();
                        }

                        @Override
                        public void dispose()
                        {
                            super.dispose();
                            
                            NodeFactory.this.visibleWhenFunctionResult.detach( this.listener );
                        }
                    };
                }
            };
            
            this.visibleWhenFunctionForNodes.init();
        }
        
        public final boolean visible()
        {
            return (Boolean) this.visibleWhenFunctionResult.value();
        }
        
        public ModelProperty property()
        {
            return this.property;
        }
        
        protected List<IModelElement> elements()
        {
            final ListFactory<IModelElement> elementsListFactory = ListFactory.start();
            
            if( this.property instanceof ListProperty )
            {
                elementsListFactory.add( this.element.read( (ListProperty) this.property ) );
            }
            else
            {
                elementsListFactory.add( this.element.read( (ElementProperty) this.property ).element() );
            }
            
            return elementsListFactory.result();
        }
        
        public final List<MasterDetailsContentNode> nodes()
        {
            final Map<IModelElement,MasterDetailsContentNode> oldCache = MapFactory.unmodifiable( this.nodesCache );
            final ListFactory<MasterDetailsContentNode> nodes = ListFactory.start();
            
            for( IModelElement element : elements() )
            {
                MasterDetailsContentNode node = this.nodesCache.get( element );
                
                if( node == null )
                {
                    MasterDetailsContentNodeDef relevantCaseDef = null;
                    
                    for( MasterDetailsContentNodeFactoryCaseDef entry : this.definition.getCases() )
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
                    
                    node = new MasterDetailsContentNode( this.visibleWhenFunctionForNodes );
                    
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
            
            return nodes.result();
        }
        
        public final boolean attach( final Listener listener )
        {
            return this.listeners.attach( listener );
        }
        
        public final boolean detach( final Listener listener )
        {
            return this.listeners.detach( listener );
        }
        
        protected final void broadcast( final Event event )
        {
            this.listeners.broadcast( event );
        }
        
        public void dispose()
        {
            if( this.visibleWhenFunctionResult != null )
            {
                this.visibleWhenFunctionResult.dispose();
            }
        }
    }
    
    public static final class NodeListEvent extends PartEvent
    {
        public NodeListEvent( final MasterDetailsContentNode node )
        {
            super( node );
        }

        @Override
        public MasterDetailsContentNode part()
        {
            return (MasterDetailsContentNode) super.part();
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
