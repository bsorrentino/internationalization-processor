/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor.implementation;

import biz.source_code.miniTemplator.MiniTemplator;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;
import java.util.PropertyResourceBundle;
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
public class ResourceProcessorInterface extends AbstractResourceProcessor {
    private static final String JAVA_SOURCE_GENERATE_INTERFACE_TEMPLATE = "JavaSourceTemplate3.txt"; 
    private static final String JAVA_SOURCE_GENERATE_FACTROY_TEMPLATE = "JavaSourceTemplate2.txt";
    private static final String RESOURCE_OPTION = "interface";

    boolean processed = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;       
        if( processed ) return false;

        processed = true;

        
        Map<String, String> options = processingEnv.getOptions();

        String fqn = options.get(RESOURCE_OPTION);

        if( fqn==null || fqn.trim().length()==0 ) {
            error( RESOURCE_OPTION.concat(" option is not set!" ));
            return false;
        }

        fqn = fqn.trim();

        if(  fqn.length()==0 ) {
            error( RESOURCE_OPTION.concat(" option is empty!" ));
            return false;
        }

        info( String.format("processOptions fqn [%s] ", fqn ));

        return generateInterface(fqn);
/*
        String packageName = getPackage( fqn );
        String simpleName = getSimpleName(fqn);

        JavaFileObject source = null;
        try {
            source = super.createSourceFile(fqn.concat("Factory"));
        } catch (IOException ex) {
            error( "error opening java source", ex);
            return false;
        }

        try {
            generateSource( packageName, simpleName, source, JAVA_SOURCE_GENERATE_FACTROY_TEMPLATE);
        } catch (Exception ex) {
            error( "error generating source", ex);
            return false;
        }

        return true;
*/
    }



    private boolean generateInterface( String fqn) {

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
            generateSource( packageName, simpleName, source, bundle, JAVA_SOURCE_GENERATE_INTERFACE_TEMPLATE);
        } catch (Exception ex) {
            error( "error generating source", ex);
            return false;
        }

        return true;
    }

    /**
     *
     * @param fqn
     * @param className
     * @param bundle
     * @throws Exception
     */
    protected void generateSource(  String packageName, String className, JavaFileObject source, String templateResourceName  ) throws Exception  {


        String date = dateFmt.format(new java.util.Date());

        java.io.InputStream template = getClass().getClassLoader().getResourceAsStream(templateResourceName);

        MiniTemplator t = new MiniTemplator(new java.io.InputStreamReader(template) );

        t.setVariable("package", packageName);
        t.setVariable("value", getClass().getName());
        t.setVariable("date", date );
        t.setVariable("className", className );
        t.setVariable("messages", String.format( "%s/%s", packageName.replace('.', '/'), className));

        Writer w = source.openWriter();
        t.generateOutput(w);
        w.close();


    }

}
