/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [342656] read.only rendering hint is not parsed as Boolean 
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEnablementEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.forms.swt.CheckBoxListPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.SlushBucketPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.TablePropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.TextFieldPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.CheckBoxGroupPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.CheckBoxPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.EnumPropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.internal.NamedValuesPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.PopUpListFieldPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.RadioButtonGroupPropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.ScalePropertyEditorPresentation;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyEditorPart extends FormComponentPart
{
    public static final String RELATED_CONTROLS = "related-controls";
    public static final String BROWSE_BUTTON = "browse-button";
    public static final String DATA_BINDING = "binding";
    
    private static final List<PropertyEditorPresentationFactory> FACTORIES = new ArrayList<PropertyEditorPresentationFactory>();
    
    static
    {
        FACTORIES.add( new CheckBoxPropertyEditorPresentation.Factory() );
        FACTORIES.add( new RadioButtonGroupPropertyEditorPresentation.Factory() );
        FACTORIES.add( new PopUpListFieldPropertyEditorPresentation.Factory() );
        FACTORIES.add( new EnumPropertyEditorPresentationFactory() );
        FACTORIES.add( new NamedValuesPropertyEditorPresentation.Factory() );
        FACTORIES.add( new ScalePropertyEditorPresentation.Factory() );
        FACTORIES.add( new TextFieldPropertyEditorPresentation.Factory() );
        FACTORIES.add( new CheckBoxGroupPropertyEditorPresentation.HorizontalFactory() );
        FACTORIES.add( new CheckBoxGroupPropertyEditorPresentation.VerticalFactory() );
        FACTORIES.add( new CheckBoxListPropertyEditorPresentation.EnumFactory() );
        FACTORIES.add( new SlushBucketPropertyEditorPresentation.Factory() );
        FACTORIES.add( new TablePropertyEditorPresentation.Factory() );
    }
    
    @Text( "Property editor''s property reference path \"{0}\" is invalid." )
    private static LocalizableText invalidPath;
    
    @Text( "Child property path \"{1}\" is invalid for \"{0}\"." )
    private static LocalizableText invalidChildPropertyPath;
    
    static
    {
        LocalizableText.init( PropertyEditorPart.class );
    }

    private Property property;
    private List<ModelPath> childPropertyPaths;
    private Map<Element,Map<ModelPath,PropertyEditorPart>> childPropertyEditors;
    private Map<String,Object> hints;
    private List<FormComponentPart> relatedContentParts;
    private Listener propertyValidationListener;
    private FunctionResult labelFunctionResult;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ISapphireUiDef rootdef = this.definition.nearest( ISapphireUiDef.class );
        final PropertyEditorDef propertyEditorPartDef = (PropertyEditorDef) this.definition;
        
        final String propertyEditorPath = substituteParams( propertyEditorPartDef.getProperty().text() );
        this.property = getModelElement().property( new ModelPath( propertyEditorPath ) );
        
        if( this.property == null )
        {
            throw new RuntimeException( invalidPath.format( propertyEditorPath ) );
        }
        
        // Read the property to ensure that initial events are broadcast and avoid being surprised
        // by them later.
        
        this.property.empty();
        
        // Child properties.        
        
        final ListFactory<ModelPath> childPropertiesListFactory = ListFactory.start();
        final ElementType type = this.property.definition().getType();
        
        if( type != null )
        {
            if( propertyEditorPartDef.getChildProperties().isEmpty() )
            {
                for( PropertyDef childProperty : type.properties() )
                {
                    if( childProperty instanceof ValueProperty )
                    {
                        childPropertiesListFactory.add( new ModelPath( childProperty.name() ) );
                    }
                }
            }
            else
            {
                for( PropertyEditorDef childPropertyEditor : propertyEditorPartDef.getChildProperties() )
                {
                    final ModelPath childPropertyPath = new ModelPath( childPropertyEditor.getProperty().content() );
                    boolean invalid = false;
                    
                    if( childPropertyPath.length() == 0 )
                    {
                        invalid = true;
                    }
                    else
                    {
                        ElementType t = type;
                        
                        for( int i = 0, n = childPropertyPath.length(); i < n && ! invalid; i++ )
                        {
                            final ModelPath.Segment segment = childPropertyPath.segment( i );
                            
                            if( segment instanceof ModelPath.PropertySegment )
                            {
                                final PropertyDef p = t.property( ( (ModelPath.PropertySegment) segment ).getPropertyName() );
                                
                                if( p instanceof ValueProperty )
                                {
                                    if( i + 1 != n )
                                    {
                                        invalid = true;
                                    }
                                }
                                else if( p instanceof ImpliedElementProperty )
                                {
                                    if( i + 1 == n )
                                    {
                                        invalid = true;
                                    }
                                    else
                                    {
                                        t = p.getType();
                                    }
                                }
                                else
                                {
                                    invalid = true;
                                }
                            }
                            else
                            {
                                invalid = true;
                            }
                        }
                    }
                    
                    if( invalid )
                    {
                        final String msg = invalidChildPropertyPath.format( this.property.name(), childPropertyPath.toString() );
                        Sapphire.service( LoggingService.class ).logError( msg );
                    }
                    else
                    {
                        childPropertiesListFactory.add( childPropertyPath );
                    }
                }
            }
        }
        
        this.childPropertyPaths = childPropertiesListFactory.result();
        this.childPropertyEditors = new HashMap<Element,Map<ModelPath,PropertyEditorPart>>();
        
        // Listen for PropertyValidationEvent and update property editor's validation.
        
        if( this.property instanceof ElementList || this.property instanceof ElementHandle )
        {
            this.propertyValidationListener = new FilteredListener<PropertyEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyEvent event )
                {
                    if( ! ( event instanceof PropertyContentEvent && event.property() instanceof Value ) )
                    {
                        refreshValidation();
                    }
                }
            };
            
            this.property.attach( this.propertyValidationListener, "*" );
        }
        else
        {
            this.propertyValidationListener = new FilteredListener<PropertyEvent>()
            {
                @Override
                protected void handleTypedEvent( final PropertyEvent event )
                {
                    if( event instanceof PropertyValidationEvent || event instanceof PropertyEnablementEvent )
                    {
                        refreshValidation();
                    }
                }
            };
            
            this.property.attach( this.propertyValidationListener );
        }

        // Hints

        this.hints = new HashMap<String,Object>();
        
        for( ISapphireHint hint : propertyEditorPartDef.getHints() )
        {
            final String name = hint.getName().text();
            final String valueString = hint.getValue().text();
            Object parsedValue = valueString;
            
            if( name.equals( PropertyEditorDef.HINT_SHOW_HEADER ) ||
                name.equals( PropertyEditorDef.HINT_BORDER ) ||
                name.equals( PropertyEditorDef.HINT_BROWSE_ONLY ) ||
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
        
        final ListFactory<FormComponentPart> relatedContentPartsListFactory = ListFactory.start();
        
        final Listener relatedContentPartListener = new FilteredListener<PartValidationEvent>()
        {
            @Override
            protected void handleTypedEvent( PartValidationEvent event )
            {
                refreshValidation();
            }
        };

        for( final FormComponentDef relatedContentPartDef : propertyEditorPartDef.getRelatedContent() )
        {
            final FormComponentPart relatedContentPart = (FormComponentPart) create( this, this.property.element(), relatedContentPartDef, this.params );
            relatedContentPart.attach( relatedContentPartListener );
            relatedContentPartsListFactory.add( relatedContentPart );
        }
        
        this.relatedContentParts = relatedContentPartsListFactory.result();
    }

    @Override
    protected Function initVisibleWhenFunction()
    {
        return AndFunction.create
        (
            super.initVisibleWhenFunction(),
            createVersionCompatibleFunction( property() )
        );
    }
    
    @Override
    public PropertyEditorDef definition()
    {
        return (PropertyEditorDef) super.definition();
    }
    
    @Override
    public Element getLocalModelElement()
    {
        return this.property.element();
    }
    
    public Property property()
    {
        return this.property;
    }
    
    public List<ModelPath> getChildProperties()
    {
        return this.childPropertyPaths;
    }
    
    public PropertyEditorPart getChildPropertyEditor( final Element element,
                                                      final PropertyDef property )
    {
        return getChildPropertyEditor( element, new ModelPath( property.name() ) );
    }
    
    public PropertyEditorPart getChildPropertyEditor( final Element element,
                                                      final ModelPath property )
    {
        Map<ModelPath,PropertyEditorPart> propertyEditorsForElement = this.childPropertyEditors.get( element );
        
        if( propertyEditorsForElement == null )
        {
            propertyEditorsForElement = new HashMap<ModelPath,PropertyEditorPart>();
            this.childPropertyEditors.put( element, propertyEditorsForElement );
            
            final Map<ModelPath,PropertyEditorPart> finalPropertyEditorsForElement = propertyEditorsForElement;
            
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
            PropertyEditorDef childPropertyEditorDef = ( (PropertyEditorDef) this.definition ).getChildPropertyEditor( property );
            
            if( childPropertyEditorDef == null )
            {
                childPropertyEditorDef = PropertyEditorDef.TYPE.instantiate();
                childPropertyEditorDef.setProperty( property.toString() );
            }
            
            childPropertyEditorPart = new PropertyEditorPart();
            childPropertyEditorPart.init( this, element, childPropertyEditorDef, this.params );
            childPropertyEditorPart.initialize();
            
            propertyEditorsForElement.put( property, childPropertyEditorPart );
        }
        
        return childPropertyEditorPart;
    }
    
    public String label()
    {
        return label( CapitalizationType.NO_CAPS, true );
    }
    
    public String label( final CapitalizationType capitalizationType, final boolean includeMnemonic )
    {
        final PropertyEditorDef def = definition();
        
        if( def.getShowLabel().content() )
        {
            if( this.labelFunctionResult == null )
            {
                this.labelFunctionResult = initExpression
                (
                    def.getLabel().content(), 
                    String.class,
                    Literal.create( this.property.definition().getLabel( false, CapitalizationType.NO_CAPS, true ) ),
                    new Runnable()
                    {
                        public void run()
                        {
                            broadcast( new LabelChangedEvent( PropertyEditorPart.this ) );
                        }
                    }
                );
            }
            
            final String label = (String) this.labelFunctionResult.value();
            return LabelTransformer.transform( label, capitalizationType, includeMnemonic );
        }
        
        return null;
    }
    
    public boolean getSpanBothColumns()
    {
        return definition().getSpanBothColumns().content();
    }
    
    public int getWidth( final int defaultValue )
    {
        final Integer width = definition().getWidth().content();
        return ( width == null || width < 1 ? defaultValue : width );
    }
    
    public int getHeight( final int defaultValue )
    {
        final Integer height = definition().getHeight().content();
        return ( height == null || height < 1 ? defaultValue : height );
    }
    
    public int getMarginLeft()
    {
        int marginLeft = definition().getMarginLeft().content();
        
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
    
    public List<FormComponentPart> getRelatedContent()
    {
        return this.relatedContentParts;
    }
    
    public int getRelatedContentWidth()
    {
        final Value<Integer> relatedContentWidth = definition().getRelatedContentWidth();
        
        if( relatedContentWidth.validation().ok() )
        {
            return relatedContentWidth.content();
        }
        else
        {
            return relatedContentWidth.getDefaultContent();
        }
    }
    
    @Override
    public FormComponentPresentation createPresentation( final SwtPresentation parent, final Composite composite )
    {
        PropertyEditorPresentation presentation = null;
        
        try
        {
            final Class<PropertyEditorPresentationFactory> factoryClass 
                = getRenderingHint( PropertyEditorDef.HINT_FACTORY, (Class<PropertyEditorPresentationFactory>) null );
            
            if( factoryClass != null )
            {
                final PropertyEditorPresentationFactory factory = factoryClass.newInstance();
                presentation = factory.create( this, parent, composite );
            }
        }
        catch( Exception e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }

        if( presentation == null )
        {
            for( final PropertyEditorPresentationFactory f : FACTORIES )
            {
                presentation = f.create( this, parent, composite );
                
                if( presentation != null )
                {
                    break;
                }
            }
        }
        
        if( presentation == null )
        {
            throw new IllegalStateException( this.property.toString() );
        }
        
        return presentation;
    }

    @Override
    protected Status computeValidation()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        if( property().enabled() )
        {
            factory.merge( this.property.validation() );
            
            if( this.property instanceof ElementList )
            {
                for( final Element child : (ElementList<?>) this.property )
                {
                    factory.merge( child.validation() );
                }
            }
            else if( this.property instanceof ElementHandle )
            {
                final Element child = ( (ElementHandle<?>) this.property ).content();
                
                if( child != null )
                {
                    factory.merge( child.validation() );
                }
            }
        }
        
        for( SapphirePart relatedContentPart : this.relatedContentParts )
        {
            factory.merge( relatedContentPart.validation() );
        }
        
        return factory.create();
    }
    
    @Override
    
    public boolean setFocus()
    {
        if( property().enabled() )
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
            
            if( propertyName.equals( this.property.name() ) )
            {
                return setFocus();
            }
        }
        
        return false;
    }

    public String getActionContext()
    {
        final String context;
        
        if( this.property.definition() instanceof ValueProperty )
        {
            context = SapphireActionSystem.CONTEXT_VALUE_PROPERTY_EDITOR;
        }
        else if( this.property.definition() instanceof ElementProperty )
        {
            context = SapphireActionSystem.CONTEXT_ELEMENT_PROPERTY_EDITOR;
        }
        else if( this.property.definition() instanceof ListProperty )
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
    
    public boolean isReadOnly()
    {
        return ( this.property.definition().isReadOnly() || getRenderingHint( PropertyEditorDef.HINT_READ_ONLY, false ) );        
    }

    @Override
    public void dispose()
    {
        if( this.propertyValidationListener != null && ! this.property.disposed() )
        {
            if( this.property instanceof ElementList )
            {
                this.property.detach( this.propertyValidationListener, "*" );
            }
            else
            {
                this.property.detach( this.propertyValidationListener );
            }
        }
        
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        for( Map<ModelPath,PropertyEditorPart> propertyEditorsForElement : this.childPropertyEditors.values() )
        {
            for( PropertyEditorPart propertyEditor : propertyEditorsForElement.values() )
            {
                propertyEditor.dispose();
            }
        }
        
        super.dispose();
    }
    
}
