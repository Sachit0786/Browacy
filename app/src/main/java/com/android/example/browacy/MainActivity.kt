@file:Suppress("DEPRECATION")
package com.android.example.browacy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.android.example.browacy.activitiesUI.MainActivityUI
import com.android.example.browacy.activitiesUI.ScoreUI
import com.android.example.browacy.activitiesUI.TakeSelfieUI
import com.android.example.browacy.camera_elements.CameraUI
import com.android.example.browacy.camera_elements.CameraViewModel
import com.android.example.browacy.camera_elements.CapturedImage
import com.android.example.browacy.ui.theme.BrowacyTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.FaceDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.Face
//import com.google.mlkit.vision.face.FaceDetection
//import com.google.mlkit.vision.face.FaceDetectorOptions
//import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    private lateinit var imgURI: Uri
    private var value: Double = 0.0
    private var leftEyebrowX : Float = 0.0F
    private var leftEyebrowY : Float = 0.0F
    private var rightEyebrowX : Float = 0.0F
    private var rightEyebrowY : Float = 0.0F
    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, PERMISSIONS, 0
            )
        }
        if (isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play Services are available", Toast.LENGTH_SHORT).show()
        }

        setContent {
            BrowacyTheme {
                var mainActivity by rememberSaveable { mutableStateOf(true) }
                if (mainActivity) {
                    MainActivityUI(takeSelfie = { mainActivity = false })
                } else {
                    val controller = remember {
                        LifecycleCameraController(applicationContext).apply {
                            setEnabledUseCases(
                                CameraController.IMAGE_CAPTURE or
                                        CameraController.VIDEO_CAPTURE
                            )
                        }
                    }

                    var openCamera by rememberSaveable { mutableStateOf(false) }
                    var imageShow by rememberSaveable { mutableStateOf(false) }
                    var showScore by rememberSaveable { mutableStateOf(false) }

                    if (!openCamera) {
                        TakeSelfieUI(openCamera = { openCamera = true })
                    } else {
                        CameraUI(
                            controller = controller,
                            photoTaken = {
                                takePhoto(
                                    controller = controller,
                                    onPhotoTaken = viewModel::onTakePhoto
                                )
                                imageShow = true
                            },
                            chooseImage = {
                                openGallery()
                                imageShow = true
                            }
                        )
                    }

                    if (imageShow) {
                        CapturedImage(
                            bitmap = viewModel.bitmap.firstOrNull(),
                            onCancel = {
                                imageShow = false
                                openCamera = true
                                viewModel.removePhoto()
                            },
                            onConfirm = {
                                imageShow = false
                                showScore = true
                            })
                    }

                    if (showScore) {

                        ScoreUI(
                            tryAgain = {
                                showScore = false
                                openCamera = true
                                viewModel.removePhoto()
                            },
                            bitmap = viewModel.bitmap.firstOrNull(),
                            iouScore = "${
                                calculateEyebrowSymmetry(
                                    leftEyebrowX,
                                    leftEyebrowY,
                                    rightEyebrowX,
                                    rightEyebrowY
                                )
                            } %")
                    }
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = this.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    private fun calculateEyebrowSymmetry(leftEyebrowX: Float, leftEyebrowY: Float, rightEyebrowX: Float, rightEyebrowY: Float): Double {
        // Calculate the horizontal distance between the eyebrows
        val horizontalDistance = abs(leftEyebrowX - rightEyebrowX)

        // Calculate the vertical difference between the eyebrows
        val verticalDifference = abs(leftEyebrowY - rightEyebrowY)

        // Calculate the symmetry score (can be based on any metric you choose)
        // For example, you could use the Euclidean distance or a simple difference
        // Here, we are using a simple average of horizontal and vertical differences
        val symmetryScore = (horizontalDistance + verticalDifference) / 2.0

        return symmetryScore
    }




    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )

                    onPhotoTaken(rotatedBitmap)
                    processImage(rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ")
                }
            }
        )
    }



//    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
//        // Get the application's cache directory
//        val cachePath = File(context.cacheDir, "images")
//        cachePath.mkdirs() // Make sure the directory exists
//
//        try {
//            // Create a temporary file to save the bitmap
//            val file = File(cachePath, "temp_image.jpg")
//            val fileOutputStream = FileOutputStream(file)
//
//            // Compress the bitmap to JPEG format
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//            fileOutputStream.close()
//
//            // Return the URI of the temporary file
//            return Uri.fromFile(file)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return null
//    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imgURI = data.data!!
            val bitmap = uriToBitmap(imgURI)
            viewModel.onTakePhoto(bitmap)
            if (bitmap != null) {
                processImage(bitmap)
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
    }


    private fun processImage(bitmap: Bitmap){
        // Perform face detection on the bitmap using the ML Kit Face Detection API
//        val image = InputImage.fromBitmap(bitmap, 0)

//        val options = FaceDetectorOptions.Builder()
//            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
//            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
//            .build()

//        val detector = FaceDetection.getClient(options)

//        detector.process(image)
//            .addOnSuccessListener { faces ->
//                for (face in faces) {
//                    drawEyebrows(face)
//                }
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//            }
        val context = this // Replace this with your actual context

        // Create a face detector
        val detector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .build()

        // Create a frame from the bitmap and detect faces
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val face = detector.detect(frame)

        detector.release()
        if(face.isNotEmpty()){
            val image = InputImage.fromBitmap(bitmap, 0)

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()

            val faceDetector = FaceDetection.getClient(options)
            faceDetector.process(image)
            .addOnSuccessListener { faces ->
                for (detectedFace in faces) {
                    drawEyebrows(detectedFace)
                }
            }
        }
    }

    private fun drawEyebrows(face: Face){
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position

        if (leftEye != null && rightEye != null) {
            // Estimate eyebrow positions based on eye and nose positions
            val leftEyebrowX = leftEye.x
            val leftEyebrowY = leftEye.y - 20 // Adjust based on the face size and eye position

            val rightEyebrowX = rightEye.x
            val rightEyebrowY = rightEye.y - 20 // Adjust based on the face size and eye position

//             Log or use the eyebrow positions
            println("Left Eyebrow Position: $leftEyebrowX, $leftEyebrowY")
            println("Right Eyebrow Position: $rightEyebrowX, $rightEyebrowY")
        }
    }


    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return resultCode == ConnectionResult.SUCCESS
    }
}
