package com.labters.documentscannerandroid

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.kotlinpermissions.KotlinPermissions
import com.labters.documentscanner.ImageCropActivity
import com.labters.documentscanner.helpers.ScannerConstants
import com.labters.documentscannerandroid.api.Uploader
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiConsumer
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var btnPick: Button
    lateinit var btnSend: Button
    lateinit var btnGet: Button
    lateinit var imgBitmap: ImageView
    lateinit var mCurrentPhotoPath: String
    lateinit var imageView: SubsamplingScaleImageView

    val GALLERY_REQUEST = 1111
    val CAMERA_REQUEST = 1231
    val CROP_REQUEST = 1234
    val GALLERY_FOR_SEND_REQUEST = 12345
    val SERVER_VIEW = 1234
    var disposable = CompositeDisposable()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data
            var bitmap: Bitmap?
            try {
                val inputStream = selectedImage?.let { contentResolver.openInputStream(it) }
                bitmap = BitmapFactory.decodeStream(inputStream)
                ScannerConstants.selectedImageBitmap = bitmap
                startActivityForResult(Intent(this, ImageCropActivity::class.java), CROP_REQUEST)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == GALLERY_FOR_SEND_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImage : Uri? = data.data
            val selimg : Uri? = data.data
            var imgName : String? = null
            var file : File? = null
            val basePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            try {

                if (selimg != null) {
                    imgName = dumpImageMetaData(selimg)
                    file = File(basePath, imgName)
                    Log.e("TAG", file.toString())
                }

                val inputStream = selectedImage?.let { contentResolver.openInputStream(it) }
                var bitmap = BitmapFactory.decodeStream(inputStream)

                var uploader = Uploader()

                disposable.add(
                    uploader.uploadAPI.uploadImage(uploader.getfileToUpload(file))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(BiConsumer { response, e ->
                            run {
                                if (e != null)
                                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG)
                                        .show()
                                else {
                                    Toast.makeText(
                                        applicationContext, "File was successfuly upload to server"
                                                + "\n" + response.uri, Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            ScannerConstants.selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                Uri.parse(mCurrentPhotoPath)
            )
            startActivityForResult(Intent(this, ImageCropActivity::class.java), CROP_REQUEST)

        } else if (requestCode == CROP_REQUEST && resultCode == Activity.RESULT_OK) {
            if (ScannerConstants.selectedImageBitmap != null) {

                val imageSource = ImageSource.bitmap(ScannerConstants.selectedImageBitmap)
                imageView.setImage(imageSource)
                imageView.visibility = View.VISIBLE

                Toast.makeText(
                    applicationContext,
                    "files has been saved on device", Toast.LENGTH_LONG
                ).show()
            }

            else
                Toast.makeText(applicationContext, "Not OK", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        val defaultOptions = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .build()
        val config = ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCache(LruMemoryCache(20 * 1024 * 1024))
            .memoryCacheSize(20 * 1024 * 1024)
            .build()

        ImageLoader.getInstance().init(config)
        initView()
        askPermission()
    }

    private fun initView(){
        imageView = findViewById(R.id.imgScale);

        btnPick = findViewById(R.id.btnRetake)
        btnSend = findViewById(R.id.btnSend)
        btnGet = findViewById(R.id.btnGet)
        btnGet.setBackgroundColor(Color.parseColor(ScannerConstants.cropColor))
        btnPick.setBackgroundColor(Color.parseColor(ScannerConstants.takeColor))
        btnSend.setBackgroundColor(Color.parseColor(ScannerConstants.cropColor))
        btnPick.setOnClickListener(View.OnClickListener {
            setView()
        })
        btnSend.setOnClickListener(View.OnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" //"application/pdf" "*/*"
            }
            startActivityForResult(intent, GALLERY_FOR_SEND_REQUEST)
        })

        btnGet.setOnClickListener(View.OnClickListener {

            val intent = Intent(this, ServerFilesListActivity::class.java)
            startActivity(intent)
        })
    }

    fun askPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            KotlinPermissions.with(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
                .onAccepted { permissions ->
                    setView()
                }
                .onDenied { permissions ->
                    askPermission()
                }
                .onForeverDenied { permissions ->
                    Toast.makeText(
                        MainActivity@ this,
                        "You have to accept permissions from app settings.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .ask()
        } else {
            setView()
        }
    }

    fun setView() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Choose an image")
        builder.setMessage("Choose an image which you want to convert")

        builder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setPositiveButton("Gallery") { dialog, which ->
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST) //
        }

        builder.setNegativeButton("Camera") { dialog, which ->
            dialog.dismiss()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    Log.i("Main", "IOException")
                }
                if (photoFile != null) {
                    val builder = StrictMode.VmPolicy.Builder()
                    StrictMode.setVmPolicy(builder.build())
                    cameraIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                    startActivityForResult(cameraIntent, CAMERA_REQUEST) //
                }
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val image = File.createTempFile(
            imageFileName, // pref
            ".jpg", // suf
            storageDir      // direct
        )
        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

    fun dumpImageMetaData(uri: Uri) : String? {

        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null
        )

        var displayName: String? = null

        cursor?.use {
            if (it.moveToFirst()) {
                displayName =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i("TAG", "Display Name: $displayName")

                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)

                val size: String = if (!it.isNull(sizeIndex)) {
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i("TAG", "Size: $size")
            }
        }

        return displayName
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}

