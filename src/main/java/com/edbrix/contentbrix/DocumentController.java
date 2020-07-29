
package com.edbrix.contentbrix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.edbrix.contentbrix.customview.CustomDialogSlideFromBottomAdvWhiteboard;
import com.myscript.iink.ContentBlock;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IRenderTarget;
import com.myscript.iink.MimeType;
import com.myscript.iink.Renderer;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.ImageDrawer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

public class DocumentController {
    private static final String TAG = "DocumentController";
    private static final String DOCUMENT_CONTROLLER_STATE_FILE_NAME = "documentControllerState.properties";
    public static final String DOCUMENT_CONTROLLER_STATE_SAVED_FILE_NAME = "content_package_file_name";
    private static final String DOCUMENT_CONTROLLER_STATE_SAVED_PART_INDEX = "content_package_part_index";

    private Activity activity;
    private Editor editor;
    private EditorView editorView;

    private File currentFile;
    private ContentPackage currentPackage;
    private ContentPart currentPart;

    private File stateFile;
    private Properties stateProperties;

    private ArrayList<File> fileList = new ArrayList<File>();

    private int selectedPartOption = 0;

    public DocumentController(Activity activity, EditorView editorView) {
        this.activity = activity;
        this.editorView = editorView;
        this.editor = editorView.getEditor();

        currentFile = null;
        currentPackage = null;
        currentPart = null;

        stateFile = new File(activity.getFilesDir().getPath() + File.separator + DOCUMENT_CONTROLLER_STATE_FILE_NAME);
        loadState();
    }

    public final void close() {
        if (currentPart != null)
            currentPart.close();
        if (currentPackage != null)
            currentPackage.close();

        // newly added by Raj 27Feb 2020
        if(currentFile != null && currentFile.exists()){
            alreadyExistedPackageDelete(currentFile);
            Log.e("DocumentController", "current file deleted : " + currentFile.delete());
        } // newly added by Raj 27Feb 2020

        currentFile = null;
        currentPackage = null;
        currentPart = null;
        // newly added by Raj 27Feb 2020
        if(stateFile.exists()){
            Log.e("DocumentController", "stateFile deleted : " + stateFile.delete());
        } // newly added by Raj 27Feb 2020
        clearFiles(activity.getFilesDir());
    }

    public final boolean hasPart() {
        return currentPart != null;
    }

    public final int getPartIndex() {
        return currentPart == null ? -1 : currentPackage.indexOfPart(currentPart);
    }

    public final int getPartCount() {
        return currentPackage == null ? 0 : currentPackage.getPartCount();
    }

    public String getFileName() {
        return currentFile.getName();
    }

    private String makeUntitledFilename() {
        int num = 0;
        String name;
        do {
            name = "Page" + (++num) + ".iink";
        }
        while (new File(activity.getFilesDir(), name).exists() ||
                (currentFile != null && currentFile.getName().equals(name)));
        return name;
    }

    public final void setPart(File newFile, ContentPackage newPackage, ContentPart newPart) {
        editor.getRenderer().setViewOffset(0, 0);
        editor.getRenderer().setViewScale(1);
        editor.setPart(newPart);
        editorView.setVisibility(View.VISIBLE);

        if (currentPart != null && currentPart != newPart)
            currentPart.close();
        if (currentPackage != null && currentPackage != newPackage)
            currentPackage.close();

        currentFile = newFile;
        currentPackage = newPackage;
        currentPart = newPart;

        activity.setTitle(currentFile.getName() + " - " + currentPart.getType());
    }

    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    fileList.add(listFile[i]);
                    getfile(listFile[i]);

                } else {
                    if (listFile[i].getName().endsWith(".iink")) {
                        fileList.add(listFile[i]);
                    }
                }
            }
        }
        return fileList;
    }

    public ArrayList<File> clearFiles(File dir) {
        File listFile[] = dir.listFiles();
        int fileLength = listFile.length;
        if (listFile != null && fileLength > 0) {
            for (int i = 0; i < fileLength; i++) {

                if (listFile[i].isDirectory()) {
                    clearFiles(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(".iink")) {
                        fileList.add(listFile[i]);
                         if(listFile[i].delete()){
                             fileLength = listFile.length;
                         }
                    }
                }
            }
        }
        return fileList;
    }

    public final boolean newPackage() {
        final Activity context = this.activity;
        String fileName = makeUntitledFilename();
        File file = new File(activity.getFilesDir(), fileName);
        ContentPackage newPackage = null;
        try {
            newPackage = editor.getEngine().createPackage(file);
//            newPart(file, newPackage, true);
            showSelectPagesOptionDialogForNewPackage(file, newPackage,true);//newly added
        } catch (IOException e) {
            e.printStackTrace();
            alreadyExistedPackageDelete(file);
//            Toast.makeText(context, "Failed to create package" + e.getMessage()
//                    , Toast.LENGTH_LONG).show();

        } catch (IllegalArgumentException e) {
            Toast.makeText(context, "Package already opened", Toast.LENGTH_LONG).show();
            //setPart(file, newPackage, null);
        }
        return true;
    }

    /**
     * Deleted Existed Package by file
     * @param file  file
     */
    public void alreadyExistedPackageDelete(File file){
        try{
            editor.getEngine().deletePackage(file);
            newPackage();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public final boolean openPackage() {
        final File[] files = activity.getFilesDir().listFiles();//09
        final Activity context = this.activity;

        if (files.length == 0) {
            Log.e(TAG, "Failed to list files in \"" + activity.getFilesDir() + "\"");
            return false;
        }
        // String[] fileNames = new String[files.length];
        final List<String> fileNames = new ArrayList<String>();
        for (int i = 0; i < files.length; ++i) {
            //Log.e("123",files[i].getName() +" Indx "+i);

            if (files[i].getName().toString().equals(".Fabric")) {
                Log.e("123", "found" + " Indx " + i);
            } else if (files[i].getName().toString().equals("tmp")) {
                Log.e("123", "found" + " Indx " + i);
            } else if (files[i].getName().toString().equals("documentControllerState.properties")) {
                Log.e("123", "found" + " Indx " + i);
            } else {
                Log.e("123", "Not found" + " Indx " + i);
                //fileNames[i] = files[i].getName();
                fileNames.add(files[i].getName());
            }
        }
       /* for (int i = 0; i < fileNames.size(); ++i){
            if(fileNames.get(i).equals(".Fabric")){
                Log.e("123","found"+" Indx "+i);
                fileNames.remove(i);
            }else if(fileNames.get(i).equals("tmp")){
                Log.e("123","found"+" Indx "+i);
                fileNames.remove(i);
            }else if(fileNames.get(i).equals("documentControllerState.properties")){
                Log.e("123","found"+" Indx "+i);
                fileNames.remove(i);
            }else{
                Log.e("123","Not found"+" Indx "+i);
            }
        }*/

        final int[] selected = new int[]{0};
        final String[] selectedFile = new String[1];
        if (fileNames.size() > 0) {
            selectedFile[0] = fileNames.get(0);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.openPackage_title);
            dialogBuilder.setSingleChoiceItems(fileNames.toArray(new String[fileNames.size()]), selected[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //selected[0] = which;
                    selectedFile[0] = fileNames.get(which);
                }
            });
            dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File newFile = null;
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().equals(selectedFile[0])) {
                            newFile = files[i];
                        }
                    }
                    try {
                        ContentPackage newPackage = editor.getEngine().openPackage(newFile);
                        ContentPart newPart = newPackage.getPart(0);
                        setPart(newFile, newPackage, newPart);
                    } catch (IOException e) {
                        Toast.makeText(context, "Failed to open package" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
            return true;
        } else {
            Toast.makeText(this.activity, "No any file available.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public final boolean savePackage() {
        if (currentPart == null)
            return false;

        try {
            currentPart.getPackage().save();
            storeState();
            Toast.makeText(this.activity, "Saved successfully.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this.activity, "Failed to save package", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public final boolean saveToTemp() {
        if (currentPart == null)
            return false;

        try {
            currentPart.getPackage().saveToTemp();
            storeState();
        } catch (IOException e) {
            Toast.makeText(this.activity, "Failed to save package to temporary directory", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public final boolean newPart() {
        return currentPart == null ? newPackage() : newPart(currentFile, currentPackage, false);
    }

    private final boolean newPart(final File targetFile, final ContentPackage targetPackage, final boolean closeOnCancel) {
        final boolean showCancel;
        if (currentPackage == null) {
            // from start
            showCancel = false;
        } else {
            if (currentPackage != targetPackage) {
                // new file
                showCancel = true;
            } else {
                // from "+" (new part)
                showCancel = true;
            }
        }

        Engine engine = editor.getEngine();
        final String[] partTypes = engine.getSupportedPartTypes();
//        final int[] selected = new int[]{0};
//        #new
        final int[] selected = new int[]{selectedPartOption};
        String partType = partTypes[selected[0]];
        ContentPart newPart = targetPackage.createPart(partType);
        setPart(targetFile, targetPackage, newPart);
//        #new

        /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity).setCancelable(false);
        dialogBuilder.setTitle(R.string.newPart_title);
        dialogBuilder.setSingleChoiceItems(partTypes, selected[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected[0] = which;
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String partType = partTypes[selected[0]];
                ContentPart newPart = targetPackage.createPart(partType);
                setPart(targetFile, targetPackage, newPart);
            }
        });
        if (showCancel) {
            dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (closeOnCancel)
                        targetPackage.close();
                }
            });
        }
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();*/
        return true;
    }

    private void showSelectPagesOptionDialogForNewPackage(final File targetFile, final ContentPackage targetPackage, final boolean closeOnCancel) {
        Engine engine = editor.getEngine();
        final String[] partTypes = engine.getSupportedPartTypes();
        CustomDialogSlideFromBottomAdvWhiteboard customDialogSlideFromBottom = new CustomDialogSlideFromBottomAdvWhiteboard(activity, R.style.DialogAnimation,"");
        customDialogSlideFromBottom.setOnActionButtonListener(new CustomDialogSlideFromBottomAdvWhiteboard.OnActionButtonListener() {
            @Override
            public void onOptionPressed(int optionType) {
                selectedPartOption = optionType;
                String partType = partTypes[optionType];
                ContentPart newPart = targetPackage.createPart(partType);
                setPart(targetFile, targetPackage, newPart);
            }

            @Override
            public void onCancelPressed() {
                if (closeOnCancel)
                    targetPackage.close();
            }
        });
        customDialogSlideFromBottom.showMe();
    }

    public final boolean openPart(@NonNull String fileName, int indexOfPart) {
        try {
            File file = new File(activity.getFilesDir(), fileName);
            ContentPackage newPackage = editor.getEngine().openPackage(file);
            ContentPart newPart = newPackage.getPart(indexOfPart);

            setPart(file, newPackage, newPart);
        } catch (IOException e) {
            Toast.makeText(this.activity, "Failed to open part for file \"" + fileName + "\" with index " + indexOfPart, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public final boolean previousPart() {
        if (currentPart == null)
            return false;
        int index = currentPackage.indexOfPart(currentPart);
        if (index - 1 < 0)
            return false;
        ContentPart newPart = currentPackage.getPart(index - 1);
        setPart(currentFile, currentPackage, newPart);
        return true;
    }

    public final boolean nextPart() {
        if (currentPart == null)
            return false;
        int index = currentPackage.indexOfPart(currentPart);
        if (index + 1 >= currentPackage.getPartCount())
            return false;
        ContentPart newPart = currentPackage.getPart(index + 1);
        setPart(currentFile, currentPackage, newPart);
        return true;
    }

    public final boolean resetView() {
        Renderer renderer = editor.getRenderer();
        renderer.setViewOffset(0, 0);
        renderer.setViewScale(1);
        editorView.invalidate(renderer, EnumSet.allOf(IRenderTarget.LayerType.class));
        return true;
    }

    public final boolean zoomIn() {
        Renderer renderer = editor.getRenderer();
        renderer.zoom(110.0f / 100.0f);
        editorView.invalidate(renderer, EnumSet.allOf(IRenderTarget.LayerType.class));
        return true;
    }

    public final boolean zoomOut() {
        Renderer renderer = editor.getRenderer();
        renderer.zoom(100.0f / 110.0f);
        editorView.invalidate(renderer, EnumSet.allOf(IRenderTarget.LayerType.class));
        return true;
    }

    public final boolean convert(final ContentBlock block) {
        ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(block);

        if (supportedStates.length == 0)
            return false;

        editor.convert(block, supportedStates[0]);

        return true;
    }

    public final boolean export(final ContentBlock block) {
        MimeType[] mimeTypes = editor.getSupportedExportMimeTypes(block);

        if (mimeTypes.length == 0)
            return false;

        final ArrayList<String> typeExtensions = new ArrayList<String>();
        ArrayList<String> typeDescriptions = new ArrayList<String>();

        for (MimeType mimeType : mimeTypes) {
            String fileExtensions = mimeType.getFileExtensions();

            if (fileExtensions == null)
                continue;

            String[] extensions = fileExtensions.split(" *, *");

            for (String extension : extensions) {
                String extension_;

                if (extension.startsWith("."))
                    extension_ = extension;
                else
                    extension_ = "." + extension;

                typeExtensions.add(extension_);
                typeDescriptions.add(mimeType.getName() + " (*" + extension_ + ")");
            }
        }

        if (typeExtensions.isEmpty())
            return false;

        final int[] selected = new int[]{0};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        dialogBuilder.setTitle(R.string.exportType_title);
        dialogBuilder.setSingleChoiceItems(typeDescriptions.toArray(new String[0]), selected[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected[0] = which;
            }
        });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                export_(block, typeExtensions.get(selected[0]));
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        return true;
    }

    private final boolean export_(final ContentBlock block, final String fileExtension) {

        final Activity context = this.activity;
        final File[] fileHolder = new File[1];
        //final EditText input = new EditText(activity);
        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + activity.getResources().getString(R.string.app_name), "exports");
        if (!f1.exists()) {
            f1.mkdirs();
        }

        //added new code for custom dialog box
        final AlertDialog dialogBuilder = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = context.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_export_file_rename_dialog, null);

        final TextView txtViewExport = (TextView) dialogView.findViewById(R.id.txtViewExport);
        final EditText edtxtExportFileName = (EditText)dialogView.findViewById(R.id.edtxtExportFileName);
        final Button btnCancel = (Button)dialogView.findViewById(R.id.btnCancel);
        final Button btnOk = (Button)dialogView.findViewById(R.id.btnOk);

        edtxtExportFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String fileName = s.toString();

                if (!fileName.endsWith(fileExtension))
                    fileName = fileName + fileExtension;

                //File file = new File(activity.getFilesDir(), fileName);
                //File file = new File(Environment.getExternalStorageDirectory(), fileName);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + activity.getResources().getString(R.string.app_name) + "/exports", fileName);

                fileHolder[0] = file;
                if (file.exists()) {
                    Toast.makeText(context, "This file name already exist.", Toast.LENGTH_LONG).show();
                    edtxtExportFileName.setTextColor(Color.RED);
                } else {
                    edtxtExportFileName.setTextColor(Color.BLACK);
                }
            }
        });


        String filename = currentFile.getName();
        int dotPos = filename.lastIndexOf('.');
        String basename = dotPos > 0 ? filename.substring(0, dotPos) : filename;
        edtxtExportFileName.setText(basename + fileExtension);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = edtxtExportFileName.getText().toString();

                if(!fileName.equals("")){

                    /*if (!fileName.endsWith(fileExtension))
                        fileName = fileName + fileExtension;

                    File file = new File(Environment.getExternalStorageDirectory() + "/" + activity.getResources().getString(R.string.app_name) + "/exports", fileName);
                    fileHolder[0] = file;
                    if (file.exists()) {
                        Toast.makeText(context, "This file name already exist.", Toast.LENGTH_LONG).show();
                        edtxtExportFileName.setTextColor(Color.RED);
                    } else {
                        edtxtExportFileName.setTextColor(Color.BLACK);
                    }*/

                    File file = fileHolder[0];
                    if (fileHolder[0] != null) {
                        try {
                            ImageDrawer imageDrawer = new ImageDrawer();
                            imageDrawer.setImageLoader(editorView.getImageLoader());
                            editor.waitForIdle();
                            editor.export_(block, file, imageDrawer);
                            Toast.makeText(context, "File is exported\nCheck in 'My Exports'", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "Failed to export file", Toast.LENGTH_LONG).show();
                        }
                    }
                    dialogBuilder.dismiss();
                }else{
                    Toast.makeText(context, "Please enter the file name.", Toast.LENGTH_LONG).show();

                }

            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogBuilder.show();
//-------------------------------------------------------------


       //comment code by pranav 02082019 for purpose of newly added custom dialog
        /*input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String fileName = s.toString();

                if (!fileName.endsWith(fileExtension))
                    fileName = fileName + fileExtension;

                //File file = new File(activity.getFilesDir(), fileName);
                //File file = new File(Environment.getExternalStorageDirectory(), fileName);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + activity.getResources().getString(R.string.app_name) + "/exports", fileName);

                fileHolder[0] = file;
                if (file.exists()) {
                    Toast.makeText(context, "This file name already exist.", Toast.LENGTH_LONG).show();
                    input.setTextColor(Color.RED);
                } else {
                    input.setTextColor(Color.BLACK);
                }
            }
        });

        String filename = currentFile.getName();
        int dotPos = filename.lastIndexOf('.');
        String basename = dotPos > 0 ? filename.substring(0, dotPos) : filename;
        input.setText(basename + fileExtension);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(R.string.exportPackage_title);
        dialogBuilder.setView(input);
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = fileHolder[0];

                if (file != null) {
                    try {
                        ImageDrawer imageDrawer = new ImageDrawer();
                        imageDrawer.setImageLoader(editorView.getImageLoader());
                        editor.waitForIdle();
                        editor.export_(block, file, imageDrawer);
                        Toast.makeText(context, "File is exported\nCheck in 'My Exports'", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Failed to export file", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();*/

        return true;
    }

    public final void loadState() {
        InputStream stream = null;
        try {
            try {
                stream = new FileInputStream(stateFile);
                try {
                    stateProperties = new Properties();
                    stateProperties.load(stream);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load state from streamed file: \"" + stateFile.getAbsolutePath() + "\"");
                }
            } catch (FileNotFoundException e) {
                // file has never been created
                stateProperties = null;
            }
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close stream loaded from file: \"" + stateFile.getAbsolutePath() + "\"");
            }
        }
    }

    private final void storeState() {
        stateProperties = new Properties();
        stateProperties.setProperty(DOCUMENT_CONTROLLER_STATE_SAVED_FILE_NAME, getFileName());
        stateProperties.setProperty(DOCUMENT_CONTROLLER_STATE_SAVED_PART_INDEX, Integer.toString(getPartIndex()));
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(stateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to open stream for file: \"" + stateFile.getAbsolutePath() + "\"");
        }
        try {
            stateProperties.store(stream, "");
        } catch (IOException e) {
            Log.e(TAG, "Failed to store stream for file: \"" + stateFile.getAbsolutePath() + "\"");
        }
    }

    public String getSavedFileName() {
        return (stateProperties != null) ? stateProperties.getProperty(DOCUMENT_CONTROLLER_STATE_SAVED_FILE_NAME) : null;
    }

    public int getSavedPartIndex() {
        return (stateProperties != null) ? Integer.valueOf(stateProperties.getProperty(DOCUMENT_CONTROLLER_STATE_SAVED_PART_INDEX)) : 0;
    }
}
