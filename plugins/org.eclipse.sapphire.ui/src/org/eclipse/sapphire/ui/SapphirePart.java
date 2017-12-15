/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Gregory Amerson - [372816] Provide adapt mechanism for SapphirePart
 *    Gregory Amerson - [373614] Suppport AdapterService in SapphirePart
 *    Gregory Amerson - [346172] Support zoom, print and save as image actions
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin.logError;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.AdapterService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.ui.def.ActuatorDef;
import org.eclipse.sapphire.ui.def.CompositeDef;
import org.eclipse.sapphire.ui.def.ConditionalDef;
import org.eclipse.sapphire.ui.def.DialogDef;
import org.eclipse.sapphire.ui.def.FormDef;
import org.eclipse.sapphire.ui.def.FormEditorPageDef;
import org.eclipse.sapphire.ui.def.HtmlPanelDef;
import org.eclipse.sapphire.ui.def.IFormPartInclude;
import org.eclipse.sapphire.ui.def.ISapphireCustomPartDef;
import org.eclipse.sapphire.ui.def.ISapphireGroupDef;
import org.eclipse.sapphire.ui.def.ISapphireLabelDef;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.ISapphirePartListenerDef;
import org.eclipse.sapphire.ui.def.ISapphireStaticTextFieldDef;
import org.eclipse.sapphire.ui.def.ISapphireWithDirectiveDef;
import org.eclipse.sapphire.ui.def.LineSeparatorDef;
import org.eclipse.sapphire.ui.def.PageBookExtDef;
import org.eclipse.sapphire.ui.def.PageBookPartControlMethod;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.def.SectionDef;
import org.eclipse.sapphire.ui.def.SplitFormBlockDef;
import org.eclipse.sapphire.ui.def.SplitFormDef;
import org.eclipse.sapphire.ui.def.TabGroupDef;
import org.eclipse.sapphire.ui.def.WhitespaceSeparatorDef;
import org.eclipse.sapphire.ui.def.WizardPageDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsEditorPagePart;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.internal.PartServiceContext;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class SapphirePart implements ISapphirePart
{
    private ISapphirePart parent;
    private IModelElement modelElement;
    protected PartDef definition;
    protected Map<String,String> params;
    private Listener modelElementListener;
    private Status validation;
    private final ListenerContext listeners = new ListenerContext();
    private Set<SapphirePartListener> listenersDeprecated;
    private SapphireImageCache imageCache;
    private Map<String,SapphireActionGroup> actions;
    private PartServiceContext serviceContext;
    private FunctionResult visibilityFunctionResult;
    private boolean visibilityReadBeforeInit;
    private boolean initialized;
    private boolean disposed;
    
    public final boolean initialized()
    {
        return this.initialized;
    }
    
    public final void init( final ISapphirePart parent,
                            final IModelElement modelElement,
                            final PartDef definition,
                            final Map<String,String> params )
    {
        this.parent = parent;
        this.definition = definition;
        this.params = params;

        if( modelElement == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.modelElement = modelElement;
        
        this.modelElementListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                handleModelElementChange( event );
            }
        };
        
        this.modelElement.attach( this.modelElementListener );
        
        this.listeners.coordinate( ( (ModelElement) this.modelElement ).listeners() );
        
        for( ISapphirePartListenerDef listenerDefinition : this.definition.getListeners() )
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
                    if( listener instanceof Listener )
                    {
                        attach( (Listener) listener );
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
        
        this.visibilityFunctionResult = initExpression
        (
            getLocalModelElement(),
            initVisibleWhenFunction(), 
            Boolean.class,
            Literal.TRUE,
            new Runnable()
            {
                public void run()
                {
                    broadcast( new PartVisibilityEvent( SapphirePart.this ) );
                }
            }
        );
        
        this.initialized = true;
        
        broadcast( new PartInitializationEvent( this ) );
        
        if( this.visibilityReadBeforeInit && this.visibilityFunctionResult.value().equals( Boolean.TRUE ) )
        {
            broadcast( new PartVisibilityEvent( this ) );
        }
    }
    
    protected void init()
    {
        // The default implement doesn't do anything.
    }
    
    public final FunctionResult initExpression( final IModelElement contextModelElement,
                                                final Function function,
                                                final Class<?> expectedType,
                                                final Function defaultValue )
    {
        return initExpression( contextModelElement, function, expectedType, defaultValue, null );
    }
    
    public final FunctionResult initExpression( final IModelElement contextModelElement,
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
        
        if( refreshOp != null )
        {
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
                                if( ! disposed() && ! getLocalModelElement().disposed() )
                                {
                                    refreshOp.run();
                                }
                            }
                        };
                     
                        Display.getDefault().asyncExec( notifyOfUpdateOperation );
                    }
                }
            );
        }
        
        return fr;
    }
    
    protected Function initVisibleWhenFunction()
    {
        return this.definition.getVisibleWhen().getContent();
    }
    
    protected static final Function createVersionCompatibleFunction( final IModelElement element,
                                                                     final ModelProperty property )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( property != null )
        {
            final MasterVersionCompatibilityService service = element.service( property, MasterVersionCompatibilityService.class );
            
            final Function function = new Function()
            {
                @Override
                public String name()
                {
                    return "VersionCompatible";
                }

                @Override
                public FunctionResult evaluate( final FunctionContext context )
                {
                    return new FunctionResult( this, context )
                    {
                        private Listener serviceListener;
                        private Listener propertyListener;
                        
                        @Override
                        protected void init()
                        {
                            this.serviceListener = new Listener()
                            {
                                @Override
                                public void handle( final Event event )
                                {
                                    refresh();
                                }
                            };
                            
                            service.attach( this.serviceListener );
                            
                            this.propertyListener = new FilteredListener<PropertyContentEvent>()
                            {
                                @Override
                                protected void handleTypedEvent( final PropertyContentEvent event )
                                {
                                    refresh();
                                }
                            };
                            
                            if( property instanceof ImpliedElementProperty )
                            {
                                element.attach( this.propertyListener, property.getName() + "/*" );
                            }
                            else
                            {
                                element.attach( this.propertyListener, property.getName() );
                            }
                        }

                        @Override
                        protected Object evaluate()
                        {
                            return service.compatible() || ! element.empty( property );
                        }
                        
                        @Override
                        public void dispose()
                        {
                            super.dispose();
                            
                            service.detach( this.serviceListener );

                            if( property instanceof ImpliedElementProperty )
                            {
                                element.detach( this.propertyListener, property.getName() + "/*" );
                            }
                            else
                            {
                                element.detach( this.propertyListener, property.getName() );
                            }
                        }
                    };
                }
            };
            
            function.init();
            
            return function;
        }
        
        return null;
    }

    public abstract void render( final SapphireRenderingContext context );
    
    public PartDef definition()
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
    
    public final Status validation()
    {
        if( this.validation == null )
        {
            refreshValidation();
        }
        
        return this.validation;
    }
    
    protected Status computeValidation()
    {
        return Status.createOkStatus();
    }
    
    protected final void refreshValidation()
    {
        final Status newValidationState = computeValidation();
        
        if( newValidationState == null )
        {
            throw new IllegalStateException();
        }
        
        if( this.validation == null )
        {
            this.validation = newValidationState;
        }
        else if( ! this.validation.equals( newValidationState ) )
        {
            this.validation = newValidationState;
            broadcast( new PartValidationEvent( this ) );
        }
    }
    
    public final boolean visible()
    {
        if( this.visibilityFunctionResult == null )
        {
            this.visibilityReadBeforeInit = true;
            return false;
        }
        
        return (Boolean) this.visibilityFunctionResult.value();
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
    
    public IContext getDocumentationContext()
    {
        return null;
    }

    public SapphireImageCache getImageCache()
    {
        if( this.imageCache == null )
        {
            this.imageCache = ( this.parent == null ? new SapphireImageCache() : this.parent.getImageCache() );
        }
        
        return this.imageCache;
    }
    
    protected void handleModelElementChange( final Event event )
    {
        // The default implement doesn't do anything.
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
    
    @Deprecated
    public final void addListener( final SapphirePartListener listener )
    {
        if( this.listenersDeprecated == null )
        {
            this.listenersDeprecated = Collections.singleton( listener );
        }
        else
        {
            this.listenersDeprecated = new HashSet<SapphirePartListener>( this.listenersDeprecated );
            this.listenersDeprecated.add( listener );
        }
    }
    
    @Deprecated
    public final void removeListener( final SapphirePartListener listener )
    {
        if( this.listenersDeprecated != null )
        {
            if( this.listenersDeprecated.contains( listener ) )
            {
                if( this.listenersDeprecated.size() == 1 )
                {
                    this.listenersDeprecated = null;
                }
                else
                {
                    this.listenersDeprecated = new HashSet<SapphirePartListener>( this.listenersDeprecated );
                    this.listenersDeprecated.remove( listener );
                }
            }
        }
    }
    
    @Deprecated
    public final Set<SapphirePartListener> getListeners()
    {
        if( this.listenersDeprecated == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return this.listenersDeprecated;
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
            
            final ModelElementType type = modelElement.type();
            final ModelProperty property = type.property( propertyName );
            
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
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;

        for( AdapterService service : services( AdapterService.class ) )
        {
            result = service.adapt( adapterType );

            if( result != null )
            {
                break;
            }
        }

        if( result == null )
        {
            final IModelElement element = getLocalModelElement();
            
            if( element != null )
            {
                result = element.adapt( adapterType );
            }
        }
    
        if( result == null && this.parent != null )
        {
            result = this.parent.adapt( adapterType );
        }
    
        return result;
    }
    
    public final <S extends Service> S service( final Class<S> serviceType )
    {
        final List<S> services = services( serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    public final <S extends Service> List<S> services( final Class<S> serviceType )
    {
        if( this.serviceContext == null )
        {
            this.serviceContext = new PartServiceContext( this );
        }
        
        return this.serviceContext.services( serviceType );
    }
    
    /**
     * Executes a job after this part has been fully initialized. If the part has already been
     * initialized, the job is executed immediately.
     * 
     * @param job the job to perform
     * @return true if the job has been performed prior to returning to the caller, false otherwise
     * @throws IllegalArgumentException if job is null
     */
    
    public final boolean executeAfterInitialization( final Runnable job )
    {
        if( job == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( initialized() )
        {
            job.run();
            return true;
        }
        else
        {
            attach
            (
                new FilteredListener<PartInitializationEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final PartInitializationEvent event )
                    {
                        detach( this );
                        job.run();
                    }
                }
            );
            
            return false;
        }
    }
    
    public void dispose()
    {
        boolean performOnDisposeTasks = false;
        
        synchronized( this )
        {
            if( ! this.disposed )
            {
                this.disposed = true;
                performOnDisposeTasks = true;
            }
        }
        
        if( performOnDisposeTasks )
        {
            this.modelElement.detach( this.modelElementListener );
        
            if( this.parent == null && this.imageCache != null )
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
            
            if( this.serviceContext != null )
            {
                this.serviceContext.dispose();
            }
            
            if( this.visibilityFunctionResult != null )
            {
                this.visibilityFunctionResult.dispose();
            }
            
            broadcast( new DisposeEvent() );
        }
    }
    
    public final boolean disposed()
    {
        synchronized( this )
        {
            return this.disposed;
        }
    }
    
    protected final class ImageManager
    {
        private final IModelElement element;
        private final Function imageFunction;
        private final Function defaultValueFunction;        
        private boolean initialized;
        private boolean broadcastImageEvents;
        private FunctionResult imageFunctionResult;
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
            this.element = element;
            this.imageFunction = imageFunction;
            this.defaultValueFunction = defaultValueFunction;
        }
        
        private void init()
        {
            if( ! this.initialized )
            {
                this.initialized = true;
                
                this.imageFunctionResult = initExpression
                (
                    this.element,
                    this.imageFunction,
                    ImageData.class,
                    this.defaultValueFunction,
                    new Runnable()
                    {
                        public void run()
                        {
                            refresh();
                        }
                    }
                );
                
                attach
                (
                    new FilteredListener<PartValidationEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( PartValidationEvent event )
                        {
                            refresh();
                        }
                    }
                );
                
                refresh();
                
                this.broadcastImageEvents = true;
            }
        }
        
        public ImageDescriptor getImage()
        {
            init();
            
            this.broadcastImageEvents = true;
            
            return this.current;
        }
        
        private void refresh()
        {
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
                    this.current = this.base;
                    
                    final Status st = validation();
                    final Status.Severity severity = st.severity();

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
                }
            }
            
            if( this.broadcastImageEvents && this.current != old )
            {
                broadcast( new ImageChangedEvent( SapphirePart.this ) );
            }
        }
        
        public void dispose()
        {
            if( this.imageFunctionResult != null )
            {
                this.imageFunctionResult.dispose();
            }
        }
    }
    
    public static abstract class PartEvent extends Event
    {
        private final SapphirePart part;
        
        public PartEvent( final SapphirePart part )
        {
            this.part = part;
        }
        
        public SapphirePart part()
        {
            return this.part;
        }

        @Override
        protected Map<String,String> fillTracingInfo( Map<String,String> info )
        {
            super.fillTracingInfo( info );
            
            final IModelElement element = this.part.getLocalModelElement();
            info.put( "part", this.part.getClass().getName() + '(' + System.identityHashCode( this.part ) + ')' );
            info.put( "element", element.type().getQualifiedName() + '(' + System.identityHashCode( element ) + ')' );
            
            return info;
        }
    }
    
    public static final class PartInitializationEvent extends PartEvent
    {
        public PartInitializationEvent( final SapphirePart part )
        {
            super( part );
        }
    }
    
    public static final class LabelChangedEvent extends PartEvent
    {
        public LabelChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }

    public static final class ImageChangedEvent extends PartEvent
    {
        public ImageChangedEvent( final SapphirePart part )
        {
            super( part );
        }
    }

    public static final class FocusReceivedEvent extends PartEvent
    {
        public FocusReceivedEvent( final SapphirePart part )
        {
            super( part );
        }
    }
    
    public static final SapphirePart create( final ISapphirePart parent,
                                             final IModelElement element,
                                             final PartDef definition,
                                             final Map<String,String> params )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        SapphirePart part = null;
        Map<String,String> partParams = params;
        PartDef def = definition;
        
        if( definition instanceof PropertyEditorDef )
        {
            part = new PropertyEditorPart();
        }
        else if( definition instanceof ISapphireLabelDef )
        {
            part = new LabelPart();
        }
        else if( definition instanceof LineSeparatorDef )
        {
            part = new LineSeparatorPart();
        }
        else if( definition instanceof WhitespaceSeparatorDef )
        {
            part = new WhitespaceSeparatorPart();
        }
        else if( definition instanceof ActuatorDef )
        {
            part = new ActuatorPart(); 
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
                = SapphireWithDirectiveHelper.resolvePath( element, (ISapphireWithDirectiveDef) definition, partParams );
            
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
        else if( definition instanceof DialogDef )
        {
            part = new SapphireDialogPart();
        }
        else if( definition instanceof WizardPageDef )
        {
            part = new SapphireWizardPagePart();
        }
        else if( definition instanceof SectionDef )
        {
            part = new SectionPart();
        }
        else if( definition instanceof CompositeDef )
        {
            part = new CompositePart();
        }
        else if( definition instanceof IFormPartInclude )
        {
            final IFormPartInclude inc = (IFormPartInclude) definition;
            def = inc.getPart().resolve();
            
            if( def == null )
            {
                final String msg = NLS.bind( Resources.couldNotResolveInclude, inc.getPart().getText() );
                throw new IllegalArgumentException( msg );
            }
            else
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
                
                return create( parent, element, def, partParams );
            }
        }
        else if( definition instanceof TabGroupDef )
        {
            part = new TabGroupPart();
        }
        else if( definition instanceof ConditionalDef )
        {
            part = new ConditionalPart();
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
            part = new FormPart();
        }
        else if( definition instanceof MasterDetailsEditorPageDef )
        {
            part = new MasterDetailsEditorPagePart();
        }
        else if( definition instanceof FormEditorPageDef )
        {
            part = new FormEditorPagePart();
        }
        
        if( part == null )
        {
            throw new IllegalStateException();
        }
        
        part.init( parent, element, def, partParams );
        
        return part;
    }

    private static final class Resources extends NLS
    {
        public static String failedToInstantiate;
        public static String doesNotExtend;
        public static String couldNotResolveInclude;
        
        static
        {
            initializeMessages( SapphirePart.class.getName(), Resources.class );
        }
    }
    
}
