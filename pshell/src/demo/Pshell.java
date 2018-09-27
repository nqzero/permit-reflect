package demo;

import com.nqzero.permit.Permit;
import java.lang.reflect.Field;
import jdk.jshell.JShell;

public class Pshell {
    public static void main(String[] args) throws Exception {
        if (args.length==0)
            Permit.godMode();

        jdk.internal.jshell.tool.JShellTool tool
                = new jdk.internal.jshell.tool.JShellToolBuilder().rawTool();
        Field field = tool.getClass().getDeclaredField("state");
        field.setAccessible(true);

        // inject some values into the shell
        new Thread(() -> { try {

            Thread.sleep(4000);
            JShell jshell = (JShell) field.get(tool);
            jshell.eval("String foobar = \"hello world\";");
            jshell.eval("System.out.println(foobar);");

        } catch (Exception ex) {} }).start();

        tool.start(new String[0]);        

        System.exit(0);
    }
    
}
