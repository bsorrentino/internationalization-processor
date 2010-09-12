/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import org.junit.Test;

/**
 *
 * @author softphone
 */
public class DynamicProxyTest {


    static class TestInvocationHandler implements InvocationHandler {
        final java.util.ResourceBundle bundle ;

        public TestInvocationHandler( java.util.ResourceBundle bundle ) {
            this.bundle = bundle;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            final String key = method.getName();

            if(bundle==null) return key;
            
            System.out.printf( "method [%s]\n", key);

            if( "getString".equals(key) && args.length==1 ) {
                return bundle.getString(  args[0].toString() );
            }

            if( !bundle.containsKey(key) ) return key;
            
            return bundle.getString(key);
        }

    }
    
    private java.util.ResourceBundle getBundle( Locale locale ) {

        if( locale==null ) locale = Locale.getDefault();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle( "test", locale,  DynamicProxyTest.class.getClassLoader() );

        return bundle;
    }

    @Test
    public void createProxy() throws Exception {



        InvocationHandler handler = new TestInvocationHandler( getBundle(null) );

        I18NInterface f = (I18NInterface) Proxy.newProxyInstance(DynamicProxyTest.class.getClassLoader(),
                new Class[]{I18NInterface.class},
                handler);


        System.out.printf( "version=[%s]\n", f.version());

        System.out.printf( "key1=[%s]\n", f.getString("key1"));
        
    }
}
