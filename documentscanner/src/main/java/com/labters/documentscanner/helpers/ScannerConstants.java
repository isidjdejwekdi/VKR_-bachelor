package com.labters.documentscanner.helpers;

import android.graphics.Bitmap;

import java.io.File;

public class ScannerConstants {
    public static Bitmap selectedImageBitmap;
    public static String cropText="Crop",backText="Cancel",
            imageError="Invalid picture",
            cropError="Crop error";
    public static String cropColor="#6666ff",backColor="#00000f",progressColor="#331199", takeColor = "#9370d8";
    public static boolean saveStorage=true; //false
    public static String cropImgPath;
    public static File fCropImgPath;

}
