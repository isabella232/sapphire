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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.SectionPart;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ProblemsTraversalServiceData
{
    static ProblemsTraversalServiceData EMPTY = new ProblemsTraversalServiceData();
    
    private Entry first;
    private Entry last;
    private final Map<MasterDetailsContentNode,Entry> index;
    
    private ProblemsTraversalServiceData()
    {
        this.index = Collections.emptyMap();
    }
    
    ProblemsTraversalServiceData( final MasterDetailsEditorPagePart page,
                                  final Listener listener )
    {
        this.index = new IdentityHashMap<MasterDetailsContentNode,Entry>();
        traverse( page.outline().getRoot(), listener );
    }
    
    private void traverse( final MasterDetailsContentNode node,
                           final Listener listener )
    {
        final Entry entry = new Entry();
        
        if( this.first == null )
        {
            this.first = entry;
            this.last = entry;
        }
        else
        {
            this.last.next = entry;
            this.last = entry;
        }
        
        this.index.put( node, entry );
        
        entry.node = node;
        
        for( SectionPart section : node.getSections() )
        {
            if( node.visible() && section.visible() )
            {
                final Status.Severity severity = section.validation().severity();
                
                if( severity == Status.Severity.ERROR )
                {
                    entry.error = true;
                }
                else if( severity == Status.Severity.WARNING )
                {
                    entry.warning = true;
                }
            }
            
            section.attach( listener );
        }
        
        for( MasterDetailsContentNode child : node.nodes() )
        {
            traverse( child, listener );
        }
        
        node.attach( listener );
    }
    
    public MasterDetailsContentNode findNextProblem( final MasterDetailsContentNode reference,
                                                     final Status.Severity severity )
    {
        if( reference == null || ! ( severity == Status.Severity.ERROR || severity == Status.Severity.WARNING ) )
        {
            throw new IllegalArgumentException();
        }
        
        return ( severity == Status.Severity.ERROR ? findNextError( reference ) : findNextWarning( reference ) );
    }
    
    public MasterDetailsContentNode findNextError( final MasterDetailsContentNode reference )
    {
        if( reference == null )
        {
            throw new IllegalArgumentException();
        }

        Entry entry = this.index.get( reference );
        
        if( entry != null )
        {
            entry = entry.next;
            
            while( entry != null && ! entry.error )
            {
                entry = entry.next;
            }
        }
        
        return ( entry == null ? null : entry.node );
    }
    
    public MasterDetailsContentNode findNextWarning( final MasterDetailsContentNode reference )
    {
        if( reference == null )
        {
            throw new IllegalArgumentException();
        }

        Entry entry = this.index.get( reference );
        
        if( entry != null )
        {
            entry = entry.next;
            
            while( entry != null && ! entry.warning )
            {
                entry = entry.next;
            }
        }
        
        return ( entry == null ? null : entry.node );
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof ProblemsTraversalServiceData )
        {
            Entry x = this.first;
            Entry y = ( (ProblemsTraversalServiceData) obj ).first;
            
            while( x != null && y != null )
            {
                if( x.node != y.node || x.error != y.error || x.warning != y.warning )
                {
                    return false;
                }
                
                x = x.next;
                y = y.next;
            }
            
            if( x == null && y == null )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        
        Entry entry = this.first;
        
        while( entry != null )
        {
            result = 31 * result + entry.node.hashCode() + Boolean.valueOf( entry.error ).hashCode() + Boolean.valueOf( entry.warning ).hashCode();
            entry = entry.next;
        }
        
        return result;
    }

    private static final class Entry
    {
        public MasterDetailsContentNode node;
        public boolean error;
        public boolean warning;
        public Entry next;
    }
    
}
