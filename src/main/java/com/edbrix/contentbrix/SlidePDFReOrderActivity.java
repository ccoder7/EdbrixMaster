package com.edbrix.contentbrix;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.edbrix.contentbrix.adapters.DragDropPDFImgGridAdapter;
import com.edbrix.contentbrix.baseclasses.BaseActivity;
import com.edbrix.contentbrix.commons.AlertDialogManager;
import com.edbrix.contentbrix.volley.VolleySingleton;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;

import org.askerov.dynamicgrid.DynamicGridView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;


public class SlidePDFReOrderActivity extends BaseActivity {

    private DynamicGridView dragDropImageGrid;
    private DragDropPDFImgGridAdapter dragDropPDFImageGridAdapter;
    private ArrayList<Image> imagesList;

    private boolean isPositionChanged;
    public static final String pdfFileKey = "pdf";
    public String TAG = SlidePDFReOrderActivity.class.getName();
    private Random generator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_image_re_order);
        generator = new Random();
        isPositionChanged = false;
        imagesList = new ArrayList<>();
        imagesList = getIntent().getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
        Log.e(TAG,"imagesList Size :"+imagesList.size());

        if(imagesList.size() > 50){
            showToast("PDF Pages count must be less then 50 pages");
            finish();
        }
        initComp();
    }

    private void initComp() {
        GridLayoutManager manager;
        dragDropImageGrid = (DynamicGridView) findViewById(R.id.dragDropImageGrid);
        imageLoaderNostra.init(ImageLoaderConfiguration.createDefault(this));
        dragDropPDFImageGridAdapter = new DragDropPDFImgGridAdapter(SlidePDFReOrderActivity.this, imagesList, 6, imageLoaderNostra);
       /* if(isChromebook() || isTablet()) {
            dragDropImageGrid.setNumColumns(8);
        }else {
            dragDropImageGrid.setNumColumns(5);
        }*/
        dragDropImageGrid.setAdapter(dragDropPDFImageGridAdapter);

        Log.e(TAG,"in initComp imagesList Size :"+imagesList.size());
        dragDropImageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SlidePDFReOrderActivity.this);
                ImageView imageView = new ImageView(dragDropImageGrid.getContext());

                imageView.setAdjustViewBounds(true);
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();
                Bitmap bitmap = dragDropPDFImageGridAdapter.getImgBimapByPos(position);//view.getDrawingCache();

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50, 50, 50, 50);
                imageView.setLayoutParams(lp);

                imageView.setImageBitmap(bitmap);
                builder.setView(imageView);
                builder.create().show();
            }
        });

        dragDropImageGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                dragDropImageGrid.startEditMode(position);
                
                MenuItem menuItemFinish = optionMenu.findItem(R.id.finishMenu);
                menuItemFinish.setVisible(true);

                MenuItem menuItemStart = optionMenu.findItem(R.id.startMenu);
                menuItemStart.setVisible(false);

                return false;
            }
        });

        dragDropImageGrid.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {
                Log.v("DragDrop", "onDragStarted : position : " + position);
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
                isPositionChanged = true;
                Log.v("DragDrop", "onDragPositionsChanged : new position : " + newPosition + " old position : " + oldPosition);
            }
        });

        dragDropImageGrid.setOnDropListener(new DynamicGridView.OnDropListener() {
            @Override
            public void onActionDrop() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (dragDropImageGrid.isEditMode()) {
            dragDropImageGrid.stopEditMode();
        }

        imagesList = (ArrayList<Image>) (Object) ((DragDropPDFImgGridAdapter) dragDropImageGrid.getAdapter()).getItems();

        Log.e(" imagesList : ",""+imagesList);
        setResult(RESULT_OK, getIntent().putParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST, imagesList));
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        imageLoaderNostra.stop();
        super.onStop();
    }

    private Menu optionMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.re_order_image_menu, menu);
        optionMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) 
        {
            case R.id.addNewPage:
                //Toast.makeText(mContext,"Add Page",Toast.LENGTH_LONG).show();



                MenuItem menuItemaddNewPage = optionMenu.findItem(R.id.addNewPage);
                menuItemaddNewPage.setVisible(true);

                //File tmpfile = saveImageFromBitmap(tmpBitmap, "" + fileId);
                //add(getCount(), new Image(fileId, Uri.fromFile(tmpfile), tmpfile.getPath(), false));

                Bitmap tmpBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.plain_white_png);
                int autoNo = generator.nextInt(10000);//0099
                File tmpfile = saveImageFromBitmap(tmpBitmap, "" + autoNo);
                Image tempNewImg = new Image(autoNo, Uri.fromFile(tmpfile), tmpfile.getPath(), false);
                /*imagesList.add(0,tempNewImg);
                dragDropPDFImageGridAdapter.notifyDataSetChanged();*/

                dragDropPDFImageGridAdapter.addNewPage(0,tempNewImg);

                return true;
                
            case R.id.startMenu:
                dragDropImageGrid.startEditMode(0);
                MenuItem menuItemFinish = optionMenu.findItem(R.id.finishMenu);
                menuItemFinish.setVisible(true);

                MenuItem menuItemAddPage = optionMenu.findItem(R.id.addNewPage);
                menuItemAddPage.setVisible(false);

                item.setVisible(false);
                return true;

            case R.id.finishMenu:
                if (dragDropImageGrid.isEditMode()) {
                    dragDropImageGrid.stopEditMode();
                }

                MenuItem menuItemDone = optionMenu.findItem(R.id.doneMenu);
                menuItemDone.setVisible(true);

                MenuItem menuItemAddPages = optionMenu.findItem(R.id.addNewPage);
                menuItemAddPages.setVisible(true);

                MenuItem menuItemStart = optionMenu.findItem(R.id.startMenu);
                menuItemStart.setVisible(true);

                item.setVisible(false);
                return true;


            case R.id.doneMenu:
                getAlertDialogManager().Dialog("Confirmation", "Continue with save?", new AlertDialogManager.onTwoButtonClickListner() {
                    @Override
                    public void onNegativeClick() {
                    }

                    @Override
                    public void onPositiveClick() {
                        onBackPressed();
                    }
                }).show();
                //onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public File saveImageFromBitmap(Bitmap finalBitmap, String fileId) {
        File myDir = new File(VolleySingleton.getAppStorageDirectory() + "/pdf");
        if (!myDir.isDirectory())
            myDir.mkdirs();

        String fname = "img_" + fileId + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}