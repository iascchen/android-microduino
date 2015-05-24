/**

* File:CC3XDialogManager.java

* Copyright © 2013, Texas Instruments Incorporated - http://www.ti.com/

* All rights reserved.

*/
package me.iasc.microduino.msmartconfig.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import me.iasc.microduino.msmartconfig.app.utils.CC3XConstants;

/**
 * universal dialog class for app which pops up a dialog upon success or failure or network failure cases
 * @author raviteja
 *
 */
public class CC3XDialogManager implements OnClickListener  
{
	/**
	 * Called activity context
	 */
	private Context mContext=null;
	/**
	 * Alert dialog instance
	 */
	private AlertDialog.Builder mCC3XAlertDialog=null;

	/**
	 * Constructor for custom alert dialog.accepting context of called activity
	 * @param mContext
	 */
	public CC3XDialogManager(Context mContext) 
	{
		this.mContext=mContext;
	}

	public void showCustomAlertDialog(int dialogType)
	{

		mCC3XAlertDialog=new AlertDialog.Builder(mContext);
		switch (dialogType) 
		{

		case CC3XConstants.DLG_NO_WIFI_AVAILABLE:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_cc3x_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_no_network_title));

			break;
		
		case CC3XConstants.DLG_CONNECTION_SUCCESS:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_cc3x_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_successfully_connected));

			break;

		case CC3XConstants.DLG_CONNECTION_FAILURE:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_cc3x_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_connection_failed));

			break;

		case CC3XConstants.DLG_CONNECTION_TIMEOUT:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_cc3x_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_connection_timeout));

			break;

		case CC3XConstants.DLG_SSID_INVALID:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_invalid_input_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_no_network_title));
			break;

		case CC3XConstants.DLG_GATEWAY_IP_INVALID:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_invalid_input_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_no_network_title));
			break;

		case CC3XConstants.DLG_KEY_INVALID:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_invalid_input_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_invalid_key_mesg));
			break;

		case CC3XConstants.DLG_PASSWORD_INVALID:
			mCC3XAlertDialog.setTitle(mContext.getResources().getString(R.string.alert_invalid_input_title));
			mCC3XAlertDialog.setMessage(mContext.getResources().getString(R.string.alert_no_network_title));
			break;

		}
		mCC3XAlertDialog.setPositiveButton((mContext.getResources().getString(R.string.cc3x_string_ok)).toUpperCase(), this);
		mCC3XAlertDialog.show();

	}


	@Override
	public void onClick(DialogInterface dialog, int dialogType) 
	{
		
		if(dialogType==DialogInterface.BUTTON_POSITIVE)
		{
			dialog.dismiss();
		}
	}
}
