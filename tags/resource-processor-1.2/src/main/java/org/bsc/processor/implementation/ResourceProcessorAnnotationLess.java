/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor.implementation;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;

/**
 *
 * @author softphone
 */
@SupportedSourceVersion(SourceVersion.RELEASE_5)
@SupportedAnnotationTypes("*")
public class ResourceProcessorAnnotationLess extends AbstractResourceProcessor {

    boolean processed = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;       
        if( processed ) return false;

        processed = true;

        
        Map<String, String> options = processingEnv.getOptions();

        String fqn = options.get("resource");

        if( fqn==null || fqn.trim().length()==0 ) {
            error( "resource option is not set!" );
            return false;
        }

        fqn = fqn.trim();

        if(  fqn.length()==0 ) {
            error( "resource option is empty!" );
            return false;
        }

        info( String.format("processOptions fqn [%s] ", fqn ));

        String packageName = getPackage( fqn );
        String simpleName = getSimpleName(fqn);

        FileObject bundle = null;
        try {

            bundle = super.getResource(packageName, simpleName.concat(".properties"));
        } catch (IOException ex) {
            error( "error loading resource", ex);
            return false;
        }

        JavaFileObject source = null;
        try {
            source = super.createSourceFile(fqn);
        } catch (IOException ex) {
            error( "error opening java source", ex);
            return false;
        }

        try {
            generateSource( packageName, simpleName, source, bundle, "JavaSourceTemplate.txt");
        } catch (Exception ex) {
            error( "error generating source", ex);
            return false;
        }
        
        return true;
    }


}
