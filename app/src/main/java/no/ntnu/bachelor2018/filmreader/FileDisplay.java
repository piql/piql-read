/*
	Copyright [yyyy] [name of copyright owner]

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package no.ntnu.bachelor2018.filmreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import junit.framework.Assert;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import filmreader.bacheloroppg.ntnu.no.filmreader.R;

public class FileDisplay extends AppCompatActivity {

	private final String TAG = this.getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_display);

		// The path where the tar file is stored
		String path = "/sdcard/Pictures/t4.tar";

		try {
			TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
					new BufferedInputStream(new FileInputStream(path)));

			ArchiveEntry archiveEntry = tarArchiveInputStream.getNextEntry();
			int entrySize = (int) archiveEntry.getSize();
			byte[] buffer = new byte[entrySize];
			tarArchiveInputStream.read(buffer, 0, entrySize);

			Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, entrySize);
			ImageView imageView = findViewById(R.id.imageView2);
			imageView.setImageBitmap(bitmap);
			tarArchiveInputStream.close();
		} catch (FileNotFoundException e){
			Log.e(TAG, "File not found");
		} catch (IOException e){
			Log.e(TAG, "IO Exception");
		}
	}
}
