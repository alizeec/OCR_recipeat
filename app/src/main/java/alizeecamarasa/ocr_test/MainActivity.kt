package alizeecamarasa.ocr_test

import alizeecamarasa.ocr_test.ui.camera.GraphicOverlay
import alizeecamarasa.ocr_test.OcrGraphic
import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.vision.text.TextBlock
import android.util.SparseArray
import com.google.android.gms.vision.Detector
import android.view.SurfaceHolder
import android.Manifest.permission
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import com.google.android.gms.vision.Frame


class MainActivity : AppCompatActivity() {

    private val imageView by lazy { findViewById<ImageView>(R.id.image) }
    private val graphicOverlay by lazy { findViewById<GraphicOverlay<OcrGraphic>>(R.id.graphicOverlay) }
    private val button by lazy { findViewById<Button>(R.id.button) }

    private var gestureDetector: GestureDetector? = null


    private val requestPermissionID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textBitmap = BitmapFactory.decodeResource(resources, R.drawable.recette2)

        val textRecognizer = TextRecognizer.Builder(this).build()
       // textRecognizer.setProcessor(OcrDetectorProcessor(graphicOverlay))

        if (!textRecognizer.isOperational) {
            AlertDialog.Builder(this)
                    .setMessage("Text recognizer could not be set up on your device :(").show()
            return
        }

        imageView.layoutParams.height =1312
        imageView.layoutParams.width =1312
        imageView.setImageBitmap(textBitmap)
        val frame = Frame.Builder().setBitmap(textBitmap).build()
        Log.d("ALIZEE", "image h:" +imageView.height+" image w:"+imageView.width)
        Log.d("ALIZEE", "bitmap h:" +textBitmap.height+" bitmap w:"+textBitmap.width)


        button.setOnClickListener {
            val text = textRecognizer.detect(frame)

            gestureDetector = GestureDetector(this, CaptureGestureListener())

            for (i in 0 until text.size()) {
                val textBlock = text.valueAt(i)
                if (textBlock != null && textBlock.value != null) {
                    val graphic = OcrGraphic(graphicOverlay, textBlock)
                    graphicOverlay.add(graphic)
                }
            }

            textRecognizer.release()
        }



        //startCameraSource()
    }

   fun dipToPixels(dipValue: Float): Int {
    val scale = getResources().getDisplayMetrics().density
       return ((dipValue - 0.5f)/scale).toInt()
   }

    override fun onTouchEvent(e: MotionEvent): Boolean {

        val c = gestureDetector?.onTouchEvent(e)

        return c!! || super.onTouchEvent(e)
    }

    private inner class CaptureGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

    private fun onTap(rawX: Float, rawY: Float): Boolean {
        val graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY)
        var text: TextBlock? = null
        if (graphic != null) {
            text = graphic.textBlock
            if (text != null && text.value != null) {
                Log.d("ALIZEE", "text data is being spoken! " + text.value)
            } else {
                Log.d("ALIZEE", "text data is null")
            }
        } else {
            Log.d("ALIZEE", "no text detected")
        }
        return text != null
    }

    /*private fun startCameraSource() {

        //Create the TextRecognizer
        val textRecognizer = TextRecognizer.Builder(applicationContext).build()

        if (!textRecognizer.isOperational) {
            Log.w("TEST", "Detector dependencies not loaded yet")
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            val mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build()

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(applicationContext,
                                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.CAMERA),
                                    requestPermissionID)
                            return
                        }
                        mCameraSource.start(mCameraView.getHolder())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                /**
                 * Release resources for cameraSource
                 */
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    mCameraSource.stop()
                }
            })

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 */
                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() != 0) {

                        mTextView.post(Runnable {
                            val stringBuilder = StringBuilder()
                            for (i in 0 until items.size()) {
                                val item = items.valueAt(i)
                                stringBuilder.append(item.value)
                                stringBuilder.append("\n")
                            }
                            mTextView.setText(stringBuilder.toString())
                        })
                    }
                }
            })
        }
    }*/
}
