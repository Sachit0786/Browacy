package com.android.example.browacy.camera_elements

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.example.browacy.R

@Composable
fun CapturedImage(bitmap: Bitmap?,
                  onCancel: () -> Unit,
                  onConfirm: () -> Unit){
    Surface(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)){

        Image(painter = painterResource(id = R.drawable.img_background), contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        
        Column(modifier = Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .size(270.dp,400.dp),
                    alignment = Alignment.Center,
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                )
                Text(
                    text = "Kindly 'Confirm' if you",
                    color = Color.Black,
                    modifier = Modifier.wrapContentSize(),
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.blacksword))
                )
                Text(
                    text = " are satisfied",
                    color = Color.Black,
                    modifier = Modifier.wrapContentSize(),
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.blacksword))
                )
            }
            else{
                Text(
                    text = "Kindly Wait While",
                    modifier = Modifier.wrapContentSize(),
                    color = Color.Black,
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.blacksword))
                )
                Text(
                    text = "Your Image is getting",
                    modifier = Modifier.wrapContentSize(),
                    color = Color.Black,
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.blacksword))
                )
                Text(
                    text = "processed",
                    modifier = Modifier.wrapContentSize(),
                    color = Color.Black,
                    fontSize = 40.sp,
                    fontFamily = FontFamily(Font(R.font.blacksword))
                )
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround) {

                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(20.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.LightGray),
                    modifier = Modifier
                        .size(width = 150.dp, height = 60.dp)
                ){
                    Text(text = "Confirm", fontSize = 21.sp, fontFamily = FontFamily(Font(R.font.black_bruno)))
                }

                Button(
                    onClick = onCancel,
                    shape = RoundedCornerShape(20.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.LightGray),
                    modifier = Modifier
                        .size(width = 150.dp, height = 60.dp)
                ){
                    Text(text = "Cancel", fontSize = 21.sp, fontFamily = FontFamily(Font(R.font.black_bruno)))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCapturedImage() {
    var imageShow by rememberSaveable { mutableStateOf(false) }
    CapturedImage(bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888),onCancel = {imageShow = false}, onConfirm = {imageShow = false})
}