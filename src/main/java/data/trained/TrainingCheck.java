package data.trained;

import debug.Debug;
import imgToText.TessReg;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenqiu on 3/12/19.
 */
public class TrainingCheck {

    public static void main(String []args) throws Exception {
        File f = Debug.newPropertyFile("../dataset/");
        if (f.isDirectory()) {
            ExecutorService service = Executors.newCachedThreadPool();
            File []files = f.listFiles();
            CountDownLatch countDownLatch = new CountDownLatch((int)f.length());
            Debug.s();
            for (File file : files) {
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(TessReg.getOCRText(file));
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();
            Debug.e();
        }
    }
}
