/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor.implementation;

import java.util.Collection;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


/**
 *
 * @author softphone
 */
@SupportedSourceVersion(SourceVersion.RELEASE_5)
@SupportedAnnotationTypes("org.bsc.processor.annotation.ResourceBundle")
public class ResourceProcessorImpl extends AbstractResourceProcessor {
	
    //final Logger logger = Logger.getLogger("processor");


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())      return false;
        
        final String resource = "messages.properties";
        final String packageName = "";
        try {

            PropertyResourceBundle bundle = super.loadResource(packageName, resource);


            TypeElement annotatedType = null;


            // discover services from the current compilation sources
            for (TypeElement type : (Collection<TypeElement>)roundEnv.getElementsAnnotatedWith(org.bsc.processor.annotation.ResourceBundle.class)) {
                
                info( String.format("type Element [%s]", type.getQualifiedName()));

                annotatedType = type;
                break;

            }

            if( annotatedType!=null ) {
                generateSource( annotatedType, bundle, "JavaSourceTemplate.txt" );
            }
            else {
                warn( "no annotation found!");
            }
            

        } catch (Exception ex) {
            error( "error on processing", ex);
            return false;
        }

        return true;
    }


    
}
