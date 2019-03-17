package com.booleaners.sherlockautosettings;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Telephone;

public class MainActivity extends AppCompatActivity {

    Button addContactsBtn;
    Button addMessagesBtn;
    Button addCallLogBtn;
    Button addCalendarBtn;
    Button setScreenOffTimeBtn;

    final int PERMISSION_CONTACTS = 1;
    final int PERMISSION_MESSAGE = 2;
    final int PERMISSION_CALLLOG = 3;
    final int PERMISSION_CALENDAR = 4;
    final int PERMISSION_SETTINGS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String[] CONTACTS_PERMISSIONS = {
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        };

        final String[] MESSAGE_PERMISSIONS = {
                android.Manifest.permission.READ_SMS
        };

        final String[] CALLLOG_PERMISSIONS = {
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.WRITE_CALL_LOG
        };

        final String[] CALENDAR_PERMISSIONS = {
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
        };

        final String[] SETTINGS_PERMISSIONS = {
                Manifest.permission.WRITE_SETTINGS
        };

        addContactsBtn = findViewById(R.id.contacts);
        addContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this, CONTACTS_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, CONTACTS_PERMISSIONS,
                            PERMISSION_CONTACTS);
                } else {
                    saveContact();
                }
            }
        });

        addMessagesBtn = findViewById(R.id.messages);
        addMessagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this, MESSAGE_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, MESSAGE_PERMISSIONS,
                            PERMISSION_MESSAGE);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        final String myPackageName = getPackageName();
                        if (!Telephony.Sms.getDefaultSmsPackage(MainActivity.this).equals(myPackageName)) {

                            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                    myPackageName);
                            startActivityForResult(intent, 1);
                        } else {
                            saveSms();
                        }
                    } else {
                        saveSms();
                    }
                }
            }
        });

        addCallLogBtn = findViewById(R.id.calllog);
        addCallLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this, CALLLOG_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, CALLLOG_PERMISSIONS,
                            PERMISSION_CALLLOG);
                } else {
                    saveCallLog();
                }
            }
        });

        addCalendarBtn = findViewById(R.id.calendar);
        addCalendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this, CALENDAR_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, CALENDAR_PERMISSIONS,
                            PERMISSION_CALENDAR);
                } else {
                    saveCalendar();
                }
            }
        });

        setScreenOffTimeBtn = findViewById(R.id.screenofftime);
        setScreenOffTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this, SETTINGS_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(MainActivity.this, SETTINGS_PERMISSIONS,
                            PERMISSION_SETTINGS);
                } else {
                    setTimeout(4);
                }
            }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final String myPackageName = getPackageName();
                    if (Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                        saveSms();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveContact();
                }
                break;

            case PERMISSION_MESSAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        final String myPackageName = getPackageName();
                        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {

                            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                    myPackageName);
                            startActivityForResult(intent, 1);
                        } else {
                            saveSms();
                        }
                    } else {
                        saveSms();
                    }
                }
                break;

            case PERMISSION_CALLLOG:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveCallLog();
                }
                break;

            case PERMISSION_CALENDAR:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveCalendar();
                }
                break;
        }
    }

    private void saveContact() {
        String displayName = "";
        String mobileNumber = "";
        String homeNumber = "";
        String workNumber = "";
        String emailID = "";
        String company = "";
        String jobTitle = "";
        String website = "";
        String street = "";
        String city = "";
        String state = "";
        String zipcode = "";
        String country = "";

        try {
            File file = new File(Environment.getExternalStorageDirectory()
                    + "/vcard.vcf");
            List<VCard> vcards = Ezvcard.parse(file).all();

            for (VCard vcard : vcards) {
                displayName = vcard.getFormattedName().getValue();

                for (Telephone tel : vcard.getTelephoneNumbers()) {
                    if (tel.getTypes().toString().contains("[home, voice]")) {
                        homeNumber = tel.getText();
                    }
                }

                insertNewContacts(displayName,
                        mobileNumber,
                        homeNumber,
                        workNumber,
                        emailID,
                        company,
                        jobTitle,
                        website,
                        street,
                        city,
                        state,
                        zipcode,
                        country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getAllContacts();
    }

    private void insertNewContacts(String displayName,
                                   String mobileNumber,
                                   String homeNumber,
                                   String workNumber,
                                   String emailID,
                                   String company,
                                   String jobTitle,
                                   String website,
                                   String street,
                                   String city,
                                   String state,
                                   String zipcode,
                                   String country) {

        System.out.println(displayName + ", " +
                mobileNumber + ", " +
                homeNumber + ", " +
                workNumber + ", " +
                emailID + ", " +
                company + ", " +
                jobTitle + ", " +
                website + ", " +
                street + ", " +
                city + ", " +
                state + ", " +
                zipcode + ", " +
                country);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // exp
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                        null)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, street)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, city)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, state)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, zipcode)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, country)
                //.withValue(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                .build());

        // ------------------------------------------------------ Names
        if (displayName != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            displayName).build());
        }
        // ------------------------------------------------------ Mobile Number
        if (mobileNumber != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                            mobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        // ------------------------------------------------------ Home Numbers
        if (homeNumber != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                            homeNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                    .build());
        }

        // ------------------------------------------------------ Work Numbers
        if (workNumber != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                            workNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build());
        }

        // ------------------------------------------------------ Email
        if (emailID != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA,
                            emailID)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                            ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        // ------------------------------------------------------ Organization
        if (company != null || jobTitle != null) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.Organization.COMPANY,
                            company)
                    .withValue(
                            ContactsContract.CommonDataKinds.Organization.TYPE,
                            ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .withValue(
                            ContactsContract.CommonDataKinds.Organization.TITLE,
                            jobTitle)
                    .withValue(
                            ContactsContract.CommonDataKinds.Organization.TYPE,
                            ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build());

            if (website != null) {
                ops.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                                rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.URL,
                                website)
                        .withValue(ContactsContract.CommonDataKinds.Website.TYPE,
                                ContactsContract.CommonDataKinds.Website.TYPE_WORK)
                        .build());
            }
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean saveSms() {
        String phoneNumber = "12345678";
        String message = "test";
        String readState = "0";
        String time = "";
        String folderName = "inbox";
        boolean ret = false;

        try {
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("body", message);
            values.put("read", readState); //"0" for have not read sms and "1" for have read sms
            values.put("date", time);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Uri uri = Telephony.Sms.Sent.CONTENT_URI;
                if (folderName.equals("inbox")) {
                    uri = Telephony.Sms.Inbox.CONTENT_URI;
                }
                MainActivity.this.getContentResolver().insert(uri, values);
            } else {
                MainActivity.this.getContentResolver().insert(Uri.parse("content://sms/" + folderName),
                        values);
            }

            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            ret = false;
        }

        getAllSms();

        return ret;
    }

    private void getAllSms() {

        ContentResolver cr = MainActivity.this.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;

        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String type = "";
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_SENT:
                            type = "sent";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                            type = "outbox";
                            break;
                        default:
                            break;
                    }

                    System.out.println("[SMS] " + smsDate + ", " +
                            number + ", " +
                            body + ", " +
                            type);
                    c.moveToNext();
                }
            }

            c.close();
            Toast.makeText(this, "SMS Count: " + totalSMS, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAllContacts() {
        int totalContacts = 0;
        Cursor phones =
                getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, null, null, null);
        totalContacts = phones.getCount();
        while (phones.moveToNext()) {
            String name =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            System.out.println("[Contacts] " + name + ", " + phoneNumber);
        }

        if (totalContacts > 0) {
            Toast.makeText(this, "Contacts Count: " + totalContacts, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "No contacts to show!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCallLog() {
        String number = "12345678";
        String name = "Kang";
        String label = "Mobile";
        String date = "";
        String duration = "10";
        int myCallType = CallLog.Calls.INCOMING_TYPE;

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.CACHED_NAME, name);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, label);
        values.put(CallLog.Calls.DATE, date);
        values.put(CallLog.Calls.DURATION, duration);
        values.put(CallLog.Calls.TYPE, myCallType);

        MainActivity.this.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

        getAllCallLog();
    }

    private void getAllCallLog() {
        int totalCallLog = 0;
        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null,
                null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");

        totalCallLog = managedCursor.getCount();

        if (managedCursor.moveToFirst()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            // String callDayTime = new Date(Long.valueOf(callDate)).toString();
            // long timestamp = convertDateToTimestamp(callDayTime);
            String callDuration = managedCursor.getString(duration);
            int calld = Integer.parseInt(callDuration);

            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall " +
                    " \nCall duration in sec :--- " + calld);
            sb.append("\n----------------------------------");

        }
        managedCursor.close();
        System.out.println(sb);

        if (totalCallLog > 0) {
            Toast.makeText(this, "Call Log Count: " + totalCallLog, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "No call log to show!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCalendar() {
        ContentResolver cr = getContentResolver();
        long nowDate = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, nowDate);
        values.put(CalendarContract.Events.DTEND, nowDate);
        values.put(CalendarContract.Events.TITLE, "title2");
        values.put(CalendarContract.Events.CALENDAR_ID, 1);

        values.put(CalendarContract.Events.EVENT_TIMEZONE,
                Calendar.getInstance().getTimeZone().getID());
        cr.insert(CalendarContract.Events.CONTENT_URI,
                values);

        getAllCalendar();
    }

    private void getAllCalendar() {
        int totalCalendar = 0;
        Cursor managedCursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null,
                null, null);

        totalCalendar = managedCursor.getCount();

        if (totalCalendar > 0) {
            Toast.makeText(this, "Calendar Count: " + totalCalendar, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "No calendar to show!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setTimeout(int screenOffTimeout) {
        int time;
        switch (screenOffTimeout) {
            case 0:
                time = 15000;
                break;
            case 1:
                time = 30000;
                break;
            case 2:
                time = 60000;
                break;
            case 3:
                time = 120000;
                break;
            case 4:
                time = 600000;
                break;
            case 5:
                time = 1800000;
                break;
            default:
                time = -1;
        }

        android.provider.Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, time);

        int currentTime = getTimeout();

        if (currentTime == time) {
            Toast.makeText(this, "SCREEN_OFF_TIMEOUT (" + currentTime + ") is saved sucessfully",
                    Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "SCREEN_OFF_TIMEOUT setting is not saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private int getTimeout() {
        int time = -1;
        try {
            time = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return time;
    }
}
