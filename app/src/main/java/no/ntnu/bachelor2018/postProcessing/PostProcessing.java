package no.ntnu.bachelor2018.postProcessing;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by bruker on 07.03.2018.
 */

public class PostProcessing {

	public static Mat cropImage(Mat image, List<Point> corners){

		if(corners.size() != 4){
			return null;
		}

		List x = new ArrayList();
		List y = new ArrayList();

		for(Point p : corners){
			x.add(p.x);
			y.add(p.y);
		}

		int minX, minY, maxX, maxY;

		minX = (int) Collections.min(x);
		minY = (int) Collections.min(y);
		maxX = (int) Collections.max(x);
		maxY = (int) Collections.max(y);

		return image.submat(minX, maxX, minY, maxY);
	}



}
