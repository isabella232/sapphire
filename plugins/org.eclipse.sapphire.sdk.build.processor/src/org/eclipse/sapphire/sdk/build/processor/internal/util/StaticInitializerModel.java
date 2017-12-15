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

package org.eclipse.sapphire.sdk.build.processor.internal.util;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StaticInitializerModel

    extends BaseModel
    
{
    private String id;
    private String body;
    
    public String getId()
    {
        return this.id;
    }
    
    public void setId( final String id )
    {
        this.id = id;
    }
    
    @Override
    public ClassModel getParent()
    {
        return (ClassModel) super.getParent();
    }
    
    public String getBody()
    {
        return this.body;
    }
    
    public void setBody( final String body )
    {
        this.body = body;
    }
    
    public void appendToBody( final String content )
    {
        if( this.body == null )
        {
            this.body = content;
        }
        else
        {
            this.body = this.body + "\n" + content;
        }
    }
    
    @Override
    public void write( final IndentingPrintWriter pw )
    {
        pw.print( "static" );
        pw.println();
        pw.print( '{' );
        pw.println();
        pw.increaseIndent();
        
        for( String line : this.body.replace( "\r", "" ).split( "\n" ) )
        {
            pw.print( line );
            pw.println();
        }
        
        pw.decreaseIndent();
        pw.print( '}' );
        
        pw.println();
        pw.println();
    }

}
