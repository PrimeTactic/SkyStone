package teamcode.test.CVNew;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

public class OpenCVTest extends LinearOpMode {
    private static final String VISION_TARGET_1_PATH = "";
    OpenCvWebcam webcam;
    Mat visionTarget1;
    @Override
    public void runOpMode() throws InterruptedException {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        visionTarget1 = Imgcodecs.imread(VISION_TARGET_1_PATH);
    }
    class skystoneDetector extends OpenCvPipeline{

        @Override
        public Mat processFrame(Mat input) {
//            for(int i = 0; i < input.rows(); i++){
//                for(int j = 0; j < input.cols(); j++){
//                    double[] currentPixel = input.get(i, j);
//                }
//            }
            input.depth();
            return null;
        }
    }
}
