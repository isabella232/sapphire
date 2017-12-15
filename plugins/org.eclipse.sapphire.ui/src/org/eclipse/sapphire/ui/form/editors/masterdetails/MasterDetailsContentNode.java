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

package org.eclipse.sapphire.ui.form.editors.masterdetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ImageService;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelService;
import org.eclipse.sapphire.modeling.ModelService.Event;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionException;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.IPropertiesViewContributorPart;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.PropertiesViewContributionManager;
import org.eclipse.sapphire.ui.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartEvent;
import org.eclipse.sapphire.ui.SapphirePartListener;
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
import org.eclipse.swt.widgets.Display;

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
    private ModelElementListener modelElementListener;
    private MasterDetailsContentNode parentNode;
    private FunctionResult labelFunctionResult;
    private ImageManager imageManager;
    private SapphirePartListener childPartListener;
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
        
        this.contentTree = nearest( MasterDetailsEditorPagePart.class ).getContentOutline();
        this.definition = (IMasterDetailsContentNodeDef) super.definition;
        
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
        
        this.visibleWhenCondition = null;

        Class<?> visibleWhenConditionClass = null;
        String visibleWhenConditionParameter = null;
        
        final Status visibleWhenConditionClassValidation = this.definition.getVisibleWhenConditionClass().validate();
        
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
            public void handleValidateStateChange( final Status oldValidateState,
                                                   final Status newValidationState )
            {
                updateValidationState();
            }
        };
        
        final SapphirePartListener validationStateListener = new SapphirePartListener()
        {
            @Override
            public void handleValidateStateChange( final Status oldValidateState,
                                                   final Status newValidationState )
            {
                getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
            }
        };
        
        addListener( validationStateListener );
        
        // Sections and Child Nodes
        
        this.sections = new ArrayList<SapphireSection>();
        this.sectionsReadOnly = Collections.unmodifiableList( this.sections );
        this.rawChildren = new ArrayList<Object>();
        
        for( ISapphireSectionDef secdef : this.definition.getSections() )
        {
            final SapphireSection section = new SapphireSection();
            section.init( this, this.modelElement, secdef, this.params );
            section.addListener( this.childPartListener );
            
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
                node.addListener( this.childPartListener );
                
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
                            return getLocalModelElement().read( prop );
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
        
        final Literal defaultImageLiteral = Literal.create( ( hasChildNodes() ? IMG_CONTAINER_NODE : IMG_LEAF_NODE ) );
        final Function imageFunction;
        
        if( this.definition.getUseModelElementImage().getContent() )
        {
            final IModelElement element = getLocalModelElement();
            final ImageService imageService = element.service( ImageService.class );
            
            if( imageService == null )
            {
                imageFunction = null;
            }
            else
            {
                imageFunction = new Function()
                {
                    @Override
                    public String name()
                    {
                        return "ImageFromModelElement";
                    }
                    
                    @Override
                    public FunctionResult evaluate( final FunctionContext context )
                    {
                        return new FunctionResult( this, context )
                        {
                            private ModelService.Listener listener;
                            
                            @Override
                            protected void init()
                            {
                                super.init();
                                
                                this.listener = new ModelService.Listener()
                                {
                                    @Override
                                    public void handleEvent( final Event event )
                                    {
                                        if( event instanceof ImageService.ImageChangedEvent )
                                        {
                                            refresh();
                                        }
                                    }
                                };
                                
                                imageService.addListener( this.listener );
                            }

                            @Override
                            protected Object evaluate() throws FunctionException
                            {
                                return imageService.provide();
                            }

                            @Override
                            public void dispose()
                            {
                                super.dispose();
                                
                                if( this.listener != null )
                                {
                                    imageService.removeListener( this.listener );
                                }
                            }
                        };
                    }
                };
                
                imageFunction.init();
            }
        }
        else
        {
            imageFunction = this.definition.getImage().getContent();
        }
        
        this.imageManager = new ImageManager( this.modelElement, imageFunction, defaultImageLiteral );
        
        addListener
        (
            new SapphirePartListener()
            {
                @Override
                public void handleEvent( final SapphirePartEvent event )
                {
                    if( event instanceof SapphirePart.ImageChangedEvent )
                    {
                        getContentTree().notifyOfNodeUpdate( MasterDetailsContentNode.this );
                    }
                }
            }
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
        
        for( SapphirePart child : this.sections )
        {
            factory.add( child.getValidationState() );
        }

        for( SapphirePart child : getChildNodes() )
        {
            factory.add( child.getValidationState() );
        }
        
        return factory.create();
    }
    
    @Override
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        super.handleModelElementChange( event );
        
        if( isChildNodeFactoryProperty( event.getProperty() ) )
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
    
    private abstract class NodeFactory
    {
        private final IMasterDetailsContentNodeFactoryDef definition;
        private final Map<String,String> params;
        private SapphireCondition visibleWhenCondition;
        private Map<Object,MasterDetailsContentNode> nodesCache;
        
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
            final Map<Object,MasterDetailsContentNode> newCache = new HashMap<Object,MasterDetailsContentNode>();
            final List<MasterDetailsContentNode> nodes = new ArrayList<MasterDetailsContentNode>();
            
            for( IModelElement element : elements() )
            {
                MasterDetailsContentNode node = ( this.nodesCache != null ? this.nodesCache.remove( element ) : null );
                
                if( node == null )
                {
                    node = node( element );
                }
                
                nodes.add( node );
                newCache.put( element, node );
            }
            
            if( this.nodesCache != null )
            {
                for( MasterDetailsContentNode node : this.nodesCache.values() )
                {
                    node.dispose();
                }
            }
            
            this.nodesCache = newCache;
            
            return nodes;
        }
        
        private final MasterDetailsContentNode node( final IModelElement element )
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
            
            final MasterDetailsContentNode node = new MasterDetailsContentNode();
            node.init( MasterDetailsContentNode.this, element, relevantCaseDef, this.params );
            node.addListener( MasterDetailsContentNode.this.childPartListener );
            node.transformLabelCase = false;
            
            return node;
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
