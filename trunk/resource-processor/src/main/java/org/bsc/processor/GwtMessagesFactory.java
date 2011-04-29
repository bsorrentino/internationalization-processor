/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

import com.google.gwt.i18n.client.LocalizableResource.Key;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

/**
 *
 * @author softphone
 */
public class GwtMessagesFactory {
	static class MyInvocationHandler implements InvocationHandler {
		final java.util.ResourceBundle bundle ;

		public MyInvocationHandler( java.util.ResourceBundle bundle ) {
			this.bundle = bundle;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			Key annoKey = method.getAnnotation(Key.class);
				
			final String key = ( annoKey!=null ) ? annoKey.value() : method.getName();

			if(bundle==null) {
				DefaultMessage msg = method.getAnnotation(DefaultMessage.class);
				return (msg!=null) ? msg : key;
			}
				
			if( "getString".equals(key) && args.length==1 ) return bundle.getString(  args[0].toString() );

			if( !bundle.containsKey(key) ) return key;

			String value = bundle.getString(key); 
			
			
            return  (args!=null ) ? java.text.MessageFormat.format(value, (Object[])args[0] ) : value;
		}

	}

	@SuppressWarnings("unchecked")
	public synchronized static <T> T createInstance( Class<T> messageInterface, Locale locale ) {
		if( messageInterface==null ) throw new IllegalArgumentException("message interface is null!");

		ClassLoader cl = messageInterface.getClassLoader();

		if( locale==null ) locale = Locale.getDefault();

		java.util.ResourceBundle bundle = null;

		try {
			bundle = java.util.ResourceBundle.getBundle( messageInterface.getName().replace('.', '/'), locale, cl  );
		}
		catch( java.util.MissingResourceException e ) {
			// TODO log
		}
		final InvocationHandler handler = new MyInvocationHandler( bundle );

		T instance = (T) Proxy.newProxyInstance(cl,new Class[]{messageInterface}, handler);

		return instance;
	}

}
