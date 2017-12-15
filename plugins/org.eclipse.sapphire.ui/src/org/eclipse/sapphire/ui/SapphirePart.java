/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin.logError;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.ui.def.FormDef;
import org.eclipse.sapphire.ui.def.HtmlPanelDef;
import org.eclipse.sapphire.ui.def.IFormPartInclude;
import org.eclipse.sapphire.ui.def.ISapphireActionLinkDef;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;
import org.eclipse.sapphire.ui.def.ISapphireCustomPartDef;
import org.eclipse.sapphire.ui.def.ISapphireDialogDef;
import org.eclipse.sapphire.ui.def.ISapphireGroupDef;
import org.eclipse.sapphire.ui.def.ISapphireIfElseDirectiveDef;
import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphirePartListenerDef;
import org.eclipse.sapphire.ui.def.ISapphireSectionDef;
import org.eclipse.sapphire.ui.def.ISapphireSeparatorDef;
import org.eclipse.sapphire.ui.def.ISapphireSpacerDef;
import org.eclipse.sapphire.ui.def.ISapphireStaticTextFieldDef;
import org.eclipse.sapphire.ui.def.ISapphireTabGroupDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;
import org.eclipse.sapphire.ui.def.ISapphireWizardPageDef;
import org.eclipse.sapphire.ui.def.PageBookExtDef;
import org.eclipse.sapphire.ui.def.PageBookPartControlMethod;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.def.SplitFormDef;
import org.eclipse.sapphire.ui.def.SplitFormBlockDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphirePart implements ISapphirePart
{
    private ISapphirePart parent;
    private IModelElement modelElement;
    protected ISapphirePartDef definition;
    protected Map<String,String> params;
    private ModelElementListener modelElementListener;
    private Status validationState;
    private Set<SapphirePartListener> listeners;
    private SapphireImageCache imageCache;
    private Map<String,SapphireActionGroup> actions;
    
    public final void init( final ISapphirePart parent,
                            final IModelElement modelElement,
                            final ISapphirePartDef definition,
                            final Map<String,String> params )
    {
        this.parent = parent;
        this.definition = definition;
        this.params = params;
        this.imageCache = ( this.parent == null ? new SapphireImageCache() : this.parent.getImageCache() );

        if( modelElement == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.modelElement = modelElement;
        
        this.modelElementListener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                handleModelElementChange( event );
            }
        };
        
        this.modelElement.addListener( this.modelElementListener );
        
        this.validationState = Status.createOkStatus();
        this.listeners = null;
        
        for( ISapphirePartListenerDef listenerDefinition : definition.getListeners() )
        {
            final JavaType listenerClass = listenerDefinition.getListenerClass().resolve();
            
            if( listenerClass != null )
            {
                Object listener = null;
                
                try
                {
                    listener = listenerClass.artifact().newInstance();
                }
                catch( Exception e )
                {
                    final String msg = NLS.bind( Resources.failedToInstantiate, listenerClass.name() );
                    logError( msg, e );
                }
                
                if( listener != null )
                {
                    if( listener instanceof SapphirePartListener )
                    {
                        addListener( (SapphirePartListener) listener );
                    }
                    else
                    {
                        final String msg = NLS.bind( Resources.doesNotExtend, listenerClass.name() );
                        logError( msg );
                    }
                }
            }
        }
        
        init();
        
        updateValidationState();
    }
    
    protected void init()
    {
        // The default implement doesn't do anything.
    }
    
    protected final FunctionResult initExpression( final IModelElement contextModelElement,
                                                   final Function function,
                                                   final Class<?> expectedType,
                                                   final Function defaultValue,
                                                   final Runnable refreshOp )
    {
        Function f = ( function == null ? Literal.NULL : function );
        f = FailSafeFunction.create( f, Literal.create( expectedType ), defaultValue );
        
        final FunctionContext context = new ModelElementFunctionContext( contextModelElement, this.definition.adapt( LocalizationService.class ) )
        {
            @Override
            public FunctionResult property( final Object element,
                                            final String name )
            {
                if( name.equalsIgnoreCase( "params" ) )
                {
                    return Literal.create( SapphirePart.this.params ).evaluate( this );
                }

                return super.property( element, name );
            }
        };
        
        final FunctionResult fr = f.evaluate( context );
        
        fr.attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    final Runnable notifyOfUpdateOperation = new Runnable()
                    {
                        public void run()
                        {
                            refreshOp.run();
                        }
                    };
                 
                    Display.getDefault().asyncExec( notifyOfUpdateOperation );
                }
            }
        );
        
        return fr;
    }

    public abstract void render( final SapphireRenderingContext context );
    
    public ISapphirePartDef getDefinition()
    {
        return this.definition;
    }
    
    public final ISapphirePart getParentPart()
    {
        return this.parent;
    }
    
    @SuppressWarnings( "unchecked" )
    public final <T> T nearest( final Class<T> partType )
    {
        if( partType.isAssignableFrom( getClass() ) )
        {
            return (T) this;
        }
        else
        {
            if( this.parent != null )
            {
                return this.parent.nearest( partType );
            }
            else
            {
                return null;
            }
        }
    }
    
    public final IModelElement getModelElement()
    {
        return this.modelElement;
    }
    
    public IModelElement getLocalModelElement()
    {
        return this.modelElement;
    }
    
    public final Map<String,String> getParams()
    {
        return Collections.unmodifiableMap( this.params );
    }
    
    public final Status getValidationState()
    {
        return this.validationState;
    }
    
    protected Status computeValidationState()
    {
        return Status.createOkStatus();
    }
    
    public final void updateValidationState()
    {
        final Status newValidationState = computeValidationState();
        
        if( this.validationState == null || newValidationState == null || ! this.validationState.equals( newValidationState ) )
        {
            final Status oldValidationState = this.validationState;
            this.validationState = newValidationState;
            
            if( this.listeners != null )
            {
                for( SapphirePartListener listener : this.listeners )
                {
                    try
                    {
                        listener.handleValidateStateChange( oldValidationState, newValidationState );
                    }
                    catch( Exception e )
                    {
                        SapphireUiFrameworkPlugin.log( e );
                    }
                }
            }
        }
    }
    
    public boolean setFocus()
    {
        return false;
    }
    
    public boolean setFocus( final ModelPath path )
    {
        return false;
    }
    
    public final boolean setFocus( final String path )
    {
        return setFocus( new ModelPath( path ) );
    }
    
    protected final void notifyFocusRecievedEventListeners()
    {
        if( this.listeners != null )
        {
            final SapphirePartEvent event = new SapphirePartEvent( this );
            
            for( SapphirePartListener listener : this.listeners )
            {
                try
                {
                    listener.handleFocusReceivedEvent( event );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }
    
    public IContext getDocumentationContext()
    {
        return null;
    }

    public SapphireImageCache getImageCache()
    {
        return this.imageCache;
    }
    
    protected void handleModelElementChange( final ModelPropertyChangeEvent event )
    {
        // The default implement doesn't do anything.
    }
    
    public final void addListener( final SapphirePartListener listener )
    {
        if( this.listeners == null )
        {
            this.listeners = Collections.singleton( listener );
        }
        else
        {
            this.listeners = new HashSet<SapphirePartListener>( this.listeners );
            this.listeners.add( listener );
        }
    }
    
    public final void removeListener( final SapphirePartListener listener )
    {
        if( this.listeners != null )
        {
            if( this.listeners.contains( listener ) )
            {
                if( this.listeners.size() == 1 )
                {
                    this.listeners = null;
                }
                else
                {
                    this.listeners = new HashSet<SapphirePartListener>( this.listeners );
                    this.listeners.remove( listener );
                }
            }
        }
    }
    
    public final Set<SapphirePartListener> getListeners()
    {
        if( this.listeners == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return this.listeners;
        }
    }
    
    public final void notifyStructureChangedEventListeners( final SapphirePartEvent event )
    {
        if( this.listeners != null )
        {
            for( SapphirePartListener listener : this.listeners )
            {
                try
                {
                    listener.handleStructureChangedEvent( event );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
        
        if( this.parent != null && this.parent instanceof SapphirePart )
        {
            ( (SapphirePart) this.parent ).notifyStructureChangedEventListeners( event );
        }
    }
    
    public final void notifyListeners( final SapphirePartEvent event )
    {
        if( this.listeners != null )
        {
            for( SapphirePartListener listener : this.listeners )
            {
                try
                {
                    listener.handleEvent( event );
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
    }

    public final ModelProperty resolve( final String propertyName )
    {
        return resolve( this.modelElement, propertyName );
    }

    public final ModelProperty resolve( final IModelElement modelElement,
                                        String propertyName )
    {
        return resolve( modelElement, propertyName, this.params );
    }
    
    public static final ModelProperty resolve( final IModelElement modelElement,
                                               String propertyName,
                                               final Map<String,String> params )
    {
        if( propertyName != null )
        {
            propertyName = substituteParams( propertyName.trim(), params );
            
            final ModelElementType type = modelElement.getModelElementType();
            final ModelProperty property = type.getProperty( propertyName );
            
            if( property == null )
            {
                throw new RuntimeException( "Could not find property " + propertyName + " in " + type.getQualifiedName() );
            }
        
            return property;
        }
        
        return null;
    }
    
    public final String substituteParams( final String str )
    {
        return substituteParams( str, this.params );
    }
    
    public static final String substituteParams( final String str,
                                                 final Map<String,String> params )
    {
        String result = str;
        
        if( str != null && str.contains( "@{" ) )
        {
            for( final Map.Entry<String,String> param : params.entrySet() )
            {
                final StringBuilder token = new StringBuilder();
                token.append( "@{" );
                token.append( param.getKey() );
                token.append( '}' );
                
                result = result.replace( token, param.getValue() );
            }
        }
        
        return result;
    }
    
    /**
     * Returns the action contexts defined by this part. The default implementation returns an empty set.
     * Part implementations should override to define action contexts.
     * 
     * @return the action contexts defined by this part
     */
    
    public Set<String> getActionContexts()
    {
        return Collections.emptySet();
    }
    
    public String getMainActionContext()
    {
        final Set<String> contexts = getActionContexts();
        
        if( ! contexts.isEmpty() )
        {
            return contexts.iterator().next();
        }
        
        return null;
    }
    
    public final SapphireActionGroup getActions()
    {
        final String context = getMainActionContext();
        
        if( context != null )
        {
            return getActions( context );
        }
        
        return null;
    }
    
    public final SapphireActionGroup getActions( final String context )
    {
        if( this.actions == null )
        {
            this.actions = new HashMap<String,SapphireActionGroup>();
            
            for( String ctxt : getActionContexts() )
            {
                final SapphireActionGroup actionsForContext = new SapphireActionGroup( this, ctxt );
                this.actions.put( ctxt.toLowerCase(), actionsForContext );
            }
        }
        
        return this.actions.get( context.toLowerCase() );
    }
    
    public final SapphireAction getAction( final String id )
    {
        for( final String context : getActionContexts() )
        {
            final SapphireAction action = getActions( context ).getAction( id );
            
            if( action != null )
            {
                return action;
            }
        }
        
        if( this.parent != null )
        {
            return this.parent.getAction( id );
        }
        
        return null;
    }
    
    public boolean isSingleLinePart()
    {
        return false;
    }
    
    public void dispose()
    {
        this.modelElement.removeListener( this.modelElementListener );
        
        if( this.parent == null )
        {
            this.imageCache.dispose();
        }
        
        if( this.actions != null )
        {
            for( SapphireActionGroup actionsForContext : this.actions.values() )
            {
                actionsForContext.dispose();
            }
        }
    }
    
    protected final class ImageManager
    {
        private final FunctionResult imageFunctionResult;
        private ImageData baseImageData;
        private ImageDescriptor base;
        private ImageDescriptor error;
        private ImageDescriptor warning;
        private ImageDescriptor current;
        
        public ImageManager( final IModelElement element,
                             final Function imageFunction )
        {
            this( element, imageFunction, Literal.NULL );
        }
        
        public ImageManager( final IModelElement element,
                             final Function imageFunction,
                             final Function defaultValueFunction )
        {
            this.imageFunctionResult = initExpression
            (
                element,
                imageFunction,
                ImageData.class,
                defaultValueFunction,
                new Runnable()
                {
                    public void run()
                    {
                        refresh( true );
                    }
                }
            );
            
            addListener
            (
                new SapphirePartListener()
                {
                    @Override
                    public void handleValidateStateChange( final Status oldValidateState,
                                                           final Status newValidationState )
                    {
                        refresh( true );
                    }
                }
            );
            
            refresh( false );
        }
        
        public ImageDescriptor getImage()
        {
            return this.current;
        }
        
        private void refresh( final boolean notifyListenersIfNecessary )
        {
            final Status st = getValidationState();
            final Status.Severity severity = st.severity();
            final ImageDescriptor old = this.current;
            
            if( this.imageFunctionResult != null )
            {
                final ImageData newBaseImageData = (ImageData) this.imageFunctionResult.value();
                
                if( this.baseImageData != newBaseImageData )
                {
                    this.baseImageData = newBaseImageData;
                    this.base = SwtRendererUtil.toImageDescriptor( this.baseImageData );
                    this.error = null;
                    this.warning = null;
                }
                
                if( this.base == null )
                {
                    this.current = null;
                }
                else
                {
                    if( severity == Status.Severity.ERROR )
                    {
                        if( this.error == null )
                        {
                            this.error = new ProblemOverlayImageDescriptor( this.base, Status.Severity.ERROR );
                        }
                        
                        this.current = this.error;
                    }
                    else if( severity == Status.Severity.WARNING )
                    {
                        if( this.warning == null )
                        {
                            this.warning = new ProblemOverlayImageDescriptor( this.base, Status.Severity.WARNING );
                        }
                        
                        this.current = this.warning;
                    }
                    else
                    {
                        this.current = this.base;
                    }
                }
            }
            
            if( notifyListenersIfNecessary && this.current != old )
            {
                notifyListeners( new ImageChangedEvent( SapphirePart.this ) );
            }
        }
        
        public void dispose()
        {
            this.imageFunctionResult.dispose();
        }
    }
    
    public static final class LabelChangedEvent extends SapphirePartEvent
    {
        public LabelChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }
    
    public static final class ImageChangedEvent extends SapphirePartEvent
    {
        public ImageChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }
    
    public static final class VisibilityChangedEvent extends SapphirePartEvent
    {
        public VisibilityChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }
    
    public static final SapphirePart create( final SapphirePart parent,
                                             final IModelElement modelElement,
                                             final ISapphirePartDef definition,
                                             final Map<String,String> params )
    {
        if( modelElement == null )
        {
            throw new IllegalArgumentException();
        }
        
        SapphirePart part = null;
        Map<String,String> partParams = params;
        ISapphirePartDef def = definition;
        
        if( definition instanceof PropertyEditorDef )
        {
            part = new SapphirePropertyEditor();
        }
        else if( definition instanceof ISapphireLabelDef )
        {
            part = new SapphireLabel();
        }
        else if( definition instanceof ISapphireSeparatorDef )
        {
            part = new SapphireSeparator();
        }
        else if( definition instanceof ISapphireSpacerDef )
        {
            part = new SapphireSpacer();
        }
        else if( definition instanceof ISapphireActionLinkDef )
        {
            part = new SapphireActionLink(); 
        }
        else if( definition instanceof ISapphireCustomPartDef )
        {
            final JavaType customPartImplClass = ( (ISapphireCustomPartDef) definition ).getImplClass().resolve();
            
            if( customPartImplClass != null )
            {
                try
                {
                    part = (SapphirePart) customPartImplClass.artifact().newInstance();
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
        else if( definition instanceof ISapphireStaticTextFieldDef )
        {
            part = new SapphireStaticTextField();
        }
        else if( definition instanceof ISapphireGroupDef )
        {
            part = new SapphireGroup();
        }
        else if( definition instanceof ISapphireWithDirectiveDef )
        {
            final SapphireWithDirectiveHelper.ResolvePathResult resolvePathResult 
                = SapphireWithDirectiveHelper.resolvePath( modelElement, (ISapphireWithDirectiveDef) definition, partParams );
            
            if( resolvePathResult.property == null )
            {
                part = new SapphireWithDirectiveImplied();
            }
            else
            {
                part = new SapphireWithDirective();
            }
        }
        else if( definition instanceof PageBookExtDef )
        {
            final PageBookExtDef pageBookPartDef = (PageBookExtDef) definition;
            
            if( pageBookPartDef.getControlMethod().getContent() == PageBookPartControlMethod.ENUM_VALUE )
            {
                part = new SapphireEnumControlledPageBook();
            }
            else
            {
                part = new SapphireListControlledPageBook();
            }
        }
        else if( definition instanceof ISapphireDialogDef )
        {
            part = new SapphireDialogPart();
        }
        else if( definition instanceof ISapphireWizardPageDef )
        {
            part = new SapphireWizardPagePart();
        }
        else if( definition instanceof ISapphireSectionDef )
        {
            part = new SapphireSection();
        }
        else if( definition instanceof ISapphireCompositeDef )
        {
            part = new SapphireComposite();
        }
        else if( definition instanceof IFormPartInclude )
        {
            final IFormPartInclude inc = (IFormPartInclude) definition;
            def = inc.getPart().resolve();
            
            if( def != null )
            {
                partParams = new HashMap<String,String>( params );
                
                for( ISapphireParam param : inc.getParams() )
                {
                    final String paramName = param.getName().getText();
                    final String paramValue = param.getValue().getText();
                    
                    if( paramName != null && paramValue != null )
                    {
                        partParams.put( paramName, paramValue );
                    }
                }
                
                part = new SapphirePartContainer();
            }
        }
        else if( definition instanceof ISapphireTabGroupDef )
        {
            part = new TabGroupPart();
        }
        else if( definition instanceof ISapphireIfElseDirectiveDef )
        {
            part = new IfThenElsePart();
        }
        else if( definition instanceof HtmlPanelDef )
        {
            part = new HtmlPanelPart();
        }
        else if( definition instanceof SplitFormDef )
        {
            part = new SplitFormPart();
        }
        else if( definition instanceof SplitFormBlockDef )
        {
            part = new SplitFormBlockPart();
        }
        else if( definition instanceof FormDef )
        {
            part = new SapphirePartContainer();
        }
        
        if( part == null )
        {
            throw new IllegalStateException();
        }
        
        part.init( parent, modelElement, def, partParams );
        
        return part;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String failedToInstantiate;
        public static String doesNotExtend;
        
        static
        {
            initializeMessages( SapphirePart.class.getName(), Resources.class );
        }
    }
    
}
