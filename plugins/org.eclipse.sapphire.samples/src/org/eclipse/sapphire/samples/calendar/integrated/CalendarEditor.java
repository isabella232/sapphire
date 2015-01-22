/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [444202] Lazy loading of editor pages
 ******************************************************************************/

package org.eclipse.sapphire.samples.calendar.integrated;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.samples.calendar.ICalendar;
import org.eclipse.sapphire.samples.calendar.integrated.internal.CalendarResource;
import org.eclipse.sapphire.samples.contacts.ContactRepository;
import org.eclipse.sapphire.ui.CorruptedResourceExceptionInterceptorImpl;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.DefinitionLoader.Reference;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.forms.swt.MasterDetailsEditorPage;
import org.eclipse.sapphire.ui.swt.xml.editor.XmlEditorResourceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class CalendarEditor extends SapphireEditor
{
    private static final String PAGE_CONTACTS = "Contacts";
    private static final String PAGE_CALENDAR = "Calendar";

    @Text( PAGE_CONTACTS )
    private static LocalizableText contactsPageName;
    
    static
    {
        LocalizableText.init( CalendarEditor.class );
    }

    private StructuredTextEditor calendarSourceEditor;
    private StructuredTextEditor contactsSourceEditor;
    
    private ICalendar modelCalendar;
    private org.eclipse.sapphire.samples.calendar.integrated.ICalendar modelCalendarIntegrated;
    private ContactRepository modelContacts;
    
    private MasterDetailsEditorPage calendarDesignPage;
    private MasterDetailsEditorPage contactsDesignPage;

    @Override
    protected void createEditorPages() throws PartInitException
    {
        addDeferredPage( PAGE_CALENDAR, PAGE_CALENDAR );
        addDeferredPage( PAGE_CONTACTS, PAGE_CONTACTS );
        
        this.calendarSourceEditor = new StructuredTextEditor();
        this.calendarSourceEditor.setEditorPart(this);
        
        final FileEditorInput rootEditorInput = (FileEditorInput) getEditorInput();
        
        int index = addPage( this.calendarSourceEditor, rootEditorInput );
        setPageText( index, "calendar.xml" );

        this.contactsSourceEditor = new StructuredTextEditor();
        this.contactsSourceEditor.setEditorPart(this);
        
        final IFile contactsFile = rootEditorInput.getFile().getParent().getFile( new Path( "contacts.xml" ) );
        
        index = addPage( this.contactsSourceEditor, new FileEditorInput( contactsFile ) );
        setPageText( index, "contacts.xml" );
    }

    @Override
    protected Element createModel()
    {
        this.modelCalendar = ICalendar.TYPE.instantiate( new RootXmlResource( new XmlEditorResourceStore( this, this.calendarSourceEditor ) ) );
        this.modelContacts = ContactRepository.TYPE.instantiate( new RootXmlResource( new XmlEditorResourceStore( this, this.contactsSourceEditor ) ) );
        this.modelCalendarIntegrated = org.eclipse.sapphire.samples.calendar.integrated.ICalendar.TYPE.instantiate( new CalendarResource( this.modelCalendar, this.modelContacts ) );
        
        return this.modelCalendarIntegrated;
    }
    
    @Override
    protected void adaptModel( final Element model )
    {
        final CorruptedResourceExceptionInterceptor interceptor 
            = new CorruptedResourceExceptionInterceptorImpl( getEditorSite().getShell() );
        
        this.modelCalendar.resource().setCorruptedResourceExceptionInterceptor( interceptor );
        this.modelContacts.resource().setCorruptedResourceExceptionInterceptor( interceptor );
    }
    
    @Override
    protected Reference<EditorPageDef> getDefinition( final String pageDefinitionId )
    {
        if( PAGE_CALENDAR.equals( pageDefinitionId ) )
        {
            return DefinitionLoader.sdef( getClass() ).page();
        }
        else if ( PAGE_CONTACTS.equals( pageDefinitionId ) )
        {
            return DefinitionLoader.context( ContactRepository.class ).sdef( "ContactRepositoryEditor" ).page();
        }

        return null;
    }

    @Override
    protected IEditorPart createPage( final String pageDefinitionId )
    {
        if( PAGE_CALENDAR.equals( pageDefinitionId ) )
        {
            this.calendarDesignPage = new MasterDetailsEditorPage( this, getModelElement(), getDefinition( pageDefinitionId ) );
            return this.calendarDesignPage;
        }
        else if ( PAGE_CONTACTS.equals( pageDefinitionId ) )
        {
            getModelElement(); // make sure createModel() has been called
            this.contactsDesignPage = new MasterDetailsEditorPage( this, this.modelContacts, getDefinition( pageDefinitionId ) );
            return this.contactsDesignPage;
        }

        return null;
    }

    @Override
    public IContentOutlinePage getContentOutline( final Object page )
    {
        if( page == this.calendarSourceEditor )
        {
            return (IContentOutlinePage) this.calendarSourceEditor.getAdapter( IContentOutlinePage.class );
        }
        else if( page == this.contactsSourceEditor )
        {
            return (IContentOutlinePage) this.contactsSourceEditor.getAdapter( IContentOutlinePage.class );
        }
        
        return super.getContentOutline( page );
    }
    
    public ICalendar getCalendar()
    {
        return this.modelCalendar;
    }
    
    public org.eclipse.sapphire.samples.calendar.integrated.ICalendar getCalendarIntegrated()
    {
        return this.modelCalendarIntegrated;
    }
    
    public ContactRepository getContactRepository()
    {
        return this.modelContacts;
    }
    
}
