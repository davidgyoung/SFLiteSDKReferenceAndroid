package com.signalframe.sflitesdksample

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.signalframe.sflitesdk.SFBeaconManager
import org.altbeacon.beacon.Beacon

public class ConfigurationDialog {
    val activity: Activity
    val context: Context
    var visibleBeaconToChooseFrom =  ArrayList<Beacon>()
    constructor(activity: Activity) {
        this.activity = activity
        this.context = activity.applicationContext
    }
    fun popConfigurationDialog(beaconOnly: Boolean = false) {
        if (!beaconOnly) {
            var beaconHexIdentifier = "None chosen"
            if (SFBeaconManager.getInstance(context).associatedBeacon != null) {
                beaconHexIdentifier = String.format("%02X%02X", SFBeaconManager.getInstance(context).associatedBeacon?.id2?.toInt(), SFBeaconManager.getInstance(activity).associatedBeacon?.id3?.toInt())
            }

            val builder =
                    AlertDialog.Builder(activity)
            builder.setTitle("Enter Configuration")
            builder.setMessage("Enter your work email address and your company or employer name.  Doing so will share beacon detection data for use by the company.\n\nBeacon: "+beaconHexIdentifier)
            val viewInflated: View =
                    LayoutInflater.from(activity).inflate(R.layout.configuration_dialog, activity.findViewById(android.R.id.content), false)
            val prompt = viewInflated.findViewById<View>(R.id.prompt) as TextView
            prompt.setText("")
            val emailInput =
                    viewInflated.findViewById<View>(R.id.emailEditText) as EditText
            val clientInput =
                    viewInflated.findViewById<View>(R.id.clientEditText) as EditText
            emailInput.setText(SFBeaconManager.getInstance(activity).email)
            clientInput.setText(SFBeaconManager.getInstance(activity).client)
            builder.setView(viewInflated)

            // Set up the buttons
            builder.setPositiveButton(
                    "Save"
            ) { dialog, which ->
                var errorString: String? = null
                if (emailInput.text.toString().length < 5 || emailInput.text.toString().indexOf("@") <= 0) {
                    errorString = "You must enter a valid email."
                }
                if (clientInput.text.toString().length < 3) {
                    errorString = "You must enter a valid client name of 3 or more characters"
                }
                if (errorString != null) {
                    val builder =
                            AlertDialog.Builder(activity)
                    builder.setCancelable(false)
                    builder.setTitle(errorString)
                    builder.setMessage(errorString)
                    builder.setPositiveButton(
                            "OK"
                    ) { dialog, which ->
                        Log.d(TAG, "ok clicked")
                        this.popConfigurationDialog()
                    }
                    builder.show()
                } else {
                    SFBeaconManager.getInstance(activity).email = emailInput.text.toString()
                    SFBeaconManager.getInstance(activity).client = clientInput.text.toString()
                    if (SFBeaconManager.getInstance(activity).associatedBeacon == null) {
                        popConfigurationDialog(true)
                    }
                }
            }
            builder.setNegativeButton(
                    android.R.string.cancel
            ) { dialog, which ->
                dialog.cancel()
            }
            if (SFBeaconManager.getInstance(activity).associatedBeacon != null) {
                builder.setNeutralButton(
                        "Change Beacon"
                ) { dialog, which ->
                    SFBeaconManager.getInstance(activity).associatedBeacon = null
                    dialog.cancel()
                    popConfigurationDialog(true)
                }
            }
            builder.show()
            return
        }

        if (SFBeaconManager.getInstance(activity).associatedBeacon == null) {
            if (visibleBeaconToChooseFrom.size == 0) {
                visibleBeaconToChooseFrom =
                        ArrayList(SFBeaconManager.getInstance(activity).visibleBeacons)
            }
            // Title: "Configure Beacon"
            // Look for nearby beacons.
            // If none, message: "No nearby beacons detected.  Ensure your personal beacon is turned on, hold it next to your phone and tap continue"
            // Order those detected by distance.  Start with the closest "Is the number printed on the back of your beacon XXXXXX?"
            // Yes -> Set beacon id, go to next step of config where we set email and company.
            // No -> Go to the next beacon, ask the same again.
            // Cancel -> Exit
            val builder =
                    AlertDialog.Builder(activity)
            builder.setTitle("Configure Beacon")
            if (visibleBeaconToChooseFrom.size == 0) {
                builder.setMessage("No nearby beacons detected.  Ensure your personal beacon is turned on, hold it next to your phone and tap continue.")
                builder.setPositiveButton(
                        "Continue"
                ) { dialog, which ->
                    dialog.cancel()
                    popConfigurationDialog(true)
                }
                builder.setNegativeButton(
                        "Cancel"
                ) { dialog, which ->
                    dialog.cancel()
                }
                builder.show()
                return
            } else {
                val beaconHexIdentfier = String.format("%04X%04X", visibleBeaconToChooseFrom.get(0).id2?.toInt(), visibleBeaconToChooseFrom.get(0).id3?.toInt())
                builder.setMessage("Is the number printed on the back of your beacon $beaconHexIdentfier ?")
                builder.setPositiveButton(
                        "YES"
                ) { dialog, which ->
                    dialog.cancel()
                    SFBeaconManager.getInstance(activity).associatedBeacon = visibleBeaconToChooseFrom.get(0)
                }
                builder.setNegativeButton(
                        "NO"
                ) { dialog, which ->
                    dialog.cancel()
                    visibleBeaconToChooseFrom.removeAt(0)
                    popConfigurationDialog(true)
                }
                builder.setNeutralButton(
                        "Cancel"
                ) { dialog, which ->
                    dialog.cancel()
                }
                builder.show()

                return
            }
        }
    }
    
    companion object {
        val TAG = "ConfigurationDialog"
    }
}

