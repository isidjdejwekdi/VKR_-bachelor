package com.labters.documentscanner;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.labters.documentscanner.API.SaveResponse;
import com.labters.documentscanner.API.Uploader;
import com.labters.documentscanner.base.CropperErrorType;
import com.labters.documentscanner.base.DocumentScanActivity;
import com.labters.documentscanner.helpers.ScannerConstants;
import com.labters.documentscanner.libraries.PolygonView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;

public class ImageCropActivity extends DocumentScanActivity {

    private FrameLayout holderImageCrop;
    private ImageView imageView;
    private PolygonView polygonView;
    private boolean isInverted = false;
    private ProgressBar progressBar;
    private Bitmap croppedImage;
    private File savePicPath; //
    private CheckBox checkBox;
    private String BASE_URL = "https://filexch.tk/";
    File directPic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    private OnClickListener btnImageEnhanceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressBar();

            String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String outputFile = "output_" +time+".pdf";


            disposable.add(
                    Observable.fromCallable(() -> {
                        croppedImage = getCroppedImage();
                        if (croppedImage == null)
                            return false;
                        if (ScannerConstants.saveStorage) {

                            changeToBW(croppedImage);
                            saveToInternalStorage(croppedImage);

                        }
                            return false;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                          .subscribe((result) -> {
                                hideProgressBar();
                                if (croppedImage != null) {
                                    ScannerConstants.selectedImageBitmap = croppedImage;
                                    setResult(RESULT_OK);

                                    //создать документ
                                    Document document = new Document();
                                    //прикрепить документ к директории
                                    PdfWriter.getInstance(document, new FileOutputStream(new File(directPic, outputFile)));
                                    //открыть документ для изменения
                                    document.open();
                                    //подготовить изображение в нужный формат
                                    Image image = Image.getInstance(savePicPath.getAbsolutePath());

                                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                                            - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                                    image.scalePercent(scaler);
                                    image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                                    document.add(image);

                                    document.close();

                                    if (checkBox.isChecked()){

                                        Uploader uploader = new Uploader();
                                        File file = new File(directPic, outputFile);

                                        Thread.sleep(2000);

                                        CompositeDisposable disposableSecond = new CompositeDisposable();

                                        disposableSecond.add(uploader.getUploadAPI().uploadImage(uploader.getfileToUpload(file))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new BiConsumer<SaveResponse, Throwable>() {
                                                    @Override
                                                    public void accept(SaveResponse saveResponse, Throwable e) throws Exception {
                                                        if (e != null)
                                                            Toast.makeText(getApplicationContext(),  e.getMessage(), Toast.LENGTH_LONG).show();
                                                        else{
                                                            Toast toast =
                                                            Toast.makeText(
                                                                    getApplicationContext(), "File was successfully upload to server", Toast.LENGTH_LONG
                                                            );

                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();
                                                        }

                                                    }
                                                }));
                                    }

                                    finish();
                                }
                          })
            );
        }
    };

    private OnClickListener btnRebase = v -> {
        croppedImage = ScannerConstants.selectedImageBitmap.copy(ScannerConstants.selectedImageBitmap.getConfig(), true);
        isInverted = false;
        startCropping();
    };

    private OnClickListener btnCloseClick = v -> finish();
    private OnClickListener btnInvertColor = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressBar();
            disposable.add(
                    Observable.fromCallable(() -> {
                        invertColor();
                        return false;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                hideProgressBar();
                                Bitmap scaledBitmap = scaledBitmap(croppedImage, holderImageCrop.getWidth(), holderImageCrop.getHeight());
                                imageView.setImageBitmap(scaledBitmap);
                            })
            );
        }
    };
    private OnClickListener onRotateClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressBar();
            disposable.add(
                    Observable.fromCallable(() -> {
                        if (isInverted)
                            invertColor();
                        croppedImage = rotateBitmap(croppedImage, 90);
                        return false;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                hideProgressBar();
                                startCropping();
                            })
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        croppedImage = ScannerConstants.selectedImageBitmap;
        isInverted = false;
        if (ScannerConstants.selectedImageBitmap != null)
            initView();
        else {
            Toast.makeText(this, ScannerConstants.imageError, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected PolygonView getPolygonView() {
        return polygonView;
    }

    @Override
    protected ImageView getImageView() {
        return imageView;
    }

    @Override
    protected FrameLayout getHolderImageCrop() {
        return holderImageCrop;
    }

    @Override
    protected Bitmap getBitmapImage() {
        return croppedImage;
    }

    @Override
    protected void showProgressBar() {
        RelativeLayout rlContainer = findViewById(R.id.rlContainer);
        setViewInteract(rlContainer, false);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgressBar() {
        RelativeLayout rlContainer = findViewById(R.id.rlContainer);
        setViewInteract(rlContainer, true);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void showError(CropperErrorType errorType) {
        if (errorType == CropperErrorType.CROP_ERROR) {
            Toast.makeText(this, ScannerConstants.cropError, Toast.LENGTH_LONG).show();
        }
    }

    private void setViewInteract(View view, boolean canDo) {
        view.setEnabled(canDo);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setViewInteract(((ViewGroup) view).getChildAt(i), canDo);
            }
        }
    }

    private void initView() {
        Button btnImageCrop = findViewById(R.id.btnImageCrop);
        Button btnClose = findViewById(R.id.btnClose);
        holderImageCrop = findViewById(R.id.holderImageCrop);
        imageView = findViewById(R.id.imageView);
        ImageView ivInvert = findViewById(R.id.ivInvert);
        ImageView ivRebase = findViewById(R.id.ivRebase);
        ImageView ivRotate = findViewById(R.id.ivRotate);
        btnImageCrop.setText(ScannerConstants.cropText);
        btnClose.setText(ScannerConstants.backText);
        polygonView = findViewById(R.id.polygonView);
        progressBar = findViewById(R.id.progressBar);
        checkBox = findViewById(R.id.checkbox1);
        checkBox.setChecked(true);

        if (progressBar.getIndeterminateDrawable() != null && ScannerConstants.progressColor != null)
            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(ScannerConstants.progressColor), android.graphics.PorterDuff.Mode.MULTIPLY);
        else if (progressBar.getProgressDrawable() != null && ScannerConstants.progressColor != null)
            progressBar.getProgressDrawable().setColorFilter(Color.parseColor(ScannerConstants.progressColor), android.graphics.PorterDuff.Mode.MULTIPLY);

        btnImageCrop.setBackgroundColor(Color.parseColor(ScannerConstants.cropColor));
        btnImageCrop.setOnClickListener(btnImageEnhanceClick);
        btnClose.setBackgroundColor(Color.parseColor(ScannerConstants.backColor));
        btnClose.setOnClickListener(btnCloseClick);
        ivInvert.setOnClickListener(btnInvertColor);
        ivRebase.setOnClickListener(btnRebase);
        ivRotate.setOnClickListener(onRotateClick);

        startCropping();
    }

    private void invertColor() {
        if (!isInverted) {
            Bitmap monochrome = Bitmap.createBitmap(croppedImage.getWidth(), croppedImage.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(monochrome);
            Paint paint = new Paint();
            ColorMatrix ma = new ColorMatrix();
            ma.setSaturation(0);
            paint.setColorFilter(new ColorMatrixColorFilter(ma));
            canvas.drawBitmap(croppedImage, 0, 0, paint);
            croppedImage = monochrome.copy(monochrome.getConfig(), true);
        } else {
            croppedImage = croppedImage.copy(croppedImage.getConfig(), true);
        }
        isInverted = !isInverted;
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "cropped_" + time + ".png";
        savePicPath = new File(directPic, imageFileName);
        ScannerConstants.cropImgPath = savePicPath.toString();////as str////
        ScannerConstants.fCropImgPath = savePicPath;//////as file////////

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savePicPath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return directPic.getAbsolutePath();
    }

    private void changeToBW(Bitmap bmp){
        int black = Color.rgb(0,0,0);
        int white = Color.rgb(255,255,255);

        int tipawhite = Color.rgb(141,145,144);
        int okolo = Color.rgb(136,138,135);

        int gray = Color.GRAY;
        int half = black /2;

        int p;
        double count =0 ;
        double colors =0;
        int curCol;


        for (int i = 0; i < bmp.getWidth(); i++) {
            for (int j = 0; j <bmp.getHeight(); j++) {
                p = bmp.getPixel(i,j);
                if (p > half)
                    p = white;
                else if (p < half)
                    p = black;

                bmp.setPixel(i,j,p);
            }
        }


//        int liteblue = Color.rgb(233, 255, 255);
//        int blue = Color.rgb(118, 255, 255);
//        int white = Color.WHITE;
//        int black = Color.BLACK;
//        int p;
//
//                for (int i = 0; i < bmp.getWidth(); i++) {
//            for (int j = 0; j <bmp.getHeight(); j++) {
//                p = bmp.getPixel(i,j);
//                if (p < liteblue && p > blue)
//                    p = black;
//
//
//                bmp.setPixel(i,j,p);
//            }
//        }


    }
}

