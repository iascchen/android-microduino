/**

 * File:CC3XApiManager.java

 * Copyright © 2013, Texas Instruments Incorporated - http://www.ti.com/

 * All rights reserved.
	@author raviteja
	<Description> A callback listener class implementing FirstTimeConfigListener from Jar file.
  	presently un-used.Can be used in future implementations if any in order to seperate tasks from being written in single class
	</Description>
 */
package me.iasc.microduino.msmartconfig.app;

import com.integrity_project.smartconfiglib.FirstTimeConfigListener;

/**
 * A callback listener class implementing FirstTimeConfigListener from Jar file.
 * presently un-used.Can be used in future implementations if any in order to seperate tasks from being written in single class
 * 
 * @author raviteja
 *
 */
public class CC3XApiManager implements FirstTimeConfigListener
{
	@Override
	public void onFirstTimeConfigEvent(FtcEvent arg0, Exception arg1)
	{
		arg1.printStackTrace();
		switch (arg0)
		{
		case FTC_ERROR:
			break;
		case FTC_SUCCESS:

			break;
		case FTC_TIMEOUT:
			break;

		default:
			break;
		}
	}

}
