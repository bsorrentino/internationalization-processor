/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor.implementation;

import biz.source_code.miniTemplator.MiniTemplator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author softphone
 */
public abstract class AbstractResourceProcessor extends AbstractProcessor {
    final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    protected void info( String msg ) {
        //logger.info(msg);
        processingEnv.getMessager().printMessage(Kind.NOTE, msg );
    }

    protected void warn( String msg ) {
        //logger.warning(msg);
        processingEnv.getMessager().printMessage(Kind.WARNING, msg );
    }
    protected void warn( String msg, Throwable t ) {
        //logger.log(Level.WARNING, msg, t );
        processingEnv.getMessager().printMessage(Kind.WARNING, msg );
    }

    protected void error( String msg ) {
        //logger.severe(msg);
        processingEnv.getMessager().printMessage(Kind.ERROR, msg );
    }
    protected void error( String msg, Throwable t ) {
        //logger.log(Level.SEVERE, msg, t );
        t.printStackTrace();
        processingEnv.getMessager().printMessage(Kind.ERROR, msg );
    }

    protected String getPackage( String fqn  ) {

        int index = fqn.lastIndexOf('.');

        if( index<0 ) return null;

        return fqn.substring(0, index);
    }

    protected String getSimpleName( String fqn  ) {

        int index = fqn.lastIndexOf('.');

        if( index<0 ) return fqn;

        return fqn.substring(index+1);
    }
    
    private boolean isValidMethodKey( String key ) {
    	return java.util.regex.Pattern.matches("\\w+", key);
    }

    protected FileObject getResource( String packageName, String resourceFileName ) throws IOException  {

        Filer filer = processingEnv.getFiler();

        return filer.getResource(StandardLocation.CLASS_PATH, packageName, resourceFileName);

    }

    protected PropertyResourceBundle loadResource( FileObject f ) throws IOException {
        //java.io.Reader r = f.openReader(true /* ignoreEncodingErrors */);
        java.io.InputStream is = f.openInputStream();

        if (is == null) {
            final String msg = String.format("resource [%s] not found!", f.getName());
            warn(msg);
            throw new FileNotFoundException(msg);
        }

        PropertyResourceBundle bundle = new PropertyResourceBundle(is);

        return bundle;

    }

    protected  JavaFileObject createSourceFile( String fqn ) throws IOException {
           Filer filer = processingEnv.getFiler();

           return filer.createSourceFile( fqn );
 
    }

    /**
     *
     * @param fqn
     * @param className
     * @param bundle
     * @throws Exception
     */
    protected void generateSource(  String packageName, String className, JavaFileObject source, FileObject resource  ) throws Exception  {

        if( source.getLastModified() >= resource.getLastModified() ) {
            info( "generation is skipped! source is updated");
            return;
        }

        PropertyResourceBundle  bundle = loadResource(resource);

        String date = dateFmt.format(new java.util.Date());

        java.io.InputStream template = getClass().getClassLoader().getResourceAsStream("JavaSourceTemplate.txt");

        MiniTemplator t = new MiniTemplator(new java.io.InputStreamReader(template) );

        t.setVariable("package", packageName);
        t.setVariable("value", getClass().getName());
        t.setVariable("date", date );
        t.setVariable("className", className );
        t.setVariable("messages", String.format( "%s/%s", packageName.replace('.', '/'), className));


        Enumeration<String> keys = bundle.getKeys();

        while( keys.hasMoreElements() ) {

            String k = keys.nextElement();

            if( !isValidMethodKey(k)) {
                warn(  String.format( "key [%s] is not a valid method name!", k ));
                continue;
            }

            info(  String.format( "[%s]=[%s]", k, bundle.getString(k) ));

            t.setVariable( "propertyName", k );
            t.setVariable( "propertyValue", bundle.getString(k), true /*optional*/ );
            t.addBlock("property");

        }

        Writer w = source.openWriter();
        t.generateOutput(w);
        w.close();


    }

    /**
     * 
     * @param fqn
     * @param className
     * @param bundle
     * @throws Exception
     */
    protected void generateSource( TypeElement annotatedType, PropertyResourceBundle bundle  ) throws Exception  {

            String fqn = annotatedType.getQualifiedName().toString().concat("Messages");
            String className = annotatedType.getSimpleName().toString().concat("Messages");
            
            String packageName = getPackage(fqn);

            Filer filer = processingEnv.getFiler();

            JavaFileObject source = filer.createSourceFile( fqn );

            long lastModified = source.getLastModified();

            info( "source last modified " + new java.util.Date(lastModified) );

            String date = dateFmt.format(new java.util.Date());

            java.io.InputStream template = getClass().getClassLoader().getResourceAsStream("JavaSourceTemplate.txt");

            MiniTemplator t = new MiniTemplator(new java.io.InputStreamReader(template) );

            t.setVariable("package", packageName);
            t.setVariable("value", getClass().getName());
            t.setVariable("date", date );
            t.setVariable("className", className );
            t.setVariable("messages", "messages");


            Enumeration<String> keys = bundle.getKeys();

            while( keys.hasMoreElements() ) {

                String k = keys.nextElement();

                if( !isValidMethodKey(k)) {
                    warn(  String.format( "key [%s] is not a valid method name!", k ));
                    continue;
                }

                info(  String.format( "[%s]=[%s]", k, bundle.getString(k) ));

                t.setVariable( "propertyName", k );
                t.setVariable( "propertyValue", bundle.getString(k), true /*optional*/ );
                t.addBlock("property");

            }

            Writer w = source.openWriter();
            t.generateOutput(w);
            w.close();


    }

    protected PropertyResourceBundle loadResource( String packageName, String resourceFileName ) throws IOException {

        Filer filer = processingEnv.getFiler();

        FileObject f = filer.getResource(StandardLocation.CLASS_PATH, packageName, resourceFileName);

        //java.io.Reader r = f.openReader(true /* ignoreEncodingErrors */);
        java.io.InputStream is = f.openInputStream();

        if (is == null) {
            final String msg = String.format("resource [%s] not found!", resourceFileName);
            warn(msg);
            throw new FileNotFoundException(msg);
        }

        PropertyResourceBundle bundle = new PropertyResourceBundle(is);

        return bundle;

    }


}
