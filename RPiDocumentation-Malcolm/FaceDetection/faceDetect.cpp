//
// Driver for face detection
// Malcolm Doering
//


#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include <stdio.h>

using namespace std;
using namespace cv;

/** Function Headers */
void detectAndDisplay( Mat frame );

/** Global variables */
String face_cascade_name = "haarcascade_frontalface_alt.xml";
CascadeClassifier face_cascade;
string window_name = "Capture - Face detection";
RNG rng(12345);

/** @function main */
int main( int argc, const char** argv )
{
	CvCapture* capture;
	Mat frame;
	
	//-- 1. Load the cascades
	if (!face_cascade.load(face_cascade_name))
	{
		printf("--(!)Error loading\n");
		return -1;
	};

	//-- 2. Read the video stream
	capture = cvCaptureFromCAM( -1 );
	if (capture)
	{
		while (true)
		{
			frame = cvQueryFrame(capture);

			//-- 3. Apply the classifier to the frame
			if (!frame.empty())
				detectAndDisplay(frame);
			else
			{
				printf(" --(!) No captured frame -- Break!"); 
				break;
			}
			
			int c = waitKey(10);
			if ((char)c == 'c')
				break;
		}
	}
	
	cvReleaseCapture(&capture);
	
	return 0;
}

/** @function detectAndDisplay */
void detectAndDisplay(Mat frame )
{
	std::vector<Rect> faces;
	Mat frame_gray;
	
	cvtColor(frame, frame_gray, CV_BGR2GRAY );
	equalizeHist(frame_gray, frame_gray );
	
	//-- Detect faces
	face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, Size(30, 30));
	
	//-- Print number of faces detected
	cout << faces.size() << endl;
}

