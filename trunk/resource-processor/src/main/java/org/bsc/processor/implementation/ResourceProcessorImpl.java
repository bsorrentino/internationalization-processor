/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor.implementation;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import biz.source_code.miniTemplator.MiniTemplator;

/**
 *
 * @author softphone
 */
@SupportedSourceVersion(SourceVersion.RELEASE_5)
@SupportedAnnotationTypes("org.bsc.processor.annotation.ResourceBundle")
public class ResourceProcessorImpl extends AbstractProcessor {
	
    final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    //final Logger logger = Logger.getLogger("processor");

    private void info( String msg ) {
        //logger.info(msg);
        processingEnv.getMessager().printMessage(Kind.NOTE, msg );
    }

    private void warn( String msg ) {
        //logger.warning(msg);
        processingEnv.getMessager().printMessage(Kind.WARNING, msg );
    }
    private void warn( String msg, Throwable t ) {
        //logger.log(Level.WARNING, msg, t );
        processingEnv.getMessager().printMessage(Kind.WARNING, msg );
    }

    private void error( String msg ) {
        //logger.severe(msg);
        processingEnv.getMessager().printMessage(Kind.ERROR, msg );
    }
    private void error( String msg, Throwable t ) {
        //logger.log(Level.SEVERE, msg, t );
        processingEnv.getMessager().printMessage(Kind.ERROR, msg );
    }
    
    private String getPackage( TypeElement e ) {

        String fqn = e.getQualifiedName().toString();

        int index = fqn.lastIndexOf('.');

        if( index<0 ) return null;

        return fqn.substring(0, index);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())      return false;
        
        Filer filer = processingEnv.getFiler();

        final String resource = "messages.properties";
        final String packageName = "";
        try {

            FileObject f = filer.getResource(StandardLocation.CLASS_PATH, packageName, resource);

            //java.io.Reader r = f.openReader(true /* ignoreEncodingErrors */);
            java.io.InputStream is = f.openInputStream();

            if( is==null ) {
                warn( String.format("resource [%s] not found!", resource) );
                return false;
            }
            
            PropertyResourceBundle bundle = new PropertyResourceBundle(is);


            TypeElement annotatedType = null;


            // discover services from the current compilation sources
            for (TypeElement type : (Collection<TypeElement>)roundEnv.getElementsAnnotatedWith(org.bsc.processor.annotation.ResourceBundle.class)) {
                
                info( String.format("type Element [%s]", type.getQualifiedName()));

                annotatedType = type;
                break;

            }

            if( annotatedType==null ) return true;
            
            String javaSource = String.format("%sMessages",annotatedType.getQualifiedName().toString());

            info( javaSource );
            
            JavaFileObject source = filer.createSourceFile( javaSource );

            String date = dateFmt.format(new java.util.Date());

            java.io.InputStream template = getClass().getClassLoader().getResourceAsStream("JavaSourceTemplate.txt");

            MiniTemplator t = new MiniTemplator(new java.io.InputStreamReader(template) );

            t.setVariable("package", getPackage(annotatedType));
            t.setVariable("value", getClass().getName());
            t.setVariable("date", date );
            t.setVariable("className", annotatedType.getSimpleName().toString().concat("Messages") );

          
            Enumeration<String> keys = bundle.getKeys();

            while( keys.hasMoreElements() ) {

                String k = keys.nextElement();

                info(  String.format( "[%s]=[%s]", k, bundle.getString(k) ));

                t.setVariable( "propertyName", k );
                t.setVariable( "propertyValue", bundle.getString(k), true /*optional*/ );
                t.addBlock("property");
                
            }

            Writer w = source.openWriter();
            t.generateOutput(w);
            w.close();



        } catch (Exception ex) {
            error( "error on processing", ex);
            return false;
        }

        return true;
    }

}
