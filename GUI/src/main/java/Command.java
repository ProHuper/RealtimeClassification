import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Command {  
    public static String exeCmd(String commandStr) {
        BufferedReader br = null;  
        try {  
            Process p = Runtime.getRuntime().exec(commandStr);  
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
            String line;
            StringBuilder sb = new StringBuilder();  
            while ((line = br.readLine()) != null) {  
                sb.append(line).append("\n");
            }  
            return sb.toString();
        } catch (Exception e) {  
            e.printStackTrace();
            return "";
        }   
        finally  
        {  
            if (br != null)  
            {  
                try {  
                    br.close();  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
  
    public static void KillProcess(String cmd) {
        String result = exeCmd(cmd);
        String[] infoArray = result.split(" ");
        List<String> infoList = Arrays.asList(infoArray);
        if(infoList.contains("LISTENING")){
            String res = infoList.get(infoList.size() - 1);
            StringBuilder pid = new StringBuilder();
            for(int i = 0; i < res.length() - 1; ++i){
                pid.append(res.charAt(i));
            }
            exeCmd("taskkill /pid " + pid.toString() + " /f");
        }
    }
}  