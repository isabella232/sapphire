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

package org.eclipse.sapphire.ui.forms;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.runOnDisplayThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentNodePart

    extends SapphirePart
    implements PropertiesViewContributorPart
    
{
    private static final ImageData IMG_CONTAINER_NODE
        = ImageData.readFromClassLoader( MasterDetailsContentNodePart.class, "ContainerNode.png" ).required();

    private static final ImageData IMG_LEAF_NODE
        = ImageData.readFromClassLoader( MasterDetailsContentNodePart.class, "LeafNode.png" ).required();

    @Text( "Could not resolve node \"{0}\"." )
    private static LocalizableText couldNotResolveNode;
    
    @Text( "Could not resolve section = \"{0}\"." )
    private static LocalizableText couldNotResolveSection;
    
    static
    {
        LocalizableText.init( MasterDetailsContentNodePart.class );
    }

    private MasterDetailsContentOutline contentTree;
    private MasterDetailsContentNodeDef definition;
    private Element modelElement;
    private ElementHandle<?> modelElementProperty;
    private MasterDetailsContentNodePart parentNode;
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
    
    public MasterDetailsContentNodePart()
    {
        this( null );
    }
    
    public MasterDetailsContentNodePart( final Function nodeFactoryVisibleFunction )
    {
        this.nodeFactoryVisibleFunction = nodeFactoryVisibleFunction;
    }
    
    @Override
    protected void init()
    {
        super.init();
        
        final ISapphirePart parent = parent();

        if( parent instanceof MasterDetailsContentNodePart )
        {
            this.parentNode = (MasterDetailsContentNodePart) parent;
        }
        else
        {
            this.parentNode = null;
        }
        
        this.contentTree = nearest( MasterDetailsEditorPagePart.class ).outline();
        this.definition = (MasterDetailsContentNodeDef) super.definition;
        
        final ImpliedElementProperty modelElementProperty = (ImpliedElementProperty) resolve( this.definition.getProperty().content() );
        
        if( modelElementProperty != null )
        {
            this.modelElementProperty = getModelElement().property( modelElementProperty );
            this.modelElement = this.modelElementProperty.content();
        }
        else
        {
            this.modelElement = getModelElement();
        }
        
        this.expanded = false;
        
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
            this.definition.getLabel().content(),
            String.class,
            null,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new LabelChangedEvent( MasterDetailsContentNodePart.this ) );
                }
            }
        );
        
        // Image
        
        final Literal defaultImageLiteral = Literal.create( ( this.definition.getChildNodes().isEmpty() ? IMG_LEAF_NODE : IMG_CONTAINER_NODE ) );
        final Function imageFunction = this.definition.getImage().content();
        
        this.imageManager = new ImageManager( imageFunction, defaultImageLiteral );
        
        // Sections and Child Nodes
        
        this.rawChildren = new ArrayList<Object>();
        
        final ListFactory<SectionPart> sectionsListFactory = ListFactory.start();
        
        for( FormComponentDef s : this.definition.getSections() )
        {
            final SectionDef sectionDefinition;
            final Map<String,String> sectionParams;
            
            if( s instanceof SectionDef )
            {
                sectionDefinition = (SectionDef) s;
                sectionParams = this.params;
            }
            else if( s instanceof SectionRef )
            {
                final SectionRef sectionReference = (SectionRef) s;
                
                sectionDefinition = sectionReference.getSection().resolve();
                
                if( sectionDefinition == null )
                {
                    final String msg = couldNotResolveSection.format( sectionReference.getSection().text() );
                    throw new RuntimeException( msg );
                }
                
                sectionParams = new HashMap<String,String>( this.params );
                
                for( ISapphireParam param : sectionReference.getParams() )
                {
                    final String paramName = param.getName().text();
                    final String paramValue = param.getValue().text();
                    
                    if( paramName != null && paramValue != null )
                    {
                        sectionParams.put( paramName, paramValue );
                    }
                }
            }
            else
            {
                throw new IllegalStateException();
            }
            
            final SectionPart section = new SectionPart();
            
            section.init( this, this.modelElement, sectionDefinition, sectionParams );
            section.initialize();
            section.attach( this.childPartListener );
            
            sectionsListFactory.add( section );
        }
        
        this.sections = sectionsListFactory.result();
        
        for( MasterDetailsContentNodeChildDef entry : this.definition.getChildNodes() )
        {
            final Map<String,String> params = new HashMap<String,String>( this.params );
            
            if( entry instanceof MasterDetailsContentNodeRef )
            {
                final MasterDetailsContentNodeRef inc = (MasterDetailsContentNodeRef) entry;
                entry = inc.resolve();
                
                if( entry == null )
                {
                    final String msg = couldNotResolveNode.format( inc.getPart() );
                    throw new RuntimeException( msg );
                }

                for( ISapphireParam param : inc.getParams() )
                {
                    final String paramName = param.getName().text();
                    final String paramValue = param.getValue().text();
                    
                    if( paramName != null && paramValue != null )
                    {
                        params.put( paramName, paramValue );
                    }
                }
            }

            if( entry instanceof MasterDetailsContentNodeDef )
            {
                final MasterDetailsContentNodeDef def = (MasterDetailsContentNodeDef) entry;
                
                final MasterDetailsContentNodePart node = new MasterDetailsContentNodePart();
                node.init( this, this.modelElement, def, params );
                node.initialize();
                node.attach( this.childPartListener );
                
                this.rawChildren.add( node );
            }
            else if( entry instanceof MasterDetailsContentNodeFactoryDef )
            {
                final NodeFactory factory = new NodeFactory( (MasterDetailsContentNodeFactoryDef) entry, params );
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
            createVersionCompatibleFunction( this.modelElementProperty ),
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
                                
                                for( Object entry : MasterDetailsContentNodePart.this.rawChildren )
                                {
                                    if( entry instanceof MasterDetailsContentNodePart )
                                    {
                                        ( (MasterDetailsContentNodePart) entry ).attach( listener );
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
                                    for( Object entry : MasterDetailsContentNodePart.this.rawChildren )
                                    {
                                        if( entry instanceof MasterDetailsContentNodePart )
                                        {
                                            final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) entry;
                                            
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

    public MasterDetailsContentNodePart getParentNode()
    {
        return this.parentNode;
    }

    public boolean isAncestorOf( final MasterDetailsContentNodePart node )
    {
        MasterDetailsContentNodePart n = node;
        
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
    public Element getLocalModelElement()
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
                final MasterDetailsContentNodePart selection = getContentTree().getSelectedNode();
                
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
            for( MasterDetailsContentNodePart child : nodes() )
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
    
    public List<MasterDetailsContentNodePart> getExpandedNodes()
    {
        final List<MasterDetailsContentNodePart> result = new ArrayList<MasterDetailsContentNodePart>();
        getExpandedNodes( result );
        return result;
    }
    
    public void getExpandedNodes( final List<MasterDetailsContentNodePart> result )
    {
        if( isExpanded() )
        {
            result.add( this );
            
            for( MasterDetailsContentNodePart child : nodes() )
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
    
    public List<PropertyDef> getChildNodeFactoryProperties()
    {
        final ArrayList<PropertyDef> properties = new ArrayList<PropertyDef>();
        
        for( Object object : this.rawChildren )
        {
            if( object instanceof NodeFactory )
            {
                final NodeFactory factory = (NodeFactory) object;
                
                if( factory.visible() )
                {
                    properties.add( factory.property().definition() );
                }
            }
        }
        
        return properties;
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
            this.nodes = new MasterDetailsContentNodeList( Collections.<MasterDetailsContentNodePart>emptyList() );
        }
        
        return this.nodes;
    }
    
    public MasterDetailsContentNodePart findNode( final String label )
    {
        for( MasterDetailsContentNodePart child : nodes() )
        {
            if( label.equalsIgnoreCase( child.getLabel() ) )
            {
                return child;
            }
        }
        
        return null;
    }
    
    public MasterDetailsContentNodePart findNode( final Element element )
    {
        if( getModelElement() == element )
        {
            return this;
        }

        for( MasterDetailsContentNodePart child : nodes() )
        {
            final MasterDetailsContentNodePart res = child.findNode( element );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }
    
    private void refreshNodes()
    {
        final ListFactory<MasterDetailsContentNodePart> nodeListFactory = ListFactory.start();
        
        for( Object entry : this.rawChildren )
        {
            if( entry instanceof MasterDetailsContentNodePart )
            {
                nodeListFactory.add( (MasterDetailsContentNodePart) entry );
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
        
        refreshValidation();
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
        final Status.CompositeStatusFactory validation = Status.factoryForComposite();
        
        for( final SapphirePart section : this.sections )
        {
            if( section.visible() )
            {
                validation.merge( section.validation() );
            }
        }

        for( final SapphirePart node : nodes() )
        {
            if( node.visible() )
            {
                validation.merge( node.validation() );
            }
        }
        
        for( final NodeFactory factory : factories() )
        {
            if( factory.visible() )
            {
                validation.merge( factory.property().validation() );
            }
        }
        
        return validation.create();
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
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
    
    public boolean controls( final Element element )
    {
        if( element == getModelElement() )
        {
            final ISapphirePart parentPart = parent();
            
            if( parentPart != null && parentPart instanceof MasterDetailsContentNodePart )
            {
                final MasterDetailsContentNodePart parentNode = (MasterDetailsContentNodePart) parentPart;
                
                return ( element != parentNode.getLocalModelElement() );
            }
        }
        
        return false;
    }
    
    public final class NodeFactory
    {
        private final Property property;
        private final Listener propertyListener;
        private final MasterDetailsContentNodeFactoryDef definition;
        private final Map<String,String> params;
        private final FunctionResult visibleWhenFunctionResult;
        private final Function visibleWhenFunctionForNodes;
        private final Map<Element,MasterDetailsContentNodePart> nodesCache = new IdentityHashMap<Element,MasterDetailsContentNodePart>();
        private final ListenerContext listeners = new ListenerContext();
        
        public NodeFactory( final MasterDetailsContentNodeFactoryDef definition,
                            final Map<String,String> params )
        {
            final ModelPath path = new ModelPath( substituteParams( definition.getProperty().content(), params ) );
            
            Element element = getLocalModelElement();
            Property p = null;
            
            for( int i = 0, n = path.length(); i < n; i++ )
            {
                if( p != null )
                {
                    throw new RuntimeException( path.toString() );
                }
                
                final ModelPath.Segment segment = path.segment( i );
                
                if( segment instanceof ModelPath.ModelRootSegment )
                {
                    element = element.root();
                }
                else if( segment instanceof ModelPath.ParentElementSegment )
                {
                    element = element.parent().element();
                }
                else if( segment instanceof ModelPath.PropertySegment )
                {
                    final Property property = element.property( ( (ModelPath.PropertySegment) segment ).getPropertyName() );
                    
                    if( property != null && property.definition() instanceof ImpliedElementProperty )
                    {
                        element = ( (ElementHandle<?>) property ).content();
                    }
                    else if( property instanceof ElementList || property instanceof ElementHandle )
                    {
                        p = property;
                    }
                    else
                    {
                        throw new RuntimeException( path.toString() );
                    }
                }
                else
                {
                    throw new RuntimeException( path.toString() );
                }
            }
            
            if( p == null )
            {
                throw new RuntimeException( path.toString() );
            }
            
            this.property = p;
            
            this.propertyListener = new Listener()
            {
                @Override
                public void handle( final Event event )
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
            };
            
            this.property.attach( this.propertyListener );

            this.definition = definition;
            this.params = params;
            
            this.visibleWhenFunctionResult = initExpression
            (
                AndFunction.create
                (
                    definition.getVisibleWhen().content(),
                    createVersionCompatibleFunction( this.property )
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
        
        public Property property()
        {
            return this.property;
        }
        
        protected List<Element> elements()
        {
            final ListFactory<Element> elementsListFactory = ListFactory.start();
            
            if( this.property instanceof ElementList )
            {
                for( Element element : (ElementList<?>) this.property )
                {
                    elementsListFactory.add( element );
                }
            }
            else
            {
                elementsListFactory.add( ( (ElementHandle<?>) this.property ).content() );
            }
            
            return elementsListFactory.result();
        }
        
        public final List<MasterDetailsContentNodePart> nodes()
        {
            final ListFactory<MasterDetailsContentNodePart> nodes = ListFactory.start();
            
            for( final Iterator<Map.Entry<Element,MasterDetailsContentNodePart>> itr = this.nodesCache.entrySet().iterator(); itr.hasNext(); )
            {
                final Map.Entry<Element,MasterDetailsContentNodePart> entry = itr.next();
                final Element element = entry.getKey();
                final MasterDetailsContentNodePart node = entry.getValue();
                
                if( element.disposed() )
                {
                    node.dispose();
                }
            }
            
            for( Element element : elements() )
            {
                MasterDetailsContentNodePart node = this.nodesCache.get( element );
                
                if( node == null )
                {
                    MasterDetailsContentNodeDef relevantCaseDef = null;
                    
                    for( MasterDetailsContentNodeFactoryCaseDef entry : this.definition.getCases() )
                    {
                        final JavaType type = entry.getElementType().resolve();
                        
                        if( type == null )
                        {
                            relevantCaseDef = entry;
                            break;
                        }
                        else
                        {
                            final Class<?> cl = (Class<?>) type.artifact();
        
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
                    
                    node = new MasterDetailsContentNodePart( this.visibleWhenFunctionForNodes );
                    
                    // It is very important to put the node into the cache prior to initializing the node as
                    // initialization can cause a re-entrant call into this function and we must avoid creating
                    // two nodes for the same element.
                    
                    this.nodesCache.put( element, node );
                    
                    node.init( MasterDetailsContentNodePart.this, element, relevantCaseDef, this.params );
                    node.initialize();
                    node.attach( MasterDetailsContentNodePart.this.childPartListener );
                    node.transformLabelCase = false;
                }
                
                nodes.add( node );
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
            this.property.detach( this.propertyListener );
            
            if( this.visibleWhenFunctionResult != null )
            {
                this.visibleWhenFunctionResult.dispose();
            }
        }
    }
    
    public static final class NodeListEvent extends PartEvent
    {
        public NodeListEvent( final MasterDetailsContentNodePart node )
        {
            super( node );
        }

        @Override
        public MasterDetailsContentNodePart part()
        {
            return (MasterDetailsContentNodePart) super.part();
        }
    }
    
}
