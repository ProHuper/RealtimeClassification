import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

class Main{

    /**
     * @param args
     */

    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {
        new ProcessExecutor().command("python", "C:\\Users\\Huper\\Desktop\\files\\test.py", "123").redirectOutput(new LogOutputStream() {
            @Override
            protected void processLine(String line) {
                System.out.println(line);
            }
        }).destroyOnExit().execute();
    }

}