package org.odk.collect.android.contracts;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.samagra.commons.Constants;
import com.samagra.commons.MainApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.ODKDriver;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormChooserList;
import org.odk.collect.android.activities.InstanceChooserList;
import org.odk.collect.android.activities.InstanceUploaderListActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.helpers.ContentResolverHelper;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.listeners.ActionListener;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceSaver;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ResetUtility;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import timber.log.Timber;

import static org.odk.collect.android.utilities.EncryptionUtils.UTF_8;

public class FormManagementSectionInteractor implements IFormManagementContract {

    @Override
    public void setODKModuleStyle(MainApplication mainApplication, int splashScreenDrawableID, int baseAppThemeStyleID,
                                  int formActivityThemeID, int customThemeId_Settings, long toolbarIconResId) {
        ODKDriver.init(mainApplication, splashScreenDrawableID, baseAppThemeStyleID, formActivityThemeID, customThemeId_Settings, toolbarIconResId);

    }

    @Override
    public void resetPreviousODKForms() {
        final List<Integer> resetActions = new ArrayList<>();
        resetActions.add(ResetUtility.ResetAction.RESET_FORMS);
        resetActions.add(ResetUtility.ResetAction.RESET_PREFERENCES);
        resetActions.add(ResetUtility.ResetAction.RESET_LAYERS);
        resetActions.add(ResetUtility.ResetAction.RESET_CACHE);
        resetActions.add(ResetUtility.ResetAction.RESET_OSM_DROID);
        Runnable runnable = () -> new ResetUtility().reset(Collect.getInstance().getApplicationContext(), resetActions);
        new Thread(runnable).start();
    }

    @Override
    public void resetEverythingODK() {
        final List<Integer> resetActions = new ArrayList<>();
        resetActions.add(ResetUtility.ResetAction.RESET_FORMS);
        resetActions.add(ResetUtility.ResetAction.RESET_PREFERENCES);
        resetActions.add(ResetUtility.ResetAction.RESET_LAYERS);
        resetActions.add(ResetUtility.ResetAction.RESET_CACHE);
        resetActions.add(ResetUtility.ResetAction.RESET_OSM_DROID);

        List<Integer> failedResetActions = new ResetUtility().reset(Collect.getInstance().getApplicationContext(), resetActions);
        Timber.e("Reset Complete%s", failedResetActions.size());

        File dir = new File(Collect.INSTANCES_PATH);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                new File(dir, child).delete();
            }
        }
    }

    public void startGetFormListCall() {
    }


    @Override
    public void createODKDirectories() {
        Collect.createODKDirs();
    }

    @Override
    public void resetODKForms(Context context) {
        final List<Integer> resetActions = new ArrayList<>();
        resetActions.add(ResetUtility.ResetAction.RESET_FORMS);
        if (!resetActions.isEmpty()) {
            Runnable runnable = () -> new ResetUtility().reset(context, resetActions);
            new Thread(runnable).start();
        }
    }

    @Override
    public HashMap<String, String> downloadFormList(String formsString) {

        HashMap<String, String> userRoleBasedForms = new HashMap<>();
        Timber.e("Role Mapping");
        if (!formsString.equals("")) {
            try {
                JSONArray jsonArray = new JSONArray(formsString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    //TODO: Check if form with the newest version is already there.
                    String formID = object.getString("FormID");
                    String formName = object.getString("FormName");
                    if (shouldUpdate()) {
                        userRoleBasedForms.put(formID, formName);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return userRoleBasedForms;
    }

    @Override
    public void initialiseODKProps() {
        Collect.getInstance().initProperties();
    }

    @Override
    public void applyODKCollectSettings(Context context, int settingResId) {
        InputStream inputStream = context.getResources().openRawResource(settingResId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String content = writer.toString();
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(content, new ActionListener() {
            @Override
            public void onSuccess() {
                initialiseODKProps();
                ToastUtils.showLongToast("Successfully loaded settings");
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof GeneralSharedPreferences.ValidationException) {
                    ToastUtils.showLongToast("Failed to load settings");
                } else {
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void launchSpecificDataForm(Context context, String formIdentifier) {
        int formToBeOpened = fetchSpecificFormID(formIdentifier);
        Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, formToBeOpened);
        Intent intent = new Intent(Intent.ACTION_EDIT, formUri);
        intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
        context.startActivity(intent);
    }


    @Override
    public void launchFormChooserView(Context context, HashMap<String, Object> toolbarModificationObject) {
        Intent i = new Intent(context, FormChooserList.class);
        i.putExtra(Constants.KEY_CUSTOMIZE_TOOLBAR, toolbarModificationObject);
        i.putIntegerArrayListExtra(Constants.CUSTOM_TOOLBAR_ARRAYLIST_HIDE_IDS, null);
        context.startActivity(i);
    }

    private Document prefillFormBasedOnTags(Document document, String tag, String tagValue) {
        try {
            if (document.getElementsByTagName(tag).item(0).getChildNodes().getLength() > 0)
                document.getElementsByTagName(tag).item(0).getChildNodes().item(0).setNodeValue(tagValue);
            else
                document.getElementsByTagName(tag).item(0).appendChild(document.createTextNode(tagValue));
        } catch (Exception e) {
            Timber.e("Unable to autofill: %s %s", tag, tagValue);
            return document;
        }
        return document;
    }


    @Override
    public void updateFormBasedOnIdentifier(String formIdentifier, String tag, String tagValue) {
        int id = fetchSpecificFormID(formIdentifier);
        Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, id);
        String fileName = ContentResolverHelper.getFormPath(formUri);
        FileOutputStream fos = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(fileName));
            prefillFormBasedOnTags(document, tag, tagValue);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            fos = new FileOutputStream(new File(fileName));
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void launchViewSubmittedFormsView(Context context, HashMap<String, Object> toolbarModificationObject) {
        Intent i = new Intent(context, InstanceChooserList.class);
        i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                ApplicationConstants.FormModes.VIEW_SENT);
        context.startActivity(i);
        HashMap<String, Object> extras = toolbarModificationObject;
//                UtilityFunctions.generateT/d Forms", true);
//        ODKDriver.launchInstanceUploaderListActivity(context, extras);
    }

    @Override
    public void launchViewUnsubmittedFormView(Context context, String className) {

        if (Collect.allowClick(className)) {
            Intent i = new Intent(context, InstanceUploaderListActivity.class);
            context.startActivity(i);
        }
    }

    @Override
    public int fetchSpecificFormID(String formIdentifier) {
        List<Form> formsFromDB = getDownloadedFormsNamesFromDatabase();
        HashMap<Integer, String> hashMap = new HashMap<>();
        for (int i = 0; i < formsFromDB.size(); i++) {
            hashMap.put(formsFromDB.get(i).getId(), formsFromDB.get(i).getDisplayName());
        }
        for (Map.Entry<Integer, String> entry : hashMap.entrySet()) {
            if (entry.getValue().contains(formIdentifier))
                return entry.getKey();
        }
        return 1;
    }


    private boolean shouldUpdate() {
        return true;
    }

    private List<Form> getDownloadedFormsNamesFromDatabase() {
        FormsDao fd = new FormsDao();
        Cursor cursor = fd.getFormsCursor();
        return fd.getFormsFromCursor(cursor);
    }

    @Override
    public boolean checkIfODKFormsMatch(String formsString) {
        HashMap<String, String> formsListToBeDownloaded = downloadFormList(formsString);
        Timber.e("formsListToBeDownloaded: " + formsListToBeDownloaded.size() + " FormsFromDatabase: " + getDownloadedFormsNamesFromDatabase().size());
        return getDownloadedFormsNamesFromDatabase().size() == formsListToBeDownloaded.size() && getDownloadedFormsNamesFromDatabase().size() != 0;
    }

    @Override
    public void startDownloadODKFormListTask(FormListDownloadResultCallback formListDownloadResultCallback) {
        DownloadFormListTask downloadFormListTask = new DownloadFormListTask(ODKDriver.getDownloadFormListUtils());
        downloadFormListTask.setDownloaderListener(value -> {
            if (value != null && !value.containsKey("dlerrormessage")) {
                formListDownloadResultCallback.onSuccessfulFormListDownload(value);
            } else {
                assert value != null;
                if (value.containsKey("dlerrormessage")) {
                    formListDownloadResultCallback.onFailureFormListDownload(true);
                } else {
                    formListDownloadResultCallback.onFailureFormListDownload(false);
                }
            }
        });
        downloadFormListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public HashMap<String, String> downloadNewFormsBasedOnDownloadedFormList(HashMap<String, String> userRoleBasedForms, HashMap<String, FormDetails> latestFormListFromServer) {
        HashMap<String, String> formsToBeDownloaded = new HashMap<>();
        List<Form> formsFromDB = getDownloadedFormsNamesFromDatabase();
        Iterator it = latestFormListFromServer.entrySet().iterator();

        // Delete excess forms
        ArrayList<String> formsToBeDeleted = new ArrayList<>();
        for (Form form : formsFromDB) {
            if (!userRoleBasedForms.containsKey(form.getJrFormId())) {
                formsToBeDeleted.add(form.getMD5Hash());
            }
        }
        // Adding new forms
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            FormDetails fd = (FormDetails) pair.getValue();
            String formID = fd.getFormID();
            boolean foundFormInDB = false;
            if (userRoleBasedForms.containsKey(fd.getFormID())) {

                for (Form form : formsFromDB) {
                    // Check if forms needs to be updated
                    if (form.getJrFormId().equals(fd.getFormID())) {
                        foundFormInDB = true;
                        boolean nullTest = false;
                        if (form.getJrVersion() == null && fd.getFormVersion() == null)
                            nullTest = true;
                        if (form.getJrVersion() == null && fd.getFormVersion() != null) {
                            formsToBeDownloaded.put(fd.getFormID(), fd.getFormName());
                            formsToBeDeleted.add(form.getMD5Hash());
                        } else if (!nullTest && !form.getJrVersion().equals(fd.getFormVersion())) {
                            formsToBeDownloaded.put(fd.getFormID(), fd.getFormName());
                            formsToBeDeleted.add(form.getMD5Hash());
                        }
                    }
                }
                if (!foundFormInDB) formsToBeDownloaded.put(fd.getFormID(), fd.getFormName());
            }
        }
        if (formsToBeDeleted.size() > 0 && formsToBeDeleted.toArray() != null) {
            new FormsDao().deleteFormsFromMd5Hash(formsToBeDeleted.toArray(new String[0]));
        }
        return formsToBeDownloaded;
    }

    @Override
    public void downloadODKForms(DataFormDownloadResultCallback dataFormDownloadResultCallback,
                                 HashMap<String, String> forms) {
        ArrayList<FormDetails> filesToDownload = new ArrayList<>();
        Iterator it = forms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String formName = pair.getValue().toString();
            String formID = pair.getKey().toString();
            String fileName = Collect.FORMS_PATH + File.separator + formName + ".xml";
            String serverURL = new WebCredentialsUtils().getServerUrlFromPreferences();
            String downloadUrl = serverURL + "/forms/" + formID + ".xml";
            FormDetails fm = new FormDetails(
                    formName,
                    downloadUrl,
                    null,
                    formID,
                    "",
                    null,
                    null,
                    false,
                    true);
            filesToDownload.add(fm);
            it.remove();
        }
        DownloadFormsTask downloadFormsTask = new DownloadFormsTask();
        downloadFormsTask.setDownloaderListener(new DownloadFormsTaskListener() {
            @Override
            public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
                if (result != null)
                    dataFormDownloadResultCallback.formsDownloadingSuccessful(result);
                else
                    dataFormDownloadResultCallback.formsDownloadingFailure();
            }

            @Override
            public void progressUpdate(String currentFile, int progress, int total) {
                dataFormDownloadResultCallback.progressUpdate(currentFile, progress, total);
            }

            @Override
            public void formsDownloadingCancelled() {
                dataFormDownloadResultCallback.formsDownloadingCancelled();
            }
        });


        downloadFormsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filesToDownload);
    }


    @Override
    public ArrayList<ArrayList<String>> readDownloadedFormReferenceCSV(Context context) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.sample)));//Specify asset file name
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                result.add(new ArrayList<>(Arrays.asList(nextLine)));
                // nextLine[] is an array of values from the line
//                Timber.d("The line is  " + nextLine.length + "   fvf  " + nextLine.toString());
            }
            Timber.d("First row of the csv is %s", result.get(0));
            Timber.d("First element of second row of the csv is %s", result.get(1).get(0));
            Timber.d("KEYS ARE %s", fetchReferenceKeys(result.get(0)));
            buildCSV(mainMethid(), fetchReferenceKeys(result.get(0)), result.get(0), result.get(1).get(0));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "The specified file was not found", Toast.LENGTH_SHORT).show();
            return null;

        }

    }

    @Override
    public void startPreFillTask(Context context, BuildCSVListener buildCSVListener, ArrayList<String> formNames) {
        ArrayList<ArrayList<String>> result = new ArrayList<>(); //List of list of Strings, containing sample csv downloaded.
        CSVReader reader;
        String fileDir = Collect.FORMS_PATH + File.separator + formNames.get(0) + "-media" + File.separator + "student_list.csv";
        try {
            //parsing a CSV file into CSVReader class constructor
            reader = new CSVReader(new FileReader(new File(fileDir)));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                result.add(new ArrayList<>(Arrays.asList(nextLine)));
                // nextLine[] is an array of values from the line
            }
            Timber.d("Final read First row of the csv is %s", result);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        /**
         *
         *         try {
         *             CSVReader reader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.sample)));//Specify asset file name
         *             String[] nextLine;
         *             while ((nextLine = reader.readNext()) != null) {
         *                 result.add(new ArrayList<>(Arrays.asList(nextLine)));
         *                 // nextLine[] is an array of values from the line
         * //                Timber.d("The line is  " + nextLine.length + "   fvf  " + nextLine.toString());
         *             }
         *             Timber.d("First row of the csv is %s", result.get(0));
         *             Timber.d("First element of second row of the csv is %s", result.get(1).get(0));
         *             Timber.d("KEYS ARE %s", fetchReferenceKeys(result.get(0)));
         *             buildCSV(mainMethid(), fetchReferenceKeys(result.get(0)), result.get(0), result.get(1).get(0));
         *             return result;
         *         } catch (Exception e) {
         *             e.printStackTrace();
         *             Toast.makeText(context, "The specified file was not found", Toast.LENGTH_SHORT).show();
         *             return null;
         *
         *         }
         */


    }


    public Set<String> fetchReferenceKeys(ArrayList<String> downloadedData) {
        Set<String> result = new HashSet<>();
        for (int i = 1; i < downloadedData.size(); i++) {
            if (downloadedData.get(i).contains("_")) {
                result.add(downloadedData.get(i).split("_")[0]);
            } else {
                result.add(downloadedData.get(i));
            }
        }
        return result;
    }


    public JSONArray mainMethid() throws JSONException {
        String data = "[\n" +
                "   {\n" +
                "      \"student\": \"sandeep\",\n" +
                "      \"section\": \"a\",\n" +
                "\t \"class\" :1\n" +
                "   }, \n" +
                "   {\n" +
                "      \"student\": \"sandeep1\",\n" +
                "      \"section\": \"a\",\n" +
                "\t \"class\" :1\n" +
                "   },\n" +
                "  {\n" +
                "      \"student\": \"sandeep2\",\n" +
                "      \"section\": \"a\",\n" +
                "\t \"class\" :1\n" +
                "   },\n" +
                "  {\n" +
                "      \"student\": \"sandeep3\",\n" +
                "      \"section\": \"a\",\n" +
                "\t \"class\" :1\n" +
                "   },\n" +
                "  {\n" +
                "      \"student\": \"sandeep4\",\n" +
                "      \"section\": \"a\",\n" +
                "\t \"class\" :4\n" +
                "   }\n" +
                "]";
        JSONArray jsonArr = new JSONArray(data);
        return jsonArr;
    }


    public void buildCSV(JSONArray jsonArray, Set<String> keySet, ArrayList<String> downloadedData, String title) throws JSONException {
        ArrayList<ArrayList<String>> finalResult = new ArrayList<>();
        finalResult.add(downloadedData);
        boolean flag = false;
//        JsonArray registrations = jsonArray.getAsJsonArray();
        for (String s : keySet) {
            if (jsonArray.getJSONObject(0).has(s))
                flag = true;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            ArrayList<String> newList = new ArrayList<>();
            newList.add(title);
            for (String s : keySet) {
                newList.add(jsonArray.getJSONObject(i).get(s).toString());
                newList.add(jsonArray.getJSONObject(i).get(s).toString());
            }
            finalResult.add(newList);
        }

        Timber.d("Input for CSV is %s", finalResult);
        saveCSV(finalResult);
    }

    private void saveCSV(ArrayList<ArrayList<String>> finalResult) {
        CSVWriter writer = null;
        File outputFile = new File(Collect.ODK_ROOT + "/sample.csv");

        try {
            writer = new CSVWriter(new FileWriter(outputFile), ',');
            for (ArrayList<String> list : finalResult) {

                String[] stockArr = new String[list.size()];
                stockArr = list.toArray(stockArr);
                writer.writeNext(stockArr);
            }
//            String[] entries = "first#second#third".split("#"); // array of your values
//            writer.writeNext(entries);
            writer.close();

            readCsv();
        } catch (IOException e) {
            Timber.e("EEEEEE");
        }
    }

    private void readCsv() {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        CSVReader reader = null;
        try {
//parsing a CSV file into CSVReader class constructor
            reader = new CSVReader(new FileReader(new File(Collect.ODK_ROOT + "/sample.csv")));


            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                result.add(new ArrayList<>(Arrays.asList(nextLine)));
                // nextLine[] is an array of values from the line
//                Timber.d("The line is  " + nextLine.length + "   fvf  " + nextLine.toString());
            }
            Timber.d("Final read First row of the csv is %s", result);
//            String [] nextLine;
////reads one line at a time
//            while ((nextLine = reader.readNext()) != null)
//            {
//                for(String token : nextLine)
//                {
//                    System.out.print(token);
//                }
//                System.out.print("\n");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<String[]> readCVSFromAssetFolder(Context context) {
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            InputStream inputStream = context.getAssets().open("sample.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = br.readLine()) != null) {
                content = line.split(",");
                csvLine.add(content);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvLine;
    }

    private void printCVSContent(List<String[]> result) {
        String cvsColumn = "";
        for (int i = 0; i < result.size(); i++) {
            String[] rows = result.get(i);
            cvsColumn += rows[0] + " " + rows[1] + " " + rows[2] + "\n";
        }
        Timber.i("This is it %s", cvsColumn);
    }
// try{
////            InputStream inputStream = context.getResources().openRawResource(settingResId);
//
//        String csvfileString = context.getApplicationInfo().dataDir + File.separatorChar + "strings.csv";
//        File csvfile = new File(csvfileString);
//        String path = csvfile.getAbsolutePath();
//        CSVReader reader = new CSVReader(new FileReader(path));
//        String [] nextLine;
//        while ((nextLine = reader.readNext()) != null) {
//            // nextLine[] is an array of values from the line
//            System.out.println(nextLine[0] + nextLine[1] + "etc...");
//        }
//    }catch(FileNotFoundException e){
//        e.printStackTrace();
//        Toast.makeText(context, "The specified file was not found", Toast.LENGTH_SHORT).show();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
}