package app.a2ms.contentproviderexemple

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val REQUEST_CODE_READ_CONTACTS = 1

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS)
        Log.d(TAG, "onCreate check permission: returned $hasReadContactPermission")

        if (hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission")
            ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
        }

        fab.setOnClickListener { view ->
            Log.d(TAG, "fab onclick: starts")
            if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                        projection,
                        null,
                        null,
                        ContactsContract.Contacts.DISPLAY_NAME)

                val contacts = ArrayList<String>() //Create a list of all our contacts
                cursor?.use {
                    //loop through the cursor
                    while (it.moveToNext())
                        contacts.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)))
                }
                val adapter = ArrayAdapter<String>(this, R.layout.contact_detail, R.id.name, contacts)
                contact_name.adapter = adapter
            } else {
                Snackbar.make(view, "Please grant access to your Contacts", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Grant Access") {
                            Log.d(TAG, "Snackbar onClick starts")
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
                                Log.d(TAG, "Snackbar onClick: calling requestPermission")
                                ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
                            } else {
                                //the user permanently denied the permission, take them directly to the app settings
                                Log.d(TAG, "Snackbar onClick: latching Settings for our App")
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts("package", this.packageName, null)
                                Log.d(TAG, "Snackbar onClick: Uri is $uri")
                                intent.data = uri
                                this.startActivity(intent)
                            }
                            Log.d(TAG, "Snackbar onClick ends")
                        }.show()
            }
            Log.d(TAG, "fab onclick:ends")
        }
        Log.d(TAG, "MainActivity: ends")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
