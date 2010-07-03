/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bsc.processor.test;



/**
 *
 * @author softphone
 */
@org.bsc.processor.annotation.ResourceBundle
public class ProcessorTest {

    public ProcessorTest() {

    }

    public static void main( String args[] ) {

        ProcessorTestMessages msg = ProcessorTestMessages.createInstance(null);


        System.out.println( msg.fileNotFound("MyFile.txt"));
        System.out.println( msg.unknow());

    }
}
