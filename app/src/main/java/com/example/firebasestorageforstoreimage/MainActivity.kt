package com.example.firebasestorageforstoreimage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.firebasestorageforstoreimage.ui.theme.FirebaseStorageForStoreImageTheme
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        mediaPermission()
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseStorageForStoreImageTheme {
                // A surface container using the 'background' color from the theme

                var imageUri = remember {
                    mutableStateOf<Uri?>(null)
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                   val  activityResultLauncher =getUriFromFile{
                       imageUri.value = it
                        uploadDocsInfirebase(it)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUri.value),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Button(onClick = { activityResultLauncher.launch("image/*") }) {
                            Text(text = "Submit")
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun getUriFromFile(onUriReceive:(uri:Uri?)->Unit):ManagedActivityResultLauncher<String,Uri?>{

        var activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
            uri.let {documentUri ->
                onUriReceive(documentUri)
                //todo if i call the uploadDocsFirebase(documentUri) here then it violets the SOLID principle
                //todo getUriFromFile has only responsibility for getting uri so i set a callback event here so we can call and use it
            // uploadDocsInfirebase(documentUri)
                Toast.makeText(this, "$documentUri", Toast.LENGTH_SHORT).show()

            }
        }
        return activityResultLauncher
    }

    private fun uploadDocsInfirebase(documentUri: Uri?) {

        val firebaseStorage: FirebaseStorage =
            FirebaseStorage.getInstance("gs://testingapplicatation1.appspot.com")
        val storageReference = firebaseStorage.reference

        val filepath: StorageReference = storageReference.child(
            "demoIsu/${
               "userName"
            }" + "/" + "profilepic/suraj"
        )

        filepath.putFile(documentUri!!).addOnSuccessListener {
            if (it.task.isSuccessful) {
                val uri: Uri? = it.task.result.uploadSessionUri
                Log.e("GetUri", "getUriFromFirebase: "+uri.toString() )
                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->

            Toast.makeText(this, exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }



    private fun mediaPermission(){
                   try {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),23)

        } else {

            val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 23)
        }
    } catch ( e:Exception) {
        e.printStackTrace()
    }

    }
    @Composable
    fun PhotoView(onclick:@Composable ()->Unit){

        var  image:Uri? = null
        SelectImage{
            image = it
        }
        Column(modifier = Modifier.fillMaxSize()) {

            Button(
                onClick = { onclick }
            ) {
                Text("Click to save")
            }
        }
    }

    @Composable
    fun SelectImage(onGetImg: (image: Uri)->Unit,){
        val launcher = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {image->

//                image?.let {
//                    onGetImg(image)
//                }
                if(image!=null){
                    onGetImg(image)
                }
                else{
                    Toast.makeText(applicationContext,"unable to select image",Toast.LENGTH_LONG).show()
                }
            })
    }


}

