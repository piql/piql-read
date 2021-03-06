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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

import filmreader.bacheloroppg.ntnu.no.filmreader.BuildConfig;
import filmreader.bacheloroppg.ntnu.no.filmreader.R;
import no.ntnu.bachelor2018.filmreader.FileDisplayClasses.ShowImage;
import no.ntnu.bachelor2018.filmreader.FileDisplayClasses.ShowText;

/**
 * This activity displays the content inside a tar file at the location given by the path
 * variable. It creates a ListView where the user can browse the files and look at them.
 * Currently loads the whole tar file into ram.
 */
public class FileDisplay extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    // The path to read tar file from. NB: this is also set in the wrapper after successfull unboxing
    //TODO(håkon) remove test path and replace with path(remove test from filename)
    private final String path = "/data/data/filmreader.bacheloroppg.ntnu.no.filmreader/app_tardir/output.tar";
    // Array with all the file names
    ArrayList<String> entryNames;

    // Array with all the file names for the current directory
    ArrayList<String> currentEntryNames;
    // Array for holding all the entries, mostly metadata
    private ArrayList<ArchiveEntry> entries;
    // Array for holding all the entry data
    private ArrayList<byte[]> fileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_display);

        entries = new ArrayList<>();
        entryNames = new ArrayList<>();
        currentEntryNames = new ArrayList<>();
        fileData = new ArrayList<>();

        try (TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {
            // Open the stream and read the archive


            // Create temporary variables
            ArchiveEntry archiveEntry;
            byte[] buffer;
            int entrySize, bytesRead;

            // Read all the data for all the entries and save it
            while ((archiveEntry = tarArchiveInputStream.getNextEntry()) != null) {
                if (archiveEntry.getName().endsWith("/")) {
                    // We skip directories, this does not skip files within directories
                    continue;
                }

                // Add the entries and name to both arrays
                entries.add(archiveEntry);
                entryNames.add(archiveEntry.getName());

                // Create a buffer to read into and put the data into the data array
                entrySize = (int) archiveEntry.getSize();
                buffer = new byte[entrySize];
                bytesRead = tarArchiveInputStream.read(buffer, 0, entrySize);
                Log.d(TAG, "bytesRead: " + bytesRead);
                fileData.add(buffer);
            }

            // Close the stream as we have now read all the files
            tarArchiveInputStream.close();

            // Create an Adapter for the listview to show the files
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.activity_file_display_textview, entryNames);
            ListView listView = findViewById(R.id.listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openFile(position);
                }
            });

            listView.setAdapter(adapter);

            //Opens first file immediately
            openFile(0);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");

            // Send an alert to the user that there is no file (or data)
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.error_title));
            builder.setMessage(getResources().getString(R.string.error_nodataread));
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int i) {
                    // When the user dismisses the dialog we close the application
                    finish();
                }
            });
            builder.create().show();
        } catch (IOException e) {
            Log.d(TAG, "IO Exception");

            // Send an alert to the user that an error occured
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.error_title));
            builder.setMessage(getResources().getString(R.string.error_message));
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int i) {
                    // When the user dismisses the dialog we close the application
                    finish();
                }
            });
            builder.create().show();
        }
    }

    /**
     * Tries to display a file, displaying different file formats is handled here
     *
     * @param index The index of the file to display
     */
    private void openFile(int index) {
        String fileName = entryNames.get(index);
        String fileType = URLConnection.guessContentTypeFromName(fileName);
        Log.d(TAG, "File name: " + fileName + " File type: " + fileType); // Debugging for adding more

        switch (fileType) {
            case "image/png":
                displayImage(index);
                break;
            case "image/jpeg":
                displayImage(index);

                break;
            case "text/plain":
                displayText(index);
                break;
            default:
                try {
                    shareOther(index, fileName, fileType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //case "text/html": break;
            //case "application/x-tar": break;
        }
    }

    /**
     * Share a file using non-supported display format.
     *
     * @param index
     * @param fileName
     * @param fileType
     */
    private void shareOther(int index, String fileName, String fileType) throws IOException {

        //shareIntent.setType(fileType);

        //Get directory to write file to.
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File outputFile = new File(dir + "/" + fileName);
        FileOutputStream ostream = null;
        //Try to create the directory, file and write the file.
        try {
            outputFile.getParentFile().mkdirs();
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            ostream = new FileOutputStream(outputFile);
            ostream.write(fileData.get(index));
            ostream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(ostream != null)
                ostream.close();
        }

        //Get the uri used to share the file with other applications
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", outputFile);
        Intent shareIntent = new Intent(Intent.ACTION_VIEW, uri);

        //Set flags required to grant permissions to file and view the file.
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //Start the activity
        startActivity(shareIntent);
    }

    /**
     * Displays an image entry in an {@link ImageView}
     *
     * @param index The index of the image to display
     */
    private void displayImage(int index) {
        Bitmap image = BitmapFactory.decodeByteArray(fileData.get(index), 0, fileData.get(index).length);
        if (image == null) {
            Log.e(TAG, "Image is null");
        }

        // Open new activity to show the image
        Intent intent = new Intent(this, ShowImage.class);
        ShowImage.setImage(image);
        ShowImage.setTitle(entryNames.get(index));
        startActivity(intent);
    }

    /**
     * Opens an activity to display the contents of a text file
     *
     * @param index The index of the file to display
     */
    private void displayText(int index) {
        // Open a new activity to display the text, here we can send it through an intent
        Intent intent = new Intent(this, ShowText.class);
        intent.putExtra("text", fileData.get(index));
        startActivity(intent);
    }

}
