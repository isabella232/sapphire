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

package org.eclipse.sapphire.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.assist.BrowseHandler;
import org.eclipse.sapphire.ui.assist.BrowseHandlersExtensionPoint;
import org.eclipse.sapphire.ui.assist.JumpHandler;
import org.eclipse.sapphire.ui.assist.JumpHandlersExtensionPoint;
import org.eclipse.sapphire.ui.def.ISapphireBrowseHandlerDef;
import org.eclipse.sapphire.ui.def.ISapphireChildPropertyInfo;
import org.eclipse.sapphire.ui.def.ISapphireHint;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphirePropertyEditorDef;
import org.eclipse.sapphire.ui.def.ISapphirePropertyMetadata;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.BooleanPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.DefaultListPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.DefaultValuePropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.EnumPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.NamedValuesPropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRendererFactory;
import org.eclipse.sapphire.ui.swt.SapphireControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphirePropertyEditor

    extends SapphirePart
    
{
    public static final String RELATED_CONTROLS = "related-controls";
    public static final String BROWSE_BUTTON = "browse-button";
    public static final String DATA_BINDING = "binding";
    public static final String DATA_ASSIST_DECORATOR = "assist-decorator";
    public static final String DATA_PROPERTY = "property";
    
    public static final String HINT_SHOW_LABEL = "show.label";
    public static final String HINT_SHOW_LABEL_ABOVE = "show.label.above";
    public static final String HINT_SHOW_HEADER = "show.header";
    public static final String HINT_AUX_TEXT = "aux.text";
    public static final String HINT_AUX_TEXT_PROVIDER = "aux.text.provider";
    public static final String HINT_HIDE_IF_DISABLED = "hide.if.disabled";
    public static final String HINT_BROWSE_ONLY = "browse.only";
    public static final String HINT_READ_ONLY = "read.only";
    public static final String HINT_BORDER = "border";
    public static final String HINT_ASSIST_CONTRIBUTORS = "assist.contributors";
    public static final String HINT_SUPPRESS_ASSIST_CONTRIBUTORS = "suppress.assist.contributors";
    public static final String HINT_LISTENERS = "listeners";
    public static final String HINT_COLUMN_WIDTHS = "column.widths";
    public static final String HINT_MARGIN_LEFT = "margin.left";
    public static final String HINT_PREFER_COMBO = "prefer.combo";
    public static final String HINT_PREFER_RADIO_BUTTONS = "prefer.radio.buttons";
    public static final String HINT_PREFER_VERTICAL_RADIO_BUTTONS = "prefer.vertical.radio.buttons";
    public static final String HINT_FACTORY = "factory";
    public static final String HINT_EXPAND_VERTICALLY = "expand.vertically";
    
    private static final List<PropertyEditorRendererFactory> FACTORIES = new ArrayList<PropertyEditorRendererFactory>();
    
    static
    {
        FACTORIES.add( new BooleanPropertyEditorRenderer.Factory() );
        FACTORIES.add( new EnumPropertyEditorRenderer.Factory() );
        FACTORIES.add( new NamedValuesPropertyEditorRenderer.Factory() );
        FACTORIES.add( new DefaultValuePropertyEditorRenderer.Factory() );
        FACTORIES.add( new SlushBucketPropertyEditor.Factory() );
        FACTORIES.add( new DefaultListPropertyEditorRenderer.Factory() );
    }
    
    private ModelProperty property;
    private List<ChildPropertyHelper> childProperties;
    private List<ChildPropertyHelper> childPropertiesReadOnly;
    private Map<String,Object> hints;
    private List<SapphirePropertyEditor> auxPropertyEditors;
    private List<SapphirePropertyEditor> auxPropertyEditorsReadOnly;
    
    @Override
    protected void init()
    {
        super.init();
        
        final ISapphireUiDef rootdef = (ISapphireUiDef) this.definition.getModel();
        final ISapphirePropertyEditorDef propertyEditorPartDef = (ISapphirePropertyEditorDef) this.definition;
        
        this.property = resolve( propertyEditorPartDef.getProperty().getContent() );
        
        this.childProperties = new ArrayList<ChildPropertyHelper>();
        this.childPropertiesReadOnly = Collections.unmodifiableList( this.childProperties );
        
        final ModelElementType type = this.property.getType();
        
        if( type != null )
        {
            if( propertyEditorPartDef.getChildProperties().isEmpty() )
            {
                for( ModelProperty childProperty : type.getProperties() )
                {
                    this.childProperties.add( new ChildPropertyHelper( childProperty, null ) );
                }
            }
            else
            {
                for( ISapphireChildPropertyInfo childPropertyInfo : propertyEditorPartDef.getChildProperties() )
                {
                    final String childPropertyName = childPropertyInfo.getName().getContent();
                    final ModelProperty childProperty = type.getProperty( childPropertyName );
                    
                    if( childProperty == null )
                    {
                        SapphireUiFrameworkPlugin.logError( "Could not resolve property: " + childPropertyName );
                    }
                    else
                    {
                        this.childProperties.add( new ChildPropertyHelper( childProperty, childPropertyInfo ) );
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
            
            if( name.equals( HINT_SHOW_LABEL ) ||
                name.equals( HINT_SHOW_LABEL_ABOVE ) ||
                name.equals( HINT_SHOW_HEADER ) ||
                name.equals( HINT_BORDER ) ||
                name.equals( HINT_BROWSE_ONLY ) ||
                name.equals( HINT_PREFER_COMBO ) ||
                name.equals( HINT_PREFER_RADIO_BUTTONS ) ||
                name.equals( HINT_PREFER_VERTICAL_RADIO_BUTTONS ) ||
                name.equals( HINT_EXPAND_VERTICALLY ) )
            {
                parsedValue = Boolean.parseBoolean( valueString );
            }
            else if( name.equals( ISapphirePartDef.HINT_HEIGHT ) ||
                     name.equals( ISapphirePartDef.HINT_WIDTH ) )
            {
                try
                {
                    parsedValue = Integer.parseInt( valueString );
                }
                catch( NumberFormatException e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
            else if( name.startsWith( HINT_FACTORY ) ||
                     name.startsWith( HINT_AUX_TEXT_PROVIDER ) )
            {
                parsedValue = rootdef.resolveClass( valueString );
            }
            else if( name.equals( HINT_ASSIST_CONTRIBUTORS ) ||
                     name.equals( HINT_LISTENERS ) )
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
            else if( name.equals( HINT_SUPPRESS_ASSIST_CONTRIBUTORS ) )
            {
                final List<String> contributors = new ArrayList<String>();
                
                for( String segment : valueString.split( "," ) )
                {
                    contributors.add( segment.trim() );
                }
                
                parsedValue = contributors;
            }
            
            this.hints.put( name, parsedValue );
        }
        
        this.auxPropertyEditors = new ArrayList<SapphirePropertyEditor>();
        this.auxPropertyEditorsReadOnly = Collections.unmodifiableList( this.auxPropertyEditors );
        
        for( ISapphirePropertyEditorDef auxPropertyEditorDef : propertyEditorPartDef.getAuxPropertyEditors() )
        {
            this.auxPropertyEditors.add( (SapphirePropertyEditor) create( this, getModelElement(), auxPropertyEditorDef, this.params ) );
        }
    }
    
    public ModelProperty getProperty()
    {
        return this.property;
    }
    
    public List<ChildPropertyHelper> getChildProperties()
    {
        return this.childPropertiesReadOnly;
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
    
    public int getLeftMarginHint()
    {
        String leftMarginHintStr = getRenderingHint( HINT_MARGIN_LEFT, null );
        int leftMarginHint = 0;
        
        if( leftMarginHintStr != null )
        {
            leftMarginHintStr = leftMarginHintStr.trim();
            final int length = leftMarginHintStr.length();
            
            if( leftMarginHintStr.endsWith( "px" ) && length > 2 )
            {
                try
                {
                    leftMarginHint = Integer.parseInt( leftMarginHintStr.substring( 0, length - 2 ) );
                }
                catch( NumberFormatException e ) {}
            }
            else if( leftMarginHintStr.endsWith( "u" ) && length > 1 )
            {
                try
                {
                    leftMarginHint = Integer.parseInt( leftMarginHintStr.substring( 0, length - 1 ) ) * 20;
                }
                catch( NumberFormatException e ) {}
            }
        }
        
        if( leftMarginHint < 0 )
        {
            leftMarginHint = 0;
        }
        
        return leftMarginHint;
    }
    
    public List<SapphirePropertyEditor> getAuxPropertyEditors()
    {
        return this.auxPropertyEditorsReadOnly;
    }

    @Override
    public void render( final SapphireRenderingContext context )
    {
        PropertyEditorRendererFactory factory = null;
        
        try
        {
            final Class<PropertyEditorRendererFactory> factoryClass 
                = getRenderingHint( HINT_FACTORY, (Class<PropertyEditorRendererFactory>) null );
            
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
            final PropertyEditorRenderer editor = factory.create( context, this );
            editor.create( context.getComposite() );
        }
        else
        {
            throw new IllegalStateException( this.property.toString() );
        }
    }

    @Override
    protected IStatus computeValidationState()
    {
        final IModelElement modelElement = getModelElement();
        
        if( modelElement.isPropertyEnabled( this.property ) )
        {
            final IModelParticle particle = (IModelParticle) this.property.invokeGetterMethod( modelElement );
            
            if( particle != null )
            {
                return particle.validate();
            }
        }
        
        return Status.OK_STATUS;
    }
    
    @Override
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        super.handleModelElementChange( event );
        
        if( event.getProperty().getName().equals( this.property.getName() ) )
        {
            updateValidationState();
        }
    }
    
    public static <T> T findControlForProperty( final Control context,
                                                final ModelProperty property,
                                                final Class<T> type )
    {
        Control root = context;
        
        while( ! ( root instanceof Section || root instanceof SapphireControl ) )
        {
            final Control parent = root.getParent();
            
            if( parent instanceof Shell )
            {
                break;
            }
            
            root = parent;
        }
        
        return findControlForPropertyHelper( root, property, type );
    }
    
    @SuppressWarnings( "unchecked" )
    
    private static <T> T findControlForPropertyHelper( final Control context,
                                                       final ModelProperty property,
                                                       final Class<T> type )
    {
        if( context.getData( DATA_PROPERTY ) == property && type.isAssignableFrom( context.getClass() ) )
        {
            return (T) context;
        }
        else if( context instanceof Composite )
        {
            for( Control child : ( (Composite) context ).getChildren() )
            {
                final T control = findControlForPropertyHelper( child, property, type );
                
                if( control != null )
                {
                    return control;
                }
            }
        }
        
        return null;
    }
    
    @Override
    
    public boolean setFocus()
    {
        if( getModelElement().isPropertyEnabled( this.property ) )
        {
            notifyFocusRecievedEventListeners();
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
    
    public final List<BrowseHandler> createBrowseHandlers()
    {
        return createBrowseHandlers( (ISapphirePropertyEditorDef) getDefinition(), getModelElement(), (ValueProperty) this.property );
    }
    
    private static final List<BrowseHandler> createBrowseHandlers( final ISapphirePropertyMetadata metadata,
                                                                   final IModelElement element,
                                                                   final ValueProperty property )
    {
        List<BrowseHandler> browseHandlers = null;
        
        if( metadata != null )
        {
            final ModelElementList<ISapphireBrowseHandlerDef> explicitBrowseHandlerDefs = metadata.getBrowseHandlers();
            
            if( ! explicitBrowseHandlerDefs.isEmpty() )
            {
                browseHandlers = new ArrayList<BrowseHandler>();
                
                for( ISapphireBrowseHandlerDef def : explicitBrowseHandlerDefs )
                {
                    BrowseHandler browseHandlerInstance = null;
                    
                    try
                    {
                        final Class<?> implClass = def.getImplClass().resolve();
                        
                        if( implClass != null )
                        {
                            browseHandlerInstance = (BrowseHandler) implClass.newInstance();
                            
                            final Map<String,String> params; 
                            
                            if( def.getParams().isEmpty() )
                            {
                                params = Collections.emptyMap();
                            }
                            else
                            {
                                params = new HashMap<String,String>();
                                
                                for( ISapphireParam param : def.getParams() )
                                {
                                    final String name = param.getName().getContent();
                                    final String value = param.getValue().getContent();
                                    
                                    if( name != null && value != null )
                                    {
                                        params.put( name, value );
                                    }
                                }
                            }
                            
                            browseHandlerInstance.init( element, property, params );
                        }
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                    }
                    
                    if( browseHandlerInstance != null )
                    {
                        browseHandlers.add( browseHandlerInstance );
                    }
                }
            }
        }
        
        if( browseHandlers == null )
        {
            browseHandlers = BrowseHandlersExtensionPoint.getBrowseHandlers( property );
            
            for( BrowseHandler browseHandler : browseHandlers )
            {
                browseHandler.init( element, property, Collections.<String,String>emptyMap() );
            }
        }
        
        return browseHandlers;
    }
    
    public final JumpHandler createJumpHandler()
    {
        return createJumpHandler( (ISapphirePropertyEditorDef) getDefinition(), (ValueProperty) this.property );
    }
    
    private static final JumpHandler createJumpHandler( final ISapphirePropertyMetadata metadata,
                                                        final ValueProperty property )
    {
        if( metadata != null )
        {
            final Class<?> explicitJumpHandlerClass = metadata.getJumpHandler().resolve();
            
            if( explicitJumpHandlerClass != null )
            {
                JumpHandler jumpHandlerInstance = null;
                
                try
                {
                    jumpHandlerInstance = (JumpHandler) explicitJumpHandlerClass.newInstance();
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
                
                return jumpHandlerInstance;
            }
        }
        
        return JumpHandlersExtensionPoint.getJumpHandler( property );
    }
    
    public final class ChildPropertyHelper
    {
        private final ModelProperty property;
        private final ISapphireChildPropertyInfo definition;
        
        public ChildPropertyHelper( final ModelProperty property,
                                    final ISapphireChildPropertyInfo definition )
        {
            this.property = property;
            this.definition = definition;
        }
        
        public ModelProperty getProperty()
        {
            return this.property;
        }
        
        public List<BrowseHandler> createBrowseHandlers( final IModelElement element )
        {
            final ValueProperty prop = (ValueProperty) this.property.refine( element );
            return SapphirePropertyEditor.createBrowseHandlers( this.definition, element, prop );
        }
        
        public JumpHandler createJumpHandler( final IModelElement element )
        {
            final ValueProperty prop = (ValueProperty) this.property.refine( element );
            return SapphirePropertyEditor.createJumpHandler( this.definition, prop );
        }
    }
    
}
