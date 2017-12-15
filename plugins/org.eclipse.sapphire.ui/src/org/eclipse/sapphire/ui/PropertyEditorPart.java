/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [342656] read.only rendering hint is not parsed as Boolean 
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.BooleanPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.CheckBoxListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.DefaultListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.DefaultValuePropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.EnumPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.NamedValuesPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRendererFactory;
import org.eclipse.sapphire.ui.renderers.swt.SlushBucketPropertyEditor;
import org.eclipse.sapphire.ui.swt.internal.PopUpListFieldPropertyEditorPresentation;
import org.eclipse.sapphire.ui.swt.internal.PopUpListFieldStyle;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PropertyEditorPart extends FormPart
{
    public static final String RELATED_CONTROLS = "related-controls";
    public static final String BROWSE_BUTTON = "browse-button";
    public static final String DATA_BINDING = "binding";
    
    private static final List<PropertyEditorRendererFactory> FACTORIES = new ArrayList<PropertyEditorRendererFactory>();
    
    static
    {
        FACTORIES.add( new BooleanPropertyEditorRenderer.Factory() );
        FACTORIES.add( new EnumPropertyEditorRenderer.Factory() );
        FACTORIES.add( new NamedValuesPropertyEditorRenderer.Factory() );
        FACTORIES.add( new DefaultValuePropertyEditorRenderer.Factory() );
        FACTORIES.add( new CheckBoxListPropertyEditorRenderer.EnumFactory() );
        FACTORIES.add( new SlushBucketPropertyEditor.Factory() );
        FACTORIES.add( new DefaultListPropertyEditorRenderer.Factory() );
    }
    
    private IModelElement element;
    private ModelProperty property;
    private List<ModelProperty> childProperties;
    private List<ModelProperty> childPropertiesReadOnly;
    private Map<IModelElement,Map<ModelProperty,PropertyEditorPart>> childPropertyEditors;
    private Map<String,Object> hints;
    private List<SapphirePart> relatedContentParts;
    private List<SapphirePart> relatedContentPartsReadOnly;
    private Listener listener;
    private FunctionResult labelFunctionResult;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ISapphireUiDef rootdef = this.definition.nearest( ISapphireUiDef.class );
        final PropertyEditorDef propertyEditorPartDef = (PropertyEditorDef) this.definition;
        
        final String pathString = propertyEditorPartDef.getProperty().getContent();
        final ModelPath path = new ModelPath( pathString );
        
        this.element = getModelElement();
        
        for( int i = 0, n = path.length(); i < n; i++ )
        {
            final ModelPath.Segment segment = path.segment( i );
            
            if( segment instanceof ModelPath.ModelRootSegment )
            {
                this.element = (IModelElement) this.element.root();
            }
            else if( segment instanceof ModelPath.ParentElementSegment )
            {
                IModelParticle parent = this.element.parent();
                
                if( ! ( parent instanceof IModelElement ) )
                {
                    parent = parent.parent();
                }
                
                this.element = (IModelElement) parent;
            }
            else if( segment instanceof ModelPath.PropertySegment )
            {
                this.property = resolve( this.element, ( (ModelPath.PropertySegment) segment ).getPropertyName() );
                
                if( i + 1 != n )
                {
                    throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
                }
            }
            else
            {
                throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
            }
        }
        
        if( this.property == null )
        {
            throw new RuntimeException( NLS.bind( Resources.invalidPath, pathString ) );
        }
        
        // Read the property to ensure that initial events are broadcast and avoid being surprised
        // by them later.
        
        this.element.read( this.property );
        
        // Listen for PropertyValidationEvent and update property editor's validation.
        
        this.listener = new FilteredListener<PropertyValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyValidationEvent event )
            {
                final Runnable op = new Runnable()
                {
                    public void run()
                    {
                        if( Display.getCurrent() == null )
                        {
                            Display.getDefault().asyncExec( this );
                            return;
                        }
                        
                        updateValidationState();
                    }
                };
                
                op.run();
            }
        };
        
        this.element.attach( this.listener, this.property.getName() );
        
        this.childProperties = new ArrayList<ModelProperty>();
        this.childPropertiesReadOnly = Collections.unmodifiableList( this.childProperties );
        this.childPropertyEditors = new HashMap<IModelElement,Map<ModelProperty,PropertyEditorPart>>();
        
        final ModelElementType type = this.property.getType();
        
        if( type != null )
        {
            if( propertyEditorPartDef.getChildProperties().isEmpty() )
            {
                for( ModelProperty childProperty : type.properties() )
                {
                    this.childProperties.add( childProperty );
                }
            }
            else
            {
                for( PropertyEditorDef childPropertyEditor : propertyEditorPartDef.getChildProperties() )
                {
                    final String childPropertyName = childPropertyEditor.getProperty().getContent();
                    final ModelProperty childProperty = type.property( childPropertyName );
                    
                    if( childProperty == null )
                    {
                        SapphireUiFrameworkPlugin.logError( "Could not resolve property: " + childPropertyName );
                    }
                    else
                    {
                        this.childProperties.add( childProperty );
                    }
                }
            }
        }
        
        this.hints = new HashMap<String,Object>();
        
        for( ISapphireHint hint : propertyEditorPartDef.getHints() )
        {
            final String name = hint.getName().getText();
            final String valueString = hint.getValue().getText();
            Object parsedValue = valueString;
            
            if( name.equals( PropertyEditorDef.HINT_SHOW_HEADER ) ||
                name.equals( PropertyEditorDef.HINT_BORDER ) ||
                name.equals( PropertyEditorDef.HINT_BROWSE_ONLY ) ||
                name.equals( PropertyEditorDef.HINT_PREFER_COMBO ) ||
                name.equals( PropertyEditorDef.HINT_PREFER_RADIO_BUTTONS ) ||
                name.equals( PropertyEditorDef.HINT_PREFER_VERTICAL_RADIO_BUTTONS ) ||
                name.equals( PropertyEditorDef.HINT_READ_ONLY ) )
            {
                parsedValue = Boolean.parseBoolean( valueString );
            }
            else if( name.startsWith( PropertyEditorDef.HINT_FACTORY ) ||
                     name.startsWith( PropertyEditorDef.HINT_AUX_TEXT_PROVIDER ) )
            {
                parsedValue = rootdef.resolveClass( valueString );
            }
            else if( name.equals( PropertyEditorDef.HINT_LISTENERS ) )
            {
                final List<Class<?>> contributors = new ArrayList<Class<?>>();
                
                for( String segment : valueString.split( "," ) )
                {
                    final Class<?> cl = rootdef.resolveClass( segment.trim() );
                    
                    if( cl != null )
                    {
                        contributors.add( cl );
                    }
                }
                
                parsedValue = contributors;
            }
            
            this.hints.put( name, parsedValue );
        }
        
        this.relatedContentParts = new ArrayList<SapphirePart>();
        this.relatedContentPartsReadOnly = Collections.unmodifiableList( this.relatedContentParts );
        
        final Listener relatedContentPartListener = new Listener()
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

        for( PartDef relatedContentPartDef : propertyEditorPartDef.getRelatedContent() )
        {
            final SapphirePart relatedContentPart = create( this, this.element, relatedContentPartDef, this.params );
            relatedContentPart.attach( relatedContentPartListener );
            this.relatedContentParts.add( relatedContentPart );
        }
        
        this.labelFunctionResult = initExpression
        (
            getLocalModelElement(),
            propertyEditorPartDef.getLabel().getContent(), 
            String.class,
            Literal.create( this.property.getLabel( false, CapitalizationType.NO_CAPS, true ) ),
            new Runnable()
            {
                public void run()
                {
                    broadcast( new LabelChangedEvent( PropertyEditorPart.this ) );
                }
            }
        );
    }
    
    @Override
    public PropertyEditorDef definition()
    {
        return (PropertyEditorDef) super.definition();
    }
    
    @Override
    public IModelElement getLocalModelElement()
    {
        return this.element;
    }
    
    public ModelProperty getProperty()
    {
        return this.property;
    }
    
    public List<ModelProperty> getChildProperties()
    {
        return this.childPropertiesReadOnly;
    }
    
    public PropertyEditorPart getChildPropertyEditor( final IModelElement element,
                                                          final ModelProperty property )
    {
        Map<ModelProperty,PropertyEditorPart> propertyEditorsForElement = this.childPropertyEditors.get( element );
        
        if( propertyEditorsForElement == null )
        {
            propertyEditorsForElement = new HashMap<ModelProperty,PropertyEditorPart>();
            this.childPropertyEditors.put( element, propertyEditorsForElement );
            
            final Map<ModelProperty,PropertyEditorPart> finalPropertyEditorsForElement = propertyEditorsForElement;
            
            element.attach
            (
                new FilteredListener<ElementDisposeEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final ElementDisposeEvent event )
                    {
                        for( PropertyEditorPart propertyEditor : finalPropertyEditorsForElement.values() )
                        {
                            propertyEditor.dispose();
                        }
                        
                        PropertyEditorPart.this.childPropertyEditors.remove( element );
                    }
                }
            );
        }
        
        PropertyEditorPart childPropertyEditorPart = propertyEditorsForElement.get( property );
        
        if( childPropertyEditorPart == null )
        {
            final String childPropertyName = property.getName();
            PropertyEditorDef childPropertyEditorDef = ( (PropertyEditorDef) this.definition ).getChildPropertyEditor( property );
            
            if( childPropertyEditorDef == null )
            {
                childPropertyEditorDef = PropertyEditorDef.TYPE.instantiate();
                childPropertyEditorDef.setProperty( childPropertyName );
            }
            
            childPropertyEditorPart = new PropertyEditorPart();
            childPropertyEditorPart.init( this, element, childPropertyEditorDef, this.params );
            
            propertyEditorsForElement.put( property, childPropertyEditorPart );
        }
        
        return childPropertyEditorPart;
    }
    
    public String getLabel( final CapitalizationType capitalizationType,
                            final boolean includeMnemonic )
    {
        final String label = (String) this.labelFunctionResult.value();
        return LabelTransformer.transform( label, capitalizationType, includeMnemonic );
    }
    
    public boolean getShowLabel()
    {
        return definition().getShowLabel().getContent();
    }
    
    public boolean getSpanBothColumns()
    {
        return definition().getSpanBothColumns().getContent();
    }
    
    public int getWidth( final int defaultValue )
    {
        final Integer width = definition().getWidth().getContent();
        return ( width == null || width < 1 ? defaultValue : width );
    }
    
    public int getHeight( final int defaultValue )
    {
        final Integer height = definition().getHeight().getContent();
        return ( height == null || height < 1 ? defaultValue : height );
    }
    
    public int getMarginLeft()
    {
        int marginLeft = definition().getMarginLeft().getContent();
        
        if( marginLeft < 0 )
        {
            marginLeft = 0;
        }
        
        return marginLeft;
    }

    @SuppressWarnings( "unchecked" )
    
    public <T> T getRenderingHint( final String name,
                                   final T defaultValue )
    {
        final Object hintValue = this.hints == null ? null : this.hints.get( name );
        return hintValue == null ? defaultValue : (T) hintValue;
    }

    public boolean getRenderingHint( final String name,
                                     final boolean defaultValue )
    {
        final Object hintValue = this.hints == null ? null : this.hints.get( name );
        return hintValue == null ? defaultValue : (Boolean) hintValue;
    }
    
    public List<SapphirePart> getRelatedContent()
    {
        return this.relatedContentPartsReadOnly;
    }
    
    public int getRelatedContentWidth()
    {
        final Value<Integer> relatedContentWidth = definition().getRelatedContentWidth();
        
        if( relatedContentWidth.validation().ok() )
        {
            return relatedContentWidth.getContent();
        }
        else
        {
            return relatedContentWidth.getDefaultContent();
        }
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        PropertyEditorRenderer presentation = null;
        final String style = definition().getStyle().getText();
        
        if( style == null )
        {
            PropertyEditorRendererFactory factory = null;
            
            try
            {
                final Class<PropertyEditorRendererFactory> factoryClass 
                    = getRenderingHint( PropertyEditorDef.HINT_FACTORY, (Class<PropertyEditorRendererFactory>) null );
                
                if( factoryClass != null )
                {
                    factory = factoryClass.newInstance();
                }
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            if( factory == null )
            {
                for( PropertyEditorRendererFactory f : FACTORIES )
                {
                    if( f.isApplicableTo( this ) )
                    {
                        factory = f;
                        break;
                    }
                }
            }
    
            if( factory != null )
            {
                presentation = factory.create( context, this );
            }
        }
        else
        {
            if( style.startsWith( "Sapphire.PropertyEditor.PopUpListField" ) )
            {
                if( this.property instanceof ValueProperty && this.element.service( this.property, PossibleValuesService.class ) != null )
                {
                    PopUpListFieldStyle popUpListFieldPresentationStyle = null;
                    
                    if( style.equals( "Sapphire.PropertyEditor.PopUpListField" ) )
                    {
                        final PossibleValues possibleValuesAnnotation = this.property.getAnnotation( PossibleValues.class );
                        
                        if( possibleValuesAnnotation != null )
                        {
                            popUpListFieldPresentationStyle 
                                = ( possibleValuesAnnotation.invalidValueSeverity() == Severity.ERROR 
                                    ? PopUpListFieldStyle.STRICT : PopUpListFieldStyle.EDITABLE );
                        }
                        else
                        {
                            popUpListFieldPresentationStyle = PopUpListFieldStyle.EDITABLE;
                        }
                    }
                    else if( style.equals( "Sapphire.PropertyEditor.PopUpListField.Editable" ) )
                    {
                        popUpListFieldPresentationStyle = PopUpListFieldStyle.EDITABLE;
                    }
                    else if( style.equals( "Sapphire.PropertyEditor.PopUpListField.Strict" ) )
                    {
                        popUpListFieldPresentationStyle = PopUpListFieldStyle.STRICT;
                    }
                    
                    if( popUpListFieldPresentationStyle != null )
                    {
                        presentation = new PopUpListFieldPropertyEditorPresentation( context, this, popUpListFieldPresentationStyle );
                    }
                }
            }
        }
        
        if( presentation != null )
        {
            presentation.create( context.getComposite() );
        }
        else
        {
            throw new IllegalStateException( this.property.toString() );
        }
        
    }

    @Override
    protected Status computeValidationState()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        if( this.element.enabled( this.property ) )
        {
            final Object particle = this.element.read( this.property );
            
            if( particle instanceof Value<?> )
            {
                factory.merge( ( (Value<?>) particle ).validation() );
            }
            else if( particle instanceof ModelElementList<?> )
            {
                factory.merge( ( (ModelElementList<?>) particle ).validation() );
            }
            else if( particle instanceof ModelElementHandle<?> )
            {
                factory.merge( ( (ModelElementHandle<?>) particle ).validation() );
            }
        }
        
        for( SapphirePart relatedContentPart : this.relatedContentParts )
        {
            factory.merge( relatedContentPart.getValidationState() );
        }
        
        return factory.create();
    }
    
    @Override
    
    public boolean setFocus()
    {
        if( this.element.enabled( this.property ) )
        {
            broadcast( new FocusReceivedEvent( this ) );
            return true;
        }
        
        return false;
    }

    @Override
    
    public boolean setFocus( final ModelPath path )
    {
        final ModelPath.Segment head = path.head();
        
        if( head instanceof ModelPath.PropertySegment )
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            
            if( propertyName.equals( this.property.getName() ) )
            {
                return setFocus();
            }
        }
        
        return false;
    }

    public String getActionContext()
    {
        final String context;
        
        if( this.property instanceof ValueProperty )
        {
            context = SapphireActionSystem.CONTEXT_VALUE_PROPERTY_EDITOR;
        }
        else if( this.property instanceof ElementProperty )
        {
            context = SapphireActionSystem.CONTEXT_ELEMENT_PROPERTY_EDITOR;
        }
        else if( this.property instanceof ListProperty )
        {
            context = SapphireActionSystem.CONTEXT_LIST_PROPERTY_EDITOR;
        }
        else
        {
            throw new IllegalStateException();
        }
        
        return context;
    }

    @Override
    public Set<String> getActionContexts()
    {
        return Collections.singleton( getActionContext() );
    }
    
    @Override
    public boolean isSingleLinePart()
    {
        if( this.property instanceof ValueProperty && ! this.property.hasAnnotation( LongString.class ) )
        {
            return true;
        }
        
        return false;
    }
    
    public boolean isReadOnly()
    {
        return ( this.property.isReadOnly() || getRenderingHint( PropertyEditorDef.HINT_READ_ONLY, false ) );        
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.listener != null )
        {
            this.element.detach( this.listener, this.property.getName() );
        }
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        for( Map<ModelProperty,PropertyEditorPart> propertyEditorsForElement : this.childPropertyEditors.values() )
        {
            for( PropertyEditorPart propertyEditor : propertyEditorsForElement.values() )
            {
                propertyEditor.dispose();
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String invalidPath;
        
        static
        {
            initializeMessages( PropertyEditorPart.class.getName(), Resources.class );
        }
    }
    
}
