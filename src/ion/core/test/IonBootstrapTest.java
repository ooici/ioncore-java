package ion.core.test;

import ion.core.IonBootstrap;

import junit.framework.TestCase;

/**
 * JUnit test cases for IonBootstrap.
 * 
 * @author carueda
 */
public class IonBootstrapTest extends TestCase {

	/**
	 * This just initializes the {@link IonBootstrap} class thus making the
	 * class go through the various java assert statements; so, assertions
	 * should be enabled to properly run this test. See
	 * .setting/ooici-build.xml's test-all target.
	 */
	public void testInitialization() {
		IonBootstrap.bootstrap();
	}

}

