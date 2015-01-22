/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.IContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementImpl;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.def.ISapphirePartListenerDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.forms.ActuatorDef;
import org.eclipse.sapphire.ui.forms.ActuatorPart;
import org.eclipse.sapphire.ui.forms.CompositeDef;
import org.eclipse.sapphire.ui.forms.CompositePart;
import org.eclipse.sapphire.ui.forms.CustomFormComponentDef;
import org.eclipse.sapphire.ui.forms.DetailSectionDef;
import org.eclipse.sapphire.ui.forms.DetailSectionPart;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.DialogPart;
import org.eclipse.sapphire.ui.forms.FormComponentRef;
import org.eclipse.sapphire.ui.forms.FormDef;
import org.eclipse.sapphire.ui.forms.FormEditorPageDef;
import org.eclipse.sapphire.ui.forms.FormEditorPagePart;
import org.eclipse.sapphire.ui.forms.FormPart;
import org.eclipse.sapphire.ui.forms.GroupDef;
import org.eclipse.sapphire.ui.forms.GroupPart;
import org.eclipse.sapphire.ui.forms.HtmlPanelDef;
import org.eclipse.sapphire.ui.forms.HtmlPanelPart;
import org.eclipse.sapphire.ui.forms.LineSeparatorDef;
import org.eclipse.sapphire.ui.forms.LineSeparatorPart;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.SectionDef;
import org.eclipse.sapphire.ui.forms.SectionPart;
import org.eclipse.sapphire.ui.forms.SectionRef;
import org.eclipse.sapphire.ui.forms.SpacerDef;
import org.eclipse.sapphire.ui.forms.SpacerPart;
import org.eclipse.sapphire.ui.forms.SplitFormDef;
import org.eclipse.sapphire.ui.forms.SplitFormPart;
import org.eclipse.sapphire.ui.forms.SplitFormSectionDef;
import org.eclipse.sapphire.ui.forms.SplitFormSectionPart;
import org.eclipse.sapphire.ui.forms.StaticTextFieldDef;
import org.eclipse.sapphire.ui.forms.StaticTextFieldPart;
import org.eclipse.sapphire.ui.forms.TabGroupDef;
import org.eclipse.sapphire.ui.forms.TabGroupPart;
import org.eclipse.sapphire.ui.forms.TextDef;
import org.eclipse.sapphire.ui.forms.TextPart;
import org.eclipse.sapphire.ui.forms.WithDef;
import org.eclipse.sapphire.ui.forms.WithImpliedPart;
import org.eclipse.sapphire.ui.forms.WithPart;
import org.eclipse.sapphire.ui.forms.WizardPageDef;
import org.eclipse.sapphire.ui.forms.WizardPagePart;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
import org.eclipse.sapphire.ui.internal.PartServiceContext;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class SapphirePart implements ISapphirePart
{
    @Text( "Failed while instantiating {0} class." )
    private static LocalizableText failedToInstantiate;
    
    @Text( "Class {0} does not extend the required Listener class." )
    private static LocalizableText doesNotExtend;
    
    @Text( "Could not resolve form part include \"{0}\"." )
    private static LocalizableText couldNotResolveInclude;
    
    @Text( "Could not resolve section \"{0}\"." )
    private static LocalizableText couldNotResolveSection;
    
    static
    {
        LocalizableText.init( SapphirePart.class );
    }

    private ISapphirePart parent;
    private Element modelElement;
    protected PartDef definition;
    protected Map<String,String> params;
    private Status validation;
    private ListenerContext listeners;
    private Set<SapphirePartListener> listenersDeprecated;
    private SwtResourceCache imageCache;
    private Map<String,SapphireActionGroup> actions;
    private PartServiceContext serviceContext;
    private FunctionResult visibilityFunctionResult;
    private boolean visibilityFunctionInitializing;
    private boolean initialized;
    private boolean disposed;
    
    public final boolean initialized()
    {
        return this.initialized;
    }
    
    public final void init( final ISapphirePart parent,
                            final Element modelElement,
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
    }
    
    public final void initialize()
    {
        this.listeners = new ListenerContext( ( (ElementImpl) this.modelElement ).queue() );
        
        for( ISapphirePartListenerDef listenerDefinition : this.definition.getListeners() )
        {
            final JavaType listenerClass = listenerDefinition.getListenerClass().target();
            
            if( listenerClass != null )
            {
                Object listener = null;
                
                try
                {
                    listener = ( (Class<?>) listenerClass.artifact() ).newInstance();
                }
                catch( Exception e )
                {
                    final String msg = failedToInstantiate.format( listenerClass.name() );
                    Sapphire.service( LoggingService.class ).logError( msg, e );
                }
                
                if( listener != null )
                {
                    if( listener instanceof Listener )
                    {
                        attach( (Listener) listener );
                    }
                    else
                    {
                        final String msg = doesNotExtend.format( listenerClass.name() );
                        Sapphire.service( LoggingService.class ).logError( msg );
                    }
                }
            }
        }
        
        init();
        
        this.initialized = true;
        
        broadcast( new PartInitializationEvent( this ) );
    }
    
    protected void init()
    {
        // The default implement doesn't do anything.
    }
    
    public final FunctionResult initExpression( final Function function,
                                                final Class<?> expectedType,
                                                final Function defaultValue )
    {
        return initExpression( getLocalModelElement(), function, expectedType, defaultValue, null );
    }
    
    public final FunctionResult initExpression( final Function function,
                                                final Class<?> expectedType,
                                                final Function defaultValue,
                                                final Runnable refreshOp )
    {
        return initExpression( getLocalModelElement(), function, expectedType, defaultValue, refreshOp );
    }
    
    public final FunctionResult initExpression( final Element element,
                                                final Function function,
                                                final Class<?> expectedType,
                                                final Function defaultValue )
    {
        return initExpression( function, expectedType, defaultValue, null );
    }
    
    public final FunctionResult initExpression( final Element element,
                                                final Function function,
                                                final Class<?> expectedType,
                                                final Function defaultValue,
                                                final Runnable refreshOp )
    {
        Function f = ( function == null ? Literal.NULL : function );
        f = FailSafeFunction.create( f, Literal.create( expectedType ), defaultValue );
        
        final FunctionResult fr = f.evaluate( new PartFunctionContext( this, element ) );
        
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
                     
                        Display.getDefault().syncExec( notifyOfUpdateOperation );
                    }
                }
            );
        }
        
        return fr;
    }
    
    protected Function initVisibleWhenFunction()
    {
        return this.definition.getVisibleWhen().content();
    }
    
    protected static final Function createVersionCompatibleFunction( final Property property )
    {
        if( property != null )
        {
            final MasterVersionCompatibilityService service = property.service( MasterVersionCompatibilityService.class );
            
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
                            
                            if( property.definition() instanceof ImpliedElementProperty )
                            {
                                property.element().attach( this.propertyListener, property.name() + "/*" );
                            }
                            else
                            {
                                property.element().attach( this.propertyListener, property.name() );
                            }
                        }

                        @Override
                        protected Object evaluate()
                        {
                            return service.compatible() || ! property.empty();
                        }
                        
                        @Override
                        public void dispose()
                        {
                            super.dispose();
                            
                            service.detach( this.serviceListener );

                            if( property.definition() instanceof ImpliedElementProperty )
                            {
                                property.element().detach( this.propertyListener, property.name() + "/*" );
                            }
                            else
                            {
                                property.element().detach( this.propertyListener, property.name() );
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

    public PartDef definition()
    {
        return this.definition;
    }
    
    public ISapphirePart parent()
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
    
    public final Element getModelElement()
    {
        return this.modelElement;
    }
    
    public Element getLocalModelElement()
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
        if( Display.getCurrent() == null )
        {
            Display.getDefault().asyncExec
            (
                new Runnable()
                {
                    public void run()
                    {
                        refreshValidation();
                    }
                }
            );
            
            return;
        }
        
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
            if( this.visibilityFunctionInitializing )
            {
                this.visibilityFunctionResult = Literal.FALSE.evaluate( new FunctionContext() );
            }
            else
            {
                this.visibilityFunctionInitializing = true;
                
                try
                {
                    final FunctionResult fr = initExpression
                    (
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
                    
                    final boolean visibilityAccessedDuringInit = ( this.visibilityFunctionResult != null );
                    
                    this.visibilityFunctionResult = fr;
                    
                    if( visibilityAccessedDuringInit && ( (Boolean) fr.value() ).booleanValue() == true )
                    {
                        broadcast( new PartVisibilityEvent( this ) );
                    }
                }
                finally
                {
                    this.visibilityFunctionInitializing = false;
                }
            }
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

    public SwtResourceCache getSwtResourceCache()
    {
        if( this.imageCache == null )
        {
            this.imageCache = ( this.parent == null ? new SwtResourceCache() : this.parent.getSwtResourceCache() );
        }
        
        return this.imageCache;
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
    
    public final PropertyDef resolve( final String propertyName )
    {
        return resolve( this.modelElement, propertyName );
    }

    public final PropertyDef resolve( final Element modelElement,
                                        String propertyName )
    {
        return resolve( modelElement, propertyName, this.params );
    }
    
    public static final PropertyDef resolve( final Element modelElement,
                                               String propertyName,
                                               final Map<String,String> params )
    {
        if( propertyName != null )
        {
            propertyName = substituteParams( propertyName.trim(), params );
            
            final ElementType type = modelElement.type();
            final PropertyDef property = type.property( propertyName );
            
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
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = service( MasterConversionService.class ).convert( this, adapterType );

        if( result == null )
        {
            final Element element = getLocalModelElement();
            
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
    
    /**
     * Returns the service of the specified type from the part service context.
     * 
     * <p>Service Context: <b>Sapphire.Part</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    public final <S extends Service> S service( final Class<S> type )
    {
        final List<S> services = services( type );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    /**
     * Returns services of the specified type from the part service context.
     * 
     * <p>Service Context: <b>Sapphire.Part</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the list of services or an empty list if none are available
     */
    
    public final <S extends Service> List<S> services( final Class<S> type )
    {
        if( this.serviceContext == null )
        {
            this.serviceContext = new PartServiceContext( this );
        }
        
        return this.serviceContext.services( type );
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
        
        public ImageManager( final Function imageFunction )
        {
            this( imageFunction, Literal.NULL );
        }
        
        public ImageManager( final Function imageFunction,
                             final Function defaultValueFunction )
        {
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
                    this.base = SwtUtil.toImageDescriptor( this.baseImageData );
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
            
            final Element element = this.part.getLocalModelElement();
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

    public static final class DescriptionChangedEvent extends PartEvent
    {
        public DescriptionChangedEvent( final SapphirePart part )
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
                                             final Element element,
                                             final PartDef definition,
                                             final Map<String,String> params )
    {
        final SapphirePart part = createWithoutInit( parent, element, definition, params );
        part.initialize();
        return part;
    }
    
    public static final SapphirePart createWithoutInit( final ISapphirePart parent,
                                                        final Element element,
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
        else if( definition instanceof TextDef )
        {
            part = new TextPart();
        }
        else if( definition instanceof LineSeparatorDef )
        {
            part = new LineSeparatorPart();
        }
        else if( definition instanceof SpacerDef )
        {
            part = new SpacerPart();
        }
        else if( definition instanceof ActuatorDef )
        {
            part = new ActuatorPart(); 
        }
        else if( definition instanceof CustomFormComponentDef )
        {
            final JavaType customPartImplClass = ( (CustomFormComponentDef) definition ).getImplClass().target();
            
            if( customPartImplClass != null )
            {
                try
                {
                    part = (SapphirePart) ( (Class<?>) customPartImplClass.artifact() ).newInstance();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
        else if( definition instanceof StaticTextFieldDef )
        {
            part = new StaticTextFieldPart();
        }
        else if( definition instanceof GroupDef )
        {
            part = new GroupPart();
        }
        else if( definition instanceof WithDef )
        {
            final String path = ( (SapphirePart) parent ).substituteParams( ( (WithDef) definition ).getPath().text() );
            
            if( path.endsWith( ".." ) )
            {
                part = new WithImpliedPart();
            }
            else
            {
                final Property property = element.property( path );
                
                if( property.definition() instanceof ImpliedElementProperty )
                {
                    part = new WithImpliedPart();
                }
                else
                {
                    part = new WithPart();
                }
            }
        }
        else if( definition instanceof DetailSectionDef )
        {
            part = new DetailSectionPart();
        }
        else if( definition instanceof DialogDef )
        {
            part = new DialogPart();
        }
        else if( definition instanceof WizardPageDef )
        {
            part = new WizardPagePart();
        }
        else if( definition instanceof SectionDef )
        {
            part = new SectionPart();
        }
        else if( definition instanceof SectionRef )
        {
            final SectionRef ref = (SectionRef) definition;
            def = ref.getSection().target();
            
            if( def == null )
            {
                final String msg = couldNotResolveSection.format( ref.getSection().text() );
                throw new IllegalArgumentException( msg );
            }
            else
            {
                partParams = new HashMap<String,String>( params );
                
                for( ISapphireParam param : ref.getParams() )
                {
                    final String paramName = param.getName().text();
                    final String paramValue = param.getValue().text();
                    
                    if( paramName != null && paramValue != null )
                    {
                        partParams.put( paramName, paramValue );
                    }
                }
                
                return createWithoutInit( parent, element, def, partParams );
            }
        }
        else if( definition instanceof FormComponentRef )
        {
            final FormComponentRef inc = (FormComponentRef) definition;
            def = inc.getPart().target();
            
            if( def == null )
            {
                final String msg = couldNotResolveInclude.format( inc.getPart().text() );
                throw new IllegalArgumentException( msg );
            }
            else
            {
                partParams = new HashMap<String,String>( params );
                
                for( ISapphireParam param : inc.getParams() )
                {
                    final String paramName = param.getName().text();
                    final String paramValue = param.getValue().text();
                    
                    if( paramName != null && paramValue != null )
                    {
                        partParams.put( paramName, paramValue );
                    }
                }
                
                return createWithoutInit( parent, element, def, partParams );
            }
        }
        else if( definition instanceof TabGroupDef )
        {
            part = new TabGroupPart();
        }
        else if( definition instanceof HtmlPanelDef )
        {
            part = new HtmlPanelPart();
        }
        else if( definition instanceof SplitFormDef )
        {
            part = new SplitFormPart();
        }
        else if( definition instanceof SplitFormSectionDef )
        {
            part = new SplitFormSectionPart();
        }
        else if( definition instanceof CompositeDef )
        {
            part = new CompositePart();
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
    
}
